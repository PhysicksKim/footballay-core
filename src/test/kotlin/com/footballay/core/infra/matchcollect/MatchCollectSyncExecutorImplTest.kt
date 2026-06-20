package com.footballay.core.infra.matchcollect

import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.domain.league.MatchCollect
import com.footballay.core.domain.matchcollect.MatchCollectStatus
import com.footballay.core.infra.dispatcher.match.MatchDataSyncDispatcher
import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.entity.FixtureMatchCollectState
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.LeagueSeasonCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.persistence.core.repository.FixtureMatchCollectStateRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class MatchCollectSyncExecutorImplTest {
    @Mock
    private lateinit var fixtureCoreRepository: FixtureCoreRepository

    @Mock
    private lateinit var stateRepository: FixtureMatchCollectStateRepository

    @Mock
    private lateinit var dispatcher: MatchDataSyncDispatcher

    private lateinit var executor: MatchCollectSyncExecutorImpl

    private val kickoff = Instant.parse("2026-06-15T00:00:00Z")

    @BeforeEach
    fun setUp() {
        executor =
            MatchCollectSyncExecutorImpl(
                fixtureCoreRepository = fixtureCoreRepository,
                stateRepository = stateRepository,
                dispatcher = dispatcher,
            )
    }

    @Test
    fun `finished checkpoint 수집 성공 시 EARLY_SYNCED state를 저장한다`() {
        val fixture = fixture("fixture-1")
        val now = kickoff.plusSeconds(3 * 60 * 60)
        whenever(fixtureCoreRepository.findNullableByUid(fixture.uid)).thenReturn(fixture)
        whenever(stateRepository.findByFixture_Uid(fixture.uid)).thenReturn(null)
        whenever(stateRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(dispatcher.syncByFixtureUid(fixture.uid))
            .thenReturn(MatchDataSyncResult.PostMatch(kickoff, shouldStopPolling = false, minutesSinceFinish = 20))

        val result = executor.collectFinished(fixture.uid, now)

        assertThat(result).isInstanceOf(MatchCollectExecutionResult.Collected::class.java)
        result as MatchCollectExecutionResult.Collected
        assertThat(result.status).isEqualTo(MatchCollectStatus.EARLY_SYNCED)
        assertThat(result.collectedAt).isEqualTo(now)

        verify(stateRepository).save(
            org.mockito.kotlin.argThat {
                fixture.uid == "fixture-1" &&
                    matchCollectStatus == MatchCollectStatus.EARLY_SYNCED &&
                    lastCollectedAt == now
            },
        )
    }

    @Test
    fun `final checkpoint 수집 성공 시 SUCCESS state를 저장한다`() {
        val fixture = fixture("fixture-final")
        val now = kickoff.plusSeconds(12 * 60 * 60)
        whenever(fixtureCoreRepository.findNullableByUid(fixture.uid)).thenReturn(fixture)
        whenever(stateRepository.findByFixture_Uid(fixture.uid)).thenReturn(null)
        whenever(stateRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(dispatcher.syncByFixtureUid(fixture.uid))
            .thenReturn(MatchDataSyncResult.PostMatch(kickoff, shouldStopPolling = true, minutesSinceFinish = 120))

        val result = executor.collectFinished(fixture.uid, now)

        assertThat(result).isInstanceOf(MatchCollectExecutionResult.Collected::class.java)
        assertThat((result as MatchCollectExecutionResult.Collected).status).isEqualTo(MatchCollectStatus.SUCCESS)
    }

    @Test
    fun `not played 결과는 NOT_PLAYED state를 저장한다`() {
        val fixture = fixture("fixture-not-played")
        val now = kickoff.plusSeconds(5 * 60 * 60)
        whenever(fixtureCoreRepository.findNullableByUid(fixture.uid)).thenReturn(fixture)
        whenever(stateRepository.findByFixture_Uid(fixture.uid)).thenReturn(null)
        whenever(stateRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(dispatcher.syncByFixtureUid(fixture.uid))
            .thenReturn(MatchDataSyncResult.NotPlayed(FixtureStatusCode.CANC, kickoff))

        val result = executor.collectFinished(fixture.uid, now)

        assertThat(result).isInstanceOf(MatchCollectExecutionResult.Collected::class.java)
        assertThat((result as MatchCollectExecutionResult.Collected).status).isEqualTo(MatchCollectStatus.NOT_PLAYED)
    }

    @Test
    fun `final checkpoint 이전 sync error는 state를 변경하지 않는다`() {
        val fixture = fixture("fixture-error")
        val existingState =
            FixtureMatchCollectState(
                fixture = fixture,
                matchCollectStatus = MatchCollectStatus.EARLY_SYNCED,
                lastCollectedAt = kickoff.plusSeconds(3 * 60 * 60),
            )
        whenever(fixtureCoreRepository.findNullableByUid(fixture.uid)).thenReturn(fixture)
        whenever(stateRepository.findByFixture_Uid(fixture.uid)).thenReturn(existingState)
        whenever(dispatcher.syncByFixtureUid(fixture.uid))
            .thenReturn(MatchDataSyncResult.Error("provider failed", kickoff))

        val result = executor.collectFinished(fixture.uid, kickoff.plusSeconds(5 * 60 * 60))

        assertThat(result).isEqualTo(MatchCollectExecutionResult.Failed(fixture.uid, "provider failed"))
        assertThat(existingState.matchCollectStatus).isEqualTo(MatchCollectStatus.EARLY_SYNCED)
        assertThat(existingState.lastCollectedAt).isEqualTo(kickoff.plusSeconds(3 * 60 * 60))
        verify(stateRepository, never()).save(any())
    }

    @Test
    fun `final checkpoint sync error는 FAIL_END state를 저장한다`() {
        val fixture = fixture("fixture-final-error")
        val existingState =
            FixtureMatchCollectState(
                fixture = fixture,
                matchCollectStatus = MatchCollectStatus.EARLY_SYNCED,
                lastCollectedAt = kickoff.plusSeconds(5 * 60 * 60),
            )
        val now = kickoff.plusSeconds(12 * 60 * 60)
        whenever(fixtureCoreRepository.findNullableByUid(fixture.uid)).thenReturn(fixture)
        whenever(stateRepository.findByFixture_Uid(fixture.uid)).thenReturn(existingState)
        whenever(dispatcher.syncByFixtureUid(fixture.uid))
            .thenReturn(MatchDataSyncResult.Error("provider failed", kickoff))

        val result = executor.collectFinished(fixture.uid, now)

        assertThat(result).isEqualTo(MatchCollectExecutionResult.Failed(fixture.uid, "provider failed"))
        assertThat(existingState.matchCollectStatus).isEqualTo(MatchCollectStatus.FAIL_END)
        assertThat(existingState.lastCollectedAt).isEqualTo(now)
        verify(stateRepository, never()).save(any())
    }

    @Test
    fun `state가 없는 final checkpoint sync error는 FAIL_END state를 생성한다`() {
        val fixture = fixture("fixture-final-error-no-state")
        val now = kickoff.plusSeconds(12 * 60 * 60)
        whenever(fixtureCoreRepository.findNullableByUid(fixture.uid)).thenReturn(fixture)
        whenever(stateRepository.findByFixture_Uid(fixture.uid)).thenReturn(null)
        whenever(stateRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(dispatcher.syncByFixtureUid(fixture.uid))
            .thenReturn(MatchDataSyncResult.Error("provider failed", kickoff))

        val result = executor.collectFinished(fixture.uid, now)

        assertThat(result).isEqualTo(MatchCollectExecutionResult.Failed(fixture.uid, "provider failed"))
        verify(stateRepository).save(
            org.mockito.kotlin.argThat {
                fixture.uid == "fixture-final-error-no-state" &&
                    matchCollectStatus == MatchCollectStatus.FAIL_END &&
                    lastCollectedAt == now
            },
        )
    }

    @Test
    fun `checkpoint에 도달하지 않았으면 sync를 실행하지 않는다`() {
        val fixture = fixture("fixture-before-checkpoint")
        whenever(fixtureCoreRepository.findNullableByUid(fixture.uid)).thenReturn(fixture)

        val result = executor.collectFinished(fixture.uid, kickoff.plusSeconds(3 * 60 * 60).minusSeconds(1))

        assertThat(result).isInstanceOf(MatchCollectExecutionResult.Skipped::class.java)
        verify(dispatcher, never()).syncByFixtureUid(any())
    }

    @Test
    fun `current season fixture가 아니면 sync를 실행하지 않는다`() {
        val fixture = fixture("fixture-old-season", currentSeason = false)
        whenever(fixtureCoreRepository.findNullableByUid(fixture.uid)).thenReturn(fixture)

        val result = executor.collectFinished(fixture.uid, kickoff.plusSeconds(12 * 60 * 60))

        assertThat(result).isInstanceOf(MatchCollectExecutionResult.Skipped::class.java)
        verify(dispatcher, never()).syncByFixtureUid(any())
    }

    @Test
    fun `이미 checkpoint 이후 수집된 state가 있으면 sync를 실행하지 않는다`() {
        val fixture = fixture("fixture-already-collected")
        val state =
            FixtureMatchCollectState(
                fixture = fixture,
                matchCollectStatus = MatchCollectStatus.EARLY_SYNCED,
                lastCollectedAt = kickoff.plusSeconds(3 * 60 * 60),
            )
        whenever(fixtureCoreRepository.findNullableByUid(fixture.uid)).thenReturn(fixture)
        whenever(stateRepository.findByFixture_Uid(fixture.uid)).thenReturn(state)

        val result = executor.collectFinished(fixture.uid, kickoff.plusSeconds(4 * 60 * 60))

        assertThat(result).isInstanceOf(MatchCollectExecutionResult.Skipped::class.java)
        verify(dispatcher, never()).syncByFixtureUid(any())
    }

    @Test
    fun `LIVE pre 수집은 dispatcher를 호출하고 EARLY_SYNCED state를 저장한다`() {
        val fixture = fixture("fixture-live-pre", matchCollect = MatchCollect.LIVE)
        val now = kickoff.minusSeconds(30 * 60)
        whenever(fixtureCoreRepository.findNullableByUid(fixture.uid)).thenReturn(fixture)
        whenever(stateRepository.findByFixture_Uid(fixture.uid)).thenReturn(null)
        whenever(stateRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(dispatcher.syncByFixtureUid(fixture.uid))
            .thenReturn(MatchDataSyncResult.PreMatch(lineupCached = true, kickoffTime = kickoff, shouldTerminatePreMatchJob = false))

        val result = executor.collectPre(fixture.uid, now)

        assertThat(result).isInstanceOf(MatchCollectExecutionResult.Collected::class.java)
        assertThat((result as MatchCollectExecutionResult.Collected).status).isEqualTo(MatchCollectStatus.EARLY_SYNCED)
        verify(dispatcher).syncByFixtureUid(fixture.uid)
        verify(stateRepository).save(
            org.mockito.kotlin.argThat {
                this.fixture.uid == "fixture-live-pre" &&
                    matchCollectStatus == MatchCollectStatus.EARLY_SYNCED &&
                    lastCollectedAt == now
            },
        )
    }

    @Test
    fun `LIVE live 수집은 dispatcher를 호출하고 EARLY_SYNCED state를 저장한다`() {
        val fixture = fixture("fixture-live-live", matchCollect = MatchCollect.LIVE)
        val now = kickoff.plusSeconds(30 * 60)
        whenever(fixtureCoreRepository.findNullableByUid(fixture.uid)).thenReturn(fixture)
        whenever(stateRepository.findByFixture_Uid(fixture.uid)).thenReturn(null)
        whenever(stateRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(dispatcher.syncByFixtureUid(fixture.uid))
            .thenReturn(MatchDataSyncResult.Live(kickoffTime = kickoff, elapsedMin = 30, statusCode = FixtureStatusCode.SECOND_HALF))

        val result = executor.collectLive(fixture.uid, now)

        assertThat(result).isInstanceOf(MatchCollectExecutionResult.Collected::class.java)
        assertThat((result as MatchCollectExecutionResult.Collected).status).isEqualTo(MatchCollectStatus.EARLY_SYNCED)
        verify(dispatcher).syncByFixtureUid(fixture.uid)
    }

    @Test
    fun `LIVE post 수집은 PostMatch 결과면 SUCCESS state를 저장한다`() {
        val fixture = fixture("fixture-live-post", matchCollect = MatchCollect.LIVE)
        val now = kickoff.plusSeconds(5 * 60 * 60)
        whenever(fixtureCoreRepository.findNullableByUid(fixture.uid)).thenReturn(fixture)
        whenever(stateRepository.findByFixture_Uid(fixture.uid)).thenReturn(null)
        whenever(stateRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(dispatcher.syncByFixtureUid(fixture.uid))
            .thenReturn(MatchDataSyncResult.PostMatch(kickoffTime = kickoff, shouldStopPolling = true, minutesSinceFinish = 90))

        val result = executor.collectPost(fixture.uid, now)

        assertThat(result).isInstanceOf(MatchCollectExecutionResult.Collected::class.java)
        assertThat((result as MatchCollectExecutionResult.Collected).status).isEqualTo(MatchCollectStatus.SUCCESS)
        verify(dispatcher).syncByFixtureUid(fixture.uid)
    }

    @Test
    fun `LIVE 수집에서 not played 결과는 NOT_PLAYED state를 저장한다`() {
        val fixture = fixture("fixture-live-not-played", matchCollect = MatchCollect.LIVE)
        val now = kickoff.plusSeconds(60 * 60)
        whenever(fixtureCoreRepository.findNullableByUid(fixture.uid)).thenReturn(fixture)
        whenever(stateRepository.findByFixture_Uid(fixture.uid)).thenReturn(null)
        whenever(stateRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(dispatcher.syncByFixtureUid(fixture.uid))
            .thenReturn(MatchDataSyncResult.NotPlayed(FixtureStatusCode.CANC, kickoff))

        val result = executor.collectLive(fixture.uid, now)

        assertThat(result).isInstanceOf(MatchCollectExecutionResult.Collected::class.java)
        assertThat((result as MatchCollectExecutionResult.Collected).status).isEqualTo(MatchCollectStatus.NOT_PLAYED)
        verify(dispatcher).syncByFixtureUid(fixture.uid)
    }

    @Test
    fun `LIVE 수집에서 error 결과는 state를 변경하지 않는다`() {
        val fixture = fixture("fixture-live-error", matchCollect = MatchCollect.LIVE)
        val existingState =
            FixtureMatchCollectState(
                fixture = fixture,
                matchCollectStatus = MatchCollectStatus.EARLY_SYNCED,
                lastCollectedAt = kickoff,
            )
        whenever(fixtureCoreRepository.findNullableByUid(fixture.uid)).thenReturn(fixture)
        whenever(stateRepository.findByFixture_Uid(fixture.uid)).thenReturn(existingState)
        whenever(dispatcher.syncByFixtureUid(fixture.uid))
            .thenReturn(MatchDataSyncResult.Error("provider failed", kickoff))

        val result = executor.collectLive(fixture.uid, kickoff.plusSeconds(60 * 60))

        assertThat(result).isEqualTo(MatchCollectExecutionResult.Failed(fixture.uid, "provider failed"))
        assertThat(existingState.matchCollectStatus).isEqualTo(MatchCollectStatus.EARLY_SYNCED)
        assertThat(existingState.lastCollectedAt).isEqualTo(kickoff)
        verify(stateRepository, never()).save(any())
    }

    @Test
    fun `LIVE 수집 대상이 아니면 dispatcher를 호출하지 않는다`() {
        val fixture = fixture("fixture-live-skip", matchCollect = MatchCollect.FINISHED)
        whenever(fixtureCoreRepository.findNullableByUid(fixture.uid)).thenReturn(fixture)

        val result = executor.collectLive(fixture.uid, kickoff.plusSeconds(60 * 60))

        assertThat(result).isInstanceOf(MatchCollectExecutionResult.Skipped::class.java)
        verify(dispatcher, never()).syncByFixtureUid(any())
    }

    @Test
    fun `SUCCESS state인 LIVE fixture는 dispatcher를 호출하지 않는다`() {
        val fixture = fixture("fixture-live-success", matchCollect = MatchCollect.LIVE)
        val state =
            FixtureMatchCollectState(
                fixture = fixture,
                matchCollectStatus = MatchCollectStatus.SUCCESS,
                lastCollectedAt = kickoff.plusSeconds(5 * 60 * 60),
            )
        whenever(fixtureCoreRepository.findNullableByUid(fixture.uid)).thenReturn(fixture)
        whenever(stateRepository.findByFixture_Uid(fixture.uid)).thenReturn(state)

        val result = executor.collectPost(fixture.uid, kickoff.plusSeconds(6 * 60 * 60))

        assertThat(result).isInstanceOf(MatchCollectExecutionResult.Skipped::class.java)
        verify(dispatcher, never()).syncByFixtureUid(any())
    }

    private fun fixture(
        uid: String,
        currentSeason: Boolean = true,
        leagueAvailable: Boolean = true,
        matchCollect: MatchCollect = MatchCollect.FINISHED,
        fixtureAvailable: Boolean = false,
    ): FixtureCore {
        val league =
            LeagueCore(
                uid = "league-$uid",
                name = "League $uid",
                available = leagueAvailable,
                matchCollect = matchCollect,
                autoGenerated = false,
            )
        val season =
            LeagueSeasonCore(
                league = league,
                seasonYear = 2026,
                current = currentSeason,
                autoGenerated = false,
            )
        return FixtureCore(
            uid = uid,
            kickoff = kickoff,
            statusText = "Not Started",
            statusCode = FixtureStatusCode.NS,
            league = league,
            leagueSeason = season,
            homeTeam = null,
            awayTeam = null,
            available = fixtureAvailable,
            autoGenerated = false,
        )
    }
}
