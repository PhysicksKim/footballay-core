package com.gyechunsik.scoreboard.domain.football.scheduler.live;

import com.gyechunsik.scoreboard.domain.football.scheduler.FootballSchedulerName;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@ActiveProfiles({"dev","mockapi"})
// @ExtendWith(MockitoExtension.class)
public class LiveFixtureJobTest {

    @Autowired
    private Scheduler scheduler; // Quartz 스케줄러를 자동 주입

    @MockBean
    private LiveFixtureTask liveFixtureTask; // Mock LiveFixtureTask

    @Autowired
    private LiveFixtureJobSchedulerService liveFixtureJobSchedulerService; // 테스트할 서비스

    // 각 테스트에서 사용할 고유한 fixtureId를 저장하기 위한 변수
    private long fixtureId;

    @AfterEach
    public void cleanUp() throws Exception {
        // JobKey 생성
        String liveJobName = FootballSchedulerName.liveFixtureJob(fixtureId);
        String postFinishJobName = FootballSchedulerName.postFinishJob(fixtureId);
        String groupName = FootballSchedulerName.fixtureGroup();

        // 먼저 Trigger 삭제
        TriggerKey liveTriggerKey = new TriggerKey(FootballSchedulerName.liveFixtureTrigger(fixtureId), groupName);
        TriggerKey postFinishTriggerKey = new TriggerKey(FootballSchedulerName.postFinishTrigger(fixtureId), groupName);

        // Trigger 삭제
        scheduler.unscheduleJob(liveTriggerKey);
        scheduler.unscheduleJob(postFinishTriggerKey);

        // Job 삭제
        scheduler.deleteJob(new JobKey(liveJobName, groupName));
        scheduler.deleteJob(new JobKey(postFinishJobName, groupName));
    }

    @Test
    public void testAddJobAndRetrieveIt() throws Exception {
        // Arrange: 고유한 fixtureId 사용
        fixtureId = 10001L;
        ZonedDateTime now = ZonedDateTime.now();
        liveFixtureJobSchedulerService.addJob(fixtureId, now);

        // FootballSchedulerName을 사용하여 jobKey 생성
        String jobName = FootballSchedulerName.liveFixtureJob(fixtureId);
        String groupName = FootballSchedulerName.fixtureGroup();
        JobKey jobKey = new JobKey(jobName, groupName);

        // Act: Scheduler에서 JobKey를 사용해 JobDetail 조회
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);

        // Assert: JobDetail이 null이 아님을 확인 (즉, Job이 정상적으로 등록되었는지 확인)
        assertNotNull(jobDetail, "Job should have been registered in the scheduler.");
    }

    @Test
    public void testPostFinishJobAddedWhenFixtureFinished() throws Exception {
        // Arrange: 고유한 fixtureId 사용
        fixtureId = 10002L;
        ZonedDateTime now = ZonedDateTime.now();
        when(liveFixtureTask.requestAndSaveLiveFixtureData(fixtureId)).thenReturn(true);

        // Act: Trigger job execution
        liveFixtureJobSchedulerService.addJob(fixtureId, now);

        // FootballSchedulerName 을 사용하여 jobKey 생성
        String jobName = FootballSchedulerName.postFinishJob(fixtureId);
        String groupName = FootballSchedulerName.fixtureGroup();
        JobKey postFinishJobKey = new JobKey(jobName, groupName);

        // Quartz Job 이 등록될 때 까지 기다립니다.
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            JobDetail postFinishJobDetail = scheduler.getJobDetail(postFinishJobKey);
            return postFinishJobDetail != null;
        });

        // Assert: PostFinishJob이 Scheduler에 추가되었는지 확인
        JobDetail postFinishJobDetail = scheduler.getJobDetail(postFinishJobKey);
        assertNotNull(postFinishJobDetail, "PostFinishJob should have been registered in the scheduler.");
    }

    @Test
    public void testPostFinishJobNotAddedWhenFixtureNotFinished() throws Exception {
        // Arrange: 고유한 fixtureId 사용
        fixtureId = 10003L;
        ZonedDateTime now = ZonedDateTime.now();
        when(liveFixtureTask.requestAndSaveLiveFixtureData(fixtureId)).thenReturn(false);

        // Act: Trigger job execution
        liveFixtureJobSchedulerService.addJob(fixtureId, now);

        // FootballSchedulerName을 사용하여 jobKey 생성
        String jobName = FootballSchedulerName.postFinishJob(fixtureId);
        String groupName = FootballSchedulerName.fixtureGroup();
        JobKey postFinishJobKey = new JobKey(jobName, groupName);

        // PostFinishJob 이 추가되어 실패할 경우를 대비해 5초 동안 기다립니다.
        await().atMost(5, TimeUnit.SECONDS).until(() -> scheduler.getJobDetail(postFinishJobKey) == null);

        // Assert: PostFinishJob이 Scheduler에 추가되지 않았는지 확인
        JobDetail postFinishJobDetail = scheduler.getJobDetail(postFinishJobKey);
        assertNull(postFinishJobDetail, "PostFinishJob should not have been registered in the scheduler.");
    }
}
