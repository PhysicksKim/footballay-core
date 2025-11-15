package com.footballay.core.common.result

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

fun DomainFail.toHttpStatus(): HttpStatus =
    when (this) {
        is DomainFail.Validation -> HttpStatus.BAD_REQUEST
        is DomainFail.NotFound -> HttpStatus.NOT_FOUND
    }

fun <S : Any> DomainResult<S, DomainFail>.toResponseEntity(
    successStatus: HttpStatus = HttpStatus.OK,
): ResponseEntity<S> =
    when (this) {
        is DomainResult.Success ->
            ResponseEntity.status(successStatus).body(this.value)
        is DomainResult.Fail ->
            ResponseEntity.status(this.error.toHttpStatus()).build()
    }
