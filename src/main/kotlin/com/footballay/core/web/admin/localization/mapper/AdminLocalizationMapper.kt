package com.footballay.core.web.admin.localization.mapper

import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.domain.model.PlayerModel
import com.footballay.core.domain.model.TeamModel
import com.footballay.core.localization.CoreLocalizationModel
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.admin.localization.dto.CoreLocalizationResponse
import com.footballay.core.web.admin.localization.dto.LocalizationResponse
import com.footballay.core.web.admin.localization.dto.SupportedLocaleResponse

object AdminLocalizationMapper {
    fun toSupportedLocaleResponse(locale: SupportedLocale): SupportedLocaleResponse = SupportedLocaleResponse(locale.code)

    fun toResponse(model: LeagueModel, localization: CoreLocalizationModel?): CoreLocalizationResponse = toResponse(model.uid, model.name, localization)

    fun toResponse(model: TeamModel, localization: CoreLocalizationModel?): CoreLocalizationResponse = toResponse(model.uid, model.name, localization)

    fun toResponse(model: PlayerModel, localization: CoreLocalizationModel?): CoreLocalizationResponse = toResponse(model.uid, model.name, localization)

    private fun toResponse(uid: String, originalName: String, localization: CoreLocalizationModel?): CoreLocalizationResponse =
        CoreLocalizationResponse(uid, originalName, localization?.let(::toLocalizationResponse))

    private fun toLocalizationResponse(localization: CoreLocalizationModel): LocalizationResponse =
        LocalizationResponse(localization.name, localization.shortName, localization.aiGenerated)
}
