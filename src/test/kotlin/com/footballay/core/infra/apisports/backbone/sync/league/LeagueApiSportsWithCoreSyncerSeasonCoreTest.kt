package com.footballay.core.infra.apisports.backbone.sync.league

import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsCoverageCreateDto
import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsCreateDto
import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsSeasonCreateDto
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
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
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LeagueApiSportsWithCoreSyncerSeasonCoreTest {
    @Autowired
    private lateinit var leagueApiSportsSyncer: LeagueApiSportsWithCoreSyncer

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
    fun `league sync maps provider seasons to core seasons`() {
        leagueApiSportsSyncer.saveLeagues(
            listOf(
                createLeagueDto(
                    apiId = 39L,
                    name = "Premier League",
                    currentSeason = 2024,
                    seasonYears = listOf(2023, 2024),
                ),
            ),
        )
        flushAndClear()

        val leagueApiSports = leagueApiSportsRepository.findByApiId(39L)!!
        val leagueCore = leagueApiSports.leagueCore!!
        val providerSeasons = leagueApiSportsSeasonRepository.findAllByLeagueApiSports(leagueApiSports)
        val coreSeasons = leagueSeasonCoreRepository.findAllByLeague(leagueCore)

        assertThat(providerSeasons).hasSize(2)
        assertThat(providerSeasons.map { it.leagueSeasonCore?.seasonYear }).containsExactlyInAnyOrder(2023, 2024)
        assertThat(coreSeasons.map { it.seasonYear }).containsExactlyInAnyOrder(2023, 2024)
        assertThat(coreSeasons.single { it.seasonYear == 2023 }.current).isFalse()
        assertThat(coreSeasons.single { it.seasonYear == 2024 }.current).isTrue()
        assertThat(coreSeasons.single { it.seasonYear == 2024 }.seasonStart).isEqualTo(LocalDate.parse("2024-08-01"))
        assertThat(coreSeasons.single { it.seasonYear == 2024 }.seasonEnd).isEqualTo(LocalDate.parse("2025-05-31"))
    }

    @Test
    fun `league sync reuses core seasons and moves current season flag`() {
        leagueApiSportsSyncer.saveLeagues(
            listOf(
                createLeagueDto(
                    apiId = 39L,
                    name = "Premier League",
                    currentSeason = 2024,
                    seasonYears = listOf(2023, 2024),
                ),
            ),
        )
        flushAndClear()

        val initialLeagueApiSports = leagueApiSportsRepository.findByApiId(39L)!!
        val leagueCore = initialLeagueApiSports.leagueCore!!
        val initialSeason2024 = leagueSeasonCoreRepository.findByLeagueAndSeasonYear(leagueCore, 2024)!!

        leagueApiSportsSyncer.saveLeagues(
            listOf(
                createLeagueDto(
                    apiId = 39L,
                    name = "Premier League Updated",
                    currentSeason = 2025,
                    seasonYears = listOf(2024, 2025),
                ),
            ),
        )
        flushAndClear()

        val coreSeasons = leagueSeasonCoreRepository.findAllByLeague(leagueCore)
        val season2024 = leagueSeasonCoreRepository.findByLeagueAndSeasonYear(leagueCore, 2024)!!
        val season2025 = leagueSeasonCoreRepository.findByLeagueAndSeasonYear(leagueCore, 2025)!!

        assertThat(coreSeasons.map { it.seasonYear }).containsExactlyInAnyOrder(2023, 2024, 2025)
        assertThat(season2024.id).isEqualTo(initialSeason2024.id)
        assertThat(season2024.current).isFalse()
        assertThat(season2025.current).isTrue()
        assertThat(coreSeasons.filter { it.current }.map { it.seasonYear }).containsExactly(2025)
    }

    @Test
    fun `repeated same league sync does not duplicate api league core league provider seasons or core seasons`() {
        val dto =
            createLeagueDto(
                apiId = 39L,
                name = "Premier League",
                currentSeason = 2024,
                seasonYears = listOf(2023, 2024),
            )

        leagueApiSportsSyncer.saveLeagues(listOf(dto))
        flushAndClear()
        leagueApiSportsSyncer.saveLeagues(listOf(dto.copy(name = "Premier League Updated")))
        flushAndClear()

        val leagueApiSports = leagueApiSportsRepository.findByApiId(39L)!!
        val leagueCore = leagueApiSports.leagueCore!!
        val providerSeasons = leagueApiSportsSeasonRepository.findAllByLeagueApiSports(leagueApiSports)
        val coreSeasons = leagueSeasonCoreRepository.findAllByLeague(leagueCore)

        assertThat(leagueApiSportsRepository.findAll().map { it.apiId }).containsExactly(39L)
        assertThat(leagueCoreRepository.findAll().map { it.id }).containsExactly(leagueCore.id)
        assertThat(providerSeasons.map { it.seasonYear }).containsExactlyInAnyOrder(2023, 2024)
        assertThat(providerSeasons.mapNotNull { it.leagueSeasonCore?.id }).containsExactlyInAnyOrderElementsOf(coreSeasons.map { it.id })
        assertThat(coreSeasons.map { it.seasonYear }).containsExactlyInAnyOrder(2023, 2024)
        assertThat(coreSeasons.filter { it.current }.map { it.seasonYear }).containsExactly(2024)
    }

    @Test
    fun `league sync creates provider season and reuses existing core season`() {
        val leagueCore = leagueCoreRepository.save(LeagueCore(uid = "core-only-season-league", name = "Premier League"))
        val leagueApiSports = leagueApiSportsRepository.save(createLeagueApiSports(39L, leagueCore))
        val existingCoreSeason =
            leagueSeasonCoreRepository.save(
                LeagueSeasonCore(
                    league = leagueCore,
                    seasonYear = 2024,
                    current = false,
                    autoGenerated = false,
                ),
            )
        flushAndClear()

        leagueApiSportsSyncer.saveLeagues(
            listOf(
                createLeagueDto(
                    apiId = leagueApiSports.apiId,
                    name = "Premier League Updated",
                    currentSeason = 2024,
                    seasonYears = listOf(2024),
                ),
            ),
        )
        flushAndClear()

        val updatedLeagueApiSports = leagueApiSportsRepository.findByApiId(39L)!!
        val providerSeasons = leagueApiSportsSeasonRepository.findAllByLeagueApiSports(updatedLeagueApiSports)
        val coreSeasons = leagueSeasonCoreRepository.findAllByLeague(leagueCore)

        assertThat(providerSeasons).hasSize(1)
        assertThat(providerSeasons.single().leagueSeasonCore?.id).isEqualTo(existingCoreSeason.id)
        assertThat(coreSeasons.map { it.id }).containsExactly(existingCoreSeason.id)
        assertThat(coreSeasons.single().autoGenerated).isFalse()
        assertThat(coreSeasons.single().current).isTrue()
    }

    @Test
    fun `league sync creates core season and reuses existing provider season`() {
        val leagueCore = leagueCoreRepository.save(LeagueCore(uid = "provider-only-season-league", name = "Premier League"))
        val leagueApiSports = leagueApiSportsRepository.save(createLeagueApiSports(39L, leagueCore))
        val existingProviderSeason =
            leagueApiSportsSeasonRepository.save(
                LeagueApiSportsSeason(
                    leagueApiSports = leagueApiSports,
                    seasonYear = 2024,
                    seasonStart = LocalDate.parse("2024-01-01"),
                    seasonEnd = LocalDate.parse("2024-12-31"),
                ),
            )
        flushAndClear()

        leagueApiSportsSyncer.saveLeagues(
            listOf(
                createLeagueDto(
                    apiId = leagueApiSports.apiId,
                    name = "Premier League Updated",
                    currentSeason = 2024,
                    seasonYears = listOf(2024),
                ),
            ),
        )
        flushAndClear()

        val updatedLeagueApiSports = leagueApiSportsRepository.findByApiId(39L)!!
        val providerSeasons = leagueApiSportsSeasonRepository.findAllByLeagueApiSports(updatedLeagueApiSports)
        val coreSeasons = leagueSeasonCoreRepository.findAllByLeague(leagueCore)

        assertThat(providerSeasons.map { it.id }).containsExactly(existingProviderSeason.id)
        assertThat(coreSeasons.map { it.seasonYear }).containsExactly(2024)
        assertThat(providerSeasons.single().leagueSeasonCore?.id).isEqualTo(coreSeasons.single().id)
        assertThat(providerSeasons.single().seasonStart).isEqualTo(LocalDate.parse("2024-08-01"))
        assertThat(providerSeasons.single().seasonEnd).isEqualTo(LocalDate.parse("2025-05-31"))
    }

    @Test
    fun `league sync fixes provider season linked to wrong core season`() {
        val leagueCore = leagueCoreRepository.save(LeagueCore(uid = "wrong-link-season-league", name = "Premier League"))
        val leagueApiSports = leagueApiSportsRepository.save(createLeagueApiSports(39L, leagueCore))
        val wrongCoreSeason =
            leagueSeasonCoreRepository.save(
                LeagueSeasonCore(
                    league = leagueCore,
                    seasonYear = 2023,
                ),
            )
        val correctCoreSeason =
            leagueSeasonCoreRepository.save(
                LeagueSeasonCore(
                    league = leagueCore,
                    seasonYear = 2024,
                ),
            )
        val providerSeason =
            leagueApiSportsSeasonRepository.save(
                LeagueApiSportsSeason(
                    leagueApiSports = leagueApiSports,
                    seasonYear = 2024,
                    leagueSeasonCore = wrongCoreSeason,
                ),
            )
        flushAndClear()

        leagueApiSportsSyncer.saveLeagues(
            listOf(
                createLeagueDto(
                    apiId = leagueApiSports.apiId,
                    name = "Premier League Updated",
                    currentSeason = 2024,
                    seasonYears = listOf(2024),
                ),
            ),
        )
        flushAndClear()

        val updatedLeagueApiSports = leagueApiSportsRepository.findByApiId(39L)!!
        val providerSeasons = leagueApiSportsSeasonRepository.findAllByLeagueApiSports(updatedLeagueApiSports)
        val coreSeasons = leagueSeasonCoreRepository.findAllByLeague(leagueCore)

        assertThat(providerSeasons.map { it.id }).containsExactly(providerSeason.id)
        assertThat(providerSeasons.single().leagueSeasonCore?.id).isEqualTo(correctCoreSeason.id)
        assertThat(coreSeasons.map { it.id }).containsExactlyInAnyOrder(wrongCoreSeason.id, correctCoreSeason.id)
    }

    @Test
    fun `provider season cannot share same core season`() {
        val leagueCore = leagueCoreRepository.save(LeagueCore(uid = "one-to-one-season-league", name = "Premier League"))
        val anotherLeagueCore = leagueCoreRepository.save(LeagueCore(uid = "one-to-one-season-another-league", name = "Another League"))
        val firstLeagueApiSports = leagueApiSportsRepository.save(createLeagueApiSports(39L, leagueCore))
        val secondLeagueApiSports = leagueApiSportsRepository.save(createLeagueApiSports(40L, anotherLeagueCore))
        val coreSeason =
            leagueSeasonCoreRepository.save(
                LeagueSeasonCore(
                    league = leagueCore,
                    seasonYear = 2024,
                ),
            )

        leagueApiSportsSeasonRepository.saveAndFlush(
            LeagueApiSportsSeason(
                leagueApiSports = firstLeagueApiSports,
                seasonYear = 2024,
                leagueSeasonCore = coreSeason,
            ),
        )

        assertThatThrownBy {
            leagueApiSportsSeasonRepository.saveAndFlush(
                LeagueApiSportsSeason(
                    leagueApiSports = secondLeagueApiSports,
                    seasonYear = 2024,
                    leagueSeasonCore = coreSeason,
                ),
            )
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    private fun createLeagueApiSports(
        apiId: Long,
        leagueCore: LeagueCore,
    ): LeagueApiSports =
        LeagueApiSports(
            leagueCore = leagueCore,
            apiId = apiId,
            name = "Premier League",
            type = "League",
            countryName = "England",
        )

    private fun createLeagueDto(
        apiId: Long,
        name: String,
        currentSeason: Int,
        seasonYears: List<Int>,
    ): LeagueApiSportsCreateDto =
        LeagueApiSportsCreateDto(
            apiId = apiId,
            name = name,
            type = "League",
            logo = "https://example.com/logo_$apiId.png",
            countryName = "England",
            countryCode = "GB",
            countryFlag = "https://example.com/flag.png",
            currentSeason = currentSeason,
            seasons =
                seasonYears.map { year ->
                    LeagueApiSportsSeasonCreateDto(
                        seasonYear = year,
                        seasonStart = "$year-08-01",
                        seasonEnd = "${year + 1}-05-31",
                        coverage = coverage(),
                    )
                },
        )

    private fun coverage(): LeagueApiSportsCoverageCreateDto =
        LeagueApiSportsCoverageCreateDto(
            fixturesEvents = true,
            fixturesLineups = true,
            fixturesStatistics = true,
            fixturesPlayers = true,
            standings = true,
            players = true,
            topScorers = true,
            topAssists = true,
            topCards = true,
            injuries = true,
            predictions = true,
            odds = true,
        )

    private fun flushAndClear() {
        em.flush()
        em.clear()
    }
}
