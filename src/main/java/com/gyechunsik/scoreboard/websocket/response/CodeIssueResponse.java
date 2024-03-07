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

    protected final boolean isAutoRemote;
    protected final String cookieGetUrl;
    protected final String type = "issue";

    public CodeIssueResponse(String remoteCode) {
        super(remoteCode);
        this.isAutoRemote = false;
        this.cookieGetUrl = null;
    }

    public CodeIssueResponse(String remoteCode, boolean isAutoRemote) {
        super(remoteCode);
        this.isAutoRemote = isAutoRemote;
        if(isAutoRemote) {
            this.cookieGetUrl = "/api/scoreboard/user/cookie";
        } else {
            this.cookieGetUrl = null;
        }
    }

}
