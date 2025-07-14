package com.footballay.core.domain.football.scheduler.lineup;

import com.footballay.core.domain.football.external.fetch.ApiCallService;
import com.footballay.core.domain.football.external.fetch.response.FixtureSingleResponse;
import com.footballay.core.domain.football.external.lineup.LineupService;
import com.footballay.core.domain.football.service.FixtureDataIntegrityService;
import com.footballay.core.monitor.alert.port.MatchAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.footballay.core.domain.football.scheduler.MatchTimeUtil.beforeStart10Minutes;

/**
 * 경기 시작 전 1시간 전 ~ 킥오프시각 까지 동작합니다.
 * 라인업을 캐싱하면서 라인업이 "완전한 라인업" 인 경우에만 job 을 종료하도록 합니다.
 * 라인업 저장은 3가지 상태로 나뉩니다.
 * 1. 데이터 없음 : 저장하지 않습니다. 스킵합니다.
 * 2. 데이터 있음. Unregistered Player 있음 : 저장합니다. 단, job 을 종료하지 않습니다.
 * 3. 데이터 있음. Unregistered Player 없음 (전원 registered) : 저장하고 job 을 종료합니다.
 * 데이터가 있으면 저장 이전에 "DB 에 저장된 이전 데이터와 비교" 합니다.
 * 이전 데이터가 존재 한다면 데이터를 비교하고 업데이트를 수행합니다.
 * 비교 업데이트는 "등장하고 사라진 registered Player 검사" 와 "unregistered player 였다가 새롭게 id 가 부여된 player 를 업데이트" 하는 과정을 필요로 합니다.
 * 이는 각각 updatePrevRegisteredPlayer() updatePrevUnregisteredPlayer() 로 해결합니다.
 * 따라서 전체적인 흐름은
 * <pre>
 * API 요청 - Lineup 존재 확인 - Lineup 데이터 추출 - 이전 데이터와 비교 및 업데이트 - 종료 조건 확인 후 리턴
 * </pre>
 * 이와 같습니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PreviousMatchProcessor implements PreviousMatchTask {

    private final ApiCallService apiCallService;

    private final FixtureDataIntegrityService fixtureDataIntegrityService;
    private final LineupService lineupService;
    private final MatchAlertService matchAlertService;

    /**
     * 주어진 경기 ID에 대한 라인업 데이터를 요청하고 데이터베이스에 저장합니다.
     * <p>
     * API 호출을 통해 경기 데이터를 가져오고, 라인업 데이터의 존재 여부를 확인합니다.
     * 클린업 및 재저장이 필요한 경우 해당 작업을 수행합니다.
     * 라인업이 완전하고(모든 선수가 등록됨) 성공적으로 저장되면 true 를 반환하여 작업을 종료할 수 있음을 나타냅니다.
     * </p>
     *
     * @param fixtureId 처리할 경기의 ID
     * @return 라인업이 완전하고 저장에 성공한 경우 true, 그렇지 않으면 false
     */
    @Override
    public boolean requestAndSaveLineup(long fixtureId) {
        FixtureSingleResponse response = null;
        try {
            response = requestData(fixtureId);
            boolean existLineupData = lineupService.existLineupDataInResponse(response);

            // Response 에 Lineup Data 가 없는 경우
            if(!existLineupData) {
                log.info("not exist lineup data in fixtureId={} response", fixtureId);
                sendAlertIfLineupDelayed(fixtureId, response);
                return false;
            }

            // Response 라인업 선수 목록 != DB 저장된 라인업 선수 목록 인 경우
            log.info("fixtureId={} response has lineup data. MatchLineup caching will be started", fixtureId);
            boolean needToCleanUpAndReSaveLineup = lineupService.isNeedToCleanUpAndReSaveLineup(response);
            if(needToCleanUpAndReSaveLineup) {
                log.info("fixtureId={} need to clean up and re-save Lineup", fixtureId);
                boolean isSuccess = cleanUpAndResaveLineup(response, fixtureId);
                sendAlertIfSuccess(fixtureId, isSuccess);
                return isSuccess;
            }
            return false;
        } catch (Exception e) {
            log.error("fixtureId={} lineup cache failed", fixtureId, e);
            if(response != null) {
                sendAlertLineupFail(fixtureId, response);
            }
            return false;
        }
    }

    private void sendAlertIfSuccess(long fixtureId, boolean isSuccess) {
        if(isSuccess) {
            matchAlertService.alertLineupSuccessOnce(
                    String.valueOf(fixtureId),
                    "The lineup for the prior match has been successfully saved."
            );
        }
    }

    private void sendAlertLineupFail(long fixtureId, FixtureSingleResponse response) {
        if(response == null) {
            log.debug("response is null while notifying, fixtureId={}", fixtureId);
            return;
        }
        matchAlertService.alertLineupFailureOnce(
                String.valueOf(fixtureId),
                "An error occurred while processing the lineup data for the match."
        );
    }

    private void sendAlertIfLineupDelayed(long fixtureId, FixtureSingleResponse response) {
        if(response == null) {
            log.debug("response is null while notifying, fixtureId={}", fixtureId);
            return;
        }
        String kickoffTime = response.getResponse().get(0).getFixture().getDate();
        if (beforeStart10Minutes(kickoffTime)) {
            matchAlertService.alertLineupFailureOnce(
                    String.valueOf(fixtureId),
                    "Lineup data is missing 10 minutes before the match starts."
            );
        }
    }

    private FixtureSingleResponse requestData(long fixtureId) {
        FixtureSingleResponse response = apiCallService.fixtureSingle(fixtureId);
        log.info("Successfully got API Response FROM 'ApiCallService' of fixtureId={}", fixtureId);
        if (response.getResponse() == null || response.getResponse().isEmpty()) {
            throw new IllegalArgumentException("FixtureSingle 응답에 Response 데이터가 없습니다. :: \n\n" + response.getResponse());
        }
        return response;
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
    private boolean cleanUpAndResaveLineup(FixtureSingleResponse response, long fixtureId) {
        log.info("fixtureId={} need to save Lineup", fixtureId);
        fixtureDataIntegrityService.cleanUpFixtureLiveData(fixtureId);
        try {
            return lineupService.saveLineup(response);
        } catch (IllegalStateException e) {
            log.info("fixtureId={} need to save Lineup again because of {}", fixtureId, e.getMessage());
            fixtureDataIntegrityService.cleanUpFixtureLiveData(fixtureId);
            return lineupService.saveLineup(response);
        } catch (Exception e) {
            log.error("Unexpected error while checking and saving Lineup when saving live data. Try to cleanUp and resave :: FixtureId={}", fixtureId, e);
            return false;
        }
    }
}
