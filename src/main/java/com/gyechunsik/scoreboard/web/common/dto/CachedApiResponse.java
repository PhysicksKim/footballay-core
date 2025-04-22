package com.gyechunsik.scoreboard.web.common.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import org.apache.poi.ss.formula.functions.T;

/**
 * 응답이 캐싱되어 있는 경우 사용하는 DTO.
 * @see JsonRawValue
 * @param metaData
 * @param response 캐싱된 응답. 직렬화된 String 타입의 JSON
 */
public record CachedApiResponse (
        MetaData metaData,
        @JsonRawValue String response
) {
}
