package com.footballay.core.localization

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/** Core localization 유즈케이스의 transaction 경계입니다. */
@Component
class LocalizationFacade(
    private val localizationService: LocalizationService,
    private val aiLocalizationService: AiLocalizationService,
) {
    @Transactional(readOnly = true)
    fun findLeagueLocalizations(
        coreUids: Collection<String>,
        locales: Collection<SupportedLocale>,
    ): List<CoreLocalizationModel> = localizationService.findLeagueLocalizations(coreUids, locales)

    @Transactional(readOnly = true)
    fun findTeamLocalizations(
        coreUids: Collection<String>,
        locales: Collection<SupportedLocale>,
    ): List<CoreLocalizationModel> = localizationService.findTeamLocalizations(coreUids, locales)

    @Transactional(readOnly = true)
    fun findPlayerLocalizations(
        coreUids: Collection<String>,
        locales: Collection<SupportedLocale>,
    ): List<CoreLocalizationModel> = localizationService.findPlayerLocalizations(coreUids, locales)

    @Transactional(readOnly = true)
    fun findLeagueLocalization(
        coreUid: String,
        locale: SupportedLocale,
    ): CoreLocalizationModel? = localizationService.findLeagueLocalization(coreUid, locale)

    @Transactional(readOnly = true)
    fun findTeamLocalization(
        coreUid: String,
        locale: SupportedLocale,
    ): CoreLocalizationModel? = localizationService.findTeamLocalization(coreUid, locale)

    @Transactional(readOnly = true)
    fun findPlayerLocalization(
        coreUid: String,
        locale: SupportedLocale,
    ): CoreLocalizationModel? = localizationService.findPlayerLocalization(coreUid, locale)

    @Transactional
    fun upsertLeagueLocalization(
        coreUid: String,
        locale: SupportedLocale,
        name: String?,
        shortName: String?,
    ): DomainResult<LocalizationUpsertResult, DomainFail> =
        localizationService.upsertLeagueLocalization(coreUid, locale, name, shortName)

    @Transactional
    fun upsertTeamLocalization(
        coreUid: String,
        locale: SupportedLocale,
        name: String?,
        shortName: String?,
    ): DomainResult<LocalizationUpsertResult, DomainFail> =
        localizationService.upsertTeamLocalization(coreUid, locale, name, shortName)

    @Transactional
    fun upsertPlayerLocalization(
        coreUid: String,
        locale: SupportedLocale,
        name: String?,
        shortName: String?,
    ): DomainResult<LocalizationUpsertResult, DomainFail> =
        localizationService.upsertPlayerLocalization(coreUid, locale, name, shortName)

    @Transactional
    fun applyAiTeamLocalizations(updates: List<AiLocalizationUpdate>): AiLocalizationApplyResult =
        aiLocalizationService.applyTeamLocalizations(updates)

    @Transactional
    fun applyAiPlayerLocalizations(updates: List<AiLocalizationUpdate>): AiLocalizationApplyResult =
        aiLocalizationService.applyPlayerLocalizations(updates)
}
