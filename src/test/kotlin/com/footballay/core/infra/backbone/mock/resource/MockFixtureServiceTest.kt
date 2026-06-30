package com.footballay.core.infra.backbone.mock.resource

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.infra.facade.AvailableFixtureFacade
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.LeagueTeamCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.persistence.core.repository.LeagueTeamCoreRepository
import com.footballay.core.infra.persistence.mockbackbone.entity.MockBackboneFixture
import com.footballay.core.infra.persistence.mockbackbone.entity.MockBackboneLeague
import com.footballay.core.infra.persistence.mockbackbone.entity.MockBackboneTeam
import com.footballay.core.infra.persistence.mockbackbone.repository.MockBackboneFixtureRepository
import com.footballay.core.infra.persistence.mockbackbone.repository.MockBackboneLeagueRepository
import com.footballay.core.infra.persistence.mockbackbone.repository.MockBackboneTeamRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
class MockFixtureServiceTest {
    @Mock
    private lateinit var fixtureCoreRepository: FixtureCoreRepository

    @Mock
    private lateinit var leagueTeamCoreRepository: LeagueTeamCoreRepository

    @Mock
    private lateinit var mockLeagueRepository: MockBackboneLeagueRepository

    @Mock
    private lateinit var mockTeamRepository: MockBackboneTeamRepository

    @Mock
    private lateinit var mockFixtureRepository: MockBackboneFixtureRepository

    @Mock
    private lateinit var availableFixtureFacade: AvailableFixtureFacade

    private val now = Instant.parse("2026-05-29T10:00:00Z")
    private lateinit var service: MockFixtureService

    @BeforeEach
    fun setUp() {
        service =
            MockFixtureService(
                fixtureCoreRepository = fixtureCoreRepository,
                leagueTeamCoreRepository = leagueTeamCoreRepository,
                mockLeagueRepository = mockLeagueRepository,
                mockTeamRepository = mockTeamRepository,
                mockFixtureRepository = mockFixtureRepository,
                availableFixtureFacade = availableFixtureFacade,
                modelMapper = MockBackboneModelMapper(),
                uidFactory = MockUidFactory(Clock.fixed(now, ZoneOffset.UTC)),
                clock = Clock.fixed(now, ZoneOffset.UTC),
            )
    }

    @Test
    fun `createFixture creates fixture from existing mock league and teams`() {
        val league = league()
        val home = team(id = 10L, uid = "home-1")
        val away = team(id = 11L, uid = "away-1")
        whenever(mockLeagueRepository.findByLeagueCoreUid("league-1")).thenReturn(mockLeague(league))
        whenever(mockTeamRepository.findByTeamCoreUid("home-1")).thenReturn(mockTeam(home))
        whenever(mockTeamRepository.findByTeamCoreUid("away-1")).thenReturn(mockTeam(away))
        whenever(leagueTeamCoreRepository.existsByLeagueIdAndTeamId(1L, 10L)).thenReturn(false)
        whenever(leagueTeamCoreRepository.existsByLeagueIdAndTeamId(1L, 11L)).thenReturn(false)
        whenever(fixtureCoreRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(mockFixtureRepository.save(any())).thenAnswer { it.arguments[0] }

        val result =
            service.createFixture(
                MockFixtureCreateCommand(
                    leagueCoreUid = "league-1",
                    homeTeamCoreUid = "home-1",
                    awayTeamCoreUid = "away-1",
                    kickoff = now,
                    scenarioUid = "scenario-1",
                ),
            )

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val value = (result as DomainResult.Success).value
        assertThat(value.leagueUid).isEqualTo("league-1")
        assertThat(value.homeTeam?.uid).isEqualTo("home-1")
        assertThat(value.awayTeam?.uid).isEqualTo("away-1")
        assertThat(value.available).isFalse()
        verify(leagueTeamCoreRepository, times(2)).save(any<LeagueTeamCore>())
        verify(fixtureCoreRepository).save(any())
        verify(mockFixtureRepository).save(any())
    }

    @Test
    fun `createFixture rejects available true because available lifecycle must use core uid API`() {
        val result =
            service.createFixture(
                MockFixtureCreateCommand(
                    leagueCoreUid = "league-1",
                    homeTeamCoreUid = "home-1",
                    awayTeamCoreUid = "away-1",
                    kickoff = now,
                    fixtureAvailable = true,
                ),
            )

        assertThat(result).isInstanceOf(DomainResult.Fail::class.java)
        val error = (result as DomainResult.Fail).error as DomainFail.Validation
        assertThat(error.errors.first().code).isEqualTo("FIXTURE_AVAILABLE_NOT_ALLOWED_ON_CREATE")
    }

    @Test
    fun `deleteFixture disables available fixture before hard delete`() {
        val fixture = fixture(available = true)
        val mockFixture = mockFixture(fixture)
        whenever(mockFixtureRepository.findByFixtureCoreUid("fixture-1")).thenReturn(mockFixture)
        whenever(availableFixtureFacade.removeAvailableFixtureByCoreUid("fixture-1")).thenReturn(DomainResult.Success("fixture-1"))

        val result = service.deleteFixture("fixture-1")

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        verify(availableFixtureFacade).removeAvailableFixtureByCoreUid("fixture-1")
        verify(mockFixtureRepository).delete(mockFixture)
        verify(fixtureCoreRepository).delete(fixture)
    }

    private fun league(): LeagueCore =
        LeagueCore(
            id = 1L,
            uid = "league-1",
            name = "League",
            available = true,
            autoGenerated = false,
        )

    private fun team(
        id: Long,
        uid: String,
    ): TeamCore =
        TeamCore(
            id = id,
            uid = uid,
            name = uid,
            autoGenerated = false,
        )

    private fun fixture(available: Boolean): FixtureCore =
        FixtureCore(
            id = 100L,
            uid = "fixture-1",
            kickoff = now,
            statusText = "Not Started",
            statusCode = FixtureStatusCode.NS,
            league = league(),
            homeTeam = team(10L, "home-1"),
            awayTeam = team(11L, "away-1"),
            available = available,
            autoGenerated = false,
        )

    private fun mockLeague(league: LeagueCore): MockBackboneLeague =
        MockBackboneLeague(mockUid = "mock-league", league = league, createdAt = now)

    private fun mockTeam(team: TeamCore): MockBackboneTeam =
        MockBackboneTeam(mockUid = "mock-team-${team.uid}", team = team, createdAt = now)

    private fun mockFixture(fixture: FixtureCore): MockBackboneFixture =
        MockBackboneFixture(
            mockUid = "mock-fixture",
            fixture = fixture,
            initialStatusCode = FixtureStatusCode.NS,
            initialKickoff = now,
            createdAt = now,
        )
}
