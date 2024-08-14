package com.gyechunsik.scoreboard.domain.football.scheduler.live;

import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.external.live.LiveFixtureEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LiveFixtureProcessor implements LiveFixtureTask {

    private final ApiCallService apiCallService;
    private final LiveFixtureEventService liveFixtureService;

    /**
     * fixtureId 를 받아서 해당 경기의 라이브 정보를 캐싱한다.
     *
     * @param fixtureId 경기 ID
     * @return live status 에 따라서 경기가 끝났는지 여부. 끝나면 true
     */
    @Override
    public boolean requestAndSaveLiveFixtureData(long fixtureId) {
        log.info("fixtureId={} live fixture cache started", fixtureId);
        boolean isFinished;
        try {
            FixtureSingleResponse fixtureSingleResponse = requestData(fixtureId);
            isFinished = saveDataAndIsFinished(fixtureSingleResponse);
            log.info("fixtureId={} live fixture cache done. isFinished={}", fixtureId, isFinished);
        } catch (Exception e) {
            isFinished = false;
            log.error("fixtureId={} live fixture cache FAILED. isFinished={}", fixtureId, isFinished, e);
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
        try {
            saveFixtureEvents(response);
        } catch (Exception e) {
            log.error("Unexpected error while saving LiveFixtureEvent :: FixtureId={}", response.getResponse().get(0).getFixture().getId(), e);
        }
        return updateLiveStatusAndIsFinished(response);
    }

    private void saveFixtureEvents(FixtureSingleResponse response) {
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
    }

    /**
     * 라이브 상태의 fixture 에 대한 처리 작업을 수행합니다.
     *
     * @return isFinished 경기 종료시 true
     */
    private boolean updateLiveStatusAndIsFinished(FixtureSingleResponse response) {
        return liveFixtureService.updateLiveStatus(response);
    }
}
