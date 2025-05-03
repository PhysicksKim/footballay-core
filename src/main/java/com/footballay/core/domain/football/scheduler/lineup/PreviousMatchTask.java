package com.footballay.core.domain.football.scheduler.lineup;

/**
 * 라인업 정보를 요청하고 저장하는 Task. Scheduler 의 job 에서 사용합니다.
 */
public interface PreviousMatchTask {

    /**
     * 라인업 정보를 요청하고 저장한다.
     * @param fixtureId 경기 ID
     * @return 라인업 캐싱 성공 및 더 이상 저장할 필요가 없는 경우 true, 그렇지 않으면 false
     */
    boolean requestAndSaveLineup(long fixtureId);
}
