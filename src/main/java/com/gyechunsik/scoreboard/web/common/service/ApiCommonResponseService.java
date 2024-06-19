package com.gyechunsik.scoreboard.web.common.service;

import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;

public interface ApiCommonResponseService {

    int CODE_SUCCESS = 200;
    int CODE_FAILURE = 400;

    String STATUS_SUCCESS = "SUCCESS";
    String STATUS_FAILURE = "FAILURE";

    String VERSION = "1.0";

    public <T> ApiResponse<T> createSuccessResponse(T[] response, String requestUrl);

    public <T> ApiResponse<T> createFailureResponse(String message, String requestUrl);

}
