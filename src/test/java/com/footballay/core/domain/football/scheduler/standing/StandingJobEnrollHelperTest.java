package com.footballay.core.domain.football.scheduler.standing;

import com.footballay.core.domain.football.scheduler.standing.StandingQuartzNames;
import com.footballay.core.util.TestQuartzJobWaitUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.footballay.core.util.TestQuartzJobWaitUtil.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Disabled("Standing feature will be reimplemented in Kotlin project migration")
class StandingJobEnrollHelperTest {

    private static final Logger log = LoggerFactory.getLogger(StandingJobEnrollHelperTest.class);

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