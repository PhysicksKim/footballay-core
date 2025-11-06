package com.footballay.core.monitor.alert.notify;

import com.footballay.core.monitor.alert.NotificationException;
import com.footballay.core.monitor.alert.manaer.AlertCategory;
import com.footballay.core.monitor.alert.manaer.AlertSeverity;

public interface AlertNotifier {

    void notifyAlert(AlertSeverity severity, String alertTargetTitle, String message) throws NotificationException;

    boolean isSupport(AlertCategory category);
}
