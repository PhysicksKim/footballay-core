package com.footballay.core.web.admin.fixture.mapper

import com.footballay.core.domain.fixture.model.FixtureModel
import com.footballay.core.web.admin.fixture.dto.FixtureSummaryDto

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
