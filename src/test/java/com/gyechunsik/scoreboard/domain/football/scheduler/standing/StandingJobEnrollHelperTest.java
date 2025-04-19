package com.gyechunsik.scoreboard.domain.football.scheduler.standing;

import com.gyechunsik.scoreboard.util.TestQuartzJobWaitUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.gyechunsik.scoreboard.util.TestQuartzJobWaitUtil.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class StandingJobEnrollHelperTest {

    @Autowired
    private Scheduler scheduler;

    @Test
    void Spring실행후StandingJob과Trigger가등록됩니다() throws SchedulerException {
        // given
        TriggerKey triggerKey = TriggerKey.triggerKey(StandingQuartzNames.TRIGGER_NAME);
        JobKey jobKey = JobKey.jobKey(StandingQuartzNames.JOB_NAME);
        log.info("jobKey: {}, triggerKey: {}", jobKey, triggerKey);

        // when
        log.info("wait for job to be scheduled");
        waitForJobToBeScheduled(scheduler, jobKey);
        Trigger trigger = scheduler.getTrigger(triggerKey);
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);

        // then
        assertNotNull(trigger);
        assertNotNull(jobDetail);
        log.info("\n\ttrigger : {} \n\tjobDetail : {}", trigger, jobDetail);
    }
}