package com.footballay.core.web.common.dto

/**
 * 클라이언트에게 전달되는 최종 API 응답 (Kotlin 새 버전)
 */
data class ApiResponseV2<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetail? = null,
    val code: Int? = null
) {
    companion object {
        fun <T> success(data: T, code: Int = 200): ApiResponseV2<T> = ApiResponseV2(
            success = true,
            code = code,
            data = data
        )
        
        fun <T> failure(error: ErrorDetail, code: Int): ApiResponseV2<T> = ApiResponseV2(
            success = false,
            code = code,
            error = error
        )
    }
}

/**
 * 에러 상세 정보 (클라이언트에 필요한 최소 정보)
 */
data class ErrorDetail(
    val message: String,
    val field: String? = null // 유효성 검사 에러 시에만 사용
) 