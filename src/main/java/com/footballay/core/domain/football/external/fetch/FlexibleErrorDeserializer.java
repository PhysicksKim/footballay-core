package com.footballay.core.domain.football.external.fetch;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FlexibleErrorDeserializer extends JsonDeserializer<ApiError> {
    @Override
    public ApiError deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        // 배열이면 에러 없음
        if (node.isArray()) {
            return new ApiError(new HashMap<>());
        }

        // 객체면 에러 있음
        if (node.isObject()) {
            Map<String, String> errorMap = new HashMap<>();
            node.fields().forEachRemaining(entry ->
                errorMap.put(entry.getKey(), entry.getValue().asText())
            );
            return new ApiError(errorMap);
        }

        return new ApiError(new HashMap<>());
    }
}
