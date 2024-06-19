package com.gyechunsik.scoreboard.web.common.service;

import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.common.dto.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ApiV1CommonResponseService implements ApiCommonResponseService {

    private static final Logger log = LoggerFactory.getLogger(ApiV1CommonResponseService.class);

    public <T> ApiResponse<T> createSuccessResponse(T[] response, String requestUrl) {
        String successUUID = UUID.randomUUID().toString();
        log.info("success request UUID :: {}", successUUID);

        MetaData metaData = new MetaData(
                successUUID,
                ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
                STATUS_SUCCESS,
                CODE_SUCCESS,
                "Request processed successfully",
                requestUrl,
                VERSION
        );
        return new ApiResponse<>(metaData, response);
    }

    public <T> ApiResponse<T> createFailureResponse(String message, String requestUrl) {
        String failureUUID = UUID.randomUUID().toString();
        log.info("failure request UUID :: {}", failureUUID);

        MetaData metaData = new MetaData(
                failureUUID,
                ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT),
                STATUS_FAILURE,
                CODE_FAILURE,
                message,
                requestUrl,
                VERSION
        );
        return new ApiResponse<>(metaData, null);
    }

}
