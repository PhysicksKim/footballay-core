package com.footballay.core.domain.football.available;

import com.footballay.core.domain.football.persistence.Fixture;
import com.footballay.core.domain.football.persistence.live.LiveStatus;
import com.footballay.core.domain.football.repository.FixtureRepository;
import com.footballay.core.domain.football.scheduler.FootballSchedulerName;
import com.footballay.core.domain.football.scheduler.lineup.PreviousMatchTask;
import com.footballay.core.domain.football.scheduler.live.LiveMatchTask;
import com.footballay.core.domain.football.service.FootballAvailableService;
import com.footballay.core.util.QuartzConnectionResetListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * AvailableFixture 추가/제거 시 Job 이 제대로 추가/제거되는지 테스트
 */
@SpringBootTest
@ActiveProfiles({"dev", "mockapi"})
@ExtendWith(QuartzConnectionResetListener.class)
public class FootballAvailableFixtureJobTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FootballAvailableFixtureJobTest.class);
    @MockitoBean
    private FixtureRepository fixtureRepository;
    @MockitoBean
    private PreviousMatchTask previousMatchTask;
    @MockitoBean
    private LiveMatchTask liveMatchTask;

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private FootballAvailableService footballAvailableService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int JOB_WAIT_TIMEOUT_SEC = 30; //

    @AfterEach
    public void logH2Locks() {
        List<Map<String, Object>> locks = jdbcTemplate.queryForList("SELECT TABLE_SCHEMA, TABLE_NAME, SESSION_ID, LOCK_TYPE " + "FROM INFORMATION_SCHEMA.LOCKS");
        log.info(">>> H2 LOCKS STATE: {}", locks);
    }

    private long fixtureId;

    private void mockFixtureRepository(long fixtureId) {
        ZoneId KST = ZoneId.of("Asia/Seoul");

        var kickoff = java.time.ZonedDateTime.now(KST).plusMinutes(30);

        Fixture mockFixture = new Fixture();
        mockFixture.setFixtureId(fixtureId);

        // 도메인이 실제로 어떤 필드를 쓰는지에 맞춰서
        // 일관성 있게 넣어주기
        mockFixture.setDate(kickoff.toLocalDateTime()); // 경기 시작 로컬시각(Seoul 기준)
        mockFixture.setTimezone("Asia/Seoul");
        mockFixture.setTimestamp(kickoff.toEpochSecond()); // 위 kickoff과 일치하는 epoch

        LiveStatus liveStatus = LiveStatus.builder()
            .elapsed(90)
            .longStatus("Match Finished")
            .shortStatus("FT")
            .homeScore(1)
            .awayScore(0)
            .build();
        mockFixture.setLiveStatus(liveStatus);

        when(fixtureRepository.findById(fixtureId))
            .thenReturn(Optional.of(mockFixture));
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
        JobKey lineupJobKey = getPreviousMatchJobKey(fixtureId);
        JobKey liveMatchJobKey = getLiveMatchJobKey(fixtureId);
        waitUntilJobAllEnrolled(lineupJobKey, liveMatchJobKey);
        assertNotNull(scheduler.getJobDetail(lineupJobKey), "PreviousMatchJob should have been registered.");
        assertNotNull(scheduler.getJobDetail(liveMatchJobKey), "LiveMatchJob should have been registered.");
    }

    @DisplayName("Available 제거 직후 Job 이 제대로 삭제되는지 테스트")
    @Test
    public void testJobsRemovedWhenAvailableFixtureIsRemoved() throws Exception {
        fixtureId = 10002L;
        mockFixtureRepository(fixtureId);
        // Step 1: AvailableFixture 추가
        footballAvailableService.addAvailableFixture(fixtureId);
        // Step 2: Job 들이 추가됐는지 확인
        JobKey lineupJobKey = getPreviousMatchJobKey(fixtureId);
        JobKey liveMatchJobKey = getLiveMatchJobKey(fixtureId);
        waitUntilJobAllEnrolled(lineupJobKey, liveMatchJobKey);
        assertNotNull(scheduler.getJobDetail(lineupJobKey), "PreviousMatchJob should have been registered.");
        assertNotNull(scheduler.getJobDetail(liveMatchJobKey), "LiveMatchJob should have been registered.");
        // Step 3: AvailableFixture 제거
        footballAvailableService.removeAvailableFixture(fixtureId);
        log.info("check H2 locks before waiting for job removal");
        logH2Locks();
        waitUntilJobAllRemoved();
        log.info("check H2 locks after waiting for job removal");
        // Step 4: Job 들이 제거됐는지 확인
        Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyGroup());
        assertThat(jobKeys).isEmpty();
        assertNull(scheduler.getJobDetail(lineupJobKey), "PreviousMatchJob should have been removed.");
        assertNull(scheduler.getJobDetail(liveMatchJobKey), "LiveMatchJob should have been removed.");
    }

    @DisplayName("Job 이 finish 되면 PostMatchJob 이 추가되는지 테스트")
    @Test
    public void testPostMatchJobAddedWhenJobsFinish() throws Exception {
        fixtureId = 10003L;
        mockFixtureRepository(fixtureId);
        // Step 1: Job Task 가 한번 실행되고 끝나도록 설정
        when(previousMatchTask.requestAndSaveLineup(fixtureId)).thenReturn(true); // StartLineupTask가 끝남
        when(liveMatchTask.requestAndSaveLiveMatchData(fixtureId)).thenReturn(true); // LiveFixtureTask가 끝남
        // Step 2: AvailableFixture 추가
        footballAvailableService.addAvailableFixture(fixtureId);
        // Step 3: PreviousMatchJob 과 LiveMatchJob 이 등록되었는지 확인
        // JobKey lineupJobKey = getPreviousMatchJobKey(fixtureId); //
        JobKey liveMatchJobKey = getLiveMatchJobKey(fixtureId);
        waitUntilJobAllEnrolled(liveMatchJobKey);
        // Step 4: 등록된 Job 들을 즉시 트리거
        triggerJobNow(scheduler, liveMatchJobKey);
        // Step 5: PostMatchJob 이 추가됐는지 확인
        JobKey postMatchJobKey = getPostMatchJobKey(fixtureId);
        waitUntilJobAllEnrolled(postMatchJobKey);
        assertNotNull(scheduler.getJobDetail(postMatchJobKey), "PostMatchJob should have been registered.");
    }

    @DisplayName("PostMatchJob 이 추가된 후 Available 제거 시 PostMatchJob 이 삭제되는지 테스트")
    @Test
    public void testPostMatchJobRemovedWhenAvailableFixtureIsRemoved() throws Exception {
        fixtureId = 10004L;
        mockFixtureRepository(fixtureId);
        // Step 1: Job Task 가 한번 실행되고 끝나도록 설정
        when(previousMatchTask.requestAndSaveLineup(fixtureId)).thenReturn(true);
        when(liveMatchTask.requestAndSaveLiveMatchData(fixtureId)).thenReturn(true);
        // Step 2: AvailableFixture 추가
        footballAvailableService.addAvailableFixture(fixtureId);
        // Step 3: PreviousMatchJob 과 LiveMatchJob 이 등록되었는지 확인
        // JobKey lineupJobKey = getPreviousMatchJobKey(fixtureId);
        JobKey liveMatchJobKey = getLiveMatchJobKey(fixtureId);
        waitUntilJobAllEnrolled(liveMatchJobKey);
        // Step 4: 등록된 Job 들을 즉시 트리거
        triggerJobNow(scheduler, liveMatchJobKey);
        // Step 5: PostMatchJob 이 추가됐는지 확인
        JobKey postMatchJobKey = getPostMatchJobKey(fixtureId);
        waitUntilJobAllEnrolled(postMatchJobKey);
        // Step 6: AvailableFixture 제거
        footballAvailableService.removeAvailableFixture(fixtureId);
        // Step 7: PostMatchJob 이 제거됐는지 확인
        waitUntilJobAllRemoved();
        assertNull(scheduler.getJobDetail(postMatchJobKey), "PostMatchJob should have been removed.");
    }

    private JobKey getPreviousMatchJobKey(long fixtureId) {
        return new JobKey(FootballSchedulerName.previousMatchJob(fixtureId), FootballSchedulerName.fixtureGroup());
    }

    private JobKey getLiveMatchJobKey(long fixtureId) {
        return new JobKey(FootballSchedulerName.liveMatchJob(fixtureId), FootballSchedulerName.fixtureGroup());
    }

    private JobKey getPostMatchJobKey(long fixtureId) {
        return new JobKey(FootballSchedulerName.postMatchJob(fixtureId), FootballSchedulerName.fixtureGroup());
    }

    private void waitUntilJobAllEnrolled(JobKey... jobKeys) {
        await().atMost(JOB_WAIT_TIMEOUT_SEC, TimeUnit.SECONDS).untilAsserted(() -> {
            for (JobKey jobKey : jobKeys) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                assertNotNull(jobDetail, "Job should have been registered: " + jobKey.getName());
            }
        });
    }

    private void waitUntilJobAllRemoved() {
        await().atMost(JOB_WAIT_TIMEOUT_SEC, TimeUnit.SECONDS).untilAsserted(() -> {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyGroup());
            assertThat(jobKeys).isEmpty();
        });
    }

    /**
     * 특정 트리거를 수동으로 즉시 실행하도록 합니다
     *
     * @param scheduler Quartz Scheduler
     * @param jobKeys   즉시 트리거할 JobKey 목록
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
