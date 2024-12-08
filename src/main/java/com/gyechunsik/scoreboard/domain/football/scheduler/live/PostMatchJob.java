package com.gyechunsik.scoreboard.domain.football.scheduler.live;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
@RequiredArgsConstructor
public class PostMatchJob implements Job {

    private final LiveMatchTask liveMatchTask;
    private final CheckPostJobDelete checkPostJobDelete;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long fixtureId = context.getMergedJobDataMap().getLong("fixtureId");
        log.info("PostMatchJob executed, fixture ID : {}", fixtureId);

        liveMatchTask.requestAndSaveLiveMatchData(fixtureId);
        if(checkPostJobDelete.isLongAfterMatchFinished(fixtureId)) {
            try {
                context.getScheduler().deleteJob(context.getJobDetail().getKey());
                log.info("PostMatchJob Job deleted :: key={}", context.getJobDetail().getKey());
            } catch (Exception e) {
                log.error("PostMatchJob key=[{}] delete failed", context.getJobDetail().getKey(), e);
                throw new RuntimeException(e);
            }
        }
    }
}
