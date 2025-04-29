package com.gyechunsik.scoreboard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        registerAfterBurner(mapper);
        registerTimeModule(mapper);
        return mapper;
    }

    /**
     * {@link ZonedDateTime} 직렬화시 ISO_OFFSET_DATE_TIME 포맷으로 직렬화 합니다
     * @param mapper
     */
    private static void registerTimeModule(ObjectMapper mapper) {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        ZonedDateTimeSerializer zonedDateTimeSerializer = new ZonedDateTimeSerializer(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        javaTimeModule.addSerializer(ZonedDateTime.class, zonedDateTimeSerializer);
        mapper.registerModule(javaTimeModule);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private static void registerAfterBurner(ObjectMapper mapper) {
        mapper.registerModule(new BlackbirdModule());
    }

}
