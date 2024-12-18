package com.gyechunsik.scoreboard.domain.football.scheduler.live;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;

import static com.gyechunsik.scoreboard.util.TestJobKeyUtil.*;
import static com.gyechunsik.scoreboard.util.TestQuartzJobWaitUtil.waitForJobToBeRemoved;
import static com.gyechunsik.scoreboard.util.TestQuartzJobWaitUtil.waitForJobToBeScheduled;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@ActiveProfiles({"dev","mockapi"})
// @ExtendWith(MockitoExtension.class)
public class LiveMatchJobTest {

    @Autowired
    private Scheduler scheduler; // Quartz 스케줄러를 자동 주입

    @MockBean
    private LiveMatchTask liveMatchTask; // Mock LiveMatchTask

    @Autowired
    private LiveMatchJobSchedulerService liveMatchJobSchedulerService; // 테스트할 서비스

    // 각 테스트에서 사용할 고유한 fixtureId를 저장하기 위한 변수
    private long fixtureId;

    @AfterEach
    public void cleanUp() throws Exception {
        // JobKey 생성
        JobKey previousMatchJobKey = createPreviousMatchJobKey(fixtureId);
        JobKey liveMatchJobKey = createLiveMatchJobKey(fixtureId);
        JobKey postMatchJobKey = createPostMatchJobKey(fixtureId);

        TriggerKey previousMatchTriggerKey = createPreviousMatchTriggerKey(fixtureId);
        TriggerKey liveMatchTriggerKey = createLiveMatchTriggerKey(fixtureId);
        TriggerKey postMatchTriggerKey = createPostMatchTriggerKey(fixtureId);

        // Trigger 삭제
        scheduler.unscheduleJob(previousMatchTriggerKey);
        scheduler.unscheduleJob(liveMatchTriggerKey);
        scheduler.unscheduleJob(postMatchTriggerKey);

        // Job 삭제
        scheduler.deleteJob(previousMatchJobKey);
        scheduler.deleteJob(liveMatchJobKey);
        scheduler.deleteJob(postMatchJobKey);
    }

    @Test
    public void testAddJobAndRetrieveIt() throws Exception {
        // Arrange: 고유한 fixtureId 사용
        fixtureId = 10001L;
        ZonedDateTime now = ZonedDateTime.now();
        liveMatchJobSchedulerService.addJob(fixtureId, now);

        // FootballSchedulerName을 사용하여 jobKey 생성
        JobKey liveMatchJobKey = createLiveMatchJobKey(fixtureId);

        // Act: Scheduler에서 JobKey를 사용해 JobDetail 조회
        JobDetail jobDetail = scheduler.getJobDetail(liveMatchJobKey);

        // Assert: JobDetail이 null이 아님을 확인 (즉, Job이 정상적으로 등록되었는지 확인)
        assertNotNull(jobDetail, "Job should have been registered in the scheduler.");
    }

    @Test
    public void testPostMatchJobAddedWhenFixtureFinished() throws Exception {
        // Arrange: 고유한 fixtureId 사용
        fixtureId = 10002L;
        ZonedDateTime now = ZonedDateTime.now();
        when(liveMatchTask.requestAndSaveLiveMatchData(fixtureId)).thenReturn(true);

        // Act: Trigger job execution
        liveMatchJobSchedulerService.addJob(fixtureId, now);

        JobKey postMatchJobKey = createPostMatchJobKey(fixtureId);
        waitForJobToBeScheduled(scheduler, postMatchJobKey);

        JobDetail postMatchJobDetail = scheduler.getJobDetail(postMatchJobKey);
        assertNotNull(postMatchJobDetail, "PostMatchJob should have been registered in the scheduler.");
    }

    @Test
    public void testPostMatchJobNotAddedWhenFixtureNotFinished() throws Exception {
        // Arrange: 고유한 fixtureId 사용
        fixtureId = 10003L;
        ZonedDateTime now = ZonedDateTime.now();
        when(liveMatchTask.requestAndSaveLiveMatchData(fixtureId)).thenReturn(false);

        // Act: Trigger job execution
        liveMatchJobSchedulerService.addJob(fixtureId, now);

        // FootballSchedulerName을 사용하여 jobKey 생성
        JobKey postMatchJobKey = createPostMatchJobKey(fixtureId);

        // PostMatchJob 이 추가되어 실패할 경우를 대비해 5초 동안 기다립니다.
        waitForJobToBeRemoved(scheduler, postMatchJobKey);

        // Assert: PostMatchJob이 Scheduler에 추가되지 않았는지 확인
        JobDetail postMatchJobDetail = scheduler.getJobDetail(postMatchJobKey);
        assertNull(postMatchJobDetail, "PostMatchJob should not have been registered in the scheduler.");
    }

}
