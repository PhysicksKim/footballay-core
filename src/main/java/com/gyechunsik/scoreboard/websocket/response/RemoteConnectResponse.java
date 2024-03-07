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
public class RemoteConnectResponse extends AbstractSubPubPathResponse {

    protected final boolean isAutoRemote;
    protected final String cookieGetUrl;
    protected final String type = "connect";

    public RemoteConnectResponse(String remoteCode) {
        super(remoteCode);
        this.isAutoRemote = false;
        this.cookieGetUrl = null;
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

    public RemoteConnectResponse(int code, String message, String remoteCode) {
        super(code, message, remoteCode);
        this.isAutoRemote = false;
        this.cookieGetUrl = null;
    }
}
