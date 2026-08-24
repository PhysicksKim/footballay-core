package com.footballay.core.web.admin.localization.service

import com.fasterxml.jackson.databind.JsonNode
import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.LeagueFacade
import com.footballay.core.localization.AiLocalizationUpdate
import com.footballay.core.localization.LocalizationFacade
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.admin.localization.ai.AiLocalizationImportValidator
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportEntityType
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportRequest
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportResponse
import com.footballay.core.web.admin.localization.dto.AiLocalizationImport
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportEntityType
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportResponse
import com.footballay.core.web.admin.localization.mapper.AdminAiLocalizationMapper
import org.springframework.stereotype.Service

/** Admin AI localization export와 import 적용을 처리합니다. */
@Service
class AdminAiLocalizationWebService(
    private val leagueFacade: LeagueFacade,
    private val localizationFacade: LocalizationFacade,
    private val aiLocalizationImportValidator: AiLocalizationImportValidator,
) {
    fun exportForAi(request: AiLocalizationExportRequest): DomainResult<AiLocalizationExportResponse, DomainFail> {
        val locales = request.locales.map { it.toSupportedLocale() }
        val validationErrors = validateExportRequest(request, locales)
        if (validationErrors.isNotEmpty()) return DomainResult.Fail(DomainFail.Validation(validationErrors))

        return when (request.entityType) {
            AiLocalizationExportEntityType.TEAM -> exportTeams(request, locales.filterNotNull())
            AiLocalizationExportEntityType.PLAYER -> exportPlayers(request, locales.filterNotNull())
        }
    }

    fun validateAiImport(payload: JsonNode) = aiLocalizationImportValidator.validate(payload)

    fun applyAiImport(import: AiLocalizationImport): AiLocalizationImportResponse {
        val updates = import.items.map { AiLocalizationUpdate(it.uid, it.locale, it.name, it.shortName) }
        val result =
            when (import.entityType) {
                AiLocalizationImportEntityType.TEAM -> localizationFacade.applyAiTeamLocalizations(updates)
                AiLocalizationImportEntityType.PLAYER -> localizationFacade.applyAiPlayerLocalizations(updates)
            }
        return AdminAiLocalizationMapper.toImportResponse(result)
    }

    private fun exportTeams(
        request: AiLocalizationExportRequest,
        locales: List<SupportedLocale>,
    ): DomainResult<AiLocalizationExportResponse, DomainFail> {
        val league =
            when (val result = leagueFacade.findLeagueByUid(request.leagueUid)) {
                is DomainResult.Success -> result.value
                is DomainResult.Fail -> return result
            }
        val availableTeams =
            when (val result = leagueFacade.findTeamsByLeagueUid(request.leagueUid)) {
                is DomainResult.Success -> result.value
                is DomainResult.Fail -> return result
            }
        val teamsByUid = availableTeams.associateBy { it.uid }
        val missingUids = request.uids.filterNot(teamsByUid::containsKey)
        if (missingUids.isNotEmpty()) return contextMismatch("TEAM_NOT_IN_LEAGUE", missingUids)

        val teams = request.uids.map(teamsByUid::getValue)
        val localizations = localizationFacade.findTeamLocalizations(request.uids, locales).associateBy { it.coreUid to it.locale }
        return DomainResult.Success(AdminAiLocalizationMapper.toTeamExportResponse(request, league, teams, locales, localizations))
    }

    private fun exportPlayers(
        request: AiLocalizationExportRequest,
        locales: List<SupportedLocale>,
    ): DomainResult<AiLocalizationExportResponse, DomainFail> {
        val teamUid = request.teamUid ?: return DomainResult.Fail(DomainFail.Validation.single("TEAM_UID_REQUIRED", "teamUid는 PLAYER export에 필요합니다."))
        val league =
            when (val result = leagueFacade.findLeagueByUid(request.leagueUid)) {
                is DomainResult.Success -> result.value
                is DomainResult.Fail -> return result
            }
        val leagueTeams =
            when (val result = leagueFacade.findTeamsByLeagueUid(request.leagueUid)) {
                is DomainResult.Success -> result.value
                is DomainResult.Fail -> return result
            }
        if (leagueTeams.none { it.uid == teamUid }) return contextMismatch("TEAM_NOT_IN_LEAGUE", listOf(teamUid))
        val team =
            when (val result = leagueFacade.findTeamByUid(teamUid)) {
                is DomainResult.Success -> result.value
                is DomainResult.Fail -> return result
            }
        val availablePlayers =
            when (val result = leagueFacade.findPlayersByTeamUid(teamUid)) {
                is DomainResult.Success -> result.value
                is DomainResult.Fail -> return result
            }
        val playersByUid = availablePlayers.associateBy { it.uid }
        val missingUids = request.uids.filterNot(playersByUid::containsKey)
        if (missingUids.isNotEmpty()) return contextMismatch("PLAYER_NOT_IN_TEAM", missingUids)

        val players = request.uids.map(playersByUid::getValue)
        val localizations = localizationFacade.findPlayerLocalizations(request.uids, locales).associateBy { it.coreUid to it.locale }
        return DomainResult.Success(AdminAiLocalizationMapper.toPlayerExportResponse(request, league, team, players, locales, localizations))
    }

    private fun validateExportRequest(
        request: AiLocalizationExportRequest,
        locales: List<SupportedLocale?>,
    ): List<DomainFail.Validation.ValidationError> =
        buildList {
            if (request.locales.isEmpty()) add(DomainFail.Validation.ValidationError("LOCALES_EMPTY", "locales는 비어 있을 수 없습니다.", "locales"))
            if (request.locales.size != request.locales.toSet().size) add(DomainFail.Validation.ValidationError("LOCALES_DUPLICATED", "locales에 중복 값이 있습니다.", "locales"))
            request.locales.zip(locales).filter { it.second == null }.forEach { (locale, _) ->
                add(DomainFail.Validation.ValidationError("UNSUPPORTED_LOCALE", "지원하지 않는 locale입니다: $locale", "locales"))
            }
            if (request.uids.isEmpty()) add(DomainFail.Validation.ValidationError("UIDS_EMPTY", "uids는 비어 있을 수 없습니다.", "uids"))
            if (request.uids.size != request.uids.toSet().size) add(DomainFail.Validation.ValidationError("UIDS_DUPLICATED", "uids에 중복 값이 있습니다.", "uids"))
        }

    private fun contextMismatch(code: String, uids: List<String>): DomainResult.Fail<DomainFail.Validation> =
        DomainResult.Fail(DomainFail.Validation(uids.map { DomainFail.Validation.ValidationError(code, "현재 탐색 문맥에 포함되지 않은 UID입니다: $it", "uids") }))

    private fun String.toSupportedLocale(): SupportedLocale? = SupportedLocale.entries.find { it.code == this }
}
