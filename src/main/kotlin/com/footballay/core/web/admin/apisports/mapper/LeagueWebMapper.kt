package com.footballay.core.web.admin.apisports.mapper

import com.footballay.core.web.admin.apisports.dto.AvailableLeagueDto

object LeagueWebMapper {
    fun toAvailableDto(
        id: Long,
        name: String,
    ): AvailableLeagueDto =
        AvailableLeagueDto(
            leagueId = id,
            name = name,
        )
}
