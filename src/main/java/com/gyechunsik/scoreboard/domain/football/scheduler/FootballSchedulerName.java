package com.gyechunsik.scoreboard.domain.football.scheduler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FootballSchedulerName {

    public static String fixtureGroup() {
        return "FixtureGroup";
    }

    public static String startLineupJob(long fixtureId) {
        return "StartLineupJob_" + fixtureId;
    }

    public static String startLineupTrigger(long fixtureId) {
        return "StartLineupTrigger_" + fixtureId;
    }
}
