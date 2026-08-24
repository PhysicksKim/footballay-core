package com.footballay.core.localization

import com.footballay.core.infra.persistence.core.entity.LeagueCoreLocalization
import com.footballay.core.infra.persistence.core.entity.PlayerCoreLocalization
import com.footballay.core.infra.persistence.core.entity.TeamCoreLocalization

internal fun LeagueCoreLocalization.toModel() =
    CoreLocalizationModel(
        coreUid = leagueCore.uid,
        locale = locale,
        name = name,
        shortName = shortName,
        aiGenerated = aiGenerated,
    )

internal fun TeamCoreLocalization.toModel() =
    CoreLocalizationModel(
        coreUid = teamCore.uid,
        locale = locale,
        name = name,
        shortName = shortName,
        aiGenerated = aiGenerated,
    )

internal fun PlayerCoreLocalization.toModel() =
    CoreLocalizationModel(
        coreUid = playerCore.uid,
        locale = locale,
        name = name,
        shortName = shortName,
        aiGenerated = aiGenerated,
    )
