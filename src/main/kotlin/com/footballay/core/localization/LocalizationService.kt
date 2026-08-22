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
        leagueLocalizationRepository.findAllByCoreUidInAndLocaleIn(coreUids, locales).map(::toModel)

    fun findTeamLocalizations(
        coreUids: Collection<String>,
        locales: Collection<SupportedLocale>,
    ): List<CoreLocalizationModel> =
        teamLocalizationRepository.findAllByCoreUidInAndLocaleIn(coreUids, locales).map(::toModel)

    fun findPlayerLocalizations(
        coreUids: Collection<String>,
        locales: Collection<SupportedLocale>,
    ): List<CoreLocalizationModel> =
        playerLocalizationRepository.findAllByCoreUidInAndLocaleIn(coreUids, locales).map(::toModel)

    fun findLeagueLocalization(
        coreUid: String,
        locale: SupportedLocale,
    ): CoreLocalizationModel? =
        leagueLocalizationRepository.findByCoreUidAndLocale(coreUid, locale)?.let(::toModel)

    fun findTeamLocalization(
        coreUid: String,
        locale: SupportedLocale,
    ): CoreLocalizationModel? =
        teamLocalizationRepository.findByCoreUidAndLocale(coreUid, locale)?.let(::toModel)

    fun findPlayerLocalization(
        coreUid: String,
        locale: SupportedLocale,
    ): CoreLocalizationModel? =
        playerLocalizationRepository.findByCoreUidAndLocale(coreUid, locale)?.let(::toModel)

    fun upsertLeagueLocalization(
        coreUid: String,
        locale: SupportedLocale,
        name: String?,
        shortName: String?,
    ): DomainResult<LocalizationUpsertResult, DomainFail> {
        val core =
            leagueCoreRepository.findByUid(coreUid)
                ?: return DomainResult.Fail(DomainFail.NotFound("LeagueCore", coreUid))
        val existing = leagueLocalizationRepository.findByCoreUidAndLocale(coreUid, locale)
        if (existing != null) {
            if (name == null && shortName == null) return DomainResult.Success(LocalizationUpsertResult(toModel(existing)))
            name?.let { existing.name = it }
            shortName?.let { existing.shortName = it }
            existing.aiGenerated = false
            return DomainResult.Success(LocalizationUpsertResult(toModel(leagueLocalizationRepository.save(existing))))
        }
        if (name == null && shortName == null) return DomainResult.Success(LocalizationUpsertResult(null))
        return DomainResult.Success(
            LocalizationUpsertResult(
                toModel(
                    leagueLocalizationRepository.save(
                        LeagueCoreLocalization(leagueCore = core, locale = locale, name = name, shortName = shortName),
                    ),
                ),
            ),
        )
    }

    fun upsertTeamLocalization(
        coreUid: String,
        locale: SupportedLocale,
        name: String?,
        shortName: String?,
    ): DomainResult<LocalizationUpsertResult, DomainFail> {
        val core =
            teamCoreRepository.findByUid(coreUid)
                ?: return DomainResult.Fail(DomainFail.NotFound("TeamCore", coreUid))
        val existing = teamLocalizationRepository.findByCoreUidAndLocale(coreUid, locale)
        if (existing != null) {
            if (name == null && shortName == null) return DomainResult.Success(LocalizationUpsertResult(toModel(existing)))
            name?.let { existing.name = it }
            shortName?.let { existing.shortName = it }
            existing.aiGenerated = false
            return DomainResult.Success(LocalizationUpsertResult(toModel(teamLocalizationRepository.save(existing))))
        }
        if (name == null && shortName == null) return DomainResult.Success(LocalizationUpsertResult(null))
        return DomainResult.Success(
            LocalizationUpsertResult(
                toModel(
                    teamLocalizationRepository.save(
                        TeamCoreLocalization(teamCore = core, locale = locale, name = name, shortName = shortName),
                    ),
                ),
            ),
        )
    }

    fun upsertPlayerLocalization(
        coreUid: String,
        locale: SupportedLocale,
        name: String?,
        shortName: String?,
    ): DomainResult<LocalizationUpsertResult, DomainFail> {
        val core =
            playerCoreRepository.findByUid(coreUid)
                ?: return DomainResult.Fail(DomainFail.NotFound("PlayerCore", coreUid))
        val existing = playerLocalizationRepository.findByCoreUidAndLocale(coreUid, locale)
        if (existing != null) {
            if (name == null && shortName == null) return DomainResult.Success(LocalizationUpsertResult(toModel(existing)))
            name?.let { existing.name = it }
            shortName?.let { existing.shortName = it }
            existing.aiGenerated = false
            return DomainResult.Success(LocalizationUpsertResult(toModel(playerLocalizationRepository.save(existing))))
        }
        if (name == null && shortName == null) return DomainResult.Success(LocalizationUpsertResult(null))
        return DomainResult.Success(
            LocalizationUpsertResult(
                toModel(
                    playerLocalizationRepository.save(
                        PlayerCoreLocalization(playerCore = core, locale = locale, name = name, shortName = shortName),
                    ),
                ),
            ),
        )
    }

    private fun toModel(localization: LeagueCoreLocalization): CoreLocalizationModel =
        CoreLocalizationModel(
            coreUid = localization.leagueCore.uid,
            locale = localization.locale,
            name = localization.name,
            shortName = localization.shortName,
            aiGenerated = localization.aiGenerated,
        )

    private fun toModel(localization: TeamCoreLocalization): CoreLocalizationModel =
        CoreLocalizationModel(
            coreUid = localization.teamCore.uid,
            locale = localization.locale,
            name = localization.name,
            shortName = localization.shortName,
            aiGenerated = localization.aiGenerated,
        )

    private fun toModel(localization: PlayerCoreLocalization): CoreLocalizationModel =
        CoreLocalizationModel(
            coreUid = localization.playerCore.uid,
            locale = localization.locale,
            name = localization.name,
            shortName = localization.shortName,
            aiGenerated = localization.aiGenerated,
        )
}
