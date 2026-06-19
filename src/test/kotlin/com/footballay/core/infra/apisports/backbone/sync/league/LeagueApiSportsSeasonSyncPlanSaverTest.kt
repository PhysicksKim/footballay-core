package com.footballay.core.infra.apisports.backbone.sync.league

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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LeagueApiSportsSeasonSyncPlanSaverTest {
    @Autowired
    private lateinit var planSaver: LeagueApiSportsSeasonSyncPlanSaver

    @Autowired
    private lateinit var leagueCoreRepository: LeagueCoreRepository

    @Autowired
    private lateinit var leagueApiSportsRepository: LeagueApiSportsRepository

    @Autowired
    private lateinit var leagueSeasonCoreRepository: LeagueSeasonCoreRepository

    @Autowired
    private lateinit var leagueApiSportsSeasonRepository: LeagueApiSportsSeasonRepository

    @PersistenceContext
    private lateinit var em: EntityManager

    @Test
    @DisplayName("plan saver는 새 core season을 먼저 저장하고 provider season FK를 저장한다")
    fun `saves new core season before provider season with foreign key`() {
        val league = saveLeague()
        val coreSeason =
            LeagueSeasonCore(
                league = league.core,
                seasonYear = 2024,
            )
        val providerSeason =
            LeagueApiSportsSeason(
                leagueApiSports = league.api,
                seasonYear = 2024,
                leagueSeasonCore = coreSeason,
            )

        planSaver.save(
            LeagueApiSportsSeasonSyncPlan(
                coreSeasonsToSave = mapOf(CoreSeasonIdentity(league.core.id!!, 2024) to coreSeason),
                providerSeasonsToSave = mapOf(ProviderSeasonIdentity(league.api.id!!, 2024) to providerSeason),
                resolvedPairs = emptyList(),
            ),
        )
        flushAndClear()

        val savedCoreSeason = leagueSeasonCoreRepository.findByLeagueAndSeasonYear(league.core, 2024)!!
        val savedProviderSeason = leagueApiSportsSeasonRepository.findAllByLeagueApiSports(league.api).single()

        assertThat(savedProviderSeason.leagueSeasonCore?.id).isEqualTo(savedCoreSeason.id)
    }

    @Test
    @DisplayName("plan saver는 기존 provider season의 core season link 보정을 저장한다")
    fun `saves provider season link correction`() {
        val league = saveLeague()
        val coreSeason =
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
                    leagueSeasonCore = null,
                ),
            )
        providerSeason.leagueSeasonCore = coreSeason

        planSaver.save(
            LeagueApiSportsSeasonSyncPlan(
                coreSeasonsToSave = emptyMap(),
                providerSeasonsToSave = mapOf(ProviderSeasonIdentity(league.api.id!!, 2024) to providerSeason),
                resolvedPairs = emptyList(),
            ),
        )
        flushAndClear()

        val savedProviderSeason = leagueApiSportsSeasonRepository.findAllByLeagueApiSports(league.api).single()

        assertThat(savedProviderSeason.id).isEqualTo(providerSeason.id)
        assertThat(savedProviderSeason.leagueSeasonCore?.id).isEqualTo(coreSeason.id)
    }

    private fun saveLeague(): LeagueFixture {
        val leagueCore =
            leagueCoreRepository.save(
                LeagueCore(
                    uid = "league-core",
                    name = "Premier League",
                ),
            )
        val leagueApiSports =
            leagueApiSportsRepository.save(
                LeagueApiSports(
                    leagueCore = leagueCore,
                    apiId = 39L,
                    name = "Premier League",
                    type = "League",
                    countryName = "England",
                ),
            )
        return LeagueFixture(core = leagueCore, api = leagueApiSports)
    }

    private fun flushAndClear() {
        em.flush()
        em.clear()
    }

    private data class LeagueFixture(
        val core: LeagueCore,
        val api: LeagueApiSports,
    )
}
