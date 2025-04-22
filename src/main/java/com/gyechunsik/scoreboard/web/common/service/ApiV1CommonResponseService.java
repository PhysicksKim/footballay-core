package com.gyechunsik.scoreboard.web.common.service;

import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.common.dto.MetaData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ApiV1CommonResponseService implements ApiCommonResponseService {

    @Override
    public <T> ApiResponse<T> createSuccessResponse(T[] response, String requestUrl) {
        return this.createSuccessResponse(response, requestUrl, Map.of());
    }

    @Override
    public <T> ApiResponse<T> createSuccessResponse(T[] response, String requestUrl, Map<String, String> params) {
        String successUUID = UUID.randomUUID().toString();
        log.info("success request UUID={}, requestUrl={}, params={}", successUUID, requestUrl, params.toString());

        MetaData metaData = new MetaData(
                successUUID,
                ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                STATUS_SUCCESS,
                CODE_SUCCESS,
                "Request processed successfully",
                requestUrl,
                params,
                VERSION
        );
        return new ApiResponse<>(metaData, response);
    }

    @Override
    public <T> ApiResponse<T> createFailureResponse(String message, String requestUrl) {
        return this.createFailureResponse(message, requestUrl, Map.of());
    }

    @Override
    public <T> ApiResponse<T> createFailureResponse(String message, String requestUrl, Map<String, String> params) {
        String failureUUID = UUID.randomUUID().toString();
        log.info("failure request UUID={}, requestUrl={}, params={}", failureUUID, requestUrl, params.toString());

        MetaData metaData = new MetaData(
                failureUUID,
                ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                STATUS_FAILURE,
                CODE_FAILURE,
                message,
                requestUrl,
                params,
                VERSION
        );
        return new ApiResponse<>(metaData, null);
    }

    @Override
    public MetaData createSuccessMetaData(String requestId, Map<String, String> params) {
        return new MetaData(
                requestId,
                ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                STATUS_SUCCESS,
                CODE_SUCCESS,
                "Request processed successfully",
                null,
                params,
                VERSION
        );
    }
}
