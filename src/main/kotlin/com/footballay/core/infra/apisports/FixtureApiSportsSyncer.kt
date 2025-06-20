package com.footballay.core.infra.apisports

import com.footballay.core.infra.apisports.dto.FixtureApiSportsCreateDto
import com.footballay.core.infra.core.util.UidGenerator
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.logger
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class FixtureApiSportsSyncer (
    //

    // Api Sports repos
    private val fixtureApiSportsRepository: FixtureApiSportsRepository,
    private val leagueApiSportsRepository: LeagueApiSportsRepository,
    private val teamApiSportsRepository: TeamApiSportsRepository,
    // Core repos
    private val fixtureCoreRepository: FixtureCoreRepository,
    private val leagueCoreRepository: LeagueCoreRepository,
    private val teamCoreRepository: TeamApiSportsRepository,
    // Utility
    private val uidGenerator: UidGenerator
){

    val log = logger()

    @Transactional
    fun saveFixturesOfLeagueWithCurrentSeason(
        leagueApiId: Long,
        dtos: List<FixtureApiSportsCreateDto>
    ) {
        val leagueApi = leagueApiSportsRepository.findByApiId(leagueApiId)
            ?: throw IllegalArgumentException("League with API ID $leagueApiId not found")
        val coreApi = leagueApi.leagueCore
            ?: throw IllegalArgumentException("League core not found")


    }

}