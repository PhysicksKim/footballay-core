package com.footballay.core.web.admin.core.service

import com.footballay.core.domain.matchcollect.MatchCollectStatus
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.entity.FixtureMatchCollectState
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.persistence.core.repository.FixtureMatchCollectStateRepository
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.web.admin.core.dto.MatchCollectFixtureStateResponse
import com.footballay.core.web.admin.core.dto.MatchCollectLeagueResponse
import com.footballay.core.web.admin.core.dto.MatchCollectLeagueStatePageResponse
import com.footballay.core.web.admin.core.dto.MatchCollectStatePageResponse
import com.footballay.core.web.admin.core.dto.MatchCollectStateResponse
import org.springframework.http.HttpStatus
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class AdminMatchCollectQueryWebService(
    private val leagueCoreRepository: LeagueCoreRepository,
    private val fixtureCoreRepository: FixtureCoreRepository,
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
        val result =
            stateRepository.findAdminStates(
                leagueUid = leagueUid?.takeIf { it.isNotBlank() },
                fixtureUid = fixtureUid?.takeIf { it.isNotBlank() },
                status = status,
                incompleteOnly = incompleteOnly,
                pageable = pageable(page, size),
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
    @Transactional(readOnly = true)
    fun findIncompleteStates(
        leagueUid: String?,
        fixtureUid: String?,
        page: Int,
        size: Int,
    ): MatchCollectStatePageResponse {
        val result =
            stateRepository.findAdminStates(
                leagueUid = leagueUid?.takeIf { it.isNotBlank() },
                fixtureUid = fixtureUid?.takeIf { it.isNotBlank() },
                status = null,
                incompleteOnly = true,
                pageable = pageable(page, size),
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
    @Transactional(readOnly = true)
    fun findLeagueStates(
        leagueUid: String,
        fixtureUid: String?,
        status: MatchCollectStatus?,
        incompleteOnly: Boolean,
        page: Int,
        size: Int,
    ): MatchCollectLeagueStatePageResponse {
        val league = findLeagueOrThrow(leagueUid)
        val result =
            fixtureCoreRepository.findAdminMatchCollectLeagueFixtures(
                leagueUid = leagueUid,
                fixtureUid = fixtureUid?.takeIf { it.isNotBlank() },
                status = status,
                incompleteOnly = incompleteOnly,
                pageable = pageable(page, size),
            )

        return MatchCollectLeagueStatePageResponse(
            league = toLeagueResponse(league),
            content = result.content.map(::toFixtureStateResponse),
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    fun findLeagueIncompleteStates(
        leagueUid: String,
        fixtureUid: String?,
        page: Int,
        size: Int,
    ): MatchCollectLeagueStatePageResponse {
        val league = findLeagueOrThrow(leagueUid)
        val result =
            fixtureCoreRepository.findAdminMatchCollectLeagueFixtures(
                leagueUid = leagueUid,
                fixtureUid = fixtureUid?.takeIf { it.isNotBlank() },
                status = null,
                incompleteOnly = true,
                pageable = pageable(page, size),
            )

        return MatchCollectLeagueStatePageResponse(
            league = toLeagueResponse(league),
            content = result.content.map(::toFixtureStateResponse),
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
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

    private fun findLeagueOrThrow(leagueUid: String): LeagueCore =
        leagueCoreRepository.findByUid(leagueUid)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "LeagueCore not found: $leagueUid")

    private fun toLeagueResponse(league: LeagueCore): MatchCollectLeagueResponse =
        MatchCollectLeagueResponse(
            leagueUid = league.uid,
            name = league.name,
            nameKo = league.nameKo,
            available = league.available,
            matchCollect = league.matchCollect,
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
            homeTeamName = fixture.homeTeam?.name,
            homeTeamNameKo = fixture.homeTeam?.nameKo,
            awayTeamName = fixture.awayTeam?.name,
            awayTeamNameKo = fixture.awayTeam?.nameKo,
            leagueMatchCollect = league?.matchCollect,
            matchCollectStatus = state.matchCollectStatus,
            lastCollectedAt = state.lastCollectedAt,
        )
    }

    private fun toFixtureStateResponse(fixture: FixtureCore): MatchCollectFixtureStateResponse {
        val state = fixture.matchCollectState
        return MatchCollectFixtureStateResponse(
            fixtureUid = fixture.uid,
            seasonYear = fixture.leagueSeason?.seasonYear,
            currentSeason = fixture.leagueSeason?.current,
            kickoff = fixture.kickoff,
            fixtureStatusCode = fixture.statusCode,
            fixtureAvailable = fixture.available,
            homeTeamName = fixture.homeTeam?.name,
            homeTeamNameKo = fixture.homeTeam?.nameKo,
            awayTeamName = fixture.awayTeam?.name,
            awayTeamNameKo = fixture.awayTeam?.nameKo,
            matchCollectStatus = state?.matchCollectStatus,
            lastCollectedAt = state?.lastCollectedAt,
        )
    }
}
