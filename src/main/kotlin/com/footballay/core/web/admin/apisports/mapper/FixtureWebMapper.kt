package com.footballay.core.web.admin.apisports.mapper

import com.footballay.core.domain.model.FixtureModel
import com.footballay.core.web.admin.apisports.dto.FixtureSummaryDto

object FixtureWebMapper {
    fun toSummaryDto(model: FixtureModel): FixtureSummaryDto =
        FixtureSummaryDto(
            uid = model.uid,
            kickoffAt = model.kickoffAt.toString(),
            homeTeam = model.homeTeam.name,
            awayTeam = model.awayTeam.name,
            status = model.status,
            available = model.available,
        )
}
