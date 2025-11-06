package com.footballay.core.web.admin.league.service

import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.web.admin.league.dto.AvailableLeagueDto
import com.footballay.core.web.admin.league.mapper.LeagueWebMapper
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class AdminLeagueQueryWebServiceImpl(
    private val leagueCoreRepository: LeagueCoreRepository,
) : AdminLeagueQueryWebService {
    @PreAuthorize("hasRole('ADMIN')")
    override fun findAvailableLeagues(): List<AvailableLeagueDto> =
        leagueCoreRepository
            .findByAvailableTrue()
            .map { LeagueWebMapper.toAvailableDto(it.id!!, it.name) }
}
