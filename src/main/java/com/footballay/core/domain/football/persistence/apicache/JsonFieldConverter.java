package com.footballay.core.domain.football.persistence.apicache;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.footballay.core.config.JacksonConfig;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@Converter(autoApply = true)
public class JsonFieldConverter implements AttributeConverter<Map<String, Object>, String> {

    private final ObjectMapper objectMapper;

    /**
     * @DataJpaTest 에서 ObjectMapper 를 주입받지 못하는 경우를 대비하여 Fallback 을 사용합니다.
     */
    private static final ObjectMapper FALLBACK = new JacksonConfig().objectMapper();

    @Autowired
    public JsonFieldConverter(@Autowired(required = false) ObjectMapper mapper) {
        if (mapper == null) {
            log.warn("ObjectMapper not found, using fallback ObjectMapper. If not test, please check your configuration.");
            this.objectMapper = FALLBACK;
        } else {
            this.objectMapper = mapper;
        }
    }

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON writing error", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        try {
            return (Map<String, Object>) objectMapper.readValue(dbData, Map.class);
        } catch (JacksonException e) {
            throw new RuntimeException("JSON reading error", e);
        }
    }
}
