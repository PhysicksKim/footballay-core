package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.domain.league.MatchCollect
import com.footballay.core.domain.matchcollect.MatchCollectStatus
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.entity.FixtureMatchCollectState
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.LeagueSeasonCore
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FixtureMatchCollectStateRepositoryTest {
    @Autowired
    private lateinit var leagueCoreRepository: LeagueCoreRepository

    @Autowired
    private lateinit var fixtureCoreRepository: FixtureCoreRepository

    @Autowired
    private lateinit var leagueSeasonCoreRepository: LeagueSeasonCoreRepository

    @Autowired
    private lateinit var stateRepository: FixtureMatchCollectStateRepository

    @PersistenceContext
    private lateinit var em: EntityManager

    @Test
    fun `fixture 하나당 match collect state는 하나만 저장된다`() {
        val fixture = saveFixture("fixture-state-unique")
        stateRepository.save(FixtureMatchCollectState(fixture = fixture))

        assertThatThrownBy {
            stateRepository.save(FixtureMatchCollectState(fixture = fixture))
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `fixture uid로 state를 조회한다`() {
        val fixture = saveFixture("fixture-state-find")
        stateRepository.save(
            FixtureMatchCollectState(
                fixture = fixture,
                matchCollectStatus = MatchCollectStatus.EARLY_SYNCED,
            ),
        )
        em.flush()
        em.clear()

        val found = stateRepository.findByFixture_Uid(fixture.uid)

        assertThat(found).isNotNull
        assertThat(found?.fixture?.uid).isEqualTo(fixture.uid)
        assertThat(found?.matchCollectStatus).isEqualTo(MatchCollectStatus.EARLY_SYNCED)
    }

    @Test
    fun `lastCollectedAt을 저장한다`() {
        val fixture = saveFixture("fixture-last-collected")
        val lastCollectedAt = Instant.parse("2026-06-15T12:00:00Z")
        stateRepository.save(
            FixtureMatchCollectState(
                fixture = fixture,
                lastCollectedAt = lastCollectedAt,
            ),
        )
        em.flush()
        em.clear()

        val found = stateRepository.findByFixture_Uid(fixture.uid)

        assertThat(found?.lastCollectedAt).isEqualTo(lastCollectedAt)
    }

    @Test
    fun `admin state 조회는 league fixture status incomplete 필터를 적용한다`() {
        val leagueUid = "league-admin-filter"
        val target = saveFixture("fixture-admin-target", leagueUid = leagueUid)
        val failEnd = saveFixture("fixture-admin-fail-end", leagueUid = leagueUid)
        val otherFixture = saveFixture("fixture-admin-other", leagueUid = leagueUid)
        val otherLeague = saveFixture("fixture-admin-other-league", leagueUid = "league-admin-other")
        stateRepository.save(
            FixtureMatchCollectState(
                fixture = target,
                matchCollectStatus = MatchCollectStatus.DATA_INCOMPLETE_NEEDS_ADMIN,
            ),
        )
        stateRepository.save(
            FixtureMatchCollectState(
                fixture = failEnd,
                matchCollectStatus = MatchCollectStatus.FAIL_END,
            ),
        )
        stateRepository.save(
            FixtureMatchCollectState(
                fixture = otherFixture,
                matchCollectStatus = MatchCollectStatus.SUCCESS,
            ),
        )
        stateRepository.save(
            FixtureMatchCollectState(
                fixture = otherLeague,
                matchCollectStatus = MatchCollectStatus.DATA_INCOMPLETE_NEEDS_ADMIN,
            ),
        )
        em.flush()
        em.clear()

        val result =
            stateRepository.findAdminStatesByLeagueUidAndStatuses(
                leagueUid = leagueUid,
                statuses = listOf(MatchCollectStatus.DATA_INCOMPLETE_NEEDS_ADMIN, MatchCollectStatus.FAIL_END),
                pageable = PageRequest.of(0, 20),
            )

        assertThat(result.content.map { it.fixture.uid }).containsExactlyInAnyOrder(target.uid, failEnd.uid)
        assertThat(result.totalElements).isEqualTo(2)
    }

    @Test
    fun `admin state 조회는 신규 leagueSeason league 경로로도 리그를 매칭한다`() {
        val seasonLeagueUid = "league-season-path"
        val fixture =
            saveFixtureWithSeasonLeague(
                uid = "fixture-season-path",
                legacyLeagueUid = "league-legacy-path",
                seasonLeagueUid = seasonLeagueUid,
            )
        stateRepository.save(
            FixtureMatchCollectState(
                fixture = fixture,
                matchCollectStatus = MatchCollectStatus.FAIL_END,
            ),
        )
        em.flush()
        em.clear()

        val result =
            stateRepository.findAdminStatesByLeagueUidAndStatuses(
                leagueUid = seasonLeagueUid,
                statuses = listOf(MatchCollectStatus.FAIL_END),
                pageable = PageRequest.of(0, 20),
            )

        assertThat(result.content.map { it.fixture.uid }).containsExactly(fixture.uid)
    }

    @Test
    fun `admin state 조회는 state row가 없는 fixture를 포함하지 않는다`() {
        val leagueUid = "league-state-row-only"
        val noStateFixture = saveFixture("fixture-no-state", leagueUid = leagueUid)
        val stateFixture = saveFixture("fixture-with-state", leagueUid = leagueUid)
        stateRepository.save(
            FixtureMatchCollectState(
                fixture = stateFixture,
                matchCollectStatus = MatchCollectStatus.FAIL_END,
            ),
        )
        em.flush()
        em.clear()

        val result =
            stateRepository.findAdminStatesByLeagueUidAndStatuses(
                leagueUid = leagueUid,
                statuses = listOf(MatchCollectStatus.FAIL_END),
                pageable = PageRequest.of(0, 20),
            )

        assertThat(result.content.map { it.fixture.uid }).containsExactly(stateFixture.uid)
        assertThat(result.content.map { it.fixture.uid }).doesNotContain(noStateFixture.uid)
    }

    @Test
    fun `admin state 조회는 fixture uid와 status로 좁힌다`() {
        val target = saveFixture("fixture-admin-fixture-status")
        stateRepository.save(
            FixtureMatchCollectState(
                fixture = target,
                matchCollectStatus = MatchCollectStatus.FAIL_END,
            ),
        )
        stateRepository.save(
            FixtureMatchCollectState(
                fixture = saveFixture("fixture-admin-fixture-status-other"),
                matchCollectStatus = MatchCollectStatus.FAIL_END,
            ),
        )
        em.flush()
        em.clear()

        val result = stateRepository.findAdminStateByFixture_Uid(target.uid)

        assertThat(result?.fixture?.uid).isEqualTo(target.uid)
        assertThat(result?.matchCollectStatus).isEqualTo(MatchCollectStatus.FAIL_END)
    }

    private fun saveFixture(
        uid: String,
        leagueUid: String = "league-$uid",
        kickoff: Instant = Instant.parse("2026-06-15T09:00:00Z"),
        fixtureAvailable: Boolean = false,
        leagueAvailable: Boolean = true,
        matchCollect: MatchCollect = MatchCollect.FINISHED,
    ): FixtureCore {
        val league = findOrSaveLeague(leagueUid, leagueAvailable, matchCollect)
        return fixtureCoreRepository.save(
            FixtureCore(
                uid = uid,
                kickoff = kickoff,
                statusText = "Not Started",
                statusCode = FixtureStatusCode.NS,
                league = league,
                homeTeam = null,
                awayTeam = null,
                available = fixtureAvailable,
                autoGenerated = false,
            ),
        )
    }

    private fun saveFixtureWithSeasonLeague(
        uid: String,
        legacyLeagueUid: String,
        seasonLeagueUid: String,
        kickoff: Instant = Instant.parse("2026-06-15T09:00:00Z"),
    ): FixtureCore {
        val legacyLeague = findOrSaveLeague(legacyLeagueUid)
        val seasonLeague = findOrSaveLeague(seasonLeagueUid)
        val season =
            leagueSeasonCoreRepository.save(
                LeagueSeasonCore(
                    league = seasonLeague,
                    seasonYear = 2026,
                    current = true,
                    autoGenerated = false,
                ),
            )
        return fixtureCoreRepository.save(
            FixtureCore(
                uid = uid,
                kickoff = kickoff,
                statusText = "Not Started",
                statusCode = FixtureStatusCode.NS,
                league = legacyLeague,
                leagueSeason = season,
                homeTeam = null,
                awayTeam = null,
                available = false,
                autoGenerated = false,
            ),
        )
    }

    private fun findOrSaveLeague(
        leagueUid: String,
        leagueAvailable: Boolean = true,
        matchCollect: MatchCollect = MatchCollect.FINISHED,
    ): LeagueCore =
        leagueCoreRepository.findByUid(leagueUid)
            ?: leagueCoreRepository.save(
                LeagueCore(
                    uid = leagueUid,
                    name = "League $leagueUid",
                    available = leagueAvailable,
                    matchCollect = matchCollect,
                    autoGenerated = false,
                ),
            )
}
