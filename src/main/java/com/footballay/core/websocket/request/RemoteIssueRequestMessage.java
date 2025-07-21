package com.footballay.core.websocket.request;

/**
 * <pre>
 * {
 *   nickname: "gyechunhoe"
 * }
 * </pre>
 */
public class RemoteIssueRequestMessage {
    protected String nickname;
    protected boolean autoRemote;

    public String getNickname() {
        return this.nickname;
    }

    public boolean isAutoRemote() {
        return this.autoRemote;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "RemoteIssueRequestMessage(nickname=" + this.getNickname() + ", autoRemote=" + this.isAutoRemote() + ")";
    }

    public RemoteIssueRequestMessage(final String nickname, final boolean autoRemote) {
        this.nickname = nickname;
        this.autoRemote = autoRemote;
    }
}
