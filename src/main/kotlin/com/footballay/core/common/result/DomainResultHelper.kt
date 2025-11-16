package com.footballay.core.common.result

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/**
 * DomainFail을 HTTP 상태 코드로 변환합니다.
 *
 * - [DomainFail.Validation] 오류는 400(BAD_REQUEST)로 매핑됩니다.
 * - [DomainFail.NotFound] 오류는 404(NOT_FOUND)로 매핑됩니다.
 */
fun DomainFail.toHttpStatus(): HttpStatus =
    when (this) {
        is DomainFail.Validation -> HttpStatus.BAD_REQUEST
        is DomainFail.NotFound -> HttpStatus.NOT_FOUND
        is DomainFail.Unknown -> HttpStatus.INTERNAL_SERVER_ERROR
    }

/**
 * DomainResult를 ResponseEntity로 변환합니다.
 *
 * 성공시 200(OK) 또는 지정된 성공 상태 코드를 반환하며,
 * 실패시 DomainFail에 매핑된 HTTP 상태 코드를 반환합니다.
 *
 * [DomainFail.toHttpStatus] 함수를 사용하여 실패 상태 코드를 결정합니다.
 */
fun <S : Any> DomainResult<S, DomainFail>.toResponseEntity(
    successStatus: HttpStatus = HttpStatus.OK,
): ResponseEntity<S> =
    when (this) {
        is DomainResult.Success ->
            ResponseEntity.status(successStatus).body(this.value)
        is DomainResult.Fail ->
            ResponseEntity.status(this.error.toHttpStatus()).build()
    }
