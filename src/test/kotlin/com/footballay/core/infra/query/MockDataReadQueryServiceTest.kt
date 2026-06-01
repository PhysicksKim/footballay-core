package com.footballay.core.infra.query

import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.MockDataReadOption
import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.persistence.mockbackbone.entity.MockBackboneFixture
import com.footballay.core.infra.persistence.mockbackbone.entity.MockBackboneLeague
import com.footballay.core.infra.persistence.mockbackbone.repository.MockBackboneFixtureRepository
import com.footballay.core.infra.persistence.mockbackbone.repository.MockBackboneLeagueRepository
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneOffset

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MockDataReadQueryServiceTest {
    @Autowired
    private lateinit var leagueReadQueryService: LeagueReadQueryService

    @Autowired
    private lateinit var fixtureScheduleReadQueryService: FixtureScheduleReadQueryService

    @Autowired
    private lateinit var leagueCoreRepository: LeagueCoreRepository

    @Autowired
    private lateinit var fixtureCoreRepository: FixtureCoreRepository

    @Autowired
    private lateinit var leagueApiSportsRepository: LeagueApiSportsRepository

    @Autowired
    private lateinit var fixtureApiSportsRepository: FixtureApiSportsRepository

    @Autowired
    private lateinit var mockBackboneLeagueRepository: MockBackboneLeagueRepository

    @Autowired
    private lateinit var mockBackboneFixtureRepository: MockBackboneFixtureRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    private lateinit var apiSportsLeague: LeagueCore
    private lateinit var mockLeague: LeagueCore
    private lateinit var sharedLeague: LeagueCore

    @BeforeEach
    fun setUp() {
        apiSportsLeague = saveLeague("api-league", "ApiSports League")
        mockLeague = saveLeague("mock-league", "Mock League")
        sharedLeague = saveLeague("shared-league", "Shared League")

        saveApiSportsLeague(apiSportsLeague, 39L)
        saveMockLeague(mockLeague)
        saveApiSportsLeague(sharedLeague, 140L)
        saveMockLeague(sharedLeague)

        entityManager.flush()
        entityManager.clear()
    }

    @Test
    fun `available league 기본 조회는 mock league를 제외한다`() {
        val result = leagueReadQueryService.findAvailableLeagues()

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val leagues = (result as DomainResult.Success).value
        assertThat(leagues.map { it.uid }).containsExactly("api-league", "shared-league")
    }

    @Test
    fun `available league mock 포함 조회는 mock league를 포함한다`() {
        val result =
            leagueReadQueryService.findAvailableLeagues(
                MockDataReadOption(includeMockData = true),
            )

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val leagues = (result as DomainResult.Success).value
        assertThat(leagues.map { it.uid }).containsExactly("api-league", "mock-league", "shared-league")
    }

    @Test
    fun `fixture exact 기본 조회는 mock fixture를 제외한다`() {
        saveApiFixture("api-exact", sharedLeague, Instant.parse("2026-06-10T10:00:00Z"))
        saveMockFixture("mock-exact", sharedLeague, Instant.parse("2026-06-10T12:00:00Z"))
        entityManager.flush()
        entityManager.clear()

        val result =
            fixtureScheduleReadQueryService.findFixturesByLeague(
                leagueUid = sharedLeague.uid,
                at = Instant.parse("2026-06-10T00:00:00Z"),
                mode = "exact",
                zoneId = ZoneOffset.UTC,
            )

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val fixtures = (result as DomainResult.Success).value
        assertThat(fixtures.map { it.uid }).containsExactly("api-exact")
    }

    @Test
    fun `fixture exact mock 포함 조회는 mock fixture를 포함한다`() {
        saveApiFixture("api-exact", sharedLeague, Instant.parse("2026-06-10T10:00:00Z"))
        saveMockFixture("mock-exact", sharedLeague, Instant.parse("2026-06-10T12:00:00Z"))
        entityManager.flush()
        entityManager.clear()

        val result =
            fixtureScheduleReadQueryService.findFixturesByLeague(
                leagueUid = sharedLeague.uid,
                at = Instant.parse("2026-06-10T00:00:00Z"),
                mode = "exact",
                zoneId = ZoneOffset.UTC,
                option = MockDataReadOption(includeMockData = true),
            )

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val fixtures = (result as DomainResult.Success).value
        assertThat(fixtures.map { it.uid }).containsExactly("api-exact", "mock-exact")
    }

    @Test
    fun `fixture nearest 기본 조회는 mock kickoff에 오염되지 않는다`() {
        saveApiFixture("api-nearest", sharedLeague, Instant.parse("2026-06-10T10:00:00Z"))
        saveMockFixture("mock-nearest", sharedLeague, Instant.parse("2026-06-05T10:00:00Z"))
        entityManager.flush()
        entityManager.clear()

        val result =
            fixtureScheduleReadQueryService.findFixturesByLeague(
                leagueUid = sharedLeague.uid,
                at = Instant.parse("2026-06-01T00:00:00Z"),
                mode = "nearest",
                zoneId = ZoneOffset.UTC,
            )

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val fixtures = (result as DomainResult.Success).value
        assertThat(fixtures.map { it.uid }).containsExactly("api-nearest")
    }

    @Test
    fun `fixture nearest mock 포함 조회는 mock kickoff도 후보로 사용한다`() {
        saveApiFixture("api-nearest", sharedLeague, Instant.parse("2026-06-10T10:00:00Z"))
        saveMockFixture("mock-nearest", sharedLeague, Instant.parse("2026-06-05T10:00:00Z"))
        entityManager.flush()
        entityManager.clear()

        val result =
            fixtureScheduleReadQueryService.findFixturesByLeague(
                leagueUid = sharedLeague.uid,
                at = Instant.parse("2026-06-01T00:00:00Z"),
                mode = "nearest",
                zoneId = ZoneOffset.UTC,
                option = MockDataReadOption(includeMockData = true),
            )

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val fixtures = (result as DomainResult.Success).value
        assertThat(fixtures.map { it.uid }).containsExactly("mock-nearest")
    }

    @Test
    fun `fixture previous 기본 조회는 mock kickoff에 오염되지 않는다`() {
        saveApiFixture("api-previous", sharedLeague, Instant.parse("2026-06-01T10:00:00Z"))
        saveMockFixture("mock-previous", sharedLeague, Instant.parse("2026-06-05T10:00:00Z"))
        entityManager.flush()
        entityManager.clear()

        val result =
            fixtureScheduleReadQueryService.findFixturesByLeague(
                leagueUid = sharedLeague.uid,
                at = Instant.parse("2026-06-10T00:00:00Z"),
                mode = "previous",
                zoneId = ZoneOffset.UTC,
            )

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val fixtures = (result as DomainResult.Success).value
        assertThat(fixtures.map { it.uid }).containsExactly("api-previous")
    }

    @Test
    fun `fixture previous mock 포함 조회는 mock kickoff도 후보로 사용한다`() {
        saveApiFixture("api-previous", sharedLeague, Instant.parse("2026-06-01T10:00:00Z"))
        saveMockFixture("mock-previous", sharedLeague, Instant.parse("2026-06-05T10:00:00Z"))
        entityManager.flush()
        entityManager.clear()

        val result =
            fixtureScheduleReadQueryService.findFixturesByLeague(
                leagueUid = sharedLeague.uid,
                at = Instant.parse("2026-06-10T00:00:00Z"),
                mode = "previous",
                zoneId = ZoneOffset.UTC,
                option = MockDataReadOption(includeMockData = true),
            )

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val fixtures = (result as DomainResult.Success).value
        assertThat(fixtures.map { it.uid }).containsExactly("mock-previous")
    }

    private fun saveLeague(
        uid: String,
        name: String,
    ): LeagueCore =
        leagueCoreRepository.save(
            LeagueCore(
                uid = uid,
                name = name,
                available = true,
                autoGenerated = false,
            ),
        )

    private fun saveApiSportsLeague(
        league: LeagueCore,
        apiId: Long,
    ) {
        leagueApiSportsRepository.save(
            LeagueApiSports(
                apiId = apiId,
                name = league.name,
                leagueCore = league,
                available = true,
            ),
        )
    }

    private fun saveMockLeague(league: LeagueCore) {
        mockBackboneLeagueRepository.save(
            MockBackboneLeague(
                mockUid = "mock-league-${league.uid}",
                league = league,
                createdAt = Instant.parse("2026-06-01T00:00:00Z"),
            ),
        )
    }

    private fun saveApiFixture(
        uid: String,
        league: LeagueCore,
        kickoff: Instant,
    ) {
        val fixture = saveFixture(uid, league, kickoff)
        fixtureApiSportsRepository.save(
            FixtureApiSports(
                apiId = uid.hashCode().toLong().let { if (it < 0) -it else it },
                core = fixture,
                round = "Round 1",
                season = null,
            ),
        )
    }

    private fun saveMockFixture(
        uid: String,
        league: LeagueCore,
        kickoff: Instant,
    ) {
        val fixture = saveFixture(uid, league, kickoff)
        mockBackboneFixtureRepository.save(
            MockBackboneFixture(
                mockUid = "mock-fixture-$uid",
                fixture = fixture,
                initialStatusCode = fixture.statusCode,
                initialKickoff = fixture.kickoff,
                createdAt = Instant.parse("2026-06-01T00:00:00Z"),
            ),
        )
    }

    private fun saveFixture(
        uid: String,
        league: LeagueCore,
        kickoff: Instant,
    ): FixtureCore =
        fixtureCoreRepository.save(
            FixtureCore(
                uid = uid,
                kickoff = kickoff,
                statusText = "Not Started",
                statusCode = FixtureStatusCode.NS,
                league = league,
                homeTeam = null,
                awayTeam = null,
                finished = false,
                available = true,
                autoGenerated = false,
            ),
        )
}
