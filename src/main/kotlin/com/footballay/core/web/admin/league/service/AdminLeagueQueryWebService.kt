package com.footballay.core.web.admin.league.service

import com.footballay.core.web.admin.league.dto.AvailableLeagueDto

interface AdminLeagueQueryWebService {
    fun findAvailableLeagues(): List<AvailableLeagueDto>
}


