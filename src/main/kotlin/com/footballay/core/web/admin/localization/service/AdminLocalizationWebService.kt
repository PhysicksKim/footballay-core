package com.footballay.core.web.admin.localization.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.LeagueFacade
import com.footballay.core.localization.LocalizationFacade
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.admin.localization.dto.CoreLocalizationResponse
import com.footballay.core.web.admin.localization.dto.SupportedLocaleResponse
import com.footballay.core.web.admin.localization.mapper.AdminLocalizationMapper
import org.springframework.stereotype.Service

/** Admin localization 조회와 수동 변경을 처리합니다. */
@Service
class AdminLocalizationWebService(
    private val leagueFacade: LeagueFacade,
    private val localizationFacade: LocalizationFacade,
) {
    fun getSupportedLocales(): List<SupportedLocaleResponse> = SupportedLocale.entries.map(AdminLocalizationMapper::toSupportedLocaleResponse)

    fun getAvailableLeagues(locale: SupportedLocale): DomainResult<List<CoreLocalizationResponse>, DomainFail> {
        val leagues =
            when (val result = leagueFacade.getAvailableCoreLeagues()) {
                is DomainResult.Success -> result.value
                is DomainResult.Fail -> return result
            }
        val localizations =
            localizationFacade
                .findLeagueLocalizations(
                    leagues.map { it.uid },
                    listOf(locale),
                ).associateBy { it.coreUid }
        return DomainResult.Success(leagues.map { AdminLocalizationMapper.toResponse(it, localizations[it.uid]) })
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

                else -> return DomainResult.Fail(
                    DomainFail.Validation.single(
                        "INVALID_LEAGUE_LOOKUP",
                        "uid 또는 apiId 중 하나만 지정해야 합니다.",
                    ),
                )
            }
        val league =
            when (leagueResult) {
                is DomainResult.Success -> leagueResult.value
                is DomainResult.Fail -> return leagueResult
            }
        return DomainResult.Success(
            AdminLocalizationMapper.toResponse(
                league,
                localizationFacade.findLeagueLocalization(league.uid, locale),
            ),
        )
    }

    fun getTeams(
        leagueUid: String,
        locale: SupportedLocale,
    ): DomainResult<List<CoreLocalizationResponse>, DomainFail> {
        val teams =
            when (val result = leagueFacade.findTeamsByLeagueUid(leagueUid)) {
                is DomainResult.Success -> result.value
                is DomainResult.Fail -> return result
            }
        val localizations =
            localizationFacade.findTeamLocalizations(teams.map { it.uid }, listOf(locale)).associateBy { it.coreUid }
        return DomainResult.Success(teams.map { AdminLocalizationMapper.toResponse(it, localizations[it.uid]) })
    }

    fun getPlayers(
        teamUid: String,
        locale: SupportedLocale,
    ): DomainResult<List<CoreLocalizationResponse>, DomainFail> {
        val players =
            when (val result = leagueFacade.findPlayersByTeamUid(teamUid)) {
                is DomainResult.Success -> result.value
                is DomainResult.Fail -> return result
            }
        val localizations =
            localizationFacade
                .findPlayerLocalizations(players.map { it.uid }, listOf(locale))
                .associateBy { it.coreUid }
        return DomainResult.Success(players.map { AdminLocalizationMapper.toResponse(it, localizations[it.uid]) })
    }

    fun updateLeagueLocalization(
        uid: String,
        locale: SupportedLocale,
        name: String?,
        shortName: String?,
    ): DomainResult<CoreLocalizationResponse, DomainFail> {
        val league =
            when (val result = leagueFacade.findLeagueByUid(uid)) {
                is DomainResult.Success -> result.value
                is DomainResult.Fail -> return result
            }
        val localization =
            when (val result = localizationFacade.upsertLeagueLocalization(uid, locale, name, shortName)) {
                is DomainResult.Success -> result.value.localization
                is DomainResult.Fail -> return result
            }
        return DomainResult.Success(AdminLocalizationMapper.toResponse(league, localization))
    }

    fun updateTeamLocalization(
        uid: String,
        locale: SupportedLocale,
        name: String?,
        shortName: String?,
    ): DomainResult<CoreLocalizationResponse, DomainFail> {
        val team =
            when (val result = leagueFacade.findTeamByUid(uid)) {
                is DomainResult.Success -> result.value
                is DomainResult.Fail -> return result
            }
        val localization =
            when (val result = localizationFacade.upsertTeamLocalization(uid, locale, name, shortName)) {
                is DomainResult.Success -> result.value.localization
                is DomainResult.Fail -> return result
            }
        return DomainResult.Success(AdminLocalizationMapper.toResponse(team, localization))
    }

    fun updatePlayerLocalization(
        uid: String,
        locale: SupportedLocale,
        name: String?,
        shortName: String?,
    ): DomainResult<CoreLocalizationResponse, DomainFail> {
        val player =
            when (val result = leagueFacade.findPlayerByUid(uid)) {
                is DomainResult.Success -> result.value
                is DomainResult.Fail -> return result
            }
        val localization =
            when (val result = localizationFacade.upsertPlayerLocalization(uid, locale, name, shortName)) {
                is DomainResult.Success -> result.value.localization
                is DomainResult.Fail -> return result
            }
        return DomainResult.Success(AdminLocalizationMapper.toResponse(player, localization))
    }
}
