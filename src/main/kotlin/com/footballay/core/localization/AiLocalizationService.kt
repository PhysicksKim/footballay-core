package com.footballay.core.localization

import com.footballay.core.infra.persistence.core.entity.PlayerCoreLocalization
import com.footballay.core.infra.persistence.core.entity.TeamCoreLocalization
import com.footballay.core.infra.persistence.core.repository.PlayerCoreLocalizationRepository
import com.footballay.core.infra.persistence.core.repository.PlayerCoreRepository
import com.footballay.core.infra.persistence.core.repository.TeamCoreLocalizationRepository
import com.footballay.core.infra.persistence.core.repository.TeamCoreRepository
import org.springframework.stereotype.Service

/** AI가 생성한 localization의 일괄 적용을 처리합니다. */
@Service
class AiLocalizationService(
    private val teamCoreRepository: TeamCoreRepository,
    private val playerCoreRepository: PlayerCoreRepository,
    private val teamLocalizationRepository: TeamCoreLocalizationRepository,
    private val playerLocalizationRepository: PlayerCoreLocalizationRepository,
) {
    fun applyTeamLocalizations(updates: List<AiLocalizationUpdate>): AiLocalizationApplyResult {
        val existingByKey = teamLocalizationRepository
            .findAllByCoreUidInAndLocaleIn(updates.map { it.coreUid }.toSet(), updates.map { it.locale }.toSet())
            .associateBy { it.teamCore.uid to it.locale }
        val changes = mutableListOf<AiLocalizationApplyChange>()
        val created = mutableListOf<TeamCoreLocalization>()
        var unchangedCount = 0

        for (update in updates) {
            val existing = existingByKey[update.coreUid to update.locale]
            if (existing == null) {
                if (update.name == null && update.shortName == null) {
                    unchangedCount++
                    continue
                }
                val core = requireNotNull(teamCoreRepository.findByUid(update.coreUid)) { "TeamCore not found: ${update.coreUid}" }
                val localization = TeamCoreLocalization(teamCore = core, locale = update.locale, name = update.name, shortName = update.shortName, aiGenerated = true)
                created += localization
                changes += AiLocalizationApplyChange(update.coreUid, update.locale, null, localization.toModel())
                continue
            }

            val nextName = update.name ?: existing.name
            val nextShortName = update.shortName ?: existing.shortName
            if (nextName == existing.name && nextShortName == existing.shortName) {
                unchangedCount++
                continue
            }
            val before = existing.toModel()
            existing.name = nextName
            existing.shortName = nextShortName
            existing.aiGenerated = true
            changes += AiLocalizationApplyChange(update.coreUid, update.locale, before, existing.toModel())
        }

        if (created.isNotEmpty()) teamLocalizationRepository.saveAll(created)
        return AiLocalizationApplyResult(changes.size, unchangedCount, changes)
    }

    fun applyPlayerLocalizations(updates: List<AiLocalizationUpdate>): AiLocalizationApplyResult {
        val existingByKey = playerLocalizationRepository
            .findAllByCoreUidInAndLocaleIn(updates.map { it.coreUid }.toSet(), updates.map { it.locale }.toSet())
            .associateBy { it.playerCore.uid to it.locale }
        val changes = mutableListOf<AiLocalizationApplyChange>()
        val created = mutableListOf<PlayerCoreLocalization>()
        var unchangedCount = 0

        for (update in updates) {
            val existing = existingByKey[update.coreUid to update.locale]
            if (existing == null) {
                if (update.name == null && update.shortName == null) {
                    unchangedCount++
                    continue
                }
                val core = requireNotNull(playerCoreRepository.findByUid(update.coreUid)) { "PlayerCore not found: ${update.coreUid}" }
                val localization = PlayerCoreLocalization(playerCore = core, locale = update.locale, name = update.name, shortName = update.shortName, aiGenerated = true)
                created += localization
                changes += AiLocalizationApplyChange(update.coreUid, update.locale, null, localization.toModel())
                continue
            }

            val nextName = update.name ?: existing.name
            val nextShortName = update.shortName ?: existing.shortName
            if (nextName == existing.name && nextShortName == existing.shortName) {
                unchangedCount++
                continue
            }
            val before = existing.toModel()
            existing.name = nextName
            existing.shortName = nextShortName
            existing.aiGenerated = true
            changes += AiLocalizationApplyChange(update.coreUid, update.locale, before, existing.toModel())
        }

        if (created.isNotEmpty()) playerLocalizationRepository.saveAll(created)
        return AiLocalizationApplyResult(changes.size, unchangedCount, changes)
    }
}
