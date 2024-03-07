package com.gyechunsik.scoreboard.websocket.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse extends AbstractBaseResponse{

    protected final String type = "error";

    public ErrorResponse() {
        super(400,"에러가 발생했습니다.");
    }

    public ErrorResponse(String message) {
        super(400, message);
    }

    public ErrorResponse(int code, String message) {
        super(code, message);
    }
}
