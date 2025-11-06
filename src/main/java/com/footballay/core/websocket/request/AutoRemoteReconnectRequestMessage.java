package com.footballay.core.websocket.request;

public class AutoRemoteReconnectRequestMessage {
    private String nickname;

    public String getNickname() {
        return this.nickname;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "AutoRemoteReconnectRequestMessage(nickname=" + this.getNickname() + ")";
    }

    public AutoRemoteReconnectRequestMessage(final String nickname) {
        this.nickname = nickname;
    }

    public AutoRemoteReconnectRequestMessage() {
    }
}
