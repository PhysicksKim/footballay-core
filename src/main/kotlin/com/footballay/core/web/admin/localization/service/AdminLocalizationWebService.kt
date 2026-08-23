package com.footballay.core.web.admin.localization.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.LeagueFacade
import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.domain.model.PlayerModel
import com.footballay.core.domain.model.TeamModel
import com.footballay.core.localization.CoreLocalizationModel
import com.footballay.core.localization.LocalizationFacade
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.admin.localization.dto.CoreLocalizationResponse
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportContext
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportContextItem
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportEntityType
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportItem
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportRequest
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportResponse
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportValue
import com.footballay.core.web.admin.localization.dto.LocalizationResponse
import com.footballay.core.web.admin.localization.dto.SupportedLocaleResponse
import org.springframework.stereotype.Service

/** Admin localization 조회와 Core 결과 조립을 처리합니다. */
@Service
class AdminLocalizationWebService(
    private val leagueFacade: LeagueFacade,
    private val localizationFacade: LocalizationFacade,
) {
    fun getSupportedLocales(): List<SupportedLocaleResponse> = SupportedLocale.entries.map { SupportedLocaleResponse(it.code) }

    fun exportForAi(request: AiLocalizationExportRequest): DomainResult<AiLocalizationExportResponse, DomainFail> {
        val locales = request.locales.map { it.toSupportedLocale() }
        val validationErrors = validateExportRequest(request, locales)
        if (validationErrors.isNotEmpty()) return DomainResult.Fail(DomainFail.Validation(validationErrors))

        return when (request.entityType) {
            AiLocalizationExportEntityType.TEAM -> exportTeams(request, locales.filterNotNull())
            AiLocalizationExportEntityType.PLAYER -> exportPlayers(request, locales.filterNotNull())
        }
    }

    fun getAvailableLeagues(locale: SupportedLocale): DomainResult<List<CoreLocalizationResponse>, DomainFail> =
        when (val result = leagueFacade.getAvailableCoreLeagues()) {
            is DomainResult.Success -> {
                val localizations =
                    localizationFacade
                        .findLeagueLocalizations(result.value.map { it.uid }, listOf(locale))
                        .associateBy { it.coreUid }
                DomainResult.Success(result.value.map { it.toResponse(localizations[it.uid]) })
            }
            is DomainResult.Fail -> result
        }

    fun lookupLeague(
        uid: String?,
        apiId: Long?,
        locale: SupportedLocale,
    ): DomainResult<CoreLocalizationResponse, DomainFail> {
        val leagueResult =
            when {
                uid != null && apiId == null -> leagueFacade.findLeagueByUid(uid)
                uid == null && apiId != null -> leagueFacade.findLeagueByApiId(apiId)
                else -> {
                    return DomainResult.Fail(
                        DomainFail.Validation.single(
                            code = "INVALID_LEAGUE_LOOKUP",
                            message = "uid 또는 apiId 중 하나만 지정해야 합니다.",
                        ),
                    )
                }
            }
        return leagueResult.withLeagueLocalization { league ->
            localizationFacade.findLeagueLocalization(league.uid, locale)
        }
    }

    fun getTeams(
        leagueUid: String,
        locale: SupportedLocale,
    ): DomainResult<List<CoreLocalizationResponse>, DomainFail> =
        when (val result = leagueFacade.findTeamsByLeagueUid(leagueUid)) {
            is DomainResult.Success -> {
                val localizations =
                    localizationFacade
                        .findTeamLocalizations(result.value.map { it.uid }, listOf(locale))
                        .associateBy { it.coreUid }
                DomainResult.Success(result.value.map { it.toResponse(localizations[it.uid]) })
            }
            is DomainResult.Fail -> result
        }

    fun getPlayers(
        teamUid: String,
        locale: SupportedLocale,
    ): DomainResult<List<CoreLocalizationResponse>, DomainFail> =
        when (val result = leagueFacade.findPlayersByTeamUid(teamUid)) {
            is DomainResult.Success -> {
                val localizations =
                    localizationFacade
                        .findPlayerLocalizations(result.value.map { it.uid }, listOf(locale))
                        .associateBy { it.coreUid }
                DomainResult.Success(result.value.map { it.toResponse(localizations[it.uid]) })
            }
            is DomainResult.Fail -> result
        }

    fun updateLeagueLocalization(
        uid: String,
        locale: SupportedLocale,
        name: String?,
        shortName: String?,
    ): DomainResult<CoreLocalizationResponse, DomainFail> =
        when (val leagueResult = leagueFacade.findLeagueByUid(uid)) {
            is DomainResult.Success ->
                when (val localizationResult = localizationFacade.upsertLeagueLocalization(uid, locale, name, shortName)) {
                    is DomainResult.Success -> DomainResult.Success(leagueResult.value.toResponse(localizationResult.value.localization))
                    is DomainResult.Fail -> localizationResult
                }
            is DomainResult.Fail -> leagueResult
        }

    fun updateTeamLocalization(
        uid: String,
        locale: SupportedLocale,
        name: String?,
        shortName: String?,
    ): DomainResult<CoreLocalizationResponse, DomainFail> =
        when (val teamResult = leagueFacade.findTeamByUid(uid)) {
            is DomainResult.Success ->
                when (val localizationResult = localizationFacade.upsertTeamLocalization(uid, locale, name, shortName)) {
                    is DomainResult.Success -> DomainResult.Success(teamResult.value.toResponse(localizationResult.value.localization))
                    is DomainResult.Fail -> localizationResult
                }
            is DomainResult.Fail -> teamResult
        }

    fun updatePlayerLocalization(
        uid: String,
        locale: SupportedLocale,
        name: String?,
        shortName: String?,
    ): DomainResult<CoreLocalizationResponse, DomainFail> =
        when (val playerResult = leagueFacade.findPlayerByUid(uid)) {
            is DomainResult.Success ->
                when (val localizationResult = localizationFacade.upsertPlayerLocalization(uid, locale, name, shortName)) {
                    is DomainResult.Success -> DomainResult.Success(playerResult.value.toResponse(localizationResult.value.localization))
                    is DomainResult.Fail -> localizationResult
                }
            is DomainResult.Fail -> playerResult
        }

    private fun exportTeams(
        request: AiLocalizationExportRequest,
        locales: List<SupportedLocale>,
    ): DomainResult<AiLocalizationExportResponse, DomainFail> {
        return when (val leagueResult = leagueFacade.findLeagueByUid(request.leagueUid)) {
            is DomainResult.Success ->
                when (val teamsResult = leagueFacade.findTeamsByLeagueUid(request.leagueUid)) {
                    is DomainResult.Success -> {
                        val teamsByUid = teamsResult.value.associateBy { it.uid }
                        val missingUids = request.uids.filterNot(teamsByUid::containsKey)
                        if (missingUids.isNotEmpty()) return contextMismatch("TEAM_NOT_IN_LEAGUE", missingUids)
                        val teams = request.uids.map(teamsByUid::getValue)
                        val localizations =
                            localizationFacade
                                .findTeamLocalizations(request.uids, locales)
                                .associateBy { it.coreUid to it.locale }
                        DomainResult.Success(
                            AiLocalizationExportResponse(
                                locales = request.locales,
                                entityType = request.entityType,
                                context = AiLocalizationExportContext(leagueResult.value.toContextItem()),
                                items = teams.map { it.toExportItem(locales, localizations) },
                            ),
                        )
                    }
                    is DomainResult.Fail -> teamsResult
                }
            is DomainResult.Fail -> leagueResult
        }
    }

    private fun exportPlayers(
        request: AiLocalizationExportRequest,
        locales: List<SupportedLocale>,
    ): DomainResult<AiLocalizationExportResponse, DomainFail> {
        val teamUid = request.teamUid ?: return DomainResult.Fail(DomainFail.Validation.single("TEAM_UID_REQUIRED", "teamUid는 PLAYER export에 필요합니다."))
        return when (val leagueResult = leagueFacade.findLeagueByUid(request.leagueUid)) {
            is DomainResult.Success ->
                when (val teamsResult = leagueFacade.findTeamsByLeagueUid(request.leagueUid)) {
                    is DomainResult.Success -> {
                        if (teamsResult.value.none { it.uid == teamUid }) return contextMismatch("TEAM_NOT_IN_LEAGUE", listOf(teamUid))
                        when (val teamResult = leagueFacade.findTeamByUid(teamUid)) {
                            is DomainResult.Success ->
                                when (val playersResult = leagueFacade.findPlayersByTeamUid(teamUid)) {
                                    is DomainResult.Success -> {
                                        val playersByUid = playersResult.value.associateBy { it.uid }
                                        val missingUids = request.uids.filterNot(playersByUid::containsKey)
                                        if (missingUids.isNotEmpty()) return contextMismatch("PLAYER_NOT_IN_TEAM", missingUids)
                                        val players = request.uids.map(playersByUid::getValue)
                                        val localizations =
                                            localizationFacade
                                                .findPlayerLocalizations(request.uids, locales)
                                                .associateBy { it.coreUid to it.locale }
                                        DomainResult.Success(
                                            AiLocalizationExportResponse(
                                                locales = request.locales,
                                                entityType = request.entityType,
                                                context = AiLocalizationExportContext(leagueResult.value.toContextItem(), teamResult.value.toContextItem()),
                                                items = players.map { it.toExportItem(locales, localizations) },
                                            ),
                                        )
                                    }
                                    is DomainResult.Fail -> playersResult
                                }
                            is DomainResult.Fail -> teamResult
                        }
                    }
                    is DomainResult.Fail -> teamsResult
                }
            is DomainResult.Fail -> leagueResult
        }
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

    private fun contextMismatch(
        code: String,
        uids: List<String>,
    ): DomainResult.Fail<DomainFail.Validation> =
        DomainResult.Fail(
            DomainFail.Validation(uids.map { DomainFail.Validation.ValidationError(code, "현재 탐색 문맥에 포함되지 않은 UID입니다: $it", "uids") }),
        )

    private fun DomainResult<LeagueModel, DomainFail>.withLeagueLocalization(
        findLocalization: (LeagueModel) -> CoreLocalizationModel?,
    ): DomainResult<CoreLocalizationResponse, DomainFail> =
        when (this) {
            is DomainResult.Success -> DomainResult.Success(value.toResponse(findLocalization(value)))
            is DomainResult.Fail -> this
        }

    private fun LeagueModel.toResponse(localization: CoreLocalizationModel?): CoreLocalizationResponse = CoreLocalizationResponse(uid, name, localization?.toResponse())

    private fun TeamModel.toResponse(localization: CoreLocalizationModel?): CoreLocalizationResponse = CoreLocalizationResponse(uid, name, localization?.toResponse())

    private fun PlayerModel.toResponse(localization: CoreLocalizationModel?): CoreLocalizationResponse = CoreLocalizationResponse(uid, name, localization?.toResponse())

    private fun CoreLocalizationModel.toResponse(): LocalizationResponse = LocalizationResponse(name, shortName, aiGenerated)

    private fun String.toSupportedLocale(): SupportedLocale? = SupportedLocale.entries.find { it.code == this }

    private fun LeagueModel.toContextItem(): AiLocalizationExportContextItem = AiLocalizationExportContextItem(uid, name)

    private fun TeamModel.toContextItem(): AiLocalizationExportContextItem = AiLocalizationExportContextItem(uid, name)

    private fun TeamModel.toExportItem(
        locales: List<SupportedLocale>,
        localizations: Map<Pair<String, SupportedLocale>, CoreLocalizationModel>,
    ): AiLocalizationExportItem =
        AiLocalizationExportItem(
            uid = uid,
            originalName = name,
            localizations = locales.associate { locale ->
                locale.code to localizations[uid to locale].toExportValue()
            },
        )

    private fun PlayerModel.toExportItem(
        locales: List<SupportedLocale>,
        localizations: Map<Pair<String, SupportedLocale>, CoreLocalizationModel>,
    ): AiLocalizationExportItem =
        AiLocalizationExportItem(
            uid = uid,
            originalName = name,
            localizations = locales.associate { locale ->
                locale.code to localizations[uid to locale].toExportValue()
            },
        )

    private fun CoreLocalizationModel?.toExportValue(): AiLocalizationExportValue = AiLocalizationExportValue(this?.name, this?.shortName)
}
