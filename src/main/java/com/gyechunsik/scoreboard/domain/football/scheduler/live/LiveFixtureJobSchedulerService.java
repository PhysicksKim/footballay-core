package com.gyechunsik.scoreboard.domain.football.scheduler.live;

import com.gyechunsik.scoreboard.domain.football.scheduler.FootballSchedulerName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Service
public class LiveFixtureJobSchedulerService {

    private static final int INTERVAL_SEC = 29;
    private static final int MAX_REPEAT_TIME_SEC = 5 * 60 * 60; // 5 hour * 60 min * 60 sec
    private static final int MAX_REPEAT_COUNT = MAX_REPEAT_TIME_SEC / INTERVAL_SEC;

    private final Scheduler scheduler;

    public void addJob(Long fixtureId, ZonedDateTime fixtureKickoffTime) throws SchedulerException {
        String jobName = FootballSchedulerName.liveFixtureJob(fixtureId);
        String triggerName = FootballSchedulerName.liveFixtureTrigger(fixtureId);
        String groupName = FootballSchedulerName.fixtureGroup();

        JobDetail jobDetail = JobBuilder.newJob(LiveFixtureJob.class)
                .withIdentity(jobName, groupName)
                .usingJobData("fixtureId", fixtureId)
                .build();

        Date startTime = Date.from(fixtureKickoffTime.toInstant());
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerName, groupName)
                .startAt(startTime)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(INTERVAL_SEC)
                        .withRepeatCount(MAX_REPEAT_COUNT)
                        .withMisfireHandlingInstructionNowWithRemainingCount())
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("addJob :: jobName={}, triggerName={}, groupName={}, startAt={}",
                jobName, triggerName, groupName, startTime);
    }

    public void removeJob(long fixtureId) throws SchedulerException {
        String jobName = FootballSchedulerName.liveFixtureJob(fixtureId);
        String jobGroup = FootballSchedulerName.fixtureGroup();
        scheduler.deleteJob(new JobKey(jobName, jobGroup));
        log.info("removeJob :: jobName={}, jobGroup={}", jobName, jobGroup);
    }

}
