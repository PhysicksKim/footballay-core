package com.gyechunsik.scoreboard.domain.football.scheduler.lineup;

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
public class StartLineupJobSchedulerService {

    private static final int INTERVAL_SEC = 60;
    private static final int MAX_REPEAT_TIME_SEC = 5 * 60 * 60; // 5 hour * 60 min * 60 sec
    private static final int MAX_REPEAT_COUNT = MAX_REPEAT_TIME_SEC / INTERVAL_SEC;

    private final Scheduler scheduler;

    public void addJob(Long fixtureId, ZonedDateTime lineupAnnounceTime) throws SchedulerException {
        String jobName = FootballSchedulerName.startLineupJob(fixtureId);
        String triggerName = FootballSchedulerName.startLineupTrigger(fixtureId);
        String groupName = FootballSchedulerName.fixtureGroup();

        JobDetail jobDetail = JobBuilder.newJob(StartLineupJob.class)
                .withIdentity(jobName, groupName)
                .usingJobData("fixtureId", fixtureId)
                .build();

        Date startTime = Date.from(lineupAnnounceTime.toInstant());
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerName, groupName)
                .startAt(startTime)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(INTERVAL_SEC)
                        .withRepeatCount(MAX_REPEAT_COUNT))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("addJob :: jobName={}, triggerName={}, groupName={}", jobName, triggerName, groupName);
    }

    public void removeJob(long fixtureId) throws SchedulerException {
        String jobName = FootballSchedulerName.startLineupJob(fixtureId);
        String jobGroup = FootballSchedulerName.fixtureGroup();
        scheduler.deleteJob(new JobKey(jobName, jobGroup));
        log.info("removeJob :: jobName={}, jobGroup={}", jobName, jobGroup);
    }
}
