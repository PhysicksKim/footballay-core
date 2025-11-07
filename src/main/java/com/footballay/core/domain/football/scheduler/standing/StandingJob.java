package com.footballay.core.domain.football.scheduler.standing;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandingJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(StandingJob.class);

    private final StandingJobTask standingJobTask;

    public StandingJob(StandingJobTask standingJobTask) {
        this.standingJobTask = standingJobTask;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("StandingJob executed");
        standingJobTask.execute();
    }

}
