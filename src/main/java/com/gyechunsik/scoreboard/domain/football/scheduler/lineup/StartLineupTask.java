package com.gyechunsik.scoreboard.domain.football.scheduler.lineup;

/**
 * 라인업 정보를 요청하고 저장하는 Task. Scheduler 의 job 에서 사용합니다.
 */
public interface StartLineupTask {

    /**
     * 라인업 정보를 요청하고 저장한다.
     * @param fixtureId 경기 ID
     * @return 라인업 캐싱 성공 여부
     */
    boolean requestAndSaveLineup(long fixtureId);
}
