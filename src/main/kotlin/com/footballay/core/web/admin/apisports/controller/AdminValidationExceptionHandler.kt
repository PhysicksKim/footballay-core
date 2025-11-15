package com.footballay.core.web.admin.apisports.controller

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class FieldErrorResponse(
    val field: String?,
    val message: String,
)

data class ValidationErrorResponse(
    val code: String,
    val message: String,
    val errors: List<FieldErrorResponse>,
)

@RestControllerAdvice(basePackages = ["com.footballay.core.web.admin"])
class AdminValidationExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors =
            ex.bindingResult
                .allErrors
                .map { error ->
                    val field =
                        if (error is FieldError) {
                            error.field
                        } else {
                            error.objectName
                        }
                    FieldErrorResponse(
                        field = field,
                        message = error.defaultMessage ?: "Invalid value",
                    )
                }

        val body =
            ValidationErrorResponse(
                code = "WEB_VALIDATION_ERROR",
                message = "요청 값이 유효하지 않습니다.",
                errors = errors,
            )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<ValidationErrorResponse> {
        val errors =
            ex.constraintViolations.map { violation ->
                FieldErrorResponse(
                    field = violation.propertyPath.toString(),
                    message = violation.message,
                )
            }

        val body =
            ValidationErrorResponse(
                code = "WEB_VALIDATION_ERROR",
                message = "요청 값이 유효하지 않습니다.",
                errors = errors,
            )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }
}
