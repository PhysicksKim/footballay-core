package com.footballay.core.monitor.alert.manaer;

import com.footballay.core.monitor.alert.duplicate.AlertDeduplicator;
import com.footballay.core.monitor.alert.notify.AlertNotifier;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;

@Service
public class MatchAlertManager implements AlertManager {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatchAlertManager.class);
    private final AlertDeduplicator deduplicator;
    private final List<AlertNotifier> notifiers;

    public MatchAlertManager(AlertDeduplicator deduplicator, List<AlertNotifier> notifiers) {
        this.deduplicator = deduplicator;
        this.notifiers = notifiers;
    }

    @Override
    public void alertOnce(AlertCategory category, AlertSeverity severity, String entityId, String message, Duration ttl) {
        AlertNotifier notifier = selectNotifier(category);
        if (notifier == null) {
            log.warn("No notifier found for category {}", category);
            return;
        }
        String deduplicateType = deduplicateTypeFrom(category, severity);
        if (deduplicator.shouldNotify(deduplicateType, entityId, ttl)) {
            try {
                notifier.notifyAlert(severity, entityId, message);
                log.info("Alert sent for category: {}, severity: {}, entityId: {}, message: {}", category, severity, entityId, message);
            } catch (Exception e) {
                log.error("Failed to send alert for {}, id: {}", category, entityId, e);
                deduplicator.invalidate(deduplicateType, entityId);
            }
        }
    }

    @Nullable
    public AlertNotifier selectNotifier(@NotNull AlertCategory category) {
        for (AlertNotifier nowNotifier : notifiers) {
            if (nowNotifier.isSupport(category)) {
                return nowNotifier;
            }
        }
        return null;
    }

    private String deduplicateTypeFrom(AlertCategory category, AlertSeverity severity) {
        return String.format("%s:%s", category.name(), severity.name());
    }
}
