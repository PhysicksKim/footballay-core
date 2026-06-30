package com.footballay.core.web.admin.apisports.mapper

import com.footballay.core.domain.model.FixtureApiSportsExtension
import com.footballay.core.domain.model.FixtureModel
import com.footballay.core.web.admin.apisports.dto.FixtureSummaryDto

object FixtureWebMapper {
    fun toSummaryDto(model: FixtureModel): FixtureSummaryDto =
        FixtureSummaryDto(
            uid = model.uid,
            kickoffAt = model.schedule.kickoffAt.toString(),
            home =
                if (model.homeTeam != null) {
                    FixtureSummaryDto.TeamDto(
                        name = model.homeTeam.name,
                        nameKo = model.homeTeam.nameKo,
                        logo = model.homeTeam.logo,
                    )
                } else {
                    null
                },
            away =
                if (model.awayTeam != null) {
                    FixtureSummaryDto.TeamDto(
                        name = model.awayTeam.name,
                        nameKo = model.awayTeam.nameKo,
                        logo = model.awayTeam.logo,
                    )
                } else {
                    null
                },
            status = model.status.code.value,
            statusText = model.status.statusText,
            available = model.available,
            apiId = if (model.extension is FixtureApiSportsExtension) model.extension.apiId else null,
        )
}
