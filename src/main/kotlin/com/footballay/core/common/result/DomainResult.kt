package com.footballay.core.common.result

/**
 * 퍼사드/도메인 유즈케이스의 표준 결과 타입.
 * S: 성공 페이로드(대부분 Int/Unit, 필요 시 도메인 전용 DTO)
 * F: 실패 타입(초기에는 DomainFail.Validation, DomainFail.NotFound만 사용)
 */
sealed class DomainResult<out S : Any, out F : DomainFail> {
    data class Success<out S : Any>(
        val value: S,
    ) : DomainResult<S, Nothing>()

    data class Fail<out F : DomainFail>(
        val error: F,
    ) : DomainResult<Nothing, F>()

    fun getOrNull(): S? =
        when (this) {
            is Success -> this.value
            is Fail -> null
        }

    fun errorOrNull(): F? =
        when (this) {
            is Success -> null
            is Fail -> this.error
        }
}
