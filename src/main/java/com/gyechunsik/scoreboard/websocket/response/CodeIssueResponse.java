package com.gyechunsik.scoreboard.websocket.response;

import lombok.Getter;

/**
 * <pre>
 *     code         : 200
 *     message      : "success"
 *     pubPath      : "/topic/remote/{remoteCode}"
 *     subPath      : "/app/remote/{remoteCode}"
 *     remoteCode   : "remoteCode"
 * </pre>
 */
@Getter
public class CodeIssueResponse extends AbstractSubPubPathResponse {

    protected final String remoteCode;

    public CodeIssueResponse(String remoteCode) {
        super(remoteCode);
        this.remoteCode = remoteCode;
    }
}
