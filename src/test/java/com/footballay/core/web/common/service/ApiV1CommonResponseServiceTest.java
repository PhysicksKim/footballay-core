package com.footballay.core.web.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.footballay.core.web.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ApiV1CommonResponseServiceTest {

    private ApiV1CommonResponseService apiV1CommonResponseService;
    private static final ObjectMapper jacksonObjectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        apiV1CommonResponseService = new ApiV1CommonResponseService();
    }

    @DisplayName("createSuccessResponse 메서드 테스트")
    @Test
    void createSuccessResponse() throws JsonProcessingException {
        // given
        String requestUrl = "/api/test/success";
        String[] response = {"data1", "data2"};

        // when
        ApiResponse<String> apiResponse = apiV1CommonResponseService.createSuccessResponse(response, requestUrl);
        String jsonApiResponse = jacksonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(apiResponse);
        log.info("apiResponse={}", jsonApiResponse);

        // then
        assertThat(apiResponse).isNotNull();
        assertThat(apiResponse.metaData().status()).isEqualTo("SUCCESS");
        assertThat(apiResponse.metaData().responseCode()).isEqualTo(200);
        assertThat(apiResponse.metaData().message()).isEqualTo("Request processed successfully");
        assertThat(apiResponse.metaData().requestUrl()).isEqualTo(requestUrl);
        assertThat(apiResponse.response()).isEqualTo(response);
    }

    @DisplayName("createFailureResponse 메서드 테스트")
    @Test
    void createFailureResponse() throws JsonProcessingException {
        // given
        String requestUrl = "/api/test/failure";
        String errorMessage = "Something went wrong";

        // when
        ApiResponse<String> apiResponse = apiV1CommonResponseService.createFailureResponse(errorMessage, requestUrl);
        String jsonApiResponse = jacksonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(apiResponse);
        log.info("apiResponse={}", jsonApiResponse);

        // then
        assertThat(apiResponse).isNotNull();
        assertThat(apiResponse.metaData().status()).isEqualTo("FAILURE");
        assertThat(apiResponse.metaData().responseCode()).isEqualTo(400);
        assertThat(apiResponse.metaData().message()).isEqualTo(errorMessage);
        assertThat(apiResponse.metaData().requestUrl()).isEqualTo(requestUrl);
        assertThat(apiResponse.response()).isNull();
    }
}