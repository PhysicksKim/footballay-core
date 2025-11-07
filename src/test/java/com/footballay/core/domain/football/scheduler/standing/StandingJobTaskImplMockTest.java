package com.footballay.core.domain.football.scheduler.standing;

import com.footballay.core.domain.football.exception.ApiRateLimitException;
import com.footballay.core.domain.football.scheduler.standing.StandingJobTaskImpl;
import com.footballay.core.domain.football.external.FootballApiCacheService;
import com.footballay.core.domain.football.persistence.League;
import com.footballay.core.domain.football.persistence.standings.Standing;
import com.footballay.core.domain.football.service.FootballLeagueStandingService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Disabled("Standing feature will be reimplemented in Kotlin project migration")
class StandingJobTaskImplMockTest {

    StandingJobTaskImpl task;

    @Mock
    FootballLeagueStandingService leagueService;

    @Mock
    FootballApiCacheService apiCacheService;

    @Mock
    ScheduledExecutorService scheduler;

    @Captor
    ArgumentCaptor<Runnable> runnableCaptor;

    @Captor
    ArgumentCaptor<Long> delayCaptor;

    @BeforeEach
    void setUp() {
        when(scheduleWithCaptors()).thenAnswer(ExecuteWithNoDelay());
        task = new StandingJobTaskImpl(
                leagueService, apiCacheService, scheduler,
                3 /* MaxTry 는 이 테스트에서 3이상 이어야 합니다 */
        );
    }

    private @NotNull ScheduledFuture<?> scheduleWithCaptors() {
        return scheduler.schedule(runnableCaptor.capture(), delayCaptor.capture(), eq(TimeUnit.MILLISECONDS));
    }

    private static @NotNull Answer<Object> ExecuteWithNoDelay() {
        return invocation -> {
            Runnable r = invocation.getArgument(0);
            r.run();
            return mock(ScheduledFuture.class);
        };
    }

    @Test
    void 성공_흐름에서는_모든_리그_한번씩_호출한다() {
        // given
        var league1 = League.builder().leagueId(1L).build();
        var league2 = League.builder().leagueId(2L).build();
        when(leagueService.getStandingAvailableLeagues())
                .thenReturn(List.of(league1, league2));
        when(apiCacheService.cacheStandingOfLeague(anyLong()))
                .thenReturn(mock(Standing.class));

        // when
        task.execute();

        // then
        verify(apiCacheService, times(1)).cacheStandingOfLeague(1L);
        verify(apiCacheService, times(1)).cacheStandingOfLeague(2L);
        assertThat(delayCaptor.getAllValues()).allSatisfy(d -> assertThat(d).isZero());
    }

    @Test
    void rateLimit_걸리면_다음윈도우에_재시도한다() {
        // given
        var id = 1L;
        var league1 = League.builder().leagueId(id).build();
        when(leagueService.getStandingAvailableLeagues())
                .thenReturn(List.of(league1));
        when(apiCacheService.cacheStandingOfLeague(id))
                .thenThrow(new ApiRateLimitException("limit"))
                .thenReturn(mock(Standing.class));

        // when
        task.execute();

        // then: API는 두 번 호출
        verify(apiCacheService, times(2)).cacheStandingOfLeague(id);
        List<Long> delays = delayCaptor.getAllValues();
        assertThat(delays).hasSize(3);
        assertThat(delays.get(0)).isZero();
        assertThat(delays.get(1)).isGreaterThan(0);
        assertThat(delays.get(2)).isZero();
    }

    @Test
    void 일반예외_걸리면_즉시_재시도한다() {
        // given
        long id = 99L;
        var league = League.builder().leagueId(id).build();
        when(leagueService.getStandingAvailableLeagues())
                .thenReturn(List.of(league));
        when(apiCacheService.cacheStandingOfLeague(id))
                .thenThrow(new RuntimeException("Test Exception"))
                .thenReturn(mock(Standing.class));

        // when
        task.execute();

        // then
        verify(apiCacheService, times(2)).cacheStandingOfLeague(id);
        assertThat(delayCaptor.getAllValues()).allSatisfy(d -> assertThat(d).isZero());
    }

    @Test
    void 최대재시도_초과한_리그는_스킵한다() {
        // given
        long id = 123L;
        var league = League.builder().leagueId(id).build();
        when(leagueService.getStandingAvailableLeagues())
                .thenReturn(List.of(league));
        when(apiCacheService.cacheStandingOfLeague(id))
                .thenThrow(new ApiRateLimitException("limit"));

        // when
        task.execute();

        // then
        verify(apiCacheService, times(task.getMaxTryCount()-1)).cacheStandingOfLeague(id);
        verifyNoMoreInteractions(apiCacheService);
        assertThat(delayCaptor.getAllValues()).hasSize(task.getMaxTryCount());
    }

}