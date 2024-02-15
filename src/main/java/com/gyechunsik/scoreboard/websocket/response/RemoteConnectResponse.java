package com.gyechunsik.scoreboard.websocket.response;

import lombok.Getter;
import lombok.ToString;


/**
 * <pre>
 *     code         : 200
 *     message      : "success"
 *     pubPath      : "/topic/remote/{remoteCode}"
 *     subPath      : "/app/remote/{remoteCode}"
 * </pre>
 */
@Getter
@ToString(callSuper = true)
public class RemoteConnectResponse extends AbstractSubPubPathResponse {

    public RemoteConnectResponse(String remoteCode) {
        super(remoteCode);
    }

    public RemoteConnectResponse(int code, String message, String remoteCode) {
        super(code, message, remoteCode);
    }
}
