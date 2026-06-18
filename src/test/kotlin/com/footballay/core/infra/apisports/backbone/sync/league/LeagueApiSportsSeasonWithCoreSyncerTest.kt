package com.footballay.core.infra.apisports.backbone.sync.league

import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsCoverageCreateDto
import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsCreateDto
import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsSeasonCreateDto
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsCoverage
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsSeason
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsSeasonRepository
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.LeagueSeasonCore
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.persistence.core.repository.LeagueSeasonCoreRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeParseException

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LeagueApiSportsSeasonWithCoreSyncerTest {
    @Autowired
    private lateinit var seasonSyncer: LeagueApiSportsSeasonWithCoreSyncer

    @Autowired
    private lateinit var leagueApiSportsRepository: LeagueApiSportsRepository

    @Autowired
    private lateinit var leagueApiSportsSeasonRepository: LeagueApiSportsSeasonRepository

    @Autowired
    private lateinit var leagueCoreRepository: LeagueCoreRepository

    @Autowired
    private lateinit var leagueSeasonCoreRepository: LeagueSeasonCoreRepository

    @PersistenceContext
    private lateinit var em: EntityManager

    @Test
    @DisplayName("빈 입력은 시즌 데이터를 저장하지 않는다")
    fun `empty input does nothing`() {
        seasonSyncer.syncSeasons(emptyList())
        flushAndClear()

        assertThat(leagueSeasonCoreRepository.findAll()).isEmpty()
        assertThat(leagueApiSportsSeasonRepository.findAll()).isEmpty()
    }

    @Test
    @DisplayName("신규 입력은 provider season, core season, 1대1 연결을 생성한다")
    fun `creates provider seasons core seasons and one to one links from ids`() {
        val league = saveLeague(apiId = 39L)

        seasonSyncer.syncSeasons(
            listOf(
                input(
                    league = league,
                    currentSeason = 2024,
                    seasons =
                        listOf(
                            seasonDto(2023, coverage = coverage(standings = false)),
                            seasonDto(2024, coverage = coverage(standings = true)),
                        ),
                ),
            ),
        )
        flushAndClear()

        val providerSeasons = providerSeasonsOf(league.api)
        val coreSeasons = coreSeasonsOf(league.core)

        assertThat(providerSeasons.map { it.seasonYear }).containsExactlyInAnyOrder(2023, 2024)
        assertThat(coreSeasons.map { it.seasonYear }).containsExactlyInAnyOrder(2023, 2024)
        assertThat(providerSeasons.mapNotNull { it.leagueSeasonCore?.id }).containsExactlyInAnyOrderElementsOf(coreSeasons.map { it.id })
        assertThat(coreSeasons.single { it.seasonYear == 2023 }.current).isFalse()
        assertThat(coreSeasons.single { it.seasonYear == 2024 }.current).isTrue()
        assertThat(coreSeasons.single { it.seasonYear == 2024 }.seasonStart).isEqualTo(LocalDate.parse("2024-08-01"))
        assertThat(coreSeasons.single { it.seasonYear == 2024 }.seasonEnd).isEqualTo(LocalDate.parse("2025-05-31"))
        assertThat(providerSeasons.single { it.seasonYear == 2023 }.coverage?.standings).isFalse()
        assertThat(providerSeasons.single { it.seasonYear == 2024 }.coverage?.standings).isTrue()
    }

    @Test
    @DisplayName("동일 입력 반복 sync는 provider/core season을 중복 생성하지 않는다")
    fun `repeated same input reuses existing provider and core seasons`() {
        val league = saveLeague(apiId = 39L)
        val syncInput =
            input(
                league = league,
                currentSeason = 2024,
                seasons = listOf(seasonDto(2023), seasonDto(2024)),
            )

        seasonSyncer.syncSeasons(listOf(syncInput))
        flushAndClear()
        val firstProviderIds = providerSeasonsOf(league.api).map { it.id }
        val firstCoreIds = coreSeasonsOf(league.core).map { it.id }

        seasonSyncer.syncSeasons(listOf(syncInput))
        flushAndClear()

        val providerSeasons = providerSeasonsOf(league.api)
        val coreSeasons = coreSeasonsOf(league.core)

        assertThat(providerSeasons.map { it.id }).containsExactlyInAnyOrderElementsOf(firstProviderIds)
        assertThat(coreSeasons.map { it.id }).containsExactlyInAnyOrderElementsOf(firstCoreIds)
        assertThat(providerSeasons).hasSize(2)
        assertThat(coreSeasons).hasSize(2)
    }

    @Test
    @DisplayName("core season만 존재하면 기존 core를 재사용하고 provider season을 생성한다")
    fun `reuses existing core season and creates missing provider season`() {
        val league = saveLeague(apiId = 39L)
        val existingCoreSeason =
            leagueSeasonCoreRepository.save(
                LeagueSeasonCore(
                    league = league.core,
                    seasonYear = 2024,
                    current = false,
                    autoGenerated = false,
                ),
            )
        flushAndClear()

        seasonSyncer.syncSeasons(
            listOf(
                input(
                    league = league,
                    currentSeason = 2024,
                    seasons = listOf(seasonDto(2024)),
                ),
            ),
        )
        flushAndClear()

        val providerSeason = providerSeasonsOf(league.api).single()
        val coreSeason = coreSeasonsOf(league.core).single()

        assertThat(coreSeason.id).isEqualTo(existingCoreSeason.id)
        assertThat(coreSeason.autoGenerated).isFalse()
        assertThat(coreSeason.current).isTrue()
        assertThat(providerSeason.leagueSeasonCore?.id).isEqualTo(existingCoreSeason.id)
    }

    @Test
    @DisplayName("provider season만 존재하면 기존 provider를 재사용하고 core season을 생성한다")
    fun `reuses existing provider season and creates missing core season`() {
        val league = saveLeague(apiId = 39L)
        val existingProviderSeason =
            leagueApiSportsSeasonRepository.save(
                LeagueApiSportsSeason(
                    leagueApiSports = league.api,
                    seasonYear = 2024,
                    seasonStart = LocalDate.parse("2024-01-01"),
                    seasonEnd = LocalDate.parse("2024-12-31"),
                    coverage = coverageEntity(standings = false),
                ),
            )
        flushAndClear()

        seasonSyncer.syncSeasons(
            listOf(
                input(
                    league = league,
                    currentSeason = 2024,
                    seasons = listOf(seasonDto(2024, coverage = coverage(standings = true))),
                ),
            ),
        )
        flushAndClear()

        val providerSeason = providerSeasonsOf(league.api).single()
        val coreSeason = coreSeasonsOf(league.core).single()

        assertThat(providerSeason.id).isEqualTo(existingProviderSeason.id)
        assertThat(providerSeason.seasonStart).isEqualTo(LocalDate.parse("2024-08-01"))
        assertThat(providerSeason.seasonEnd).isEqualTo(LocalDate.parse("2025-05-31"))
        assertThat(providerSeason.coverage?.standings).isTrue()
        assertThat(providerSeason.leagueSeasonCore?.id).isEqualTo(coreSeason.id)
        assertThat(coreSeason.current).isTrue()
    }

    @Test
    @DisplayName("provider/core season이 모두 존재하면 identity를 유지하고 필드만 갱신한다")
    fun `updates existing provider and core fields without changing identities`() {
        val league = saveLeague(apiId = 39L)
        val coreSeason =
            leagueSeasonCoreRepository.save(
                LeagueSeasonCore(
                    league = league.core,
                    seasonYear = 2024,
                    seasonStart = LocalDate.parse("2024-01-01"),
                    seasonEnd = LocalDate.parse("2024-12-31"),
                    current = false,
                ),
            )
        val providerSeason =
            leagueApiSportsSeasonRepository.save(
                LeagueApiSportsSeason(
                    leagueApiSports = league.api,
                    seasonYear = 2024,
                    seasonStart = LocalDate.parse("2024-01-01"),
                    seasonEnd = LocalDate.parse("2024-12-31"),
                    coverage = coverageEntity(standings = false),
                    leagueSeasonCore = coreSeason,
                ),
            )
        flushAndClear()

        seasonSyncer.syncSeasons(
            listOf(
                input(
                    league = league,
                    currentSeason = 2024,
                    seasons = listOf(seasonDto(2024, coverage = coverage(standings = true))),
                ),
            ),
        )
        flushAndClear()

        val updatedProviderSeason = providerSeasonsOf(league.api).single()
        val updatedCoreSeason = coreSeasonsOf(league.core).single()

        assertThat(updatedProviderSeason.id).isEqualTo(providerSeason.id)
        assertThat(updatedCoreSeason.id).isEqualTo(coreSeason.id)
        assertThat(updatedProviderSeason.coverage?.standings).isTrue()
        assertThat(updatedProviderSeason.seasonStart).isEqualTo(LocalDate.parse("2024-08-01"))
        assertThat(updatedCoreSeason.seasonStart).isEqualTo(LocalDate.parse("2024-08-01"))
        assertThat(updatedCoreSeason.current).isTrue()
    }

    @Test
    @DisplayName("이전 current season이 입력에 없어도 current flag를 새 season으로 이동한다")
    fun `moves current flag away from existing core seasons even when old current season is not in incoming seasons`() {
        val league = saveLeague(apiId = 39L)
        val season2023 =
            leagueSeasonCoreRepository.save(
                LeagueSeasonCore(
                    league = league.core,
                    seasonYear = 2023,
                    current = true,
                ),
            )
        flushAndClear()

        seasonSyncer.syncSeasons(
            listOf(
                input(
                    league = league,
                    currentSeason = 2024,
                    seasons = listOf(seasonDto(2024)),
                ),
            ),
        )
        flushAndClear()

        val coreSeasons = coreSeasonsOf(league.core)

        assertThat(coreSeasons.single { it.id == season2023.id }.current).isFalse()
        assertThat(coreSeasons.single { it.seasonYear == 2024 }.current).isTrue()
        assertThat(coreSeasons.filter { it.current }.map { it.seasonYear }).containsExactly(2024)
    }

    @Test
    @DisplayName("currentSeason이 null이면 기존 current flag를 모두 해제한다")
    fun `null current season clears current flags and creates non current seasons`() {
        val league = saveLeague(apiId = 39L)
        leagueSeasonCoreRepository.save(
            LeagueSeasonCore(
                league = league.core,
                seasonYear = 2023,
                current = true,
            ),
        )
        flushAndClear()

        seasonSyncer.syncSeasons(
            listOf(
                input(
                    league = league,
                    currentSeason = null,
                    seasons = listOf(seasonDto(2024)),
                ),
            ),
        )
        flushAndClear()

        val coreSeasons = coreSeasonsOf(league.core)

        assertThat(coreSeasons.map { it.seasonYear }).containsExactlyInAnyOrder(2023, 2024)
        assertThat(coreSeasons.filter { it.current }).isEmpty()
    }

    @Test
    @DisplayName("입력 season 목록이 비어 있어도 기존 core season의 current flag는 갱신한다")
    fun `empty incoming seasons still updates current flags for existing core seasons`() {
        val league = saveLeague(apiId = 39L)
        leagueSeasonCoreRepository.save(
            LeagueSeasonCore(
                league = league.core,
                seasonYear = 2023,
                current = true,
            ),
        )
        val season2024 =
            leagueSeasonCoreRepository.save(
                LeagueSeasonCore(
                    league = league.core,
                    seasonYear = 2024,
                    current = false,
                ),
            )
        flushAndClear()

        seasonSyncer.syncSeasons(
            listOf(
                input(
                    league = league,
                    currentSeason = 2024,
                    seasons = emptyList(),
                ),
            ),
        )
        flushAndClear()

        val coreSeasons = coreSeasonsOf(league.core)

        assertThat(providerSeasonsOf(league.api)).isEmpty()
        assertThat(coreSeasons.single { it.seasonYear == 2023 }.current).isFalse()
        assertThat(coreSeasons.single { it.id == season2024.id }.current).isTrue()
    }

    @Test
    @DisplayName("provider season의 core season 연결이 null이면 올바른 core season으로 보정한다")
    fun `fixes provider season with null core season link`() {
        val league = saveLeague(apiId = 39L)
        val providerSeason =
            leagueApiSportsSeasonRepository.save(
                LeagueApiSportsSeason(
                    leagueApiSports = league.api,
                    seasonYear = 2024,
                    leagueSeasonCore = null,
                ),
            )
        flushAndClear()

        seasonSyncer.syncSeasons(
            listOf(
                input(
                    league = league,
                    currentSeason = 2024,
                    seasons = listOf(seasonDto(2024)),
                ),
            ),
        )
        flushAndClear()

        val updatedProviderSeason = providerSeasonsOf(league.api).single()
        val coreSeason = coreSeasonsOf(league.core).single()

        assertThat(updatedProviderSeason.id).isEqualTo(providerSeason.id)
        assertThat(updatedProviderSeason.leagueSeasonCore?.id).isEqualTo(coreSeason.id)
    }

    @Test
    @DisplayName("provider season이 같은 league의 잘못된 core season을 참조하면 올바른 season으로 보정한다")
    fun `fixes provider season linked to wrong core season`() {
        val league = saveLeague(apiId = 39L)
        val wrongCoreSeason =
            leagueSeasonCoreRepository.save(
                LeagueSeasonCore(
                    league = league.core,
                    seasonYear = 2023,
                ),
            )
        val correctCoreSeason =
            leagueSeasonCoreRepository.save(
                LeagueSeasonCore(
                    league = league.core,
                    seasonYear = 2024,
                ),
            )
        val providerSeason =
            leagueApiSportsSeasonRepository.save(
                LeagueApiSportsSeason(
                    leagueApiSports = league.api,
                    seasonYear = 2024,
                    leagueSeasonCore = wrongCoreSeason,
                ),
            )
        flushAndClear()

        seasonSyncer.syncSeasons(
            listOf(
                input(
                    league = league,
                    currentSeason = 2024,
                    seasons = listOf(seasonDto(2024)),
                ),
            ),
        )
        flushAndClear()

        val updatedProviderSeason = providerSeasonsOf(league.api).single()
        val coreSeasons = coreSeasonsOf(league.core)

        assertThat(updatedProviderSeason.id).isEqualTo(providerSeason.id)
        assertThat(updatedProviderSeason.leagueSeasonCore?.id).isEqualTo(correctCoreSeason.id)
        assertThat(coreSeasons.map { it.id }).containsExactlyInAnyOrder(wrongCoreSeason.id, correctCoreSeason.id)
    }

    @Test
    @DisplayName("provider season이 다른 league의 core season을 참조하면 올바른 core season으로 보정한다")
    fun `fixes provider season linked to core season from another league`() {
        val premierLeague = saveLeague(apiId = 39L, uid = "premier-league")
        val laLiga = saveLeague(apiId = 140L, uid = "la-liga")
        val wrongCoreSeason =
            leagueSeasonCoreRepository.save(
                LeagueSeasonCore(
                    league = laLiga.core,
                    seasonYear = 2024,
                ),
            )
        val correctCoreSeason =
            leagueSeasonCoreRepository.save(
                LeagueSeasonCore(
                    league = premierLeague.core,
                    seasonYear = 2024,
                ),
            )
        val providerSeason =
            leagueApiSportsSeasonRepository.save(
                LeagueApiSportsSeason(
                    leagueApiSports = premierLeague.api,
                    seasonYear = 2024,
                    leagueSeasonCore = wrongCoreSeason,
                ),
            )
        flushAndClear()

        seasonSyncer.syncSeasons(
            listOf(
                input(
                    league = premierLeague,
                    currentSeason = 2024,
                    seasons = listOf(seasonDto(2024)),
                ),
            ),
        )
        flushAndClear()

        val updatedProviderSeason = providerSeasonsOf(premierLeague.api).single()

        assertThat(updatedProviderSeason.id).isEqualTo(providerSeason.id)
        assertThat(updatedProviderSeason.leagueSeasonCore?.id).isEqualTo(correctCoreSeason.id)
        assertThat(coreSeasonsOf(laLiga.core).map { it.id }).containsExactly(wrongCoreSeason.id)
    }

    @Test
    @DisplayName("여러 리그가 같은 seasonYear를 가져도 리그별 season identity는 독립적이다")
    fun `syncs multiple leagues with same season year independently`() {
        val premierLeague = saveLeague(apiId = 39L, uid = "premier-league")
        val laLiga = saveLeague(apiId = 140L, uid = "la-liga")

        seasonSyncer.syncSeasons(
            listOf(
                input(
                    league = premierLeague,
                    currentSeason = 2024,
                    seasons = listOf(seasonDto(2024)),
                ),
                input(
                    league = laLiga,
                    currentSeason = 2023,
                    seasons = listOf(seasonDto(2024)),
                ),
            ),
        )
        flushAndClear()

        val premierProviderSeason = providerSeasonsOf(premierLeague.api).single()
        val premierCoreSeason = coreSeasonsOf(premierLeague.core).single()
        val laLigaProviderSeason = providerSeasonsOf(laLiga.api).single()
        val laLigaCoreSeason = coreSeasonsOf(laLiga.core).single()

        assertThat(premierCoreSeason.seasonYear).isEqualTo(2024)
        assertThat(laLigaCoreSeason.seasonYear).isEqualTo(2024)
        assertThat(premierCoreSeason.id).isNotEqualTo(laLigaCoreSeason.id)
        assertThat(premierProviderSeason.leagueSeasonCore?.id).isEqualTo(premierCoreSeason.id)
        assertThat(laLigaProviderSeason.leagueSeasonCore?.id).isEqualTo(laLigaCoreSeason.id)
        assertThat(premierCoreSeason.current).isTrue()
        assertThat(laLigaCoreSeason.current).isFalse()
    }

    @Test
    @DisplayName("같은 리그 입력이 중복되면 저장 전에 예외 처리한다")
    fun `duplicate league api sports input is rejected before saving`() {
        val league = saveLeague(apiId = 39L)
        val firstInput =
            input(
                league = league,
                currentSeason = 2024,
                seasons = listOf(seasonDto(2024)),
            )
        val secondInput =
            input(
                league = league,
                currentSeason = 2025,
                seasons = listOf(seasonDto(2025)),
            )

        assertThatThrownBy {
            seasonSyncer.syncSeasons(listOf(firstInput, secondInput))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("duplicate leagueApiSportsIds")
    }

    @Test
    @DisplayName("한 리그 입력 안의 seasonYear가 중복되면 저장 전에 예외 처리한다")
    fun `duplicate season years in one league input are rejected before saving`() {
        val league = saveLeague(apiId = 39L)

        assertThatThrownBy {
            seasonSyncer.syncSeasons(
                listOf(
                    input(
                        league = league,
                        currentSeason = 2024,
                        seasons = listOf(seasonDto(2024), seasonDto(2024)),
                    ),
                ),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("duplicate seasonYears")
    }

    @Test
    @DisplayName("seasonYear가 null이면 저장 전에 예외 처리한다")
    fun `null season year is rejected`() {
        val league = saveLeague(apiId = 39L)

        assertThatThrownBy {
            seasonSyncer.syncSeasons(
                listOf(
                    input(
                        league = league,
                        currentSeason = 2024,
                        seasons = listOf(seasonDto(null)),
                    ),
                ),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("seasonYear is required")
    }

    @Test
    @DisplayName("날짜 형식이 잘못되면 저장 전에 예외 처리한다")
    fun `invalid season date is rejected`() {
        val league = saveLeague(apiId = 39L)

        assertThatThrownBy {
            seasonSyncer.syncSeasons(
                listOf(
                    input(
                        league = league,
                        currentSeason = 2024,
                        seasons =
                            listOf(
                                seasonDto(
                                    year = 2024,
                                    start = "not-a-date",
                                ),
                            ),
                    ),
                ),
            )
        }.isInstanceOf(DateTimeParseException::class.java)
    }

    @Test
    @DisplayName("날짜 형식 오류가 발생하면 기존 current flag를 변경하지 않는다")
    fun `invalid season date does not partially update existing current flags`() {
        val league = saveLeague(apiId = 39L)
        leagueSeasonCoreRepository.save(
            LeagueSeasonCore(
                league = league.core,
                seasonYear = 2023,
                current = true,
            ),
        )
        flushAndClear()

        assertThatThrownBy {
            seasonSyncer.syncSeasons(
                listOf(
                    input(
                        league = league,
                        currentSeason = 2024,
                        seasons =
                            listOf(
                                seasonDto(
                                    year = 2024,
                                    start = "not-a-date",
                                ),
                            ),
                    ),
                ),
            )
        }.isInstanceOf(DateTimeParseException::class.java)
        flushAndClear()

        val coreSeasons = coreSeasonsOf(league.core)

        assertThat(coreSeasons.map { it.seasonYear }).containsExactly(2023)
        assertThat(coreSeasons.single().current).isTrue()
        assertThat(providerSeasonsOf(league.api)).isEmpty()
    }

    @Test
    @DisplayName("존재하지 않는 LeagueApiSports id는 예외 처리한다")
    fun `missing league api sports id is rejected`() {
        val league = saveLeague(apiId = 39L)
        val missingLeagueApiSportsId = requireNotNull(league.api.id) + 9999

        assertThatThrownBy {
            seasonSyncer.syncSeasons(
                listOf(
                    LeagueSeasonSyncInput(
                        leagueCreateDto = leagueDto(apiId = 39L, currentSeason = 2024, seasons = listOf(seasonDto(2024))),
                        leagueApiSportsId = missingLeagueApiSportsId,
                        leagueCoreId = requireNotNull(league.core.id),
                    ),
                ),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("LeagueApiSports not found")
    }

    @Test
    @DisplayName("존재하지 않는 LeagueCore id는 예외 처리한다")
    fun `missing league core id is rejected`() {
        val league = saveLeague(apiId = 39L)
        val missingLeagueCoreId = requireNotNull(league.core.id) + 9999

        assertThatThrownBy {
            seasonSyncer.syncSeasons(
                listOf(
                    LeagueSeasonSyncInput(
                        leagueCreateDto = leagueDto(apiId = 39L, currentSeason = 2024, seasons = listOf(seasonDto(2024))),
                        leagueApiSportsId = requireNotNull(league.api.id),
                        leagueCoreId = missingLeagueCoreId,
                    ),
                ),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("LeagueCore not found")
    }

    @Test
    @DisplayName("LeagueApiSports와 LeagueCore 연결이 맞지 않으면 예외 처리한다")
    fun `league api sports and league core mismatch is rejected`() {
        val premierLeague = saveLeague(apiId = 39L, uid = "premier-league")
        val laLiga = saveLeague(apiId = 140L, uid = "la-liga")

        assertThatThrownBy {
            seasonSyncer.syncSeasons(
                listOf(
                    LeagueSeasonSyncInput(
                        leagueCreateDto = leagueDto(apiId = 39L, currentSeason = 2024, seasons = listOf(seasonDto(2024))),
                        leagueApiSportsId = requireNotNull(premierLeague.api.id),
                        leagueCoreId = requireNotNull(laLiga.core.id),
                    ),
                ),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("must be linked to the LeagueCore")
    }

    private fun saveLeague(
        apiId: Long,
        uid: String = "league-$apiId",
    ): SavedLeague {
        val leagueCore =
            leagueCoreRepository.save(
                LeagueCore(
                    uid = uid,
                    name = "League $apiId",
                ),
            )
        val leagueApiSports =
            leagueApiSportsRepository.save(
                LeagueApiSports(
                    leagueCore = leagueCore,
                    apiId = apiId,
                    name = "League $apiId",
                    type = "League",
                    countryName = "England",
                ),
            )
        return SavedLeague(
            core = leagueCore,
            api = leagueApiSports,
        )
    }

    private fun input(
        league: SavedLeague,
        currentSeason: Int?,
        seasons: List<LeagueApiSportsSeasonCreateDto>,
    ): LeagueSeasonSyncInput =
        LeagueSeasonSyncInput(
            leagueCreateDto =
                leagueDto(
                    apiId = league.api.apiId,
                    currentSeason = currentSeason,
                    seasons = seasons,
                ),
            leagueApiSportsId = requireNotNull(league.api.id),
            leagueCoreId = requireNotNull(league.core.id),
        )

    private fun leagueDto(
        apiId: Long,
        currentSeason: Int?,
        seasons: List<LeagueApiSportsSeasonCreateDto>,
    ): LeagueApiSportsCreateDto =
        LeagueApiSportsCreateDto(
            apiId = apiId,
            name = "League $apiId",
            type = "League",
            logo = "https://example.com/logo_$apiId.png",
            countryName = "England",
            countryCode = "GB",
            countryFlag = "https://example.com/flag.png",
            currentSeason = currentSeason,
            seasons = seasons,
        )

    private fun seasonDto(
        year: Int?,
        start: String? = year?.let { "$it-08-01" },
        end: String? = year?.let { "${it + 1}-05-31" },
        coverage: LeagueApiSportsCoverageCreateDto? = coverage(),
    ): LeagueApiSportsSeasonCreateDto =
        LeagueApiSportsSeasonCreateDto(
            seasonYear = year,
            seasonStart = start,
            seasonEnd = end,
            coverage = coverage,
        )

    private fun coverage(standings: Boolean = true): LeagueApiSportsCoverageCreateDto =
        LeagueApiSportsCoverageCreateDto(
            fixturesEvents = true,
            fixturesLineups = true,
            fixturesStatistics = true,
            fixturesPlayers = true,
            standings = standings,
            players = true,
            topScorers = true,
            topAssists = true,
            topCards = true,
            injuries = true,
            predictions = true,
            odds = true,
        )

    private fun coverageEntity(standings: Boolean): LeagueApiSportsCoverage =
        LeagueApiSportsCoverage(
            fixturesEvents = true,
            fixturesLineups = true,
            fixturesStatistics = true,
            fixturesPlayers = true,
            standings = standings,
            players = true,
            topScorers = true,
            topAssists = true,
            topCards = true,
            injuries = true,
            predictions = true,
            odds = true,
        )

    private fun providerSeasonsOf(leagueApiSports: LeagueApiSports): List<LeagueApiSportsSeason> = leagueApiSportsSeasonRepository.findAllByLeagueApiSports(leagueApiSports)

    private fun coreSeasonsOf(leagueCore: LeagueCore): List<LeagueSeasonCore> = leagueSeasonCoreRepository.findAllByLeague(leagueCore)

    private fun flushAndClear() {
        em.flush()
        em.clear()
    }

    private data class SavedLeague(
        val core: LeagueCore,
        val api: LeagueApiSports,
    )
}
