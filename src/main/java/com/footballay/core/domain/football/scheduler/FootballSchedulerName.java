package com.footballay.core.domain.football.scheduler;

public class FootballSchedulerName {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FootballSchedulerName.class);

    public static String fixtureGroup() {
        return "FixtureGroup";
    }

    public static String previousMatchJob(long fixtureId) {
        return "PreviousMatchJob_" + fixtureId;
    }

    public static String previousMatchTrigger(long fixtureId) {
        return "StartLineupTrigger_" + fixtureId;
    }

    public static String liveMatchJob(long fixtureId) {
        return "LiveMatchJob_" + fixtureId;
    }

    public static String liveMatchTrigger(long fixtureId) {
        return "LiveMatchTrigger_" + fixtureId;
    }

    public static String postMatchJob(long fixtureId) {
        return "PostMatchJob_" + fixtureId;
    }

    public static String postMatchTrigger(long fixtureId) {
        return "PostMatchTrigger_" + fixtureId;
    }
}
