package com.footballay.core.infra.apisports.syncer

import com.footballay.core.infra.apisports.VenueApiSportsService
import com.footballay.core.infra.apisports.dto.FixtureApiSportsCreateDto
import com.footballay.core.infra.core.util.UidGenerator
import com.footballay.core.infra.persistence.apisports.entity.*
import com.footballay.core.infra.persistence.apisports.repository.*
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.logger
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class FixtureApiSportsSyncer (
    // Api Sports repos
    private val fixtureApiSportsRepository: FixtureApiSportsRepository,
    private val leagueApiSportsRepository: LeagueApiSportsRepository,
    private val leagueApiSportsSeasonRepository: LeagueApiSportsSeasonRepository,
    private val teamApiSportsRepository: TeamApiSportsRepository,
    // Core repos
    private val fixtureCoreRepository: FixtureCoreRepository,
    private val leagueCoreRepository: LeagueCoreRepository,
    // Services
    private val venueApiSportsService: VenueApiSportsService,
    // Utility
    private val uidGenerator: UidGenerator
){

    val log = logger()

    @Transactional
    fun saveFixturesOfLeagueWithCurrentSeason(
        leagueApiId: Long,
        dtos: List<FixtureApiSportsCreateDto>
    ) {
        // 1. 입력 검증
        validateInput(leagueApiId, dtos)
        
        // 2. 리그/시즌 검증 및 조회
        val validationResult = validateAndRetrieveLeagueAndSeason(leagueApiId, dtos)
        
        // 3. DTO 정합성 검증
        validateDtoConsistency(dtos, leagueApiId, validationResult.seasonYear)
        
        // 4. 팀 사전 검증
        val teamCoreMap = validateAndRetrieveTeams(dtos)
        
        // 5. 기존 엔티티 조회
        val existingFixtureMap = retrieveExistingFixtures(dtos)
        
        // 6. Venue 처리
        val venueMap = processVenues(dtos)
        
        // 7. 엔티티 처리 및 저장
        val processedFixtures = processFixtures(
            dtos, validationResult, teamCoreMap, existingFixtureMap, venueMap
        )
        
        // 8. 일괄 저장
        saveFixtures(processedFixtures, leagueApiId, validationResult.seasonYear)
    }

    /**
     * 1. 입력 검증
     */
    private fun validateInput(leagueApiId: Long, dtos: List<FixtureApiSportsCreateDto>) {
        if (dtos.isEmpty()) {
            log.warn("No fixtures provided for league API ID: $leagueApiId")
            throw IllegalArgumentException("Fixtures list cannot be empty")
        }
        
        dtos.forEach { dto ->
            if (dto.apiId == null) {
                throw IllegalArgumentException("Fixture API ID must not be null")
            }
        }
    }

    /**
     * 2. 리그/시즌 검증 및 조회
     */
    private fun validateAndRetrieveLeagueAndSeason(
        leagueApiId: Long, 
        dtos: List<FixtureApiSportsCreateDto>
    ): LeagueSeasonValidationResult {
        val leagueApi = leagueApiSportsRepository.findByApiId(leagueApiId)
            ?: throw IllegalStateException("League with API ID $leagueApiId must be synced first")
        
        val leagueCore = leagueApi.leagueCore
            ?: throw IllegalStateException("League core not found for API ID: $leagueApiId")

        val firstDto = dtos.first()
        val seasonYear = firstDto.seasonYear?.toIntOrNull()
            ?: throw IllegalArgumentException("Season year must be provided")
        
        val season = leagueApiSportsSeasonRepository.findAllByLeagueApiSports(leagueApi)
            .find { it.seasonYear == seasonYear }
            ?: throw IllegalStateException("Season $seasonYear not found for league $leagueApiId. League seasons must be synced first")

        return LeagueSeasonValidationResult(leagueApi, leagueCore, season, seasonYear)
    }

    /**
     * 3. DTO 정합성 검증
     */
    private fun validateDtoConsistency(
        dtos: List<FixtureApiSportsCreateDto>, 
        expectedLeagueApiId: Long, 
        expectedSeasonYear: Int
    ) {
        dtos.forEach { dto ->
            if (dto.leagueApiId != expectedLeagueApiId) {
                throw IllegalArgumentException(
                    "DTO contains inconsistent league API ID: ${dto.leagueApiId}, expected: $expectedLeagueApiId"
                )
            }
            if (dto.seasonYear?.toIntOrNull() != expectedSeasonYear) {
                throw IllegalArgumentException(
                    "DTO contains inconsistent season year: ${dto.seasonYear}, expected: $expectedSeasonYear"
                )
            }
        }
    }

    /**
     * 4. 팀 사전 검증
     */
    private fun validateAndRetrieveTeams(dtos: List<FixtureApiSportsCreateDto>): Map<Long, TeamCore> {
        val teamApiIds = extractTeamApiIds(dtos)
        
        if (teamApiIds.isEmpty()) {
            return emptyMap()
        }

        val teamApiSportsList = teamApiSportsRepository.findAllByApiIdIn(teamApiIds.toList())
        validateTeamCompleteness(teamApiIds, teamApiSportsList)
        
        return teamApiSportsList.associate { it.apiId!! to it.teamCore!! }
    }

    /**
     * 5. 기존 엔티티 조회
     */
    private fun retrieveExistingFixtures(dtos: List<FixtureApiSportsCreateDto>): Map<Long, FixtureApiSports> {
        val fixtureApiIds = dtos.mapNotNull { it.apiId }
        return fixtureApiSportsRepository.findAllByApiIdIn(fixtureApiIds)
            .associateBy { it.apiId }
    }

    /**
     * 6. Venue 처리
     */
    private fun processVenues(dtos: List<FixtureApiSportsCreateDto>): Map<Long, VenueApiSports> {
        val venueDtos = dtos.mapNotNull { it.venue }.distinctBy { it.apiId }
        log.info("venueDtos: $venueDtos")
        return venueApiSportsService.processVenuesWithNewTransaction(venueDtos)
    }

    /**
     * 7. 엔티티 처리
     */
    private fun processFixtures(
        dtos: List<FixtureApiSportsCreateDto>,
        validationResult: LeagueSeasonValidationResult,
        teamCoreMap: Map<Long, TeamCore>,
        existingFixtureMap: Map<Long, FixtureApiSports>,
        venueMap: Map<Long, VenueApiSports>
    ): List<FixtureApiSports> {
        return dtos.map { dto ->
            val apiId = dto.apiId!!
            val existingFixture = existingFixtureMap[apiId]

            if (existingFixture != null) {
                updateExistingFixtureApiSports(existingFixture, dto, validationResult.season, venueMap, teamCoreMap)
            } else {
                createNewFixtureApiSports(dto, validationResult.leagueCore, validationResult.season, venueMap, teamCoreMap)
            }
        }
    }

    /**
     * 8. 일괄 저장
     */
    private fun saveFixtures(fixtures: List<FixtureApiSports>, leagueApiId: Long, seasonYear: Int) {
        fixtureApiSportsRepository.saveAll(fixtures)
        log.info("Successfully processed ${fixtures.size} fixtures for league $leagueApiId, season $seasonYear")
    }

    // === 헬퍼 메서드들 ===
    private fun extractTeamApiIds(dtos: List<FixtureApiSportsCreateDto>): Set<Long> {
        val teamApiIds = mutableSetOf<Long>()
        dtos.forEach { dto ->
            dto.homeTeam?.apiId?.let { teamApiIds.add(it) }
            dto.awayTeam?.apiId?.let { teamApiIds.add(it) }
        }
        return teamApiIds
    }

    private fun validateTeamCompleteness(
        expectedTeamApiIds: Set<Long>, 
        foundTeamApiSports: List<TeamApiSports>
    ) {
        val foundTeamApiIds = foundTeamApiSports.mapNotNull { it.apiId }.toSet()
        val missingTeamApiIds = expectedTeamApiIds - foundTeamApiIds
        
        if (missingTeamApiIds.isNotEmpty()) {
            throw IllegalStateException("Teams with API IDs $missingTeamApiIds must be synced first")
        }
        
        val teamsWithoutCore = foundTeamApiSports.filter { it.teamCore == null }
        if (teamsWithoutCore.isNotEmpty()) {
            val idsWithoutCore = teamsWithoutCore.mapNotNull { it.apiId }
            throw IllegalStateException("TeamCore missing for teams with API IDs: $idsWithoutCore")
        }
    }

    // === 데이터 클래스 ===
    
    data class LeagueSeasonValidationResult(
        val leagueApiSports: LeagueApiSports,
        val leagueCore: LeagueCore,
        val season: LeagueApiSportsSeason,
        val seasonYear: Int
    )

    /**
     * 기존 FixtureApiSports 엔티티 업데이트
     */
    private fun updateExistingFixtureApiSports(
        fixtureApiSports: FixtureApiSports,
        dto: FixtureApiSportsCreateDto,
        season: LeagueApiSportsSeason,
        processedVenueMap: Map<Long, VenueApiSports>,
        teamCoreMap: Map<Long, TeamCore>
    ): FixtureApiSports {
        // 기본 정보 업데이트
        fixtureApiSports.apply {
            referee = dto.referee
            timezone = dto.timezone
            date = dto.date?.let { OffsetDateTime.parse(it) }
            timestamp = dto.timestamp
            round = dto.round
            this.season = season

            // Status 업데이트
            status = dto.status?.let { statusDto ->
                ApiSportsStatus().apply {
                    longStatus = statusDto.longStatus
                    shortStatus = statusDto.shortStatus
                    elapsed = statusDto.elapsed
                    extra = statusDto.extra
                }
            }

            // Score 업데이트
            score = dto.score?.let { scoreDto ->
                ApiSportsScore().apply {
                    halftimeHome = scoreDto.halftimeHome
                    halftimeAway = scoreDto.halftimeAway
                    fulltimeHome = scoreDto.fulltimeHome
                    fulltimeAway = scoreDto.fulltimeAway
                    extratimeHome = scoreDto.extratimeHome
                    extratimeAway = scoreDto.extratimeAway
                    penaltyHome = scoreDto.penaltyHome
                    penaltyAway = scoreDto.penaltyAway
                }
            }

            // Venue 처리
            venue = dto.venue?.apiId?.let { processedVenueMap[it] }
        }

        // FixtureCore가 없는 비정상 케이스 처리
        if (fixtureApiSports.core == null) {
            log.warn("FixtureApiSports exists but FixtureCore is missing for API ID: ${dto.apiId}. Creating new FixtureCore.")
            val newFixtureCore = createFixtureCore(dto, season.leagueApiSports?.leagueCore!!, teamCoreMap)
            val savedFixtureCore = fixtureCoreRepository.save(newFixtureCore)
            fixtureApiSports.core = savedFixtureCore
        } else {
            // 기존 FixtureCore 업데이트
            updateFixtureCore(fixtureApiSports.core!!, dto, teamCoreMap)
        }

        return fixtureApiSports
    }

    /**
     * 새로운 FixtureApiSports 엔티티 생성
     */
    private fun createNewFixtureApiSports(
        dto: FixtureApiSportsCreateDto,
        leagueCore: LeagueCore,
        season: LeagueApiSportsSeason,
        processedVenueMap: Map<Long, VenueApiSports>,
        teamCoreMap: Map<Long, TeamCore>
    ): FixtureApiSports {
        // 1. FixtureCore 생성
        val fixtureCore = createFixtureCore(dto, leagueCore, teamCoreMap)
        val savedFixtureCore = fixtureCoreRepository.save(fixtureCore)

        // 2. Venue 처리
        val venueEntity = dto.venue?.apiId?.let { processedVenueMap[it] }

        // 3. FixtureApiSports 생성
        return FixtureApiSports(
            core = savedFixtureCore,
            apiId = dto.apiId!!,
            referee = dto.referee,
            timezone = dto.timezone,
            date = dto.date?.let { OffsetDateTime.parse(it) },
            timestamp = dto.timestamp,
            round = dto.round,
            venue = venueEntity,
            season = season,
            status = dto.status?.let { statusDto ->
                ApiSportsStatus().apply {
                    longStatus = statusDto.longStatus
                    shortStatus = statusDto.shortStatus
                    elapsed = statusDto.elapsed
                    extra = statusDto.extra
                }
            },
            score = dto.score?.let { scoreDto ->
                ApiSportsScore().apply {
                    halftimeHome = scoreDto.halftimeHome
                    halftimeAway = scoreDto.halftimeAway
                    fulltimeHome = scoreDto.fulltimeHome
                    fulltimeAway = scoreDto.fulltimeAway
                    extratimeHome = scoreDto.extratimeHome
                    extratimeAway = scoreDto.extratimeAway
                    penaltyHome = scoreDto.penaltyHome
                    penaltyAway = scoreDto.penaltyAway
                }
            }
            // homeTeam과 awayTeam은 LiveMatch 시에만 처리하므로 null로 유지
        )
    }

    /**
     * FixtureCore 엔티티 생성
     */
    private fun createFixtureCore(
        dto: FixtureApiSportsCreateDto,
        leagueCore: LeagueCore,
        teamCoreMap: Map<Long, TeamCore>
    ): FixtureCore {
        val homeTeam = dto.homeTeam?.apiId?.let { teamCoreMap[it] }
        val awayTeam = dto.awayTeam?.apiId?.let { teamCoreMap[it] }
        
        return FixtureCore(
            uid = uidGenerator.generateUid(),
            kickoff = dto.date?.let { OffsetDateTime.parse(it) } ?: OffsetDateTime.now(),
            timestamp = dto.timestamp ?: 0L,
            status = dto.status?.longStatus ?: "Unknown",
            statusShort = dto.status?.shortStatus ?: "UNK",
            elapsedMin = dto.status?.elapsed,
            league = leagueCore,
            homeTeam = homeTeam, // nullable 처리
            awayTeam = awayTeam, // nullable 처리
            goalsHome = dto.score?.fulltimeHome,
            goalsAway = dto.score?.fulltimeAway,
            finished = dto.status?.shortStatus == "FT",
            available = false, // 기본값: 관리자가 수동으로 활성화
            autoGenerated = true
        )
    }

    /**
     * 기존 FixtureCore 엔티티 업데이트
     */
    private fun updateFixtureCore(
        fixtureCore: FixtureCore,
        dto: FixtureApiSportsCreateDto,
        teamCoreMap: Map<Long, TeamCore>
    ) {
        val homeTeam = dto.homeTeam?.apiId?.let { teamCoreMap[it] }
        val awayTeam = dto.awayTeam?.apiId?.let { teamCoreMap[it] }
        
        fixtureCore.apply {
            kickoff = dto.date?.let { OffsetDateTime.parse(it) } ?: kickoff
            timestamp = dto.timestamp ?: timestamp
            status = dto.status?.longStatus ?: status
            statusShort = dto.status?.shortStatus ?: statusShort
            elapsedMin = dto.status?.elapsed ?: elapsedMin
            goalsHome = dto.score?.fulltimeHome ?: goalsHome
            goalsAway = dto.score?.fulltimeAway ?: goalsAway
            finished = dto.status?.shortStatus == "FT"
            // 팀 정보 업데이트 (nullable 처리)
            this.homeTeam = homeTeam
            this.awayTeam = awayTeam
        }
    }

}