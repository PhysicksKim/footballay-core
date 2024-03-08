package com.gyechunsik.scoreboard.websocket.response;

import lombok.Getter;
import lombok.ToString;

/**
 * <pre>
 *     code         : 200
 *     message      : "success"
 *     pubPath      : "/app/remote/{remoteCode}"
 *     subPath      : "/user/topic/remote/{remoteCode}"
 *     remoteCode   : "remoteCode"
 * </pre>
 */
@Getter
@ToString(callSuper = true)
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
        if(isAutoRemote) {
            this.cookieGetUrl = "/api/scoreboard/user/cookie";
        } else {
            this.cookieGetUrl = null;
        }
        this.type = type;
    }

    public RemoteConnectResponse(String remoteCode, boolean isAutoRemote) {
        super(remoteCode);
        this.isAutoRemote = isAutoRemote;
        if(isAutoRemote) {
            this.cookieGetUrl = "/api/scoreboard/user/cookie";
        } else {
            this.cookieGetUrl = null;
        }
    }
}
