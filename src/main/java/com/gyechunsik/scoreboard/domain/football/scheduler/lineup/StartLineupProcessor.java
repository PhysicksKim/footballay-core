package com.gyechunsik.scoreboard.domain.football.scheduler.lineup;

import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.external.lineup.LineupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class StartLineupProcessor implements StartLineupTask {

    private final ApiCallService apiCallService;
    private final LineupService lineupService;

    /**
     * fixtureId 를 받아서 해당 경기의 라인업 정보를 캐싱한다.
     * @param fixtureId 경기 ID
     * @return 라인업 캐싱 성공 여부
     */
    @Override
    public boolean requestAndSaveLineup(long fixtureId) {
        try {
            FixtureSingleResponse fixtureSingleResponse = apiCallService.fixtureSingle(fixtureId);

            boolean hasLineupData = lineupService.hasLineupData(fixtureSingleResponse);
            // Lineup Data 가 없는 경우
            if(!hasLineupData) {
                log.info("fixtureId={} has no lineup data", fixtureId);
                return false;
            }

            // Lineup Data 가 있는 경우
            log.info("fixtureId={} has lineup data. Cache will be started", fixtureId);
            lineupService.saveLineup(fixtureSingleResponse);
        } catch (Exception e) {
            log.error("fixtureId={} lineup cache failed", fixtureId, e);
            return false;
        }
        return true;
    }
}
