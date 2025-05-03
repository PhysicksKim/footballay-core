package com.footballay.core.domain.football.scheduler.standing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
@RequiredArgsConstructor
public class StandingJob implements Job {

    private final StandingJobTask standingJobTask;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("StandingJob executed");
        standingJobTask.execute();
    }

}
