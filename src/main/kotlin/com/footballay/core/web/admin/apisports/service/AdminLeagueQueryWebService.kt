package com.footballay.core.web.admin.apisports.service

import com.footballay.core.web.admin.apisports.dto.AvailableLeagueDto

interface AdminLeagueQueryWebService {
    fun findAvailableLeagues(): List<AvailableLeagueDto>
}
