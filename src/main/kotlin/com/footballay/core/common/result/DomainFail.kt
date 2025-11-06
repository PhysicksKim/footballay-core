package com.footballay.core.common.result

/**
 * 공통 실패 타입(초기 2종)
 */
sealed interface DomainFail {
    data class Validation(
        val errors: List<ValidationError>,
    ) : DomainFail {
        data class ValidationError(
            val code: String,
            val message: String,
            val field: String? = null,
        )

        companion object {
            fun single(
                code: String,
                message: String,
                field: String? = null,
            ): Validation = Validation(listOf(ValidationError(code, message, field)))
        }
    }

    data class NotFound(
        val resource: String,
        val id: String,
    ) : DomainFail
}
