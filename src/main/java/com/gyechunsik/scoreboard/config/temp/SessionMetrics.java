package com.gyechunsik.scoreboard.config.temp;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

@Component
public class SessionMetrics implements MeterBinder {

    private final ActiveSessionListener listener;

    public SessionMetrics(ActiveSessionListener listener) {
        this.listener = listener;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("http.sessions.active", listener, ActiveSessionListener::getActiveSessions)
                .description("현재 활성화된 HTTP 세션 수")
                .register(registry);
    }
}
