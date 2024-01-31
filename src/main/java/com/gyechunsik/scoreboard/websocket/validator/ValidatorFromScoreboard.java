package com.gyechunsik.scoreboard.websocket.validator;

import com.gyechunsik.scoreboard.websocket.messages.BaseWebSocketMessage;
import com.gyechunsik.scoreboard.websocket.messages.MessageFromControl;
import com.gyechunsik.scoreboard.websocket.messages.MessageFromScoreboard;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

@Slf4j
public class ValidatorFromScoreboard implements WebsocketMessageValidator {

    @Override
    public boolean validate(BaseWebSocketMessage message) {
        if(!isSupport(message.getClass())) {
            log.info("ValidatorFromScoreboard.validate() : " + message.getClass().getName());
            throw new IllegalArgumentException("Should Check isSupport() before validate()");
        }

        MessageFromScoreboard messageFromScoreboard = (MessageFromScoreboard) message;

        String givenType = messageFromScoreboard.getType();
        Map<String, Object> givenData = messageFromScoreboard.getData();
        Map<String, Object> givenMetadata = messageFromScoreboard.getMetadata();

        // TODO : Score BOard 쪽에선 어떤 메세지를 보내지?

        return true;
    }

    @Override
    public boolean isSupport(Class<? extends BaseWebSocketMessage> clazz) {
        return clazz.isAssignableFrom(MessageFromScoreboard.class);
    }

    private boolean validateType(String type) {
        return Keys.ofType().contains(type);
    }
    private boolean validateData(String givenType, Map<String, Object> givenData) {
        return Keys.ofData(givenType).equals(givenData.keySet());
    }

    private static class Keys {

        private static final Map<String, Set<String>> dataKeys = Map.of(
        );
        private static final Set<String> metadataKeys = Set.of("timestamp", "messageId");

        public static Set<String> ofType() {
            return dataKeys.keySet();
        }

        public static Set<String> ofData(String type) {
            return dataKeys.get(type);
        }

        public static Set<String> ofMetadata() {
            return metadataKeys;
        }
    }
}


