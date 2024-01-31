package com.gyechunsik.scoreboard.websocket;

import com.gyechunsik.scoreboard.websocket.messages.BaseWebSocketMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MessageValidator {

    public boolean validate(BaseWebSocketMessage message) {
        String msgType = message.getType();
        Map<String, Object> data = message.getData();
        Map<String, Object> metadata = message.getMetadata();

        return true;
    }

}
