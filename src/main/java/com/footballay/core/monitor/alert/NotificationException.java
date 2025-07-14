package com.footballay.core.monitor.alert;

/**
 * 알림을 보내지 못했을 때 발생하는 예외
 */
public class NotificationException extends RuntimeException {
    public NotificationException(String message) {
        super(message);
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
