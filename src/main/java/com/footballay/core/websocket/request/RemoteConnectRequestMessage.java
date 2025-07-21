package com.footballay.core.websocket.request;

/**
 * <pre>
 * {
 *   remoteCode: "a2s3kw3",
 *   nickname: "gyechunhoe"
 *   isAutoRemote: true,
 *   afterSetCookieUrl: "/api/scoreboard/usercookie"
 * }
 * </pre>
 */
public class RemoteConnectRequestMessage {
    protected String remoteCode;
    protected String nickname;
    protected boolean autoRemote;

    public String getRemoteCode() {
        return this.remoteCode;
    }

    public String getNickname() {
        return this.nickname;
    }

    public boolean isAutoRemote() {
        return this.autoRemote;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "RemoteConnectRequestMessage(remoteCode=" + this.getRemoteCode() + ", nickname=" + this.getNickname() + ", autoRemote=" + this.isAutoRemote() + ")";
    }

    public RemoteConnectRequestMessage(final String remoteCode, final String nickname, final boolean autoRemote) {
        this.remoteCode = remoteCode;
        this.nickname = nickname;
        this.autoRemote = autoRemote;
    }
}
