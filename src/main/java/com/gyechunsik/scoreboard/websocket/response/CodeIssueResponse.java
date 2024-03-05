package com.gyechunsik.scoreboard.websocket.response;

import lombok.Getter;

/**
 * <pre>
 *     code         : 200
 *     message      : "success"
 *     pubPath      : "/app/remote/{remoteCode}"
 *     subPath      : "/user/topic/user/remote/{remoteCode}"
 *     remoteCode   : "remoteCode"
 * </pre>
 */
@Getter
public class CodeIssueResponse extends AbstractSubPubPathResponse {

    protected final String remoteCode;
    protected final boolean isAutoRemote;
    protected final String cookieGetUrl;

    public CodeIssueResponse(String remoteCode) {
        super(remoteCode);
        this.remoteCode = remoteCode;
        this.isAutoRemote = false;
        this.cookieGetUrl = null;
    }

    public CodeIssueResponse(String remoteCode, boolean isAutoRemote, String cookieGetUrl) {
        super(remoteCode);
        this.remoteCode = remoteCode;
        this.isAutoRemote = isAutoRemote;
        this.cookieGetUrl = cookieGetUrl;
    }

}
