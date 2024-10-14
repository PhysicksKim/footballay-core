package com.gyechunsik.scoreboard.domain.football.scheduler.live;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
@RequiredArgsConstructor
public class PostFinishJob implements Job {

    private final LiveFixtureTask liveFixtureTask;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long fixtureId = context.getMergedJobDataMap().getLong("fixtureId");
        log.info("PostFinishJob executed, fixture ID : {}", fixtureId);
        liveFixtureTask.requestAndSaveLiveFixtureData(fixtureId);
    }
}
