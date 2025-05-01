package com.footballay.core.websocket.request;

import com.footballay.core.websocket.response.AbstractBaseResponse;
import lombok.Getter;

@Getter
public class RemoteControlMessage extends AbstractBaseResponse {
    public RemoteControlMessage(int code, String message) {
        super(code, message);
    }
}
