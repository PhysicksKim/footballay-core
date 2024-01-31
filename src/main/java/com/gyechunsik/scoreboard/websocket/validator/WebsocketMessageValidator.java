package com.gyechunsik.scoreboard.websocket.validator;

import com.gyechunsik.scoreboard.websocket.messages.BaseWebSocketMessage;

public interface WebsocketMessageValidator {

    public boolean validate(BaseWebSocketMessage message);

    public boolean isSupport(Class<? extends BaseWebSocketMessage> clazz);
}
