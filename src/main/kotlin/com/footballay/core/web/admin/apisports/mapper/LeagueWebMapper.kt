package com.footballay.core.web.admin.apisports.mapper

import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.web.admin.apisports.dto.AvailableLeagueDto

object LeagueWebMapper {
    fun toAvailableDto(
        core: LeagueCore,
        apiSports: LeagueApiSports,
    ): AvailableLeagueDto =
        AvailableLeagueDto(
            photo = apiSports.logo,
            uid = core.uid,
            name = core.name,
            apiSports =
                AvailableLeagueDto.LeagueApiSportsDto(
                    apiId = apiSports.apiId,
                ),
        )
}
