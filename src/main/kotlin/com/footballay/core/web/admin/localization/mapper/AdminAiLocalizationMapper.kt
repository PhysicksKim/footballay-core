package com.footballay.core.web.admin.localization.mapper

import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.domain.model.PlayerModel
import com.footballay.core.domain.model.TeamModel
import com.footballay.core.localization.AiLocalizationApplyResult
import com.footballay.core.localization.CoreLocalizationModel
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportContext
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportContextItem
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportItem
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportRequest
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportResponse
import com.footballay.core.web.admin.localization.dto.AiLocalizationExportValue
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportChangeResponse
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportResponse
import com.footballay.core.web.admin.localization.dto.AiLocalizationImportValueResponse

object AdminAiLocalizationMapper {
    fun toTeamExportResponse(
        request: AiLocalizationExportRequest,
        league: LeagueModel,
        teams: List<TeamModel>,
        locales: List<SupportedLocale>,
        localizations: Map<Pair<String, SupportedLocale>, CoreLocalizationModel>,
    ): AiLocalizationExportResponse =
        AiLocalizationExportResponse(
            locales = request.locales,
            entityType = request.entityType,
            context = AiLocalizationExportContext(league.toContextItem()),
            items = teams.map { it.toExportItem(locales, localizations) },
        )

    fun toPlayerExportResponse(
        request: AiLocalizationExportRequest,
        league: LeagueModel,
        team: TeamModel,
        players: List<PlayerModel>,
        locales: List<SupportedLocale>,
        localizations: Map<Pair<String, SupportedLocale>, CoreLocalizationModel>,
    ): AiLocalizationExportResponse =
        AiLocalizationExportResponse(
            locales = request.locales,
            entityType = request.entityType,
            context = AiLocalizationExportContext(league.toContextItem(), team.toContextItem()),
            items = players.map { it.toExportItem(locales, localizations) },
        )

    fun toImportResponse(result: AiLocalizationApplyResult): AiLocalizationImportResponse =
        AiLocalizationImportResponse(
            updatedCount = result.updatedCount,
            unchangedCount = result.unchangedCount,
            changes = result.changes.map { change ->
                AiLocalizationImportChangeResponse(
                    uid = change.coreUid,
                    locale = change.locale.code,
                    before = change.before?.toImportValueResponse(),
                    after = change.after.toImportValueResponse(),
                )
            },
        )

    private fun LeagueModel.toContextItem(): AiLocalizationExportContextItem = AiLocalizationExportContextItem(uid, name)

    private fun TeamModel.toContextItem(): AiLocalizationExportContextItem = AiLocalizationExportContextItem(uid, name)

    private fun TeamModel.toExportItem(
        locales: List<SupportedLocale>,
        localizations: Map<Pair<String, SupportedLocale>, CoreLocalizationModel>,
    ): AiLocalizationExportItem =
        AiLocalizationExportItem(uid, name, locales.associate { locale -> locale.code to localizations[uid to locale].toExportValue() })

    private fun PlayerModel.toExportItem(
        locales: List<SupportedLocale>,
        localizations: Map<Pair<String, SupportedLocale>, CoreLocalizationModel>,
    ): AiLocalizationExportItem =
        AiLocalizationExportItem(uid, name, locales.associate { locale -> locale.code to localizations[uid to locale].toExportValue() })

    private fun CoreLocalizationModel?.toExportValue(): AiLocalizationExportValue = AiLocalizationExportValue(this?.name, this?.shortName)

    private fun CoreLocalizationModel.toImportValueResponse(): AiLocalizationImportValueResponse =
        AiLocalizationImportValueResponse(name, shortName, aiGenerated)
}
