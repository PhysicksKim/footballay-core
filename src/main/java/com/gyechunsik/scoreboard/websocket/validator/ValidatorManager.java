package com.gyechunsik.scoreboard.websocket.validator;

import com.gyechunsik.scoreboard.websocket.messages.BaseWebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ValidatorManager {

    private final List<WebsocketMessageValidator> validators
            = List.of(new ValidatorFromControl(), new ValidatorFromScoreboard());

    public boolean validate(BaseWebSocketMessage message) {
        for (WebsocketMessageValidator validator : validators) {
            log.info("validator : " + validator.getClass().getName());
            if (validator.isSupport(message.getClass())) {
                return validator.validate(message);
            }
        }

        throw new RuntimeException("No validator found for " + message.getClass().getName());
    }

}
