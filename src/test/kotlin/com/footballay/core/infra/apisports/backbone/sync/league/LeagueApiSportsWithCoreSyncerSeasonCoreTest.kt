package com.footballay.core.infra.apisports.backbone.sync.league

import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsCoverageCreateDto
import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsCreateDto
import com.footballay.core.infra.apisports.shared.dto.LeagueApiSportsSeasonCreateDto
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsSeasonRepository
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.persistence.core.repository.LeagueSeasonCoreRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
