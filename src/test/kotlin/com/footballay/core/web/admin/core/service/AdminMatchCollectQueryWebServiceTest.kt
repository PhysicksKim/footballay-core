package com.footballay.core.web.admin.core.service

import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.domain.league.MatchCollect
import com.footballay.core.domain.matchcollect.AdminMatchCollectLeagueModel
import com.footballay.core.domain.matchcollect.AdminMatchCollectLeagueStatePage
import com.footballay.core.domain.matchcollect.AdminMatchCollectQueryFacade
import com.footballay.core.domain.matchcollect.AdminMatchCollectStateModel
import com.footballay.core.domain.matchcollect.MatchCollectStatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class AdminMatchCollectQueryWebServiceTest {
    @Mock
    private lateinit var adminMatchCollectQueryFacade: AdminMatchCollectQueryFacade

    @Test
    fun `global match collect state page를 admin response로 변환한다`() {
        val service = service()
        val state = stateModel()
        whenever(
            adminMatchCollectQueryFacade.findLeagueStatePage(
                leagueUid = eq("league-1"),
                status = eq(null),
                incompleteOnly = eq(true),
                pageable = org.mockito.kotlin.any(),
            ),
        ).thenReturn(
            AdminMatchCollectLeagueStatePage(
                league = leagueModel(),
                states = PageImpl(listOf(state), PageRequest.of(0, 50), 1),
            ),
        )

        val result =
            service.findStates(
                leagueUid = "league-1",
                fixtureUid = null,
                status = null,
                incompleteOnly = true,
                page = 0,
                size = 50,
            )

        assertThat(result.totalElements).isEqualTo(1)
        assertThat(result.content).hasSize(1)
        assertThat(result.content.first().fixtureUid).isEqualTo("fixture-1")
        assertThat(result.content.first().leagueUid).isEqualTo("league-1")
        assertThat(result.content.first().seasonYear).isEqualTo(2026)
        assertThat(result.content.first().leagueMatchCollect).isEqualTo(MatchCollect.LIVE)
        assertThat(result.content.first().homeTeamName).isEqualTo("Home Team")
        assertThat(result.content.first().homeTeamNameKo).isEqualTo("홈팀")
        assertThat(result.content.first().awayTeamName).isEqualTo("Away Team")
        assertThat(result.content.first().awayTeamNameKo).isEqualTo("원정팀")
        assertThat(result.content.first().matchCollectStatus).isEqualTo(MatchCollectStatus.DATA_INCOMPLETE_NEEDS_ADMIN)
    }

    @Test
    fun `page와 size를 안전한 범위로 보정한다`() {
        val service = service()
        val pageableCaptor = argumentCaptor<Pageable>()
        whenever(
            adminMatchCollectQueryFacade.findAllStatePage(
                status = eq(MatchCollectStatus.FAIL_END),
                incompleteOnly = eq(false),
                pageable = org.mockito.kotlin.any(),
            ),
        ).thenReturn(PageImpl(emptyList(), PageRequest.of(0, 200), 0))

        service.findStates(
            leagueUid = "",
            fixtureUid = "",
            status = MatchCollectStatus.FAIL_END,
            incompleteOnly = false,
            page = -1,
            size = 999,
        )

        verify(adminMatchCollectQueryFacade).findAllStatePage(
            status = eq(MatchCollectStatus.FAIL_END),
            incompleteOnly = eq(false),
            pageable = pageableCaptor.capture(),
        )
        assertThat(pageableCaptor.firstValue.pageNumber).isZero()
        assertThat(pageableCaptor.firstValue.pageSize).isEqualTo(200)
    }

    @Test
    fun `league scoped state page는 리그 정보와 현재 시즌 대상 fixture state를 함께 반환한다`() {
        val service = service()
        val league = leagueModel()
        val fixture = fixtureStateModel()
        whenever(
            adminMatchCollectQueryFacade.findLeagueStatePage(
                leagueUid = eq("league-1"),
                status = eq(null),
                incompleteOnly = eq(false),
                pageable = org.mockito.kotlin.any(),
            ),
        ).thenReturn(
            AdminMatchCollectLeagueStatePage(
                league = league,
                states = PageImpl(listOf(fixture), PageRequest.of(0, 50), 1),
            ),
        )

        val result =
            service.findLeagueStates(
                leagueUid = "league-1",
                status = null,
                incompleteOnly = false,
                page = 0,
                size = 50,
            )

        assertThat(result.league.leagueUid).isEqualTo("league-1")
        assertThat(result.league.name).isEqualTo("League")
        assertThat(result.league.matchCollect).isEqualTo(MatchCollect.LIVE)
        assertThat(result.content).hasSize(1)
        assertThat(result.content.first().fixtureUid).isEqualTo("fixture-1")
        assertThat(result.content.first().homeTeamName).isEqualTo("Home Team")
        assertThat(result.content.first().awayTeamName).isEqualTo("Away Team")
        assertThat(result.content.first().matchCollectStatus).isEqualTo(MatchCollectStatus.DATA_INCOMPLETE_NEEDS_ADMIN)
        assertThat(result.content.first().lastCollectedAt).isEqualTo(Instant.parse("2026-06-20T12:00:00Z"))
    }

    @Test
    fun `global state 조회는 incompleteOnly true를 facade에 전달한다`() {
        val service = service()
        whenever(
            adminMatchCollectQueryFacade.findStatePageByFixtureUid(
                fixtureUid = eq("fixture-1"),
                status = eq(null),
                incompleteOnly = eq(true),
                pageable = org.mockito.kotlin.any(),
            ),
        ).thenReturn(PageImpl(emptyList(), PageRequest.of(0, 50), 0))

        service.findStates(
            leagueUid = "league-1",
            fixtureUid = "fixture-1",
            status = null,
            incompleteOnly = true,
            page = 0,
            size = 50,
        )

        verify(adminMatchCollectQueryFacade).findStatePageByFixtureUid(
            fixtureUid = eq("fixture-1"),
            status = eq(null),
            incompleteOnly = eq(true),
            pageable = org.mockito.kotlin.any(),
        )
    }

    @Test
    fun `없는 leagueUid 조회는 404 ResponseStatusException으로 변환한다`() {
        val service = service()
        whenever(
            adminMatchCollectQueryFacade.findLeagueStatePage(
                leagueUid = eq("missing-league"),
                status = eq(null),
                incompleteOnly = eq(false),
                pageable = org.mockito.kotlin.any(),
            ),
        ).thenThrow(NoSuchElementException("LeagueCore not found: missing-league"))

        assertThatThrownBy {
            service.findLeagueStates(
                leagueUid = "missing-league",
                status = null,
                incompleteOnly = false,
                page = 0,
                size = 50,
            )
        }.isInstanceOf(ResponseStatusException::class.java)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.NOT_FOUND)
    }

    private fun service() =
        AdminMatchCollectQueryWebService(
            adminMatchCollectQueryFacade = adminMatchCollectQueryFacade,
        )

    private fun leagueModel() =
        AdminMatchCollectLeagueModel(
            leagueUid = "league-1",
            name = "League",
            nameKo = null,
            available = true,
            matchCollect = MatchCollect.LIVE,
        )

    private fun stateModel() =
        AdminMatchCollectStateModel(
            fixtureUid = "fixture-1",
            leagueUid = "league-1",
            seasonYear = 2026,
            currentSeason = true,
            kickoff = Instant.parse("2026-06-20T09:00:00Z"),
            fixtureStatusCode = FixtureStatusCode.FT,
            fixtureAvailable = false,
            homeTeamName = "Home Team",
            homeTeamNameKo = "홈팀",
            awayTeamName = "Away Team",
            awayTeamNameKo = "원정팀",
            leagueMatchCollect = MatchCollect.LIVE,
            matchCollectStatus = MatchCollectStatus.DATA_INCOMPLETE_NEEDS_ADMIN,
            lastCollectedAt = Instant.parse("2026-06-20T12:00:00Z"),
        )

    private fun fixtureStateModel() = stateModel()
}
