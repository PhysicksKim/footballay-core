package com.gyechunsik.scoreboard.domain.football.exception;

public class ApiRateLimitException extends RuntimeException{
    public ApiRateLimitException() {
    }

    public ApiRateLimitException(String message) {
        super(message);
    }

    public ApiRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiRateLimitException(Throwable cause) {
        super(cause);
    }

    public ApiRateLimitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
