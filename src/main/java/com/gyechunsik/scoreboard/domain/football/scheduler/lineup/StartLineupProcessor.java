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
     * fixtureId 를 받아서 해당 경기의 라인업 정보를 캐싱합니다. <br>
     * 만약 동일한 fixture 에 대해 여러 번 호출되는 경우 lineup 데이터가 중복으로 저장될 수 있습니다. <br>
     * 라인업 데이터의 무결성 보장을 위하여 이전 데이터를 삭제하고 다시 저장하도록 합니다.
     * @param fixtureId 경기 ID
     * @return 라인업 캐싱 성공 여부
     */
    @Override
    public boolean requestAndSaveLineup(long fixtureId) {
        try {
            // 이미 lineup 정보가 있는데 다시 call 된 경우
            // available fixture 를 지웠다가 다시 등록한 경우, 좀 더 나은 데이터가 나올 수 있으므로 그냥 삭제했다가 다시 등록하도록함
            lineupService.cleanupPreviousLineup(fixtureId);

            FixtureSingleResponse fixtureSingleResponse = apiCallService.fixtureSingle(fixtureId);

            boolean hasLineupData = lineupService.hasLineupData(fixtureSingleResponse);
            // Lineup Data 가 없는 경우
            if(!hasLineupData) {
                log.info("fixtureId={} has no lineup data", fixtureId);
                return false;
            }

            // Lineup Data 가 있는 경우
            log.info("fixtureId={} has lineup data. 'MatchLineup' caching will be started", fixtureId);
            lineupService.saveLineup(fixtureSingleResponse);
        } catch (Exception e) {
            log.error("fixtureId={} lineup cache failed", fixtureId, e);
            return false;
        }
        return true;
    }
}
