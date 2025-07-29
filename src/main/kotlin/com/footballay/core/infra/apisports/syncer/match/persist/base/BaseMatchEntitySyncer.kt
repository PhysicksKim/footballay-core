package com.footballay.core.infra.apisports.syncer.match.persist.base

import com.footballay.core.infra.apisports.syncer.match.context.MatchEntityBundle
import com.footballay.core.infra.apisports.syncer.match.dto.FixtureApiSportsDto
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchTeamRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.util.UidGenerator
import com.footballay.core.logger
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Phase 2: Base DTO 처리 (Fixture + MatchTeam 생성/업데이트)
 * 
 * **책임:**
 * - FixtureApiSports 엔티티 업데이트
 * - ApiSportsMatchTeam (home/away) 생성/업데이트
 * - 팀 정보 연결 및 유니폼 색상 설정
 * 
 * **처리 과정:**
 * 1. 기존 FixtureApiSports 조회
 * 2. FixtureApiSports 기본 정보 업데이트
 * 3. Home/Away MatchTeam 생성/업데이트
 * 4. 팀 정보 연결 및 유니폼 색상 설정
 * 5. 변경사항 저장
 */
@Component
class BaseMatchEntitySyncer(
    private val matchTeamRepository: ApiSportsMatchTeamRepository,
    private val teamApiSportsRepository: TeamApiSportsRepository,
) {
    
    private val log = logger()
    
    /**
     * Base DTO를 처리하여 Fixture와 MatchTeam 엔티티를 생성/업데이트합니다.
     * 
     * @param fixtureApiId 경기 API ID
     * @param baseDto 기본 경기 정보 DTO
     * @param entityBundle 기존 엔티티 번들 (fetch join으로 로드된 상태)
     * @return 처리된 엔티티들
     */
    @Transactional
    fun syncBaseEntities(fixtureApiId: Long, baseDto: FixtureApiSportsDto, entityBundle: MatchEntityBundle): BaseMatchSyncResult {
        log.info("Starting base entity sync for fixture: $fixtureApiId")
        
        try {
            // 1. EntityBundle에서 기존 FixtureApiSports 사용 (이미 fetch join으로 로드됨)
            val existingFixture = entityBundle.fixture
            if (existingFixture == null) {
                log.warn("Fixture not found in entityBundle for apiId: $fixtureApiId")
                return BaseMatchSyncResult.failure("Fixture not found in entityBundle for apiId: $fixtureApiId")
            }
            
            // 2. FixtureApiSports 기본 정보 업데이트
            updateFixtureApiSports(existingFixture, baseDto)
            
            // 3. Home/Away MatchTeam 생성/업데이트
            val homeMatchTeam = syncMatchTeam(existingFixture, baseDto.homeTeam, isHome = true)
            val awayMatchTeam = syncMatchTeam(existingFixture, baseDto.awayTeam, isHome = false)
            
            // 4. Fixture에 MatchTeam 연결
            existingFixture.homeTeam = homeMatchTeam
            existingFixture.awayTeam = awayMatchTeam
            
            // 5. EntityBundle 업데이트
            entityBundle.homeTeam = homeMatchTeam
            entityBundle.awayTeam = awayMatchTeam
            
            // 6. 변경사항 저장 (이미 영속 상태이므로 변경 감지로 자동 저장됨)
            log.info("Base entity sync completed successfully for fixture: $fixtureApiId")
            
            return BaseMatchSyncResult.success(
                fixture = existingFixture,
                homeMatchTeam = homeMatchTeam,
                awayMatchTeam = awayMatchTeam
            )
            
        } catch (e: Exception) {
            log.error("Failed to sync base entities for fixture: $fixtureApiId", e)
            return BaseMatchSyncResult.failure("Base entity sync failed: ${e.message}")
        }
    }
    
    /**
     * FixtureApiSports 엔티티의 기본 정보를 업데이트합니다.
     */
    private fun updateFixtureApiSports(fixture: FixtureApiSports, baseDto: FixtureApiSportsDto) {
        fixture.apply {
            referee = baseDto.referee ?: referee
            timezone = baseDto.timezone ?: timezone
            date = baseDto.date ?: date
            timestamp = baseDto.timestamp ?: timestamp
            round = baseDto.round ?: round
            
            // Status 업데이트
            if (baseDto.status != null) {
                if (status == null) {
                    status = com.footballay.core.infra.persistence.apisports.entity.ApiSportsStatus()
                }
                status?.apply {
                    longStatus = baseDto.status.longStatus ?: longStatus
                    shortStatus = baseDto.status.shortStatus ?: shortStatus
                    elapsed = baseDto.status.elapsed ?: elapsed
                    extra = baseDto.status.extra ?: extra
                }
            }
            
            // Score 업데이트
            if (baseDto.score != null) {
                if (score == null) {
                    score = com.footballay.core.infra.persistence.apisports.entity.ApiSportsScore()
                }
                score?.apply {
                    totalHome = baseDto.score.totalHome ?: totalHome
                    totalAway = baseDto.score.totalAway ?: totalAway
                    halftimeHome = baseDto.score.halftimeHome ?: halftimeHome
                    halftimeAway = baseDto.score.halftimeAway ?: halftimeAway
                    fulltimeHome = baseDto.score.fulltimeHome ?: fulltimeHome
                    fulltimeAway = baseDto.score.fulltimeAway ?: fulltimeAway
                    extratimeHome = baseDto.score.extratimeHome ?: extratimeHome
                    extratimeAway = baseDto.score.extratimeAway ?: extratimeAway
                    penaltyHome = baseDto.score.penaltyHome ?: penaltyHome
                    penaltyAway = baseDto.score.penaltyAway ?: penaltyAway
                }
            }
        }
    }
    
    /**
     * MatchTeam 엔티티를 생성하거나 업데이트합니다.
     */
    private fun syncMatchTeam(
        fixture: FixtureApiSports, 
        teamDto: FixtureApiSportsDto.BaseTeamDto?, 
        isHome: Boolean
    ): ApiSportsMatchTeam? {
        if (teamDto == null) {
            log.warn("Team DTO is null for ${if (isHome) "home" else "away"} team")
            return null
        }
        
        // 기존 MatchTeam 조회
        val existingMatchTeam = if (isHome) fixture.homeTeam else fixture.awayTeam
        
        return if (existingMatchTeam != null) {
            // 기존 MatchTeam 업데이트
            log.info("Updating existing MatchTeam for ${if (isHome) "home" else "away"} team: ${teamDto.apiId}")
            updateMatchTeam(existingMatchTeam, teamDto)
            matchTeamRepository.save(existingMatchTeam)
        } else {
            // 새로운 MatchTeam 생성
            log.info("Creating new MatchTeam for ${if (isHome) "home" else "away"} team: ${teamDto.apiId}")
            createMatchTeam(teamDto)
        }
    }
    
    /**
     * 기존 MatchTeam 엔티티를 업데이트합니다.
     */
    private fun updateMatchTeam(matchTeam: ApiSportsMatchTeam, teamDto: FixtureApiSportsDto.BaseTeamDto) {
        if(matchTeam.teamApiSports == null || matchTeam.teamApiSports?.apiId != teamDto.apiId) {
            log.warn("Unexpected null or changed teamApiSports for MatchTeam with id: ${matchTeam.id} try to relate it with TeamApiSports")
            val teamApiSports = teamApiSportsRepository.findByApiId(teamDto.apiId)
            if(teamApiSports == null) {
                log.error("TeamApiSports not found for MatchTeam of apiId: ${teamDto.apiId}")
                return
            }
        }
        matchTeam.winner = teamDto.winner
    }
    
    /**
     * 새로운 MatchTeam 엔티티를 생성합니다.
     */
    private fun createMatchTeam(
        teamDto: FixtureApiSportsDto.BaseTeamDto,
    ): ApiSportsMatchTeam {
        // 팀 정보 조회
        val teamApiSports = teamApiSportsRepository.findByApiId(teamDto.apiId)
            ?: throw IllegalArgumentException("TeamApiSports not found for apiId: ${teamDto.apiId}")
        
        // 기본 유니폼 색상 설정
        val playerColor = com.footballay.core.infra.persistence.apisports.entity.live.UniformColor(
            primary = "000000",
            number = "ffffff", 
            border = "000000"
        )
        
        val goalkeeperColor = com.footballay.core.infra.persistence.apisports.entity.live.UniformColor(
            primary = "ffffff",
            number = "000000",
            border = "000000"
        )
        
        val matchTeam = ApiSportsMatchTeam(
            teamApiSports = teamApiSports,
            formation = "", // 라인업에서 설정됨
            playerColor = playerColor,
            goalkeeperColor = goalkeeperColor,
            winner = teamDto.winner
        )
        
        return matchTeamRepository.save(matchTeam)
    }
}

/**
 * Base Match 동기화 결과
 */
data class BaseMatchSyncResult(
    val success: Boolean,
    val fixture: FixtureApiSports? = null,
    val homeMatchTeam: ApiSportsMatchTeam? = null,
    val awayMatchTeam: ApiSportsMatchTeam? = null,
    val errorMessage: String? = null
) {
    companion object {
        fun success(
            fixture: FixtureApiSports,
            homeMatchTeam: ApiSportsMatchTeam?,
            awayMatchTeam: ApiSportsMatchTeam?
        ): BaseMatchSyncResult {
            return BaseMatchSyncResult(
                success = true,
                fixture = fixture,
                homeMatchTeam = homeMatchTeam,
                awayMatchTeam = awayMatchTeam
            )
        }
        
        fun failure(errorMessage: String): BaseMatchSyncResult {
            return BaseMatchSyncResult(
                success = false,
                errorMessage = errorMessage
            )
        }
    }
} 