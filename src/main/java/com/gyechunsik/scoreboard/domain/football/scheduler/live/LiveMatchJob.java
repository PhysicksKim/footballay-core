package com.gyechunsik.scoreboard.domain.football.scheduler.live;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class LiveMatchJob implements Job {

    private final LiveMatchTask liveMatchTask;
    private final LiveMatchJobSchedulerService liveMatchJobSchedulerService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long fixtureId = context.getMergedJobDataMap().getLong("fixtureId");
        log.info("LiveMatchJob executed at {}, fixture ID : {}", LocalDateTime.now(), fixtureId);
        boolean isFixtureFinished = liveMatchTask.requestAndSaveLiveMatchData(fixtureId);
        if(isFixtureFinished) {
            log.info("LiveMatch is finished. Deleting job");
            try {
                context.getScheduler().deleteJob(context.getJobDetail().getKey());
                log.info("Job deleted :: key={}", context.getJobDetail().getKey());
                // TODO : 경기 끝난지 1일 이상 지났다면 PostMatchJob 을 실행하지 않는 게 좋을 듯? (추후 검토)
                liveMatchJobSchedulerService.addPostMatchJob(fixtureId);
                log.info("PostMatchJob added :: fixtureId={}", fixtureId);
            } catch (Exception e) {
                // log.error("jobDetail {}", context.getJobDetail());
                log.error("LiveMatchJob key=[{}] delete failed", context.getJobDetail().getKey(), e);
                throw new RuntimeException(e);
            }
        }
    }
}
