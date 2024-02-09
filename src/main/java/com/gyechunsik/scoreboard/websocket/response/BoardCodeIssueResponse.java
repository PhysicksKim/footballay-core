package com.gyechunsik.scoreboard.websocket.response;

import lombok.Getter;

@Getter
public class BoardCodeIssueResponse extends SuccessResponse {

    protected final String remoteCode;
    protected final String subPath;

    public BoardCodeIssueResponse(int code, String message, String remoteCode) {
        super(code, message);
        this.remoteCode = remoteCode;
        this.subPath = "/topic/board/"+remoteCode;
    }
}
