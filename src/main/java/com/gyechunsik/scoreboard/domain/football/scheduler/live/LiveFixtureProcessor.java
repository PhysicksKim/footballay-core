package com.gyechunsik.scoreboard.domain.football.scheduler.live;

import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.external.live.LiveFixtureEventService;
import com.gyechunsik.scoreboard.domain.football.external.live.MatchLineupService;
import com.gyechunsik.scoreboard.domain.football.external.live.PlayerStatisticsService;
import com.gyechunsik.scoreboard.domain.football.external.live.TeamStatisticsService;
import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchLineup;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchPlayer;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LiveFixtureProcessor implements LiveFixtureTask {

    private final ApiCallService apiCallService;
    private final LiveFixtureEventService liveFixtureService;
    private final PlayerStatisticsService playerStatisticsService;
    private final TeamStatisticsService teamStatisticsService;
    private final MatchLineupService matchLineupService;
    private final FixtureRepository fixtureRepository;

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
    public boolean requestAndSaveLiveFixtureData(long fixtureId) {
        log.info("fixtureId={} live fixture cache started", fixtureId);
        boolean isFinished;
        try {
            FixtureSingleResponse fixtureSingleResponse = requestData(fixtureId);
            if(!isLineupCached(fixtureId)) {
                log.info("fixtureId={} live fixture cache FAILED. Lineup is not cached yet", fixtureId);
                return false;
            }

            isFinished = saveDataAndIsFinished(fixtureSingleResponse);
            log.info("fixtureId={} live fixture cache done. isFinished={}", fixtureId, isFinished);
        } catch (Exception e) {
            isFinished = false;
            log.error("fixtureId={} live fixture cache FAILED. isFinished={}", fixtureId, isFinished, e);
        }
        return isFinished;
    }

    private boolean isLineupCached(long fixtureId) {
        Fixture fixture = fixtureRepository.findById(fixtureId).orElseThrow(
                () -> new IllegalArgumentException("LiveFixtureData 저장 중 fixture 를 찾지 못했습니다. fixtureId=" + fixtureId + " 에 해당하는 Fixture 데이터가 없습니다.")
        );
        return matchLineupService.hasLineupData(fixture);
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
        try {
            saveFixtureEventsAndStatistics(response);
        } catch (Exception e) {
            log.error("Unexpected error while saving LiveFixtureEvent :: FixtureId={}", response.getResponse().get(0).getFixture().getId(), e);
        }
        return updateLiveStatusAndIsFinished(response);
    }

    private void saveFixtureEventsAndStatistics(FixtureSingleResponse response) {
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
