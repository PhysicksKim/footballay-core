package com.gyechunsik.scoreboard.domain.football.scheduler.live;

public interface CheckPostJobDelete {

    /**
     * 경기 종료 후로부터 오랜 시간이 지났는지 확인합니다.
     * @param fixtureId 경기 ID
     * @return 경기 종료 후로부터 오랜 시간이 지났다면 true
     */
    boolean isLongAfterMatchFinished(long fixtureId);

}
