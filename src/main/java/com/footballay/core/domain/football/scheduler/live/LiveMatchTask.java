package com.footballay.core.domain.football.scheduler.live;

/*
Service 가 job 등록 -> Job 이 task 수행 -> Task 가 실직적으로 로직 처리
 */
/**
 * Quartz Job 을 통해 라이브 상태의 fixture 에 대한 처리 작업을 수행합니다.
 * 이 작업은 Single fixture Request 를 발생시키고, _Response 를 받아서 FixtureEvent 값을 업데이트하는 작업을 발생시킵니다.
 * 기타 라이브 정보를 캐싱하는 작업을 포함합니다.
 */
public interface LiveMatchTask {

    /**
     * 라이브 fixture 정보를 사용해 FixtureEvent 정보를 업데이트합니다.
     * @param fixtureId
     * @return 업데이트 성공 여부
     */
    boolean requestAndSaveLiveMatchData(long fixtureId);

}
