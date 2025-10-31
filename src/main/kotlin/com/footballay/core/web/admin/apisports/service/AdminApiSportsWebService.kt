package com.footballay.core.web.admin.apisports.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.infra.facade.ApiSportsBackboneSyncFacade
import com.footballay.core.infra.facade.AvailableLeagueFacade
import com.footballay.core.logger
import com.footballay.core.web.admin.apisports.dto.LeaguesSyncResultDto
import com.footballay.core.web.admin.apisports.dto.PlayersSyncResultDto
import com.footballay.core.web.admin.apisports.dto.TeamsSyncResultDto
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class AdminApiSportsWebService(
    private val apiSportsBackboneSyncFacade: ApiSportsBackboneSyncFacade,
    private val availableLeagueFacade: AvailableLeagueFacade,
) {
    val log = logger()

    /**
     * 현재 시즌의 모든 리그를 동기화합니다.
     */
    @PreAuthorize("hasRole('ADMIN')")
    fun syncCurrentLeagues(): DomainResult<LeaguesSyncResultDto, DomainFail> {
        log.info("Starting current leagues sync request")
        val result = apiSportsBackboneSyncFacade.syncCurrentLeagues()
        return when (result) {
            is DomainResult.Success -> {
                val count = result.value
                log.info("Successfully synced $count leagues")
                DomainResult.Success(
                    LeaguesSyncResultDto(
                        syncedCount = count,
                        message = "현재 시즌 리그 $count 개가 동기화되었습니다",
                    ),
                )
            }
            is DomainResult.Fail -> DomainResult.Fail(result.error)
        }
    }

    /**
     * 특정 리그의 팀들을 동기화합니다.
     *
     * @param leagueApiId 리그의 ApiSports ID
     * @param season 시즌 연도 (선택사항, 없으면 현재 시즌 사용)
     */
    @PreAuthorize("hasRole('ADMIN')")
    fun syncTeamsOfLeague(
        leagueApiId: Long,
        season: Int?,
    ): DomainResult<TeamsSyncResultDto, DomainFail> {
        log.info("Starting teams sync request for leagueApiId=$leagueApiId, season=$season")
        val result =
            if (season != null) {
                apiSportsBackboneSyncFacade.syncTeamsOfLeague(leagueApiId, season)
            } else {
                apiSportsBackboneSyncFacade.syncTeamsOfLeagueWithCurrentSeason(leagueApiId)
            }
        return when (result) {
            is DomainResult.Success -> {
                val count = result.value
                log.info("Successfully synced $count teams for league $leagueApiId")
                DomainResult.Success(
                    TeamsSyncResultDto(
                        syncedCount = count,
                        leagueApiId = leagueApiId,
                        season = season,
                        message = "리그($leagueApiId)의 팀 $count 개가 동기화되었습니다",
                    ),
                )
            }
            is DomainResult.Fail -> DomainResult.Fail(result.error)
        }
    }

    /**
     * 특정 팀의 선수들을 동기화합니다.
     *
     * @param teamApiId 팀의 ApiSports ID
     */
    @PreAuthorize("hasRole('ADMIN')")
    fun syncPlayersOfTeam(teamApiId: Long): DomainResult<PlayersSyncResultDto, DomainFail> {
        log.info("Starting players sync request for teamApiId=$teamApiId")
        val result = apiSportsBackboneSyncFacade.syncPlayersOfTeam(teamApiId)
        return when (result) {
            is DomainResult.Success -> {
                val count = result.value
                log.info("Successfully synced $count players for team $teamApiId")
                DomainResult.Success(
                    PlayersSyncResultDto(
                        syncedCount = count,
                        teamApiId = teamApiId,
                        message =
                            if (count > 0) {
                                "팀($teamApiId)의 선수 $count 명이 동기화되었습니다"
                            } else {
                                "팀($teamApiId)에 대한 선수 정보를 찾을 수 없습니다"
                            },
                    ),
                )
            }
            is DomainResult.Fail -> DomainResult.Fail(result.error)
        }
    }

    /**
     * 특정 리그의 현재 시즌 경기들을 sync 합니다.
     *
     * @param leagueApiId 리그의 ApiSports ID
     */
    @PreAuthorize("hasRole('ADMIN')")
    fun syncFixturesOfLeague(leagueApiId: Long): DomainResult<Int, DomainFail> {
        log.info("Starting fixtures sync request for leagueId=$leagueApiId")
        val result = apiSportsBackboneSyncFacade.syncFixturesOfLeagueWithCurrentSeason(leagueApiId)
        return when (result) {
            is DomainResult.Success -> DomainResult.Success(result.value)
            is DomainResult.Fail -> DomainResult.Fail(result.error)
        }
    }

    /**
     * 리그의 available 상태를 설정합니다.
     *
     * Available 리그는 해당 리그의 경기들이 실시간 동기화 대상이 됩니다.
     * (Fixture 개별 available 설정과는 별개로 작동)
     *
     * @param leagueApiId LeagueApiId ID
     * @param available Available 상태 (true: 활성화, false: 비활성화)
     * @return 성공 시 league UID, 실패 시 DomainFail
     */
    @PreAuthorize("hasRole('ADMIN')")
    fun setLeagueAvailable(
        leagueApiId: Long,
        available: Boolean,
    ): DomainResult<String, DomainFail> = availableLeagueFacade.setLeagueAvailable(leagueApiId, available)
}
