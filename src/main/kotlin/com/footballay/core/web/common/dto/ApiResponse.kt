package com.footballay.core.web.common.dto

/**
 * Controller - WebService 간의 전달받는 API 응답 포맷
 */
data class ApiResponseV2<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetail? = null,
    /**
     * [org.apache.http.HttpStatus] 를 사용하여 int 값을 전달할 수 있습니다.
     */
    val code: Int? = null,
) {
    companion object {
        fun <T> success(
            data: T,
            code: Int = 200,
        ): ApiResponseV2<T> =
            ApiResponseV2(
                success = true,
                code = code,
                data = data,
            )

        fun <T> failure(
            error: ErrorDetail,
            code: Int,
        ): ApiResponseV2<T> =
            ApiResponseV2(
                success = false,
                code = code,
                error = error,
            )
    }
}

/**
 * 에러 상세 정보 (클라이언트에 필요한 최소 정보)
 */
data class ErrorDetail(
    val message: String,
    val field: String? = null, // 유효성 검사 에러 시에만 사용
)
