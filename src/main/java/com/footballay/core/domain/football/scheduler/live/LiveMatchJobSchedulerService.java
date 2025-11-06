package com.footballay.core.domain.football.scheduler.live;

import com.footballay.core.domain.football.scheduler.FootballSchedulerName;
import org.quartz.*;
import org.springframework.stereotype.Service;
import java.time.ZonedDateTime;
import java.util.Date;

@Service
public class LiveMatchJobSchedulerService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LiveMatchJobSchedulerService.class);
    private static final int INTERVAL_SEC = 17;
    private static final int MAX_REPEAT_TIME_SEC = 5 * 60 * 60; // 5 hour * 60 min * 60 sec
    private static final int MAX_REPEAT_COUNT = MAX_REPEAT_TIME_SEC / INTERVAL_SEC;
    private static final int POST_FINISH_INTERVAL_SEC = 60;
    private static final int POST_FINISH_MAX_REPEAT_TIME_SEC = 60 * 60; // 60 min * 60 sec
    private static final int POST_FINISH_MAX_REPEAT_COUNT = POST_FINISH_MAX_REPEAT_TIME_SEC / POST_FINISH_INTERVAL_SEC;
    private final Scheduler scheduler;

    public void addJob(Long fixtureId, ZonedDateTime jobStartTime) throws SchedulerException {
        String jobName = FootballSchedulerName.liveMatchJob(fixtureId);
        String triggerName = FootballSchedulerName.liveMatchTrigger(fixtureId);
        String groupName = FootballSchedulerName.fixtureGroup();
        JobDetail jobDetail = JobBuilder.newJob(LiveMatchJob.class).withIdentity(jobName, groupName).usingJobData("fixtureId", fixtureId).build();
        Date startTime = Date.from(jobStartTime.toInstant());
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerName, groupName).startAt(startTime).withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(INTERVAL_SEC).withRepeatCount(MAX_REPEAT_COUNT).withMisfireHandlingInstructionNowWithRemainingCount()).build();
        scheduler.scheduleJob(jobDetail, trigger);
        log.info("addJob :: jobName={}, triggerName={}, groupName={}, startAt={}", jobName, triggerName, groupName, startTime);
    }

    public void removeJob(long fixtureId) throws SchedulerException {
        String jobName = FootballSchedulerName.liveMatchJob(fixtureId);
        String jobGroup = FootballSchedulerName.fixtureGroup();
        scheduler.deleteJob(new JobKey(jobName, jobGroup));
        log.info("removeJob :: jobName={}, jobGroup={}", jobName, jobGroup);
    }

    public void addPostMatchJob(long fixtureId) throws SchedulerException {
        String jobName = FootballSchedulerName.postMatchJob(fixtureId);
        String triggerName = FootballSchedulerName.postMatchTrigger(fixtureId);
        String groupName = FootballSchedulerName.fixtureGroup();
        JobDetail jobDetail = JobBuilder.newJob(PostMatchJob.class).withIdentity(jobName, groupName).usingJobData("fixtureId", fixtureId).build();
        // start now
        Date startTime = new Date();
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerName, groupName).startNow().withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(POST_FINISH_INTERVAL_SEC).withRepeatCount(POST_FINISH_MAX_REPEAT_COUNT).withMisfireHandlingInstructionNowWithRemainingCount()).build();
        scheduler.scheduleJob(jobDetail, trigger);
        log.info("addPostMatchJob :: jobName={}, triggerName={}, groupName={}, startAt={}", jobName, triggerName, groupName, startTime);
    }

    public void removePostJob(long fixtureId) {
        String jobName = FootballSchedulerName.postMatchJob(fixtureId);
        String jobGroup = FootballSchedulerName.fixtureGroup();
        try {
            scheduler.deleteJob(new JobKey(jobName, jobGroup));
            log.info("removePostJob :: jobName={}, jobGroup={}", jobName, jobGroup);
        } catch (SchedulerException e) {
            log.error("removePostJob :: jobName={}, jobGroup={}", jobName, jobGroup, e);
        }
    }

    public LiveMatchJobSchedulerService(final Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
