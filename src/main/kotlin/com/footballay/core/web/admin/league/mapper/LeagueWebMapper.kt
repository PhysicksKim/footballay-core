package com.footballay.core.web.admin.league.mapper

import com.footballay.core.web.admin.league.dto.AvailableLeagueDto

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
