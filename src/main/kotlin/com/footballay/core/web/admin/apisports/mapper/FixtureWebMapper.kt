package com.footballay.core.web.admin.apisports.mapper

import com.footballay.core.admin.apisports.query.model.AdminApiSportsFixtureSummaryView
import com.footballay.core.web.admin.apisports.dto.FixtureSummaryDto

object FixtureWebMapper {
    fun toSummaryDto(model: AdminApiSportsFixtureSummaryView): FixtureSummaryDto =
        FixtureSummaryDto(
            uid = model.uid,
            kickoffAt = model.kickoffAt.toString(),
            home =
                if (model.home != null) {
                    FixtureSummaryDto.TeamDto(
                        name = model.home.name,
                        nameKo = model.home.nameKo,
                        logo = model.home.logo,
                    )
                } else {
                    null
                },
            away =
                if (model.away != null) {
                    FixtureSummaryDto.TeamDto(
                        name = model.away.name,
                        nameKo = model.away.nameKo,
                        logo = model.away.logo,
                    )
                } else {
                    null
                },
            status = model.status,
            statusText = model.statusText,
            available = model.available,
            apiId = model.apiId,
        )
}
