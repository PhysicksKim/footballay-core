package com.gyechunsik.scoreboard.websocket.validator;

import com.gyechunsik.scoreboard.websocket.messages.BaseWebSocketMessage;
import com.gyechunsik.scoreboard.websocket.messages.MessageFromControl;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

/**
 * @see {MessageFromControl}
 */
@Slf4j
public class ValidatorFromControl implements WebsocketMessageValidator{

    @Override
    public boolean validate(BaseWebSocketMessage message) {
        if(!isSupport(message.getClass())) {
            log.info("ValidatorFromControl.validate() : " + message.getClass().getName());
            throw new IllegalArgumentException("Should Check isSupport() before validate()");
        }

        MessageFromControl messageFromControl = (MessageFromControl) message;

        String givenType = messageFromControl.getType();
        Map<String, Object> givenData = messageFromControl.getData();
        Map<String, Object> givenMetadata = messageFromControl.getMetadata();

        // type 검증
        if(!validateType(givenType)) {
            return false;
        }

        // data 검증
        if(!validateData(givenType, givenData)) {
            return false;
        }

        // metadata 검증
        if(!validateMetadata(givenMetadata)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isSupport(Class<? extends BaseWebSocketMessage> clazz) {
        return clazz.isAssignableFrom(MessageFromControl.class);
    }

    private boolean validateType(String type) {
        return Keys.ofType().contains(type);
    }
    private boolean validateData(String givenType, Map<String, Object> givenData) {
        Set<String> expectedKeys = Keys.ofData(givenType);

        // 키 확인
        if (!expectedKeys.equals(givenData.keySet())) {
            return false;
        }

        // 'uniform' 타입의 경우, 데이터 값이 UniformEnum 값들 중 하나인지 확인
        if ("uniform".equals(givenType)) {
            for (String key : expectedKeys) {
                Object value = givenData.get(key);
                if (value instanceof String) {
                    String valueStr = (String) value;
                    try {
                        UniformEnum.valueOf(valueStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return false; // 주어진 데이터 값이 유효한 Enum 값이 아님
                    }
                } else {
                    return false; // 데이터 값이 문자열이 아님
                }
            }
        }

        return true;

    }

    private boolean validateMetadata(Map<String, Object> givenMetadata) {
        return givenMetadata.keySet().equals(Keys.ofMetadata());
    }

    private static class Keys {

        private static final Map<String, Set<String>> dataKeys = Map.of(
                "score", Set.of("teamA", "teamB"),
                "uniform", Set.of("teamA", "teamB"),
                "injury", Set.of("given")
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
