package com.footballay.core.localization

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.infra.persistence.core.entity.LeagueCoreLocalization
import com.footballay.core.infra.persistence.core.entity.PlayerCoreLocalization
import com.footballay.core.infra.persistence.core.entity.TeamCoreLocalization
import com.footballay.core.infra.persistence.core.repository.LeagueCoreLocalizationRepository
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.persistence.core.repository.PlayerCoreLocalizationRepository
import com.footballay.core.infra.persistence.core.repository.PlayerCoreRepository
import com.footballay.core.infra.persistence.core.repository.TeamCoreLocalizationRepository
import com.footballay.core.infra.persistence.core.repository.TeamCoreRepository
import org.springframework.stereotype.Service

/** Core localization의 조회와 수동 저장을 처리합니다. */
@Service
class LocalizationService(
    private val leagueCoreRepository: LeagueCoreRepository,
    private val teamCoreRepository: TeamCoreRepository,
    private val playerCoreRepository: PlayerCoreRepository,
    private val leagueLocalizationRepository: LeagueCoreLocalizationRepository,
    private val teamLocalizationRepository: TeamCoreLocalizationRepository,
    private val playerLocalizationRepository: PlayerCoreLocalizationRepository,
) {
    fun findLeagueLocalizations(
        coreUids: Collection<String>,
        locales: Collection<SupportedLocale>,
    ): List<CoreLocalizationModel> =
        leagueLocalizationRepository
            .findAllByCoreUidInAndLocaleIn(coreUids, locales)
            .map { it.toModel() }

    fun findTeamLocalizations(
        coreUids: Collection<String>,
        locales: Collection<SupportedLocale>,
    ): List<CoreLocalizationModel> =
        teamLocalizationRepository
            .findAllByCoreUidInAndLocaleIn(coreUids, locales)
            .map { it.toModel() }

    fun findPlayerLocalizations(
        coreUids: Collection<String>,
        locales: Collection<SupportedLocale>,
    ): List<CoreLocalizationModel> =
        playerLocalizationRepository
            .findAllByCoreUidInAndLocaleIn(coreUids, locales)
            .map { it.toModel() }

    fun findLeagueLocalization(coreUid: String, locale: SupportedLocale): CoreLocalizationModel? =
        leagueLocalizationRepository.findByCoreUidAndLocale(coreUid, locale)?.toModel()

    fun findTeamLocalization(coreUid: String, locale: SupportedLocale): CoreLocalizationModel? =
        teamLocalizationRepository.findByCoreUidAndLocale(coreUid, locale)?.toModel()

    fun findPlayerLocalization(coreUid: String, locale: SupportedLocale): CoreLocalizationModel? =
        playerLocalizationRepository.findByCoreUidAndLocale(coreUid, locale)?.toModel()

    fun upsertLeagueLocalization(
        coreUid: String,
        locale: SupportedLocale,
        name: String?,
        shortName: String?,
    ): DomainResult<LocalizationUpsertResult, DomainFail> {
        val core = leagueCoreRepository.findByUid(coreUid) ?: return notFound("LeagueCore", coreUid)
        val existing = leagueLocalizationRepository.findByCoreUidAndLocale(coreUid, locale)
        if (existing == null) {
            if (name == null && shortName == null) return success(null)
            return success(
                leagueLocalizationRepository.save(
                    LeagueCoreLocalization(leagueCore = core, locale = locale, name = name, shortName = shortName),
                ).toModel(),
            )
        }

        val nextName = name ?: existing.name
        val nextShortName = shortName ?: existing.shortName
        if (nextName == existing.name && nextShortName == existing.shortName) return success(existing.toModel())

        existing.name = nextName
        existing.shortName = nextShortName
        existing.aiGenerated = false
        return success(leagueLocalizationRepository.save(existing).toModel())
    }

    fun upsertTeamLocalization(
        coreUid: String,
        locale: SupportedLocale,
        name: String?,
        shortName: String?,
    ): DomainResult<LocalizationUpsertResult, DomainFail> {
        val core = teamCoreRepository.findByUid(coreUid) ?: return notFound("TeamCore", coreUid)
        val existing = teamLocalizationRepository.findByCoreUidAndLocale(coreUid, locale)
        if (existing == null) {
            if (name == null && shortName == null) return success(null)
            return success(
                teamLocalizationRepository.save(
                    TeamCoreLocalization(teamCore = core, locale = locale, name = name, shortName = shortName),
                ).toModel(),
            )
        }

        val nextName = name ?: existing.name
        val nextShortName = shortName ?: existing.shortName
        if (nextName == existing.name && nextShortName == existing.shortName) return success(existing.toModel())

        existing.name = nextName
        existing.shortName = nextShortName
        existing.aiGenerated = false
        return success(teamLocalizationRepository.save(existing).toModel())
    }

    fun upsertPlayerLocalization(
        coreUid: String,
        locale: SupportedLocale,
        name: String?,
        shortName: String?,
    ): DomainResult<LocalizationUpsertResult, DomainFail> {
        val core = playerCoreRepository.findByUid(coreUid) ?: return notFound("PlayerCore", coreUid)
        val existing = playerLocalizationRepository.findByCoreUidAndLocale(coreUid, locale)
        if (existing == null) {
            if (name == null && shortName == null) return success(null)
            return success(
                playerLocalizationRepository.save(
                    PlayerCoreLocalization(playerCore = core, locale = locale, name = name, shortName = shortName),
                ).toModel(),
            )
        }

        val nextName = name ?: existing.name
        val nextShortName = shortName ?: existing.shortName
        if (nextName == existing.name && nextShortName == existing.shortName) return success(existing.toModel())

        existing.name = nextName
        existing.shortName = nextShortName
        existing.aiGenerated = false
        return success(playerLocalizationRepository.save(existing).toModel())
    }

    private fun success(localization: CoreLocalizationModel?): DomainResult<LocalizationUpsertResult, DomainFail> =
        DomainResult.Success(LocalizationUpsertResult(localization))

    private fun notFound(type: String, uid: String): DomainResult<LocalizationUpsertResult, DomainFail> =
        DomainResult.Fail(DomainFail.NotFound(type, uid))
}
