package com.footballay.core.domain.football.scheduler.standing;

import com.footballay.core.domain.football.persistence.standings.Standing;
import com.footballay.core.domain.football.exception.ApiRateLimitException;
import com.footballay.core.domain.football.external.FootballApiCacheService;
import com.footballay.core.domain.football.service.FootballLeagueStandingService;
import jakarta.annotation.PreDestroy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

@Component
public class StandingJobTaskImpl implements StandingJobTask {

    private static final Logger log = LoggerFactory.getLogger(StandingJobTaskImpl.class);

    private static final int DEFAULT_MAX_TRIES = 3;

    private final int MAX_TRY_COUNT;

    private final FootballLeagueStandingService leagueStandingService;
    private final FootballApiCacheService apiCacheService;
    private final ScheduledExecutorService scheduler;

    private static final int RETRY_WINDOW_SECOND = 3;  // 0~2초 서버 시간 동기화 오차 방지

    private static final ScheduledExecutorService DEFAULT_SCHEDULED_EXECUTOR =
            createDefaultStandingExecutor();

    @Autowired
    public StandingJobTaskImpl(
            FootballLeagueStandingService leagueService,
            FootballApiCacheService apiCacheService
    ) {
        this(leagueService, apiCacheService, DEFAULT_SCHEDULED_EXECUTOR, DEFAULT_MAX_TRIES);
    }

    protected StandingJobTaskImpl(
            FootballLeagueStandingService leagueService,
            FootballApiCacheService apiCacheService,
            ScheduledExecutorService scheduler
    ) {
        this(leagueService, apiCacheService, scheduler, DEFAULT_MAX_TRIES);
    }

    protected StandingJobTaskImpl(
            FootballLeagueStandingService leagueService,
            FootballApiCacheService apiCacheService,
            ScheduledExecutorService scheduler,
            int maxTryCount
    ) {
        if(maxTryCount < 1) {
            throw new IllegalArgumentException("maxTryCount must be >= 1");
        }
        this.leagueStandingService = leagueService;
        this.apiCacheService = apiCacheService;
        this.scheduler = scheduler;
        this.MAX_TRY_COUNT = maxTryCount;
    }

    @Override
    public void execute() {
        BlockingQueue<StandingLeague> queue =
                new LinkedBlockingQueue<>(getStandingAvailableLeagues());
        scheduleNext(queue, 0);
    }

    private void scheduleNext(BlockingQueue<StandingLeague> queue, long delayMillis) {
        try {
            scheduler.schedule(() -> processQueue(queue), delayMillis, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException rex) {
            log.warn("Scheduler is shut down while executing StandingJobTask. skipping next task scheduling", rex);
        }
    }

    private void processQueue(BlockingQueue<StandingLeague> queue) {
        if(queue.isEmpty()) {
            log.info("All standing leagues have been processed.");
            return;
        }

        StandingLeague league = queue.poll();
        if (league == null) {
            log.warn("unexpected null league from queue");
            return;
        }

        league.incrementTryCount();
        if (league.exceedMaxTry(getMaxTryCount())) {
            log.error("exceed try count ({}) of StandingLeague. league=[id={},name={},season={}]",
                    getMaxTryCount(), league.getLeagueId(), league.getName(), league.getCurrentSeason());
            if (!queue.isEmpty()) {
                scheduleNext(queue, 0);
            }
            return;
        }

        try {
            // Standing standing = apiCacheService.cacheStandingOfLeague(league.getLeagueId());
            log.info("success to save standing of league=[id={},name={}]", league.getLeagueId(), league.getName());
            scheduleNext(queue, 0);
        } catch (ApiRateLimitException e) {
            long delay = calculateRetryDelay();
            log.info("API rate limit exceed, retry after {}ms : league=[id={},name={}]", delay, league.getLeagueId(), league.getName());
            queue.offer(league);
            scheduleNext(queue, delay);
        } catch (Exception e) {
            log.error("exception while saving standing of league=[id={},name={}] , retry immediately",
                    league.getLeagueId(), league.getName(), e);
            queue.offer(league);
            scheduleNext(queue, 0);
        }
    }

    /**
     * 다음 재시도까지 남은 밀리초를 RETRY_WINDOW_SECOND 를 근거로 계산합니다.
     */
    private long calculateRetryDelay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = now
                .plusMinutes(1)
                .withSecond(RETRY_WINDOW_SECOND)
                .withNano(0);
        return Duration.between(now, next).toMillis();
    }

    protected int getMaxTryCount() {
        return this.MAX_TRY_COUNT;
    }

    private static @NotNull ScheduledExecutorService createDefaultStandingExecutor() {
        return Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "standing-job-scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    private List<StandingLeague> getStandingAvailableLeagues() {
        return leagueStandingService.getStandingAvailableLeagues()
                .stream()
                .map(league -> StandingLeague.builder()
                        .leagueId(league.getLeagueId())
                        .name(league.getName())
                        .currentSeason(league.getCurrentSeason())
                        .build())
                .toList();
    }

    @PreDestroy
    public void shutdownScheduler() {
        scheduler.shutdownNow();
    }

    final private static class StandingLeague {
        private Long leagueId;
        private String name;
        private Integer currentSeason;
        private int tryCount;

        private StandingLeague(Long leagueId, String name, Integer currentSeason, int tryCount) {
            this.leagueId = leagueId;
            this.name = name;
            this.currentSeason = currentSeason;
            this.tryCount = tryCount;
        }

        public Long getLeagueId() {
            return leagueId;
        }

        public String getName() {
            return name;
        }

        public Integer getCurrentSeason() {
            return currentSeason;
        }

        public int getTryCount() {
            return tryCount;
        }

        private void incrementTryCount() {
            this.tryCount++;
        }

        private boolean exceedMaxTry(int maxTryCount) {
            return this.tryCount >= maxTryCount;
        }

        public static StandingLeagueBuilder builder() {
            return new StandingLeagueBuilder();
        }

        public static class StandingLeagueBuilder {
            private Long leagueId;
            private String name;
            private Integer currentSeason;
            private int tryCount;

            StandingLeagueBuilder() {
            }

            public StandingLeagueBuilder leagueId(Long leagueId) {
                this.leagueId = leagueId;
                return this;
            }

            public StandingLeagueBuilder name(String name) {
                this.name = name;
                return this;
            }

            public StandingLeagueBuilder currentSeason(Integer currentSeason) {
                this.currentSeason = currentSeason;
                return this;
            }

            public StandingLeagueBuilder tryCount(int tryCount) {
                this.tryCount = tryCount;
                return this;
            }

            public StandingLeague build() {
                return new StandingLeague(leagueId, name, currentSeason, tryCount);
            }
        }
    }
}
