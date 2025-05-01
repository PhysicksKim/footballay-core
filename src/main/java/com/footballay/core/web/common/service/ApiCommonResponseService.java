package com.footballay.core.web.common.service;

import com.footballay.core.web.common.dto.ApiResponse;
import com.footballay.core.web.common.dto.MetaData;

import java.util.Map;

public interface ApiCommonResponseService {

    int CODE_SUCCESS = 200;
    int CODE_FAILURE = 400;

    String STATUS_SUCCESS = "SUCCESS";
    String STATUS_FAILURE = "FAILURE";

    String VERSION = "1.0";

    <T> ApiResponse<T> createSuccessResponse(T[] response, String requestUrl);

    <T> ApiResponse<T> createSuccessResponse(T[] response, String requestUrl, Map<String, String> params);

    <T> ApiResponse<T> createFailureResponse(String message, String requestUrl);

    <T> ApiResponse<T> createFailureResponse(String message, String requestUrl, Map<String, String> params);

    MetaData createSuccessMetaData(String requestId, Map<String, String> params);

}
