package com.footballay.core.web.admin.football.service;

import com.footballay.core.domain.football.FootballRoot;
import com.footballay.core.domain.football.dto.ExternalApiStatusDto;
import com.footballay.core.domain.football.persistence.Player;
import com.footballay.core.web.admin.football.response.ExternalApiStatusResponse;
import com.footballay.core.web.admin.football.response.mapper.FootballDtoMapper;
import com.footballay.core.web.common.dto.ApiResponse;
import com.footballay.core.web.common.service.ApiCommonResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminFootballCacheWebService {

    private final FootballRoot footballRoot;
    private final ApiCommonResponseService apiCommonResponseService;

    public ApiResponse<ExternalApiStatusResponse> getApiStatus(String requestUrl) {
        try{
            ExternalApiStatusDto apiStatus = footballRoot.getExternalApiStatus();
            ExternalApiStatusResponse[] response = {FootballDtoMapper.toExternalApiStatusDto(apiStatus)};
            return apiCommonResponseService.createSuccessResponse(response, requestUrl);
        } catch (Exception e) {
            log.error("error while getting api status :: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("API 상태 조회 실패", requestUrl);
        }
    }

    public ApiResponse<Void> cacheLeague(Long leagueId, String requestUrl) {
        boolean isSuccess = footballRoot.cacheLeagueById(leagueId);
        if(!isSuccess) {
            log.error("error while caching league :: leagueId={}", leagueId);
            return apiCommonResponseService.createFailureResponse("리그 캐싱 실패", requestUrl);
        }
        log.info("api league {} cached", leagueId);
        return apiCommonResponseService.createSuccessResponse(null, requestUrl);
    }

    public ApiResponse<Void> cacheAllCurrentLeagues(String requestUrl) {
        boolean isSuccess = footballRoot.cacheAllCurrentLeagues();
        if(!isSuccess) {
            log.error("error while caching all current leagues");
            return apiCommonResponseService.createFailureResponse("모든 현재 리그 캐싱 실패", requestUrl);
        }
        log.info("api All Current Leagues Cached");
        return apiCommonResponseService.createSuccessResponse(null, requestUrl);
    }

    public ApiResponse<Void> cacheTeamAndCurrentLeagues(Long teamId, String requestUrl) {
        boolean isSuccess = footballRoot.cacheTeamAndCurrentLeagues(teamId);
        if(!isSuccess) {
            log.error("error while caching team and current leagues :: teamId={}", teamId);
            return apiCommonResponseService.createFailureResponse("팀 및 현재 리그 캐싱 실패", requestUrl);
        }
        log.info("api team and current leagues of teamId {} cached", teamId);
        return apiCommonResponseService.createSuccessResponse(null, requestUrl);
    }

    public ApiResponse<Void> cacheTeam(Long teamId, String requestUrl) {
        boolean isSuccess = footballRoot.cacheTeamAndCurrentLeagues(teamId);
        if(!isSuccess) {
            log.error("error while caching team :: teamId={}", teamId);
            return apiCommonResponseService.createFailureResponse("팀 캐싱 실패", requestUrl);
        }
        log.info("api teamId {} cached. _eam and CurrentLeagues of the team", teamId);
        return apiCommonResponseService.createSuccessResponse(null, requestUrl);
    }

    public ApiResponse<Void> cacheTeamsByLeagueId(Long leagueId, String requestUrl) {
        boolean isSuccess = footballRoot.cacheTeamsOfLeague(leagueId);
        if(!isSuccess) {
            log.error("error while caching teams by leagueId :: leagueId={}", leagueId);
            return apiCommonResponseService.createFailureResponse("리그에 속한 팀 캐싱 실패", requestUrl);
        }
        log.info("api teams cached of leagueId {}", leagueId);
        return apiCommonResponseService.createSuccessResponse(null, requestUrl);
    }

    public ApiResponse<Void> cacheSquad(Long teamId, String requestUrl) {
        boolean isSuccess = footballRoot.cacheSquadOfTeam(teamId);
        if(!isSuccess) {
            log.error("error while caching squad :: teamId={}", teamId);
            return apiCommonResponseService.createFailureResponse("팀 선수단 캐싱 실패", requestUrl);
        }
        log.info("api teamId {} squad cached", teamId);
        return apiCommonResponseService.createSuccessResponse(null, requestUrl);
    }

    public ApiResponse<Void> cacheFixturesOfLeagueCurrentSeason(Long leagueId, String requestUrl) {
        boolean isSuccess = footballRoot.cacheAllFixturesOfLeague(leagueId);
        if(!isSuccess) {
            log.error("error while caching fixtures by league and season :: leagueId={}", leagueId);
            return apiCommonResponseService.createFailureResponse("리그 일정 캐싱 실패", requestUrl);
        }
        log.info("Fixtures of league {} cached successfully.", leagueId);
        return apiCommonResponseService.createSuccessResponse(null, requestUrl);
    }

    public ApiResponse<Void> cachePlayerSingle(long playerId, long leagueId, int season, String requestUrl) {
        boolean isSuccess = footballRoot.cachePlayerSingle(playerId, leagueId, season);
        if(!isSuccess) {
            log.error("error while caching player :: playerId={}", playerId);
            return apiCommonResponseService.createFailureResponse("선수 캐싱 실패", requestUrl);
        }
        log.info("api playerId {} cached", playerId);
        return apiCommonResponseService.createSuccessResponse(null, requestUrl);
    }

    public ApiResponse<Void> cacheStandingOfLeague(long leagueId, String requestUrl) {
        boolean isSuccess = footballRoot.cacheStandingOfLeague(leagueId);
        if(!isSuccess) {
            log.error("error while caching standing of league :: leagueId={}", leagueId);
            return apiCommonResponseService.createFailureResponse("리그 순위 캐싱 실패", requestUrl);
        }
        log.info("api leagueId {} standing cached", leagueId);
        return apiCommonResponseService.createSuccessResponse(null, requestUrl);
    }

    /**
     * 선수 Prevent Unlink 설정 <br>
     * @see Player#getPreventUnlink()
     * @param playerId
     * @param preventUnlink
     * @param requestUrl
     * @return
     */
    public ApiResponse<Void> setPlayerPreventUnlink(long playerId, boolean preventUnlink, String requestUrl) {
        boolean isSuccess = footballRoot.setPlayerPreventUnlink(playerId, preventUnlink);
        if(!isSuccess) {
            log.error("error while setting player prevent unlink :: playerId={}", playerId);
            return apiCommonResponseService.createFailureResponse("playerId="+playerId+" Prevent Unlink 설정 실패", requestUrl);
        }
        log.info("api playerId {} Prevent Unlink={} 설정 성공", playerId, preventUnlink);
        return apiCommonResponseService.createSuccessResponse(null, requestUrl);
    }

}
