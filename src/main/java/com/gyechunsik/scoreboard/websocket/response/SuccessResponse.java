package com.gyechunsik.scoreboard.websocket.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SuccessResponse {

    protected int code;
    protected String message;

    public SuccessResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
