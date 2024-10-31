package com.gyechunsik.scoreboard.domain.football.scheduler.lineup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class StartLineupJob implements Job {

    private final StartLineupTask lineupTask;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        long fixtureId = jobExecutionContext.getMergedJobDataMap().getLong("fixtureId");
        log.info("StartLineupJob executed at {}, fixture ID : {}", LocalDateTime.now(), fixtureId);
        boolean isSuccess = lineupTask.requestAndSaveLineup(fixtureId);
        if(isSuccess) {
            log.info("MatchLineup is Saved. Deleting job");
            JobKey key = jobExecutionContext.getJobDetail().getKey();
            try {
                jobExecutionContext.getScheduler().deleteJob(key);
                log.info("Job deleted :: key={}", key);
            } catch (Exception e) {
                log.error("StartLineupJob key=[{}] delete failed", key, e);
                throw new RuntimeException(e);
            }
        }
    }
}
