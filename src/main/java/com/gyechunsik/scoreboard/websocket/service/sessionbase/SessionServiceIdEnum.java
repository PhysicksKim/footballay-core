package com.gyechunsik.scoreboard.websocket.service.sessionbase;

public enum SessionServiceIdEnum {
    SCOREBOARD("scoreboard"),
    CONTROL("control");

    private final String serviceId;

    SessionServiceIdEnum(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceId() {
        return serviceId;
    }
}
