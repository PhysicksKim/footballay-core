package com.gyechunsik.scoreboard.domain.football.scheduler.live;

import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.external.lineup.LineupService;
import com.gyechunsik.scoreboard.domain.football.external.live.LiveFixtureEventService;
import com.gyechunsik.scoreboard.domain.football.external.live.PlayerStatisticsService;
import com.gyechunsik.scoreboard.domain.football.external.live.TeamStatisticsService;
import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchLineup;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchPlayer;
import com.gyechunsik.scoreboard.domain.football.service.FixtureDataIntegrityService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LiveMatchProcessor implements LiveMatchTask {

    private final ApiCallService apiCallService;

    private final FixtureDataIntegrityService fixtureDataIntegrityService;
    private final LineupService lineupService;

    private final LiveFixtureEventService liveFixtureService;
    private final TeamStatisticsService teamStatisticsService;
    private final PlayerStatisticsService playerStatisticsService;

    /**
     * `fixtureId` 를 받아서 해당 경기의 라이브 정보를 캐싱합니다. <br>
     * 라이브 정보란 매치 중 라이브로 변경되는 데이터들을 말합니다. <br>
     * 라이브 캐싱은 경기 시작 부터 경기가 진행되는 동안 Polling 하여 데이터를 업데이트 합니다. <br>
     * 해당 {@link Fixture} 의 {@link MatchLineup} 이 아직 캐싱되지 않았다면 캐싱을 시도하지 않고 시행을 넘깁니다. <br>
     * 이는 라이브 정보 Task 가 시행되기 이전에, {@link MatchLineup} 의 {@link MatchPlayer} 가 우선 캐싱 될 것을 전제로 하기 때문입니다. <br>
     * @param fixtureId 경기 ID
     * @return live status 에 따라서 경기가 끝났는지 여부. 끝나면 true
     */
    @Override
    public boolean requestAndSaveLiveMatchData(long fixtureId) {
        log.info("fixtureId={} live fixture cache started", fixtureId);
        boolean isFinished = false;
        try {
            FixtureSingleResponse fixtureSingleResponse = requestData(fixtureId);
            isFinished = saveDataAndIsFinished(fixtureSingleResponse);
            log.info("fixtureId={} live data cache done. isFinished={}", fixtureId, isFinished);
        } catch (Exception e) {
            log.error("fixtureId={} live data cache FAILED. isFinished={}", fixtureId, isFinished, e);
        }
        return isFinished;
    }

    private FixtureSingleResponse requestData(long fixtureId) {
        FixtureSingleResponse response = apiCallService.fixtureSingle(fixtureId);
        log.info("Successfully got API Response FROM 'ApiCallService' of fixtureId={}", fixtureId);
        if (response.getResponse().isEmpty()) {
            throw new IllegalArgumentException("FixtureSingle 응답에 Response 데이터가 없습니다. :: \n\n" + response.getResponse());
        }
        return response;
    }

    private boolean saveDataAndIsFinished(FixtureSingleResponse response) {
        log.info("Data Saving is Started");
        long fixtureId = response.getResponse().get(0).getFixture().getId();
        checkWhetherLineupChangedAndResave(response, fixtureId);
        saveFixtureLiveData(response);
        return updateLiveStatusAndIsFinished(response);
    }

    private void checkWhetherLineupChangedAndResave(FixtureSingleResponse response, long fixtureId) {
        try{
            boolean needToReSaveLineup = lineupService.isNeedToCleanUpAndReSaveLineup(response);
            if(!needToReSaveLineup) {
                log.info("no need to save Lineup while saving live data");
                return;
            }
            log.info("need to save Lineup while saving live data");
            cleanUpAndResaveLineup(response, fixtureId);
        } catch (Exception e) {
            log.error("Unexpected error while checking and saving Lineup when saving live data. Try to cleanUp and resave :: FixtureId={}", fixtureId, e);
            cleanUpAndResaveLineup(response, fixtureId);
        }
    }

    /**
     * 해당 경기의 기존 라이브 데이터를 정리하고 새로운 라인업 데이터를 다시 저장합니다.
     * <p>
     * 기존 라인업 데이터를 새로 저장해야 할 때 호출됩니다.
     * 먼저 경기와 관련된 기존 라이브 데이터를 정리한 후, 새로운 라인업 데이터를 저장합니다.
     * 라인업이 완전하고(모든 선수가 등록됨) 성공적으로 저장되면 true 를 반환합니다.
     * </p>
     *
     * @param response 새로운 라인업 데이터를 포함하는 FixtureSingleResponse
     * @param fixtureId 경기의 ID
     * @return 라인업이 완전하고 저장에 성공한 경우 true, 그렇지 않으면 false
     */
    private void cleanUpAndResaveLineup(FixtureSingleResponse response, long fixtureId) {
        fixtureDataIntegrityService.cleanUpFixtureLiveData(fixtureId);
        lineupService.saveLineup(response);
    }

    private void saveFixtureLiveData(@NotNull FixtureSingleResponse response) {
        assert !response.getResponse().isEmpty();
        long fixtureId = response.getResponse().get(0).getFixture().getId();
        try {
            log.info("fixtureId={} has live fixture data. caching events will be started", fixtureId);
            liveFixtureService.saveLiveEvent(response);
        } catch (Exception e) {
            // 저장된 fixture 의 live Event Entity 들을 다시 삭제하고, response 를 기반을 전부 다시 저장 시도
            log.error("Unexpected error while saving LiveFixtureEvent :: FixtureId={}", fixtureId, e);
            liveFixtureService.resolveFixtureEventIntegrityError(response);
            log.info("Resolved Unexpected error while saving LiveFixtureEvent :: FixtureId={}", fixtureId);
            log.info("Removed Previous Saved FixtureEvent Entities and Re-Saved All Events :: FixtureId={}", fixtureId);
        }
        try {
            log.info("fixtureId={} has live fixture data. caching team statistics will be started", fixtureId);
            teamStatisticsService.saveTeamStatistics(response);
        } catch (Exception e) {
            log.error("Unexpected error while saving TeamStatistics :: FixtureId={}", fixtureId, e);
        }
        try {
            log.info("fixtureId={} has live fixture data. caching player statistics will be started", fixtureId);
            playerStatisticsService.savePlayerStatistics(response);
        } catch (Exception e) {
            log.error("Unexpected error while saving PlayerStatistics :: FixtureId={}", fixtureId, e);
        }
    }

    /**
     * 라이브 상태의 fixture 에 대한 처리 작업을 수행합니다.
     * @return isFinished 경기 종료시 true
     */
    private boolean updateLiveStatusAndIsFinished(FixtureSingleResponse response) {
        return liveFixtureService.updateLiveStatus(response);
    }
}
