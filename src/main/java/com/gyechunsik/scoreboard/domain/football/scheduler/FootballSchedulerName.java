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
    public static String liveFixtureJob(long fixtureId) {
        return "LiveFixtureJob_" + fixtureId;
    }
    public static String liveFixtureTrigger(long fixtureId) {
        return "LiveFixtureTrigger_" + fixtureId;
    }
    public static String postFinishJob(long fixtureId) {
        return "PostFinishJob_" + fixtureId;
    }
    public static String postFinishTrigger(long fixtureId) {
        return "PostFinishTrigger_" + fixtureId;
    }
}
