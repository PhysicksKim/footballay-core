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
}
