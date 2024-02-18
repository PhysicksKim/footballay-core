package com.gyechunsik.scoreboard.websocket.request;

import com.gyechunsik.scoreboard.websocket.response.AbstractBaseResponse;
import lombok.Getter;

@Getter
public class RemoteControlMessage extends AbstractBaseResponse {
    public RemoteControlMessage(int code, String message) {
        super(code, message);
    }
}
