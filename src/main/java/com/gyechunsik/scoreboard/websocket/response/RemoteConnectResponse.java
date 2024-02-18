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

    protected final String remoteCode;

    public RemoteConnectResponse(String remoteCode) {
        super(remoteCode);
        this.remoteCode = remoteCode;
    }

    public RemoteConnectResponse(int code, String message, String remoteCode) {
        super(code, message, remoteCode);
        this.remoteCode = remoteCode;
    }
}
