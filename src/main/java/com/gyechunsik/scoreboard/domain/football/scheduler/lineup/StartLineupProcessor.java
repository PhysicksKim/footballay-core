package com.gyechunsik.scoreboard.domain.football.scheduler.lineup;

import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.external.lineup.LineupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 경기 시작 전 1시간 전 ~ 킥오프시각 까지 동작합니다.
 * 라인업을 캐싱하면서 라인업이 "완전한 라인업" 인 경우에만 job 을 종료하도록 합니다.
 * 라인업 저장은 3가지 상태로 나뉩니다.
 * 1. 데이터 없음 : 저장하지 않습니다. 스킵합니다.
 * 2. 데이터 있음. Unregistered Player 있음 : 저장합니다. 단, job 을 종료하지 않습니다.
 * 3. 데이터 있음. Unregistered Player 없음 (전원 registered) : 저장하고 job 을 종료합니다.
 *
 * 데이터가 있으면 저장 이전에 "이전 데이터와 비교" 합니다.
 * "이전 데이터가 존재" 한다면 데이터를 비교 업데이트 하도록 동작합니다.
 * 비교 업데이트는 "등장하고 사라진 registered Player 검사" 와 "unregistered player 였다가 새롭게 id 가 부여된 player 를 업데이트" 하는 과정을 필요로 합니다.
 * 이는 각각 updatePrevRegisteredPlayer() updatePrevUnregisteredPlayer() 로 해결합니다.
 *
 * 따라서 전체적인 흐름은
 * <pre>
 * API 요청 - Lineup 존재 확인 - Lineup 데이터 추출 - 이전 데이터와 비교 및 업데이트 - 종료 조건 확인 후 리턴
 * </pre>
 * 이와 같습니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class StartLineupProcessor implements StartLineupTask {

    private final ApiCallService apiCallService;
    private final LineupService lineupService;

    // TODO : Lineup 의 선수 중 id=null 인 선수가 있다면 에러가 발생합니다.
    //  이를 unregisteredPlayer 로 처리하더라도, 잠시 후에 id=null 인 선수가 id 가 부여되는 경우가 있습니다.
    //  특히 유스 선수들에게 이런 문제가 자주 발생합니다.
    //  name 으로 비교하면 안됩니다. id=null 로 주어졌다가 id 가 부여된 뒤에 이름이 바뀌기도 합니다.
    //  예를 들어 fixture: 1208117 에서 {id: null, name: "Jack Andrew Fletcher"} 가 {id: 383770, name: "Jack Fletcher"} 로 된 사례가 있습니다.
    //  따라서 unregisteredPlayer 가 registeredPlayer 로 바뀐 경우에 어떻게 처리할지에 대한 대책이 필요합니다.

    /*
    1) 이전에 라인업 데이터가 있는 경우 - 이전 데이터 가져와서 완전성 검사
    2-1) 라인업 데이터 있는 경우 - 미등록 선수 있는 경우
    2-2) 라인업 데이터 있는 경우
     */
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

            boolean hasLineupData = lineupService.existLineupDataInResponse(fixtureSingleResponse);
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
