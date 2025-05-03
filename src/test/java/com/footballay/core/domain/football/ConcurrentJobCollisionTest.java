package com.footballay.core.domain.football;

import com.footballay.core.domain.football.FootballRoot;
import com.footballay.core.domain.football.persistence.Fixture;
import com.footballay.core.domain.football.persistence.League;
import com.footballay.core.domain.football.persistence.Team;
import com.footballay.core.domain.football.persistence.live.LiveStatus;
import com.footballay.core.domain.football.persistence.relations.LeagueTeam;
import com.footballay.core.domain.football.repository.FixtureRepository;
import com.footballay.core.domain.football.repository.LeagueRepository;
import com.footballay.core.domain.football.repository.TeamRepository;
import com.footballay.core.domain.football.repository.live.LiveStatusRepository;
import com.footballay.core.domain.football.repository.relations.LeagueTeamRepository;
import com.footballay.core.domain.football.util.GenerateLeagueTeamFixture;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.*;

import static com.footballay.core.domain.football.util.GenerateLeagueTeamFixture.generate;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
class ConcurrentJobCollisionTest {

    private static final Logger log = LoggerFactory.getLogger(ConcurrentJobCollisionTest.class);
    @Autowired
    private EntityManager em;
    @Autowired
    private Scheduler scheduler;

    @Autowired
    private FootballRoot footballRoot;
    @Autowired
    private LiveStatusRepository liveStatusRepository;
    @Autowired
    private LeagueTeamRepository leagueTeamRepository;
    @Autowired
    private LeagueRepository leagueRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private FixtureRepository fixtureRepository;

    private long fixtureId;
    // footballRoot.addAvailableFixture(fixtureId) 등 내부에서 job 등록을 수행하는 메서드가 있다고 가정

    /**
     * 이 테스트는 두 작업을 동시에 실행하여, 하나는 정상적으로 job 등록을 수행하고,
     * 다른 하나는 의도적으로 예외를 발생시켜 job 등록에 실패하게 합니다.
     * 두 작업 모두 동일한 fixtureId를 사용하여 충돌 상황을 유도합니다.
     *
     * ★ 주의: fixtureId, 그룹 이름 등은 실제 환경에 맞게 조정 필요.
     * 또한, 실제 충돌이 발생하는 맥락(예: DB 락 발생 여부)은 Quartz 설정이나 DB LOCK_TIMEOUT 등 외부 설정에 따라 달라질 수 있습니다.
     */
    @Test
    @DisplayName("동시 job 등록으로 인한 충돌 재현 테스트 (정상 vs 실패 job)")
    public void testConcurrentJobCollision() throws Exception {
        saveLeagueFixtureTeamPlayersData();
        final int taskCount = 2;
        // 동시에 시작하기 위해 준비 및 시작 latch 사용
        CountDownLatch readyLatch = new CountDownLatch(taskCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(taskCount);

        // 작업 중 발생한 예외들을 담을 스레드 안전 큐
        ConcurrentLinkedQueue<Exception> exceptionQueue = new ConcurrentLinkedQueue<>();

        log.info("fixtureId={}", fixtureId);
        log.info("get fixture={}", fixtureRepository.findById(fixtureId).get());

        // 정상 job 등록 작업: FootballRoot 내부 메서드를 호출하여 job을 추가하는 로직
        Runnable normalJobTask = () -> {
            try {
                readyLatch.countDown();
                startLatch.await(); // 동시 시작 대기
                // 정상 job 추가: 내부적으로 Quartz 스케줄러에 job 등록
                footballRoot.addAvailableFixture(fixtureId);
            } catch (Exception e) {
                exceptionQueue.add(e);
            } finally {
                doneLatch.countDown();
            }
        };

        // 실패하는 job 등록 작업: 의도적으로 예외를 발생시켜 Quartz가 cleanup에 실패하도록 유도
        Runnable failingJobTask = () -> {
            try {
                readyLatch.countDown();
                startLatch.await(); // 동시 시작 대기
                // FailingJob을 사용하여 job 등록
                JobDetail failingJob = JobBuilder.newJob(FailingJob.class)
                        .withIdentity("FailingJob_" + fixtureId, "FixtureGroup")
                        .usingJobData("fixtureId", fixtureId)
                        .build();
                // 바로 실행하도록 트리거 생성
                Trigger failingTrigger = TriggerBuilder.newTrigger()
                        .withIdentity("FailingTrigger_" + fixtureId, "FixtureGroup")
                        .startNow()
                        .build();
                scheduler.scheduleJob(failingJob, failingTrigger);
            } catch (Exception e) {
                exceptionQueue.add(e);
            } finally {
                doneLatch.countDown();
            }
        };

        // 두 작업을 병렬 실행 (동시 시작)
        ExecutorService executor = Executors.newFixedThreadPool(taskCount);
        executor.submit(normalJobTask);
        executor.submit(failingJobTask);

        readyLatch.await();  // 두 작업 모두 준비될 때까지 대기
        startLatch.countDown(); // 동시에 시작하도록 신호 전달
        doneLatch.await();   // 모든 작업 완료 대기
        executor.shutdown();

        // Quartz 내부 처리 및 DB cleanup 확인을 위해 잠시 대기 (필요시 시간 조정)
        Thread.sleep(1000);

        // 작업 실행 후 예외가 발생한 경우 출력 후 테스트 실패
        if (!exceptionQueue.isEmpty()) {
            exceptionQueue.forEach(Throwable::printStackTrace);
            fail("동시 작업 실행 중 예외가 발생했습니다: " + exceptionQueue);
        }

        // TODO: 추가 검증 로직 - 예를 들어 scheduler.getJobKeys(...)를 통해 job 상태나 trigger cleanup 상태를 점검 가능
    }

    @Transactional
    protected void saveLeagueFixtureTeamPlayersData() {
        GenerateLeagueTeamFixture.LeagueTeamFixture leagueTeamFixture = generate();
        League league = leagueTeamFixture.league;
        Team homeTeam = leagueTeamFixture.home;
        Team awayTeam = leagueTeamFixture.away;
        Fixture fixture = leagueTeamFixture.fixture;
        fixtureId = fixture.getFixtureId();
        leagueRepository.save(league);
        teamRepository.saveAll(List.of(homeTeam, awayTeam));
        LeagueTeam leagueTeamHome = LeagueTeam.builder().league(league).team(homeTeam).build();
        LeagueTeam leagueTeamAway = LeagueTeam.builder().league(league).team(awayTeam).build();
        leagueTeamRepository.saveAll(List.of(leagueTeamHome, leagueTeamAway));
        LiveStatus saveLiveStatus = liveStatusRepository.save(createFullTimeLiveStatus());
        fixture.setLiveStatus(saveLiveStatus);
        fixtureRepository.save(fixture);
    }
    private static LiveStatus createFullTimeLiveStatus() {
        return LiveStatus.builder()
                .elapsed(90)
                .longStatus("Match Finished")
                .shortStatus("FT")
                .homeScore(1)
                .awayScore(0)
                .build();
    }

    /**
     * FailingJob: 실행 시 의도적으로 예외를 발생시켜 Quartz가 해당 job의 trigger cleanup을 제대로 처리하지 못하게 함.
     * 이로 인해 DB row lock 문제 등이 발생할 수 있는 상황을 재현할 수 있습니다.
     *
     * ★ 주의: 실제 오류 상황과 동일한 조건을 재현하려면, Quartz의 관련 설정(예: lockHandler, acquireTriggersWithinLock)과
     * DB의 LOCK_TIMEOUT, 트랜잭션 설정 등을 함께 확인해야 합니다.
     */
    public static class FailingJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            // 예: fixtureId 로그 출력 (필요시)
            Long fixtureId = context.getJobDetail().getJobDataMap().getLong("fixtureId");
            // 의도적으로 예외 발생. unschedule 옵션 설정으로 Quartz가 job cleanup을 시도하게 함.
            JobExecutionException exception = new JobExecutionException("의도적 실행 실패: fixtureId=" + fixtureId);
            exception.setUnscheduleAllTriggers(true);
            throw exception;
        }
    }
}