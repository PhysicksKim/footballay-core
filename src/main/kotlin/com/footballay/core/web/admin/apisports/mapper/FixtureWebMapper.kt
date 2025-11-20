package com.footballay.core.web.admin.apisports.mapper

import com.footballay.core.domain.model.FixtureModel
import com.footballay.core.web.admin.apisports.dto.FixtureSummaryDto

object FixtureWebMapper {
    fun toSummaryDto(model: FixtureModel): FixtureSummaryDto =
        FixtureSummaryDto(
            uid = model.uid,
            kickoffAt = model.kickoffAt.toString(),
            home =
                FixtureSummaryDto.TeamDto(
                    name = model.homeTeam.name,
                    nameKo = model.homeTeam.nameKo,
                    logo = model.homeTeam.logo,
                ),
            away =
                FixtureSummaryDto.TeamDto(
                    name = model.awayTeam.name,
                    nameKo = model.awayTeam.nameKo,
                    logo = model.awayTeam.logo,
                ),
            status = model.status,
            available = model.available,
        )
}
