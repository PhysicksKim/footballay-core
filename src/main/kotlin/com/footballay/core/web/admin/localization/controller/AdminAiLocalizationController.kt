package com.footballay.core.web.admin.localization.controller

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.common.result.toResponseEntity
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportRequest
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportResponse
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportValidationError
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportValidationFailureResponse
import com.footballay.core.web.admin.localization.service.AdminLocalizationWebService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** Admin AI localization export/import HTTP endpoint입니다. */
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/localizations")
class AdminAiLocalizationController(
    private val adminLocalizationWebService: AdminLocalizationWebService,
    private val objectMapper: ObjectMapper,
) {
    @PostMapping("/ai-export")
    fun exportForAi(
        @RequestBody @Valid request: AiLocalizationExportRequest,
    ): ResponseEntity<AiLocalizationExportResponse> = adminLocalizationWebService.exportForAi(request).toResponseEntity()

    @PostMapping("/ai-import")
    fun importForAi(
        @RequestBody(required = false) rawPayload: String?,
    ): ResponseEntity<*> {
        if (rawPayload.isNullOrBlank()) return malformedJsonResponse()
        return try {
            val result = adminLocalizationWebService.validateAiImport(objectMapper.readTree(rawPayload))
            if (result.isSuccess) ResponseEntity.ok().build<Any>() else ResponseEntity.badRequest().body(requireNotNull(result.failure))
        } catch (_: JsonProcessingException) {
            malformedJsonResponse()
        }
    }

    private fun malformedJsonResponse(): ResponseEntity<AiLocalizationImportValidationFailureResponse> =
        ResponseEntity.badRequest().body(
            AiLocalizationImportValidationFailureResponse(
                listOf(AiLocalizationImportValidationError("MALFORMED_JSON", "JSON 형식이 올바르지 않습니다.")),
            ),
        )
}
