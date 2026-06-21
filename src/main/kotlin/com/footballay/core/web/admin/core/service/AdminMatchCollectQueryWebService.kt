package com.footballay.core.web.admin.core.service

import com.footballay.core.domain.matchcollect.MatchCollectStatus
import com.footballay.core.infra.persistence.core.entity.FixtureMatchCollectState
import com.footballay.core.infra.persistence.core.repository.FixtureMatchCollectStateRepository
import com.footballay.core.web.admin.core.dto.MatchCollectStatePageResponse
import com.footballay.core.web.admin.core.dto.MatchCollectStateResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminMatchCollectQueryWebService(
    private val stateRepository: FixtureMatchCollectStateRepository,
) {
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    fun findStates(
        leagueUid: String?,
        fixtureUid: String?,
        status: MatchCollectStatus?,
        incompleteOnly: Boolean,
        page: Int,
        size: Int,
    ): MatchCollectStatePageResponse {
        val pageable =
            PageRequest.of(
                page.coerceAtLeast(0),
                size.coerceIn(1, 200),
                Sort.unsorted(),
            )
        val result =
            stateRepository.findAdminStates(
                leagueUid = leagueUid?.takeIf { it.isNotBlank() },
                fixtureUid = fixtureUid?.takeIf { it.isNotBlank() },
                status = status,
                incompleteOnly = incompleteOnly,
                pageable = pageable,
            )

        return MatchCollectStatePageResponse(
            content = result.content.map(::toResponse),
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    fun findIncompleteStates(
        leagueUid: String?,
        fixtureUid: String?,
        page: Int,
        size: Int,
    ): MatchCollectStatePageResponse =
        findStates(
            leagueUid = leagueUid,
            fixtureUid = fixtureUid,
            status = null,
            incompleteOnly = true,
            page = page,
            size = size,
        )

    private fun toResponse(state: FixtureMatchCollectState): MatchCollectStateResponse {
        val fixture = state.fixture
        val season = fixture.leagueSeason
        @Suppress("DEPRECATION")
        val league = season?.league ?: fixture.league
        return MatchCollectStateResponse(
            fixtureUid = fixture.uid,
            leagueUid = league?.uid,
            seasonYear = season?.seasonYear,
            currentSeason = season?.current,
            kickoff = fixture.kickoff,
            fixtureStatusCode = fixture.statusCode,
            fixtureAvailable = fixture.available,
            leagueMatchCollect = league?.matchCollect,
            matchCollectStatus = state.matchCollectStatus,
            lastCollectedAt = state.lastCollectedAt,
        )
    }
}
