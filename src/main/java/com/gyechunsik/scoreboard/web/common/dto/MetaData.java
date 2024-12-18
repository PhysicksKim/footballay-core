package com.gyechunsik.scoreboard.web.common.dto;

import java.util.Map;

/**
 * <h2>api 응답 메타데이터</h2>
 * @param requestId UUID
 * @param timestamp 요청 시간
 * @param status 응답 상태 "SUCCESS", "FAILURE"
 * @param responseCode 응답 코드. / 성공 : "200" / 실패 : "400"
 * @param message 응답 메시지. 실패시 실패 이유
 * @param requestUrl 요청한 URL
 * @param version API 버전
 */
public record MetaData(
        String requestId, // UUID
        String timestamp,
        String status,
        int responseCode,
        String message,
        String requestUrl,
        Map<String, String> params,
        String version
) {
}
