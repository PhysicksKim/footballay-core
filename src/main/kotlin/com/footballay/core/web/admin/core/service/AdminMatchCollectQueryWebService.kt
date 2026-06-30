package com.footballay.core.web.admin.core.service

import com.footballay.core.domain.matchcollect.AdminMatchCollectLeagueModel
import com.footballay.core.domain.matchcollect.AdminMatchCollectQueryFacade
import com.footballay.core.domain.matchcollect.AdminMatchCollectStateModel
import com.footballay.core.domain.matchcollect.MatchCollectStatus
import com.footballay.core.web.admin.core.dto.MatchCollectFixtureStateResponse
import com.footballay.core.web.admin.core.dto.MatchCollectLeagueResponse
import com.footballay.core.web.admin.core.dto.MatchCollectLeagueStatePageResponse
import com.footballay.core.web.admin.core.dto.MatchCollectStatePageResponse
import com.footballay.core.web.admin.core.dto.MatchCollectStateResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AdminMatchCollectQueryWebService(
    private val adminMatchCollectQueryFacade: AdminMatchCollectQueryFacade,
) {
    @PreAuthorize("hasRole('ADMIN')")
    fun findStates(
        leagueUid: String?,
        fixtureUid: String?,
        status: MatchCollectStatus?,
        incompleteOnly: Boolean,
        page: Int,
        size: Int,
    ): MatchCollectStatePageResponse {
        val pageable = pageable(page, size)
        val result = findStatePage(
            leagueUid = leagueUid,
            fixtureUid = fixtureUid,
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

    @PreAuthorize("hasRole('ADMIN')")
    fun findLeagueStates(
        leagueUid: String,
        status: MatchCollectStatus?,
        incompleteOnly: Boolean,
        page: Int,
        size: Int,
    ): MatchCollectLeagueStatePageResponse {
        val pageable = pageable(page, size)
        val result = findLeagueStatesOrThrow(
            leagueUid = leagueUid,
            status = status,
            incompleteOnly = incompleteOnly,
            pageable = pageable,
        )

        return MatchCollectLeagueStatePageResponse(
            league = toLeagueResponse(result.league),
            content = result.states.content.map(::toFixtureStateResponse),
            page = result.states.number,
            size = result.states.size,
            totalElements = result.states.totalElements,
            totalPages = result.states.totalPages,
        )
    }

    private fun pageable(
        page: Int,
        size: Int,
    ) = PageRequest.of(
        page.coerceAtLeast(0),
        size.coerceIn(1, 200),
        Sort.unsorted(),
    )

    private fun findStatePage(
        leagueUid: String?,
        fixtureUid: String?,
        status: MatchCollectStatus?,
        incompleteOnly: Boolean,
        pageable: PageRequest,
    ) = when {
        !fixtureUid.isNullOrBlank() ->
            adminMatchCollectQueryFacade.findStatePageByFixtureUid(
                fixtureUid = fixtureUid,
                status = status,
                incompleteOnly = incompleteOnly,
                pageable = pageable,
            )
        !leagueUid.isNullOrBlank() ->
            findLeagueStatesOrThrow(
                leagueUid = leagueUid,
                status = status,
                incompleteOnly = incompleteOnly,
                pageable = pageable,
            ).states
        else ->
            adminMatchCollectQueryFacade.findAllStatePage(
                status = status,
                incompleteOnly = incompleteOnly,
                pageable = pageable,
            )
    }

    private fun findLeagueStatesOrThrow(
        leagueUid: String,
        status: MatchCollectStatus?,
        incompleteOnly: Boolean,
        pageable: PageRequest,
    ) = try {
        adminMatchCollectQueryFacade.findLeagueStatePage(
            leagueUid = leagueUid,
            status = status,
            incompleteOnly = incompleteOnly,
            pageable = pageable,
        )
    } catch (e: NoSuchElementException) {
        throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message, e)
    }

    private fun toLeagueResponse(league: AdminMatchCollectLeagueModel): MatchCollectLeagueResponse =
        MatchCollectLeagueResponse(
            leagueUid = league.leagueUid,
            name = league.name,
            nameKo = league.nameKo,
            available = league.available,
            matchCollect = league.matchCollect,
        )

    private fun toResponse(state: AdminMatchCollectStateModel): MatchCollectStateResponse {
        return MatchCollectStateResponse(
            fixtureUid = state.fixtureUid,
            leagueUid = state.leagueUid,
            seasonYear = state.seasonYear,
            currentSeason = state.currentSeason,
            kickoff = state.kickoff,
            fixtureStatusCode = state.fixtureStatusCode,
            fixtureAvailable = state.fixtureAvailable,
            homeTeamName = state.homeTeamName,
            homeTeamNameKo = state.homeTeamNameKo,
            awayTeamName = state.awayTeamName,
            awayTeamNameKo = state.awayTeamNameKo,
            leagueMatchCollect = state.leagueMatchCollect,
            matchCollectStatus = state.matchCollectStatus,
            lastCollectedAt = state.lastCollectedAt,
        )
    }

    private fun toFixtureStateResponse(state: AdminMatchCollectStateModel): MatchCollectFixtureStateResponse {
        return MatchCollectFixtureStateResponse(
            fixtureUid = state.fixtureUid,
            seasonYear = state.seasonYear,
            currentSeason = state.currentSeason,
            kickoff = state.kickoff,
            fixtureStatusCode = state.fixtureStatusCode,
            fixtureAvailable = state.fixtureAvailable,
            homeTeamName = state.homeTeamName,
            homeTeamNameKo = state.homeTeamNameKo,
            awayTeamName = state.awayTeamName,
            awayTeamNameKo = state.awayTeamNameKo,
            matchCollectStatus = state.matchCollectStatus,
            lastCollectedAt = state.lastCollectedAt,
        )
    }
}
