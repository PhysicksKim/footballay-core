package com.footballay.core.util;

import com.footballay.core.domain.football.scheduler.FootballSchedulerName;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

public class TestJobKeyUtil {

    public static JobKey createPreviousMatchJobKey(long fixtureId) {
        String groupName = FootballSchedulerName.fixtureGroup();
        String jobName = FootballSchedulerName.previousMatchJob(fixtureId);
        return JobKey.jobKey(jobName, groupName);
    }

    public static JobKey createLiveMatchJobKey(long fixtureId) {
        String groupName = FootballSchedulerName.fixtureGroup();
        String jobName = FootballSchedulerName.liveMatchJob(fixtureId);
        return JobKey.jobKey(jobName, groupName);
    }

    public static JobKey createPostMatchJobKey(long fixtureId) {
        String groupName = FootballSchedulerName.fixtureGroup();
        String jobName = FootballSchedulerName.postMatchJob(fixtureId);
        return JobKey.jobKey(jobName, groupName);
    }

    public static TriggerKey createPreviousMatchTriggerKey(long fixtureId) {
        String groupName = FootballSchedulerName.fixtureGroup();
        String triggerName = FootballSchedulerName.previousMatchTrigger(fixtureId);
        return TriggerKey.triggerKey(triggerName, groupName);
    }

    public static TriggerKey createLiveMatchTriggerKey(long fixtureId) {
        String groupName = FootballSchedulerName.fixtureGroup();
        String triggerName = FootballSchedulerName.liveMatchTrigger(fixtureId);
        return TriggerKey.triggerKey(triggerName, groupName);
    }

    public static TriggerKey createPostMatchTriggerKey(long fixtureId) {
        String groupName = FootballSchedulerName.fixtureGroup();
        String triggerName = FootballSchedulerName.postMatchTrigger(fixtureId);
        return TriggerKey.triggerKey(triggerName, groupName);
    }

}
