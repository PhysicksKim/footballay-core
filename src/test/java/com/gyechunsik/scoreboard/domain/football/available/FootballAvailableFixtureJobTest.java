package com.gyechunsik.scoreboard.domain.football.available;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.scheduler.FootballSchedulerName;
import com.gyechunsik.scoreboard.domain.football.scheduler.lineup.StartLineupJobSchedulerService;
import com.gyechunsik.scoreboard.domain.football.scheduler.lineup.StartLineupTask;
import com.gyechunsik.scoreboard.domain.football.scheduler.live.LiveFixtureJobSchedulerService;
import com.gyechunsik.scoreboard.domain.football.scheduler.live.LiveFixtureTask;
import com.gyechunsik.scoreboard.domain.football.service.FootballAvailableService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * AvailableFixture 추가/제거 시 Job 이 제대로 추가/제거되는지 테스트
 */
@Slf4j
@SpringBootTest
@ActiveProfiles({"dev", "mockapi"})
public class FootballAvailableFixtureJobTest {

    @MockBean
    private FixtureRepository fixtureRepository;

    @Autowired
    private Scheduler scheduler;

    @MockBean
    private StartLineupTask startLineupTask;

    @MockBean
    private LiveFixtureTask liveFixtureTask;

    @Autowired
    private FootballAvailableService footballAvailableService;

    private long fixtureId;

    private void mockFixtureRepository(long fixtureId) {
        Fixture mockFixture = new Fixture();
        mockFixture.setFixtureId(fixtureId);
        mockFixture.setDate(LocalDateTime.now());
        mockFixture.setTimezone("Asia/Seoul");
        mockFixture.setTimestamp(Instant.now().getEpochSecond());

        when(fixtureRepository.findById(fixtureId)).thenReturn(Optional.of(mockFixture));
    }

    @AfterEach
    public void cleanUp() throws SchedulerException {
        scheduler.clear();
    }

    @DisplayName("Available 등록 직후 Job 이 제대로 등록되는지 테스트")
    @Test
    public void testJobsAddedWhenAvailableFixtureIsAdded() throws Exception {
        fixtureId = 10001L;
        mockFixtureRepository(fixtureId);

        // Step 1: AvailableFixture 추가
        footballAvailableService.addAvailableFixture(fixtureId);

        // Step 2: Job들이 추가됐는지 확인
        JobKey lineupJobKey = getStartLineupJobKey(fixtureId);
        JobKey liveFixtureJobKey = getLiveFixtureJobKey(fixtureId);
        waitUntilJobAllEnrolled(lineupJobKey, liveFixtureJobKey);

        assertNotNull(scheduler.getJobDetail(lineupJobKey), "StartLineupJob should have been registered.");
        assertNotNull(scheduler.getJobDetail(liveFixtureJobKey), "LiveFixtureJob should have been registered.");
    }

    @DisplayName("Available 제거 직후 Job 이 제대로 삭제되는지 테스트")
    @Test
    public void testJobsRemovedWhenAvailableFixtureIsRemoved() throws Exception {
        fixtureId = 10002L;
        mockFixtureRepository(fixtureId);

        // Step 1: AvailableFixture 추가
        footballAvailableService.addAvailableFixture(fixtureId);

        // Step 2: Job 들이 추가됐는지 확인
        JobKey lineupJobKey = getStartLineupJobKey(fixtureId);
        JobKey liveFixtureJobKey = getLiveFixtureJobKey(fixtureId);
        waitUntilJobAllEnrolled(lineupJobKey, liveFixtureJobKey);

        assertNotNull(scheduler.getJobDetail(lineupJobKey), "StartLineupJob should have been registered.");
        assertNotNull(scheduler.getJobDetail(liveFixtureJobKey), "LiveFixtureJob should have been registered.");

        // Step 3: AvailableFixture 제거
        footballAvailableService.removeAvailableFixture(fixtureId);
        waitUntilJobAllRemoved();

        // Step 4: Job 들이 제거됐는지 확인
        Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyGroup());
        assertThat(jobKeys).isEmpty();
        assertNull(scheduler.getJobDetail(lineupJobKey), "StartLineupJob should have been removed.");
        assertNull(scheduler.getJobDetail(liveFixtureJobKey), "LiveFixtureJob should have been removed.");
    }

    @DisplayName("Job 이 finish 되면 PostFinishJob 이 추가되는지 테스트")
    @Test
    public void testPostFinishJobAddedWhenJobsFinish() throws Exception {
        fixtureId = 10003L;
        mockFixtureRepository(fixtureId);

        // Step 1: Job Task 가 한번 실행되고 끝나도록 설정
        when(startLineupTask.requestAndSaveLineup(fixtureId)).thenReturn(true); // StartLineupTask가 끝남
        when(liveFixtureTask.requestAndSaveLiveFixtureData(fixtureId)).thenReturn(true); // LiveFixtureTask가 끝남

        // Step 2: AvailableFixture 추가
        footballAvailableService.addAvailableFixture(fixtureId);

        // Step 3: StartLineupJob 과 LiveFixtureJob 이 등록되었는지 확인
        JobKey lineupJobKey = getStartLineupJobKey(fixtureId);
        JobKey liveFixtureJobKey = getLiveFixtureJobKey(fixtureId);
        waitUntilJobAllEnrolled(lineupJobKey, liveFixtureJobKey);

        // Step 4: 등록된 Job 들을 즉시 트리거
        triggerJobNow(scheduler, lineupJobKey, liveFixtureJobKey);

        // Step 5: PostFinishJob 이 추가됐는지 확인
        JobKey postFinishJobKey = getPostFinishJobKey(fixtureId);
        waitUntilJobAllEnrolled(postFinishJobKey);
        assertNotNull(scheduler.getJobDetail(postFinishJobKey), "PostFinishJob should have been registered.");
    }

    @DisplayName("PostFinishJob 이 추가된 후 Available 제거 시 PostFinishJob 이 삭제되는지 테스트")
    @Test
    public void testPostFinishJobRemovedWhenAvailableFixtureIsRemoved() throws Exception {
        fixtureId = 10004L;
        mockFixtureRepository(fixtureId);

        // Step 1: Job Task 가 한번 실행되고 끝나도록 설정
        when(startLineupTask.requestAndSaveLineup(fixtureId)).thenReturn(true);
        when(liveFixtureTask.requestAndSaveLiveFixtureData(fixtureId)).thenReturn(true);

        // Step 2: AvailableFixture 추가
        footballAvailableService.addAvailableFixture(fixtureId);

        // Step 3: StartLineupJob 과 LiveFixtureJob 이 등록되었는지 확인
        JobKey lineupJobKey = getStartLineupJobKey(fixtureId);
        JobKey liveFixtureJobKey = getLiveFixtureJobKey(fixtureId);
        waitUntilJobAllEnrolled(lineupJobKey, liveFixtureJobKey);

        // Step 4: 등록된 Job 들을 즉시 트리거
        triggerJobNow(scheduler, lineupJobKey, liveFixtureJobKey);

        // Step 5: PostFinishJob 이 추가됐는지 확인
        JobKey postFinishJobKey = getPostFinishJobKey(fixtureId);
        waitUntilJobAllEnrolled(postFinishJobKey);

        // Step 6: AvailableFixture 제거
        footballAvailableService.removeAvailableFixture(fixtureId);

        // Step 7: PostFinishJob 이 제거됐는지 확인
        waitUntilJobAllRemoved();
        assertNull(scheduler.getJobDetail(postFinishJobKey), "PostFinishJob should have been removed.");
    }

    private JobKey getStartLineupJobKey(long fixtureId) {
        return new JobKey(FootballSchedulerName.startLineupJob(fixtureId), FootballSchedulerName.fixtureGroup());
    }

    private JobKey getLiveFixtureJobKey(long fixtureId) {
        return new JobKey(FootballSchedulerName.liveFixtureJob(fixtureId), FootballSchedulerName.fixtureGroup());
    }

    private JobKey getPostFinishJobKey(long fixtureId) {
        return new JobKey(FootballSchedulerName.postFinishJob(fixtureId), FootballSchedulerName.fixtureGroup());
    }

    private void waitUntilJobAllEnrolled(JobKey... jobKeys) {
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            for (JobKey jobKey : jobKeys) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                assertNotNull(jobDetail, "Job should have been registered: " + jobKey.getName());
            }
        });
    }

    private void waitUntilJobAllRemoved() {
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyGroup());
            assertThat(jobKeys).isEmpty();
        });
    }

    /**
     * 특정 트리거를 수동으로 즉시 실행하도록 합니다
     * @param scheduler Quartz Scheduler
     * @param jobKeys 즉시 트리거할 JobKey 목록
     * @throws SchedulerException check 및 trigger 과정에서 Quartz 스케줄러 예외 발생 가능
     */
    public void triggerJobNow(Scheduler scheduler, JobKey... jobKeys) throws SchedulerException {
        for (JobKey jobKey : jobKeys) {
            if (scheduler.checkExists(jobKey)) {
                scheduler.triggerJob(jobKey); // Job을 즉시 트리거
                log.info("Job triggered immediately :: jobKey={}", jobKey);
            } else {
                log.warn("Job not found :: jobKey={}", jobKey);
            }
        }
    }

}
