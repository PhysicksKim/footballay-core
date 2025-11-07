package com.footballay.core.domain.football.scheduler.standing;

import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.springframework.stereotype.Component;

@Component
public class StandingJobEnrollHelper {

    private static final String CRON_EVERY_5MIN_OF_EACH_HOURS = "0 5 0/1 * * ?"; // 매일 오전 5시

    private final Scheduler scheduler;

    public StandingJobEnrollHelper(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostConstruct
    private void init() throws SchedulerException {
        // # 현재는 Standing 사용 안함
        // JobKey jobKey = JobKey.jobKey(StandingQuartzNames.JOB_NAME);
        // TriggerKey triggerKey = TriggerKey.triggerKey(StandingQuartzNames.TRIGGER_NAME);
        //
        // scheduleStandingJobIfNotExist(jobKey, triggerKey);
    }

    private void scheduleStandingJobIfNotExist(JobKey jobKey, TriggerKey triggerKey) throws SchedulerException {
        if (!scheduler.checkExists(jobKey)) {
            JobDetail standingJobDetail = buildJobDetail();
            Trigger  standingJobTrigger = buildTrigger(standingJobDetail);
            scheduler.scheduleJob(standingJobDetail, standingJobTrigger);
        }
        else if (!scheduler.checkExists(triggerKey)) {
            Trigger standingJobTrigger = buildTrigger(scheduler.getJobDetail(jobKey));
            scheduler.scheduleJob(standingJobTrigger);
        }
    }

    private Trigger buildTrigger(JobDetail standingJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(standingJobDetail)
                .withIdentity(StandingQuartzNames.TRIGGER_NAME)
                .withSchedule(
                        CronScheduleBuilder.cronSchedule(CRON_EVERY_5MIN_OF_EACH_HOURS)
                )
                .build();
    }

    private JobDetail buildJobDetail() {
        return JobBuilder.newJob(StandingJob.class)
                .withIdentity(StandingQuartzNames.JOB_NAME)
                .storeDurably()
                .build();
    }

}
