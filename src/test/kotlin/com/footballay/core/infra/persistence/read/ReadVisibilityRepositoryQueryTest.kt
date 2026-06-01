package com.footballay.core.infra.persistence.read

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

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReadVisibilityRepositoryQueryTest {
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
    private lateinit var coreOnlyLeague: LeagueCore

    @BeforeEach
    fun setUp() {
        apiSportsLeague = saveLeague("api-league", "ApiSports League", available = true)
        mockLeague = saveLeague("mock-league", "Mock League", available = true)
        coreOnlyLeague = saveLeague("core-only-league", "Core Only League", available = true)
        saveLeague("unavailable-mock-league", "Unavailable Mock League", available = false)
            .also(::saveMockBackboneLeague)

        leagueApiSportsRepository.save(
            LeagueApiSports(
                apiId = 39L,
                name = "ApiSports League",
                leagueCore = apiSportsLeague,
                available = true,
            ),
        )
        saveMockBackboneLeague(mockLeague)

        entityManager.flush()
        entityManager.clear()
    }

    @Test
    fun `available league query는 ApiSports-backed와 MockBackbone-backed를 분리한다`() {
        val apiSportsLeagues = leagueCoreRepository.findApiSportsBackedAvailableLeagues()
        val mockLeagues = mockBackboneLeagueRepository.findMockBackedAvailableLeagues()

        assertThat(apiSportsLeagues.map { it.uid }).containsExactly("api-league")
        assertThat(mockLeagues.map { it.uid }).containsExactly("mock-league")
        assertThat(apiSportsLeagues.map { it.uid }).doesNotContain("core-only-league", "mock-league")
        assertThat(mockLeagues.map { it.uid }).doesNotContain("core-only-league", "api-league", "unavailable-mock-league")
    }

    @Test
    fun `fixture range query는 ApiSports-backed와 MockBackbone-backed를 분리한다`() {
        val start = Instant.parse("2026-06-01T00:00:00Z")
        val end = Instant.parse("2026-06-02T00:00:00Z")
        val apiFixture = saveFixture("api-fixture", apiSportsLeague, Instant.parse("2026-06-01T10:00:00Z"))
        val mockFixture = saveFixture("mock-fixture", mockLeague, Instant.parse("2026-06-01T11:00:00Z"))
        saveFixture("core-only-fixture", coreOnlyLeague, Instant.parse("2026-06-01T12:00:00Z"))
        saveApiSportsFixture(apiFixture)
        saveMockBackboneFixture(mockFixture)

        entityManager.flush()
        entityManager.clear()

        val apiSportsFixtures =
            fixtureCoreRepository.findApiSportsBackedFixturesByLeagueUidInKickoffRange(
                apiSportsLeague.uid,
                start,
                end,
            )
        val mockFixtures =
            mockBackboneFixtureRepository.findMockBackedFixturesByLeagueUidInKickoffRange(
                mockLeague.uid,
                start,
                end,
            )

        assertThat(apiSportsFixtures.map { it.uid }).containsExactly("api-fixture")
        assertThat(mockFixtures.map { it.uid }).containsExactly("mock-fixture")
    }

    @Test
    fun `min max kickoff query는 provider visibility별 후보만 사용한다`() {
        val apiFixture = saveFixture("api-kickoff", apiSportsLeague, Instant.parse("2026-06-10T10:00:00Z"))
        val mockFixture = saveFixture("mock-kickoff", mockLeague, Instant.parse("2026-06-05T10:00:00Z"))
        saveFixture("core-only-kickoff", coreOnlyLeague, Instant.parse("2026-06-03T10:00:00Z"))
        saveApiSportsFixture(apiFixture)
        saveMockBackboneFixture(mockFixture)

        entityManager.flush()
        entityManager.clear()

        assertThat(
            fixtureCoreRepository.findMinApiSportsBackedKickoffAfterByLeagueUid(
                apiSportsLeague.uid,
                Instant.parse("2026-06-01T00:00:00Z"),
            ),
        ).isEqualTo(Instant.parse("2026-06-10T10:00:00Z"))
        assertThat(
            fixtureCoreRepository.findMaxApiSportsBackedKickoffBeforeByLeagueUid(
                apiSportsLeague.uid,
                Instant.parse("2026-06-30T00:00:00Z"),
            ),
        ).isEqualTo(Instant.parse("2026-06-10T10:00:00Z"))
        assertThat(
            mockBackboneFixtureRepository.findMinMockBackedKickoffAfterByLeagueUid(
                mockLeague.uid,
                Instant.parse("2026-06-01T00:00:00Z"),
            ),
        ).isEqualTo(Instant.parse("2026-06-05T10:00:00Z"))
        assertThat(
            mockBackboneFixtureRepository.findMaxMockBackedKickoffBeforeByLeagueUid(
                mockLeague.uid,
                Instant.parse("2026-06-30T00:00:00Z"),
            ),
        ).isEqualTo(Instant.parse("2026-06-05T10:00:00Z"))
    }

    private fun saveLeague(
        uid: String,
        name: String,
        available: Boolean,
    ): LeagueCore =
        leagueCoreRepository.save(
            LeagueCore(
                uid = uid,
                name = name,
                available = available,
                autoGenerated = false,
            ),
        )

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

    private fun saveApiSportsFixture(fixture: FixtureCore) {
        fixtureApiSportsRepository.save(
            FixtureApiSports(
                apiId = requireNotNull(fixture.uid.hashCode().toLong().let { if (it < 0) -it else it }),
                core = fixture,
                round = "Round 1",
                season = null,
            ),
        )
    }

    private fun saveMockBackboneLeague(league: LeagueCore) {
        mockBackboneLeagueRepository.save(
            MockBackboneLeague(
                mockUid = "mock-league-${league.uid}",
                league = league,
                createdAt = Instant.parse("2026-06-01T00:00:00Z"),
            ),
        )
    }

    private fun saveMockBackboneFixture(fixture: FixtureCore) {
        mockBackboneFixtureRepository.save(
            MockBackboneFixture(
                mockUid = "mock-fixture-${fixture.uid}",
                fixture = fixture,
                initialStatusCode = fixture.statusCode,
                initialKickoff = fixture.kickoff,
                createdAt = Instant.parse("2026-06-01T00:00:00Z"),
            ),
        )
    }
}
