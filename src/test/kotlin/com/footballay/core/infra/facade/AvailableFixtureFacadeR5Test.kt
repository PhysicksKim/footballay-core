package com.footballay.core.infra.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.fixture.FixtureStatusCode
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.scheduler.AvailableFixtureJobReconciler
import com.footballay.core.infra.scheduler.ReconcileError
import com.footballay.core.infra.scheduler.ReconcileResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class AvailableFixtureFacadeR5Test {
    @Mock
    private lateinit var fixtureCoreRepository: FixtureCoreRepository

    @Mock
    private lateinit var fixtureApiSportsRepository: FixtureApiSportsRepository

    @Mock
    private lateinit var availableFixtureJobReconciler: AvailableFixtureJobReconciler

    private lateinit var facade: AvailableFixtureFacade

    private val fixtureApiId = 100L
    private val fixtureUid = "fixture-1"

    @BeforeEach
    fun setUp() {
        facade =
            AvailableFixtureFacade(
                fixtureCoreRepository = fixtureCoreRepository,
                fixtureApiSportsRepository = fixtureApiSportsRepository,
                availableFixtureJobReconciler = availableFixtureJobReconciler,
            )
    }

    @Test
    fun `available true toggle은 flag 저장 후 reconciler를 호출한다`() {
        val fixture = fixture(available = false, kickoff = Instant.parse("2026-05-15T12:00:00Z"))
        val fixtureApi = fixtureApi(fixture)
        whenever(fixtureApiSportsRepository.findByApiId(fixtureApiId)).thenReturn(fixtureApi)
        whenever(fixtureCoreRepository.save(fixture)).thenReturn(fixture)
        whenever(fixtureApiSportsRepository.save(fixtureApi)).thenReturn(fixtureApi)
        whenever(availableFixtureJobReconciler.reconcileFixture(fixture)).thenReturn(successResult())

        val result = facade.addAvailableFixture(fixtureApiId)

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        assertThat((result as DomainResult.Success).value).isEqualTo(fixtureUid)
        assertThat(fixture.available).isTrue()
        assertThat(fixtureApi.available).isTrue()
        verify(availableFixtureJobReconciler).reconcileFixture(fixture)
    }

    @Test
    fun `available true toggle에서 reconcile 실패 시 flag rollback 후 best effort compensation을 시도한다`() {
        val fixture = fixture(available = false, kickoff = Instant.parse("2026-05-15T12:00:00Z"))
        val fixtureApi = fixtureApi(fixture)
        whenever(fixtureApiSportsRepository.findByApiId(fixtureApiId)).thenReturn(fixtureApi)
        whenever(fixtureCoreRepository.save(fixture)).thenReturn(fixture)
        whenever(fixtureApiSportsRepository.save(fixtureApi)).thenReturn(fixtureApi)
        whenever(availableFixtureJobReconciler.reconcileFixture(fixture)).thenReturn(failedResult())
        whenever(availableFixtureJobReconciler.reconcileFixture(fixtureUid)).thenReturn(successResult())

        val result = facade.addAvailableFixture(fixtureApiId)

        assertThat(result).isInstanceOf(DomainResult.Fail::class.java)
        val validation = (result as DomainResult.Fail).error as DomainFail.Validation
        assertThat(validation.errors.first().code).isEqualTo("AVAILABLE_FIXTURE_JOB_RECONCILE_FAILED")
        assertThat(fixture.available).isFalse()
        assertThat(fixtureApi.available).isFalse()
        verify(availableFixtureJobReconciler).reconcileFixture(fixtureUid)
    }

    @Test
    fun `available false toggle에서 cleanup reconcile 실패 시 flag rollback 후 best effort compensation을 시도한다`() {
        val fixture = fixture(available = true, kickoff = Instant.parse("2026-05-15T12:00:00Z"))
        val fixtureApi = fixtureApi(fixture, available = true)
        whenever(fixtureApiSportsRepository.findByApiId(fixtureApiId)).thenReturn(fixtureApi)
        whenever(fixtureCoreRepository.save(fixture)).thenReturn(fixture)
        whenever(fixtureApiSportsRepository.save(fixtureApi)).thenReturn(fixtureApi)
        whenever(availableFixtureJobReconciler.reconcileFixture(fixture)).thenReturn(failedResult())
        whenever(availableFixtureJobReconciler.reconcileFixture(fixtureUid)).thenReturn(successResult())

        val result = facade.removeAvailableFixture(fixtureApiId)

        assertThat(result).isInstanceOf(DomainResult.Fail::class.java)
        assertThat(fixture.available).isTrue()
        assertThat(fixtureApi.available).isTrue()
        verify(availableFixtureJobReconciler).reconcileFixture(fixtureUid)
    }

    @Test
    fun `kickoff null fixture는 available true toggle을 실패 처리한다`() {
        val fixture = fixture(available = false, kickoff = null)
        val fixtureApi = fixtureApi(fixture)
        whenever(fixtureApiSportsRepository.findByApiId(fixtureApiId)).thenReturn(fixtureApi)

        val result = facade.addAvailableFixture(fixtureApiId)

        assertThat(result).isInstanceOf(DomainResult.Fail::class.java)
        val validation = (result as DomainResult.Fail).error as DomainFail.Validation
        assertThat(validation.errors.first().code).isEqualTo("KICKOFF_TIME_NOT_SET")
        assertThat(fixture.available).isFalse()
    }

    private fun fixture(
        available: Boolean,
        kickoff: Instant?,
    ): FixtureCore =
        FixtureCore(
            id = 1L,
            uid = fixtureUid,
            kickoff = kickoff,
            statusText = "Not Started",
            statusCode = FixtureStatusCode.NS,
            elapsedMin = null,
            league =
                LeagueCore(
                    id = 1L,
                    uid = "league-1",
                    name = "League",
                    available = true,
                ),
            homeTeam = null,
            awayTeam = null,
            available = available,
        )

    private fun fixtureApi(
        fixture: FixtureCore,
        available: Boolean = false,
    ): FixtureApiSports =
        FixtureApiSports(
            id = 1L,
            core = fixture,
            apiId = fixtureApiId,
            available = available,
            season = null,
        )

    private fun successResult(): ReconcileResult =
        ReconcileResult.empty(
            fixtureUid = fixtureUid,
            leagueUid = "league-1",
        )

    private fun failedResult(): ReconcileResult =
        ReconcileResult.empty(
            fixtureUid = fixtureUid,
            leagueUid = "league-1",
        ).copy(
            success = false,
            errors =
                listOf(
                    ReconcileError(
                        fixtureUid = fixtureUid,
                        leagueUid = "league-1",
                        phase = null,
                        operation = "register-or-replace",
                        message = "failed",
                    ),
                ),
        )
}
