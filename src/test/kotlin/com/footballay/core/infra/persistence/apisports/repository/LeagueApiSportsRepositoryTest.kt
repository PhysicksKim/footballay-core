package com.footballay.core.infra.persistence.apisports.repository

import com.footballay.core.MatchEntityGenerator
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsCoverage
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsSeason
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.util.UidGenerator
import com.footballay.core.logger
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(MatchEntityGenerator::class)
class LeagueApiSportsRepositoryTest {
    val log = logger()

    @Autowired
    private lateinit var leagueApiSportsRepository: LeagueApiSportsRepository

    @Autowired
    private lateinit var leagueCoreRepository: LeagueCoreRepository

    @Autowired
    private lateinit var leagueApiSportsSeasonRepository: LeagueApiSportsSeasonRepository

    @Autowired
    private lateinit var uidGenerator: UidGenerator

    @Autowired
    private lateinit var em: EntityManager

    private lateinit var leagueCore: LeagueCore

    private lateinit var leagueApiSports: LeagueApiSports

    private lateinit var leagueSeason: LeagueApiSportsSeason

    @BeforeEach
    fun setUp() {
        val uid = uidGenerator.generateUid()
        val name = "Premier League"
        leagueCore = leagueCoreRepository.save(LeagueCore(uid = uid, name = name))
        val apiId = 39L
        leagueApiSports =
            leagueApiSportsRepository.save(LeagueApiSports(leagueCore = leagueCore, apiId = apiId, name = name))
        val seasonYear = 2024
        val seasonStart = "2024-08-10"
        val seasonEnd = "2025-05-18"
        leagueSeason =
            createLeagueSeason(seasonYear, seasonStart, seasonEnd, leagueApiSports).also {
                leagueApiSportsSeasonRepository.save(it)
            }
        em.flush()
        em.clear()
    }

    @Test
    fun `정해진 apiId 와 시즌에 해당하는 값만 찾습니다`() {
        // given
        // 조회되면 안되는 시즌
        val wrongYear = 9999
        val wrongStart = "9998-01-01"
        val wrongEnd = "9999-06-20"
        val wrongSeason =
            createLeagueSeason(
                wrongYear,
                wrongStart,
                wrongEnd,
                leagueApiSports,
            ).also { leagueApiSportsSeasonRepository.save(it) }

        // when
        val apiId = 39L
        val seasonYear = 2024
        val leagueApiSports = leagueApiSportsRepository.findByApiIdAndSeasonWithCoreAndSeasons(apiId, seasonYear)

        // then
        assertThat(leagueApiSports).isNotNull()
        assertThat(leagueApiSports!!.apiId).isEqualTo(apiId)
        assertThat(leagueApiSports.seasons.size).isEqualTo(1)
        assertThat(leagueApiSports.seasons[0].seasonYear).isNotEqualTo(wrongSeason.seasonYear)
        assertThat(leagueApiSports.seasons[0].seasonYear).isEqualTo(leagueSeason.seasonYear)
    }

    private fun createLeagueSeason(
        year: Int,
        start: String,
        end: String,
        leagueApiSports: LeagueApiSports,
    ): LeagueApiSportsSeason {
        val leagueCoverage =
            LeagueApiSportsCoverage(
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
            )
        return LeagueApiSportsSeason(
            seasonYear = year,
            seasonStart = start,
            seasonEnd = end,
            coverage = leagueCoverage,
            leagueApiSports = leagueApiSports,
        )
    }
}
