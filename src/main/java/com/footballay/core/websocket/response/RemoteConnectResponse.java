package com.footballay.core.websocket.response;

/**
 * <pre>
 *     code         : 200
 *     message      : "success"
 *     pubPath      : "/app/remote/{remoteCode}"
 *     subPath      : "/user/topic/remote/{remoteCode}"
 *     remoteCode   : "remoteCode"
 * </pre>
 */
public class RemoteConnectResponse extends AbstractRemoteResponse {
    protected final boolean isAutoRemote;
    protected final String cookieGetUrl;
    protected String type = "connect";

    public RemoteConnectResponse(String remoteCode) {
        super(remoteCode);
        this.isAutoRemote = false;
        this.cookieGetUrl = null;
    }

    public RemoteConnectResponse(String remoteCode, boolean isAutoRemote, String type) {
        super(remoteCode);
        this.isAutoRemote = isAutoRemote;
        if (isAutoRemote) {
            this.cookieGetUrl = "/api/scoreboard/user/cookie";
        } else {
            this.cookieGetUrl = null;
        }
        this.type = type;
    }

    public RemoteConnectResponse(String remoteCode, boolean isAutoRemote) {
        super(remoteCode);
        this.isAutoRemote = isAutoRemote;
        if (isAutoRemote) {
            this.cookieGetUrl = "/api/scoreboard/user/cookie";
        } else {
            this.cookieGetUrl = null;
        }
    }

    public boolean isAutoRemote() {
        return this.isAutoRemote;
    }

    public String getCookieGetUrl() {
        return this.cookieGetUrl;
    }

    public String getType() {
        return this.type;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "RemoteConnectResponse(super=" + super.toString() + ", isAutoRemote=" + this.isAutoRemote() + ", cookieGetUrl=" + this.getCookieGetUrl() + ", type=" + this.getType() + ")";
    }
}
