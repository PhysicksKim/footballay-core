package com.footballay.core.domain.football.scheduler.live;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class PostMatchJob implements Job {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PostMatchJob.class);
    private final LiveMatchTask liveMatchTask;
    private final CheckPostJobDelete checkPostJobDelete;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            long fixtureId = context.getMergedJobDataMap().getLong("fixtureId");
            log.info("PostMatchJob executed, fixture ID : {}", fixtureId);
            liveMatchTask.requestAndSaveLiveMatchData(fixtureId);
            if (checkPostJobDelete.isLongAfterMatchFinished(fixtureId)) {
                try {
                    context.getScheduler().deleteJob(context.getJobDetail().getKey());
                    log.info("PostMatchJob Job deleted :: key={}", context.getJobDetail().getKey());
                } catch (Exception e) {
                    log.error("PostMatchJob key=[{}] delete failed", context.getJobDetail().getKey(), e);
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            log.error("PostMatchJob execution failed", e);
            throw new JobExecutionException(e);
        }
    }

    public PostMatchJob(final LiveMatchTask liveMatchTask, final CheckPostJobDelete checkPostJobDelete) {
        this.liveMatchTask = liveMatchTask;
        this.checkPostJobDelete = checkPostJobDelete;
    }
}
