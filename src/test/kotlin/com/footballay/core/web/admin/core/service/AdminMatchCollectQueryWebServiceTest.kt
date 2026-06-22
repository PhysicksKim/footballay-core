package com.footballay.core.web.admin.core.service

import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.domain.league.MatchCollect
import com.footballay.core.domain.matchcollect.MatchCollectStatus
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.entity.FixtureMatchCollectState
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.LeagueSeasonCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.persistence.core.repository.FixtureMatchCollectStateRepository
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import org.assertj.core.api.Assertions.assertThat
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
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class AdminMatchCollectQueryWebServiceTest {
    @Mock
    private lateinit var leagueCoreRepository: LeagueCoreRepository

    @Mock
    private lateinit var fixtureCoreRepository: FixtureCoreRepository

    @Mock
    private lateinit var stateRepository: FixtureMatchCollectStateRepository

    @Test
    fun `global match collect state page를 admin response로 변환한다`() {
        val service = service()
        val state =
            FixtureMatchCollectState(
                fixture = fixture(),
                matchCollectStatus = MatchCollectStatus.DATA_INCOMPLETE_NEEDS_ADMIN,
                lastCollectedAt = Instant.parse("2026-06-20T12:00:00Z"),
            )
        whenever(
            stateRepository.findAdminStates(
                leagueUid = eq("league-1"),
                fixtureUid = eq(null),
                status = eq(null),
                incompleteOnly = eq(true),
                pageable = org.mockito.kotlin.any(),
            ),
        ).thenReturn(PageImpl(listOf(state), PageRequest.of(0, 50), 1))

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
            stateRepository.findAdminStates(
                leagueUid = eq(null),
                fixtureUid = eq(null),
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

        verify(stateRepository).findAdminStates(
            leagueUid = eq(null),
            fixtureUid = eq(null),
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
        val league = league()
        val fixture = fixture()
        fixture.matchCollectState =
            FixtureMatchCollectState(
                fixture = fixture,
                matchCollectStatus = MatchCollectStatus.EARLY_SYNCED,
                lastCollectedAt = Instant.parse("2026-06-20T11:00:00Z"),
            )
        whenever(leagueCoreRepository.findByUid("league-1")).thenReturn(league)
        whenever(
            fixtureCoreRepository.findAdminMatchCollectLeagueFixtures(
                leagueUid = eq("league-1"),
                fixtureUid = eq(null),
                status = eq(null),
                incompleteOnly = eq(false),
                pageable = org.mockito.kotlin.any(),
            ),
        ).thenReturn(PageImpl(listOf(fixture), PageRequest.of(0, 50), 1))

        val result =
            service.findLeagueStates(
                leagueUid = "league-1",
                fixtureUid = null,
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
        assertThat(result.content.first().matchCollectStatus).isEqualTo(MatchCollectStatus.EARLY_SYNCED)
        assertThat(result.content.first().lastCollectedAt).isEqualTo(Instant.parse("2026-06-20T11:00:00Z"))
    }

    @Test
    fun `incomplete 조회는 self invocation 없이 repository를 직접 호출한다`() {
        val service = service()
        whenever(
            stateRepository.findAdminStates(
                leagueUid = eq("league-1"),
                fixtureUid = eq("fixture-1"),
                status = eq(null),
                incompleteOnly = eq(true),
                pageable = org.mockito.kotlin.any(),
            ),
        ).thenReturn(PageImpl(emptyList(), PageRequest.of(0, 50), 0))

        service.findIncompleteStates(
            leagueUid = "league-1",
            fixtureUid = "fixture-1",
            page = 0,
            size = 50,
        )

        verify(stateRepository).findAdminStates(
            leagueUid = eq("league-1"),
            fixtureUid = eq("fixture-1"),
            status = eq(null),
            incompleteOnly = eq(true),
            pageable = org.mockito.kotlin.any(),
        )
    }

    private fun service() =
        AdminMatchCollectQueryWebService(
            leagueCoreRepository = leagueCoreRepository,
            fixtureCoreRepository = fixtureCoreRepository,
            stateRepository = stateRepository,
        )

    private fun league() =
        LeagueCore(
            id = 1L,
            uid = "league-1",
            name = "League",
            available = true,
            matchCollect = MatchCollect.LIVE,
            autoGenerated = false,
        )

    private fun fixture(): FixtureCore {
        val league = league()
        val season =
            LeagueSeasonCore(
                id = 1L,
                league = league,
                seasonYear = 2026,
                current = true,
                autoGenerated = false,
            )
        val homeTeam =
            TeamCore(
                id = 1L,
                uid = "team-home",
                name = "Home Team",
                nameKo = "홈팀",
                autoGenerated = false,
            )
        val awayTeam =
            TeamCore(
                id = 2L,
                uid = "team-away",
                name = "Away Team",
                nameKo = "원정팀",
                autoGenerated = false,
            )
        return FixtureCore(
            id = 1L,
            uid = "fixture-1",
            kickoff = Instant.parse("2026-06-20T09:00:00Z"),
            statusText = FixtureStatusCode.FT.code,
            statusCode = FixtureStatusCode.FT,
            league = league,
            leagueSeason = season,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            available = false,
            autoGenerated = false,
        )
    }
}
