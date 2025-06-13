package com.footballay.core.infra.apisports

import com.footballay.core.infra.apisports.dto.LeagueApiSportsCoverageCreateDto
import com.footballay.core.infra.apisports.dto.LeagueApiSportsCreateDto
import com.footballay.core.infra.apisports.dto.LeagueApiSportsSeasonCreateDto
import com.footballay.core.infra.apisports.dto.ApiSportsTeamSaveDto
import com.footballay.core.infra.core.util.UidGenerator
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsCoverage
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsSeason
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsSeasonRepository
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ApiSportsSyncService(
    // Api sports repos
    private val leagueApiSportsRepository: LeagueApiSportsRepository,
    private val leagueSeasonRepository: LeagueApiSportsSeasonRepository,
    // Core repos
    private val leagueCoreRepository: LeagueCoreRepository,

    // Utility
    private val uidGenerator: UidGenerator
) {

    @Transactional
    fun saveLeagues(dtos: List<LeagueApiSportsCreateDto>) {
        val apiIdList: List<Long> = dtos.map { it.apiId }
        val apiIdEntityMap: Map<Long, LeagueApiSports> = leagueApiSportsRepository
            .findLeagueApiSportsInApiId(apiIdList)
            .associateBy { it.apiId }

        val case1Dtos = mutableListOf<LeagueApiSportsCreateDto>()
        val case2Dtos = mutableListOf<LeagueApiSportsCreateDto>()
        val case3Dtos = mutableListOf<LeagueApiSportsCreateDto>()

        dtos.forEach { dto ->
            val apiEntity = apiIdEntityMap[dto.apiId]
            when {
                apiEntity != null && apiEntity.leagueCore != null -> case1Dtos.add(dto)
                apiEntity != null && apiEntity.leagueCore == null -> case2Dtos.add(dto)
                apiEntity == null -> case3Dtos.add(dto)
            }
        }

        val case1Entities = case1Dtos.map { dto ->
            val apiEntity = apiIdEntityMap[dto.apiId]!!
            updateApiEntity(apiEntity, dto)
            apiEntity.seasons = leagueSeasonRepository.saveAll(createSeasonEntities(dto.seasons, apiEntity))
            apiEntity
        }
        leagueApiSportsRepository.saveAll(case1Entities)

        val newLeagueCoresForCase2 = case2Dtos.map { dto ->
            val apiEntity = apiIdEntityMap[dto.apiId]!!
            val newCore = leagueCoreRepository.save(createCoreEntityBy(dto))
            apiEntity.leagueCore = newCore
            apiEntity.seasons = leagueSeasonRepository.saveAll(createSeasonEntities(dto.seasons, apiEntity))
            apiEntity
        }
        leagueApiSportsRepository.saveAll(newLeagueCoresForCase2)

        val newLeagueApiSportsForCase3 = case3Dtos.map { dto ->
            val newCore = leagueCoreRepository.save(createCoreEntityBy(dto))
            val newApiEntity = createApiEntity(newCore, dto)
            newApiEntity.seasons = leagueSeasonRepository.saveAll(createSeasonEntities(dto.seasons, newApiEntity))
            newApiEntity
        }
        leagueApiSportsRepository.saveAll(newLeagueApiSportsForCase3)
    }

    @Transactional
    fun saveTeamsOfLeague(dto: ApiSportsTeamSaveDto) {

    }

    private fun createSeasonEntities(
        seasonDtos: List<LeagueApiSportsSeasonCreateDto>,
        apiEntity: LeagueApiSports
    ): List<LeagueApiSportsSeason> {
        return seasonDtos.map { seasonDto ->
            LeagueApiSportsSeason(
                seasonYear = seasonDto.seasonYear,
                seasonStart = seasonDto.seasonStart,
                seasonEnd = seasonDto.seasonEnd,
                coverage = seasonDto.coverage?.let {
                    createLeagueApiSportsCoverage(it)
                },
                leagueApiSports = apiEntity
            )
        }
    }

    private fun createLeagueApiSportsCoverage(it: LeagueApiSportsCoverageCreateDto) =
        LeagueApiSportsCoverage(
            fixturesEvents = it.fixturesEvents,
            fixturesLineups = it.fixturesLineups,
            fixturesStatistics = it.fixturesStatistics,
            fixturesPlayers = it.fixturesPlayers,
            standings = it.standings,
            players = it.players,
            topScorers = it.topScorers,
            topAssists = it.topAssists,
            topCards = it.topCards,
            injuries = it.injuries,
            predictions = it.predictions,
            odds = it.odds
        )

    private fun createApiEntity(
        newCore: LeagueCore,
        dto: LeagueApiSportsCreateDto
    ) = LeagueApiSports(
        leagueCore = newCore,
        apiId = dto.apiId,
        name = dto.name,
        type = dto.type,
        logo = dto.logo,
        countryName = dto.countryName,
        countryCode = dto.countryCode,
        countryFlag = dto.countryFlag,
        currentSeason = dto.currentSeason
    )

    private fun createCoreEntityBy(dto: LeagueApiSportsCreateDto) = LeagueCore(
        uid = uidGenerator.generateUid(),
        name = dto.name,
        autoGenerated = true,
        available = false
    )

    private fun updateApiEntity(
        apiEntity: LeagueApiSports,
        dto: LeagueApiSportsCreateDto
    ) {
        apiEntity.apply{
            name = dto.name
            type = dto.type
            logo = dto.logo
            countryName = dto.countryName
            countryCode = dto.countryCode
            countryFlag = dto.countryFlag
            currentSeason = dto.currentSeason
        }
    }
}
