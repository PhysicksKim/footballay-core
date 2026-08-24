package com.footballay.core.web.admin.localization.controller

import com.footballay.core.common.result.toResponseEntity
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.admin.localization.dto.CoreLocalizationResponse
import com.footballay.core.web.admin.localization.dto.LocalizationUpdateRequest
import com.footballay.core.web.admin.localization.dto.SupportedLocaleResponse
import com.footballay.core.web.admin.localization.service.AdminLocalizationWebService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/** Admin localization CRUD HTTP endpoint입니다. */
@Validated
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/localizations")
class AdminLocalizationManagementController(
    private val adminLocalizationWebService: AdminLocalizationWebService,
) {
    @GetMapping("/locales")
    fun getSupportedLocales(): ResponseEntity<List<SupportedLocaleResponse>> = ResponseEntity.ok(adminLocalizationWebService.getSupportedLocales())

    @GetMapping("/leagues")
    fun getAvailableLeagues(
        @RequestParam locale: String,
    ): ResponseEntity<List<CoreLocalizationResponse>> {
        val supportedLocale = locale.toSupportedLocale() ?: return ResponseEntity.badRequest().build()
        return adminLocalizationWebService.getAvailableLeagues(supportedLocale).toResponseEntity()
    }

    @GetMapping("/leagues/lookup")
    fun lookupLeague(
        @RequestParam(required = false) uid: String?,
        @RequestParam(required = false) apiId: Long?,
        @RequestParam locale: String,
    ): ResponseEntity<CoreLocalizationResponse> {
        val supportedLocale = locale.toSupportedLocale() ?: return ResponseEntity.badRequest().build()
        return adminLocalizationWebService.lookupLeague(uid, apiId, supportedLocale).toResponseEntity()
    }

    @GetMapping("/leagues/{leagueUid}/teams")
    fun getTeams(
        @PathVariable leagueUid: String,
        @RequestParam locale: String,
    ): ResponseEntity<List<CoreLocalizationResponse>> {
        val supportedLocale = locale.toSupportedLocale() ?: return ResponseEntity.badRequest().build()
        return adminLocalizationWebService.getTeams(leagueUid, supportedLocale).toResponseEntity()
    }

    @GetMapping("/teams/{teamUid}/players")
    fun getPlayers(
        @PathVariable teamUid: String,
        @RequestParam locale: String,
    ): ResponseEntity<List<CoreLocalizationResponse>> {
        val supportedLocale = locale.toSupportedLocale() ?: return ResponseEntity.badRequest().build()
        return adminLocalizationWebService.getPlayers(teamUid, supportedLocale).toResponseEntity()
    }

    @PutMapping("/leagues/{uid}/{locale}")
    fun updateLeagueLocalization(
        @PathVariable uid: String,
        @PathVariable locale: String,
        @RequestBody @Valid request: LocalizationUpdateRequest,
    ): ResponseEntity<CoreLocalizationResponse> {
        val supportedLocale = locale.toSupportedLocale() ?: return ResponseEntity.badRequest().build()
        return adminLocalizationWebService.updateLeagueLocalization(uid, supportedLocale, request.name, request.shortName).toResponseEntity()
    }

    @PutMapping("/teams/{uid}/{locale}")
    fun updateTeamLocalization(
        @PathVariable uid: String,
        @PathVariable locale: String,
        @RequestBody @Valid request: LocalizationUpdateRequest,
    ): ResponseEntity<CoreLocalizationResponse> {
        val supportedLocale = locale.toSupportedLocale() ?: return ResponseEntity.badRequest().build()
        return adminLocalizationWebService.updateTeamLocalization(uid, supportedLocale, request.name, request.shortName).toResponseEntity()
    }

    @PutMapping("/players/{uid}/{locale}")
    fun updatePlayerLocalization(
        @PathVariable uid: String,
        @PathVariable locale: String,
        @RequestBody @Valid request: LocalizationUpdateRequest,
    ): ResponseEntity<CoreLocalizationResponse> {
        val supportedLocale = locale.toSupportedLocale() ?: return ResponseEntity.badRequest().build()
        return adminLocalizationWebService.updatePlayerLocalization(uid, supportedLocale, request.name, request.shortName).toResponseEntity()
    }

    private fun String.toSupportedLocale(): SupportedLocale? = SupportedLocale.entries.find { it.code == this }
}
