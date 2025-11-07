package com.footballay.core.monitor.alert.manaer;

import com.footballay.core.monitor.alert.duplicate.AlertDeduplicator;
import com.footballay.core.monitor.alert.notify.AlertNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("dev")
class MatchAlertManagerIntegrationTest {

    @Mock
    private AlertNotifier lineupNotifier;

    @Mock
    private AlertNotifier matchDataNotifier;

    @Mock
    private AlertNotifier matchIdNullNotifier;

    @Autowired
    private AlertDeduplicator deduplicator;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private MatchAlertManager manager;

    private static final Duration TTL = Duration.ofMinutes(10);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 테스트 전 Redis DB를 깨끗이 비웁니다.
        RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
        conn.flushDb();
        conn.close();

        // 각 notifier가 어떤 카테고리를 지원하는지 정의
        when(lineupNotifier.isSupport(AlertCategory.LINEUP)).thenReturn(true);
        when(matchDataNotifier.isSupport(AlertCategory.MATCHDATA)).thenReturn(true);
        when(matchIdNullNotifier.isSupport(AlertCategory.MATCHIDNULL)).thenReturn(true);

        // manager에 실제 deduplicator와 mock notifiers 주입
        manager = new MatchAlertManager(
                deduplicator,
                List.of(lineupNotifier, matchDataNotifier, matchIdNullNotifier)
        );
    }

    @Test
    void firstCall_shouldNotify_secondCall_shouldSkip() throws Exception {
        String id = "fixture-123";
        String msg = "test message";

        // 첫 번째 호출: shouldNotify → true → notifier.notifyAlert() 호출
        manager.alertOnce(AlertCategory.LINEUP, AlertSeverity.FAILURE, id, msg, TTL);
        verify(lineupNotifier).notifyAlert(AlertSeverity.FAILURE, id, msg);

        // 두 번째 호출: 같은 key → shouldNotify → false → skip
        manager.alertOnce(AlertCategory.LINEUP, AlertSeverity.FAILURE, id, msg, TTL);
        verify(lineupNotifier, times(1)).notifyAlert(any(), any(), any());
    }

    @Test
    void differentCategories_useDifferentNotifiers() throws Exception {
        String id = "abc";
        String msg = "hello";

        // LINEUP
        manager.alertOnce(AlertCategory.LINEUP, AlertSeverity.SUCCESS, id, msg, TTL);
        verify(lineupNotifier).notifyAlert(AlertSeverity.SUCCESS, id, msg);

        // MATCHDATA
        manager.alertOnce(AlertCategory.MATCHDATA, AlertSeverity.SUCCESS, id, msg, TTL);
        verify(matchDataNotifier).notifyAlert(AlertSeverity.SUCCESS, id, msg);

        // MATCHIDNULL
        manager.alertOnce(AlertCategory.MATCHIDNULL, AlertSeverity.WARNING, id, msg, TTL);
        verify(matchIdNullNotifier).notifyAlert(AlertSeverity.WARNING, id, msg);
    }
}