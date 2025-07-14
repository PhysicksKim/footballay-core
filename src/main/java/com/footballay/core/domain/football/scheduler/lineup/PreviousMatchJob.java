package com.footballay.core.domain.football.scheduler.lineup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class PreviousMatchJob implements Job {

    private final PreviousMatchTask lineupTask;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            long fixtureId = jobExecutionContext.getMergedJobDataMap().getLong("fixtureId");
            log.info("PreviousMatchJob executed at {}, fixture ID : {}", LocalDateTime.now(), fixtureId);
            boolean isSuccess = lineupTask.requestAndSaveLineup(fixtureId);
            if(isSuccess) {
                log.info("MatchLineup is Saved. Job completed and try to delete job. fixtureId={}", fixtureId);
                JobKey key = jobExecutionContext.getJobDetail().getKey();
                try {
                    jobExecutionContext.getScheduler().deleteJob(key);
                    log.info("Job deleted. key={}", key);
                } catch (Exception e) {
                    log.error("Error PreviousMatchJob key={} delete failed", key, e);
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            log.error("PreviousMatchJob execution failed", e);
            throw new JobExecutionException(e);
        }
    }
}
