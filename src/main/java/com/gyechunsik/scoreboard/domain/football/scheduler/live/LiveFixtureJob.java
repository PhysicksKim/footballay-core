package com.gyechunsik.scoreboard.domain.football.scheduler.live;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class LiveFixtureJob implements Job {

    private final LiveFixtureTask liveFixtureTask;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long fixtureId = context.getMergedJobDataMap().getLong("fixtureId");
        log.info("LiveFixtureJob executed at {}, fixture ID : {}", LocalDateTime.now(), fixtureId);
        boolean isFixtureFinished = liveFixtureTask.requestAndSaveLiveFixtureData(fixtureId);
        if(isFixtureFinished) {
            log.info("LiveFixture is finished. Deleting job");
            try {
                context.getScheduler().deleteJob(context.getJobDetail().getKey());
                log.info("Job deleted :: key={}", context.getJobDetail().getKey());
            } catch (Exception e) {
                log.error("LiveFixtureJob key=[{}] delete failed", context.getJobDetail().getKey(), e);
                throw new RuntimeException(e);
            }
        }
    }
}
