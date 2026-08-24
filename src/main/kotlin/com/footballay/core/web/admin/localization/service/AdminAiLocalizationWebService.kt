package com.footballay.core.web.admin.localization.service

import com.fasterxml.jackson.databind.JsonNode
import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.localization.AiLocalizationUpdate
import com.footballay.core.localization.LocalizationFacade
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.admin.localization.ai.AiLocalizationExportContextLoader
import com.footballay.core.web.admin.localization.ai.AiLocalizationEntityType
import com.footballay.core.web.admin.localization.ai.AiLocalizationExportValidator
import com.footballay.core.web.admin.localization.ai.AiLocalizationImportValidator
import com.footballay.core.web.admin.localization.ai.ValidatedAiLocalizationImport
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportRequest
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportResponse
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportResponse
import com.footballay.core.web.admin.localization.mapper.AdminAiLocalizationMapper
import org.springframework.stereotype.Service

/** Admin AI localization export와 import 적용을 조합합니다. */
@Service
class AdminAiLocalizationWebService(
    private val localizationFacade: LocalizationFacade,
    private val exportValidator: AiLocalizationExportValidator,
    private val exportContextLoader: AiLocalizationExportContextLoader,
    private val importValidator: AiLocalizationImportValidator,
) {
    fun exportForAi(request: AiLocalizationExportRequest): DomainResult<AiLocalizationExportResponse, DomainFail> {
        val locales = when (val result = exportValidator.validate(request)) {
            is DomainResult.Success -> result.value
            is DomainResult.Fail -> return result
        }
        return when (request.entityType) {
            AiLocalizationEntityType.TEAM -> exportTeams(request, locales)
            AiLocalizationEntityType.PLAYER -> exportPlayers(request, locales)
        }
    }

    fun validateAiImport(payload: JsonNode) = importValidator.validate(payload)

    fun applyAiImport(import: ValidatedAiLocalizationImport): AiLocalizationImportResponse {
        val updates = import.items.map { AiLocalizationUpdate(it.uid, it.locale, it.name, it.shortName) }
        val result = when (import.entityType) {
            AiLocalizationEntityType.TEAM -> localizationFacade.applyAiTeamLocalizations(updates)
            AiLocalizationEntityType.PLAYER -> localizationFacade.applyAiPlayerLocalizations(updates)
        }
        return AdminAiLocalizationMapper.toImportResponse(result)
    }

    private fun exportTeams(
        request: AiLocalizationExportRequest,
        locales: List<SupportedLocale>,
    ): DomainResult<AiLocalizationExportResponse, DomainFail> {
        val context = when (val result = exportContextLoader.loadTeams(request.leagueUid, request.uids)) {
            is DomainResult.Success -> result.value
            is DomainResult.Fail -> return result
        }
        val localizations = localizationFacade.findTeamLocalizations(request.uids, locales).associateBy { it.coreUid to it.locale }
        return DomainResult.Success(
            AdminAiLocalizationMapper.toTeamExportResponse(request, context.league, context.teams, locales, localizations),
        )
    }

    private fun exportPlayers(
        request: AiLocalizationExportRequest,
        locales: List<SupportedLocale>,
    ): DomainResult<AiLocalizationExportResponse, DomainFail> {
        val teamUid = request.teamUid ?: return DomainResult.Fail(
            DomainFail.Validation.single("TEAM_UID_REQUIRED", "teamUid는 PLAYER export에 필요합니다."),
        )
        val context = when (val result = exportContextLoader.loadPlayers(request.leagueUid, teamUid, request.uids)) {
            is DomainResult.Success -> result.value
            is DomainResult.Fail -> return result
        }
        val localizations = localizationFacade.findPlayerLocalizations(request.uids, locales).associateBy { it.coreUid to it.locale }
        return DomainResult.Success(
            AdminAiLocalizationMapper.toPlayerExportResponse(request, context.league, context.team, context.players, locales, localizations),
        )
    }
}
