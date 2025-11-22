package com.footballay.core.infra.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.infra.core.FixtureCoreQueryService
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.entity.FixtureStatusCode
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.scheduler.JobSchedulerService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * AvailableFixtureFacade 단위 테스트
 *
 * Repository와 Service를 Mock으로 주입하여 Facade 로직만 테스트합니다.
 */
@Disabled("Mock 및 변경된 스펙으로 인한 비활성화")
@ExtendWith(MockitoExtension::class)
class AvailableFixtureFacadeTest {
    @Mock
    private lateinit var fixtureCoreQueryService: FixtureCoreQueryService

    @Mock
    private lateinit var fixtureCoreRepository: FixtureCoreRepository

    @Mock
    private lateinit var fixtureApiSportsRepository: FixtureApiSportsRepository

    @Mock
    private lateinit var jobSchedulerService: JobSchedulerService

    @InjectMocks
    private lateinit var facade: AvailableFixtureFacade

    @Test
    fun `Fixture available 설정 및 PreMatchJob, LiveMatchJob 동시 등록`() {
        // Given
        val fixtureId = 1L
        val fixtureUid = "fixture_uid_123"
        val kickoff = Instant.now().plus(2, ChronoUnit.HOURS)

        val fixtureCore =
            createFixtureCore(
                id = fixtureId,
                uid = fixtureUid,
                kickoff = kickoff,
                available = false,
            )

        val preMatchStartTime = kickoff.atZone(ZoneId.systemDefault()).toInstant().minus(1, ChronoUnit.HOURS)

        given(fixtureCoreQueryService.findById(fixtureId)).willReturn(fixtureCore)
        given(fixtureCoreRepository.save(fixtureCore)).willReturn(fixtureCore)
        given(jobSchedulerService.addPreMatchJob(fixtureUid, preMatchStartTime))
            .willReturn(true)
        given(jobSchedulerService.addLiveMatchJob(fixtureUid, kickoff))
            .willReturn(true)

        // When
        val result = facade.addAvailableFixture(fixtureId)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        assertThat((result as DomainResult.Success).value).isEqualTo(fixtureUid)
        assertThat(fixtureCore.available).isTrue()

        verify(fixtureCoreRepository).save(fixtureCore)
        verify(jobSchedulerService).addPreMatchJob(fixtureUid, kickoff.atZone(ZoneId.systemDefault()).toInstant().minus(1, ChronoUnit.HOURS))
        verify(jobSchedulerService).addLiveMatchJob(fixtureUid, kickoff)
        verify(jobSchedulerService, never()).removeAllJobsForFixture(fixtureUid)
    }

    @Test
    fun `이미 available인 Fixture는 Job 재등록 안함`() {
        // Given
        val fixtureId = 1L
        val fixtureUid = "fixture_uid_123"
        val kickoff = Instant.now().plus(2, ChronoUnit.HOURS)

        val fixtureCore =
            createFixtureCore(
                id = fixtureId,
                uid = fixtureUid,
                kickoff = kickoff,
                available = true,
            )

        given(fixtureCoreQueryService.findById(fixtureId)).willReturn(fixtureCore)

        // When
        val result = facade.addAvailableFixture(fixtureId)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        assertThat((result as DomainResult.Success).value).isEqualTo(fixtureUid)

        verify(fixtureCoreRepository, never()).save(fixtureCore)
        verify(jobSchedulerService, never()).addPreMatchJob(any(String::class.java), any(Instant::class.java))
        verify(jobSchedulerService, never()).addLiveMatchJob(any(String::class.java), any(Instant::class.java))
    }

    @Test
    fun `kickoff 시간이 null인 경우 에러 반환`() {
        // Given
        val fixtureId = 1L
        val fixtureUid = "fixture_uid_123"

        val fixtureCore =
            createFixtureCore(
                id = fixtureId,
                uid = fixtureUid,
                kickoff = null,
                available = false,
            )

        given(fixtureCoreQueryService.findById(fixtureId)).willReturn(fixtureCore)

        // When
        val result = facade.addAvailableFixture(fixtureId)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Fail::class.java)
        val fail = result as DomainResult.Fail
        assertThat(fail.error).isInstanceOf(DomainFail.Validation::class.java)
        val validation = fail.error as DomainFail.Validation
        assertThat(validation.errors.first().code).isEqualTo("KICKOFF_TIME_NOT_SET")

        verify(fixtureCoreRepository, never()).save(fixtureCore)
        verify(jobSchedulerService, never()).addPreMatchJob(any(String::class.java), any(Instant::class.java))
        verify(jobSchedulerService, never()).addLiveMatchJob(any(String::class.java), any(Instant::class.java))
    }

    @Test
    fun `킥오프 1시간 전 PreMatchJob 시작 시각 계산`() {
        // Given
        val fixtureId = 1L
        val fixtureUid = "fixture_uid_123"
        val kickoff = Instant.now().plus(2, ChronoUnit.HOURS)

        val fixtureCore =
            createFixtureCore(
                id = fixtureId,
                uid = fixtureUid,
                kickoff = kickoff,
                available = false,
            )

        given(fixtureCoreQueryService.findById(fixtureId)).willReturn(fixtureCore)
        given(fixtureCoreRepository.save(fixtureCore)).willReturn(fixtureCore)
        given(jobSchedulerService.addPreMatchJob(fixtureUid, kickoff.atZone(ZoneId.systemDefault()).toInstant().minus(1, ChronoUnit.HOURS)))
            .willReturn(true)
        given(jobSchedulerService.addLiveMatchJob(fixtureUid, kickoff))
            .willReturn(true)

        // When
        facade.addAvailableFixture(fixtureId)

        // Then
        verify(jobSchedulerService).addPreMatchJob(fixtureUid, kickoff.atZone(ZoneId.systemDefault()).toInstant().minus(1, ChronoUnit.HOURS))
    }

    @Test
    fun `킥오프가 이미 지난 경우 PreMatchJob 즉시 시작`() {
        // Given
        val fixtureId = 1L
        val fixtureUid = "fixture_uid_123"
        val kickoff = Instant.now().plus(30, ChronoUnit.MINUTES) // 킥오프 30분 전 (1시간 전은 이미 지남)

        val fixtureCore =
            createFixtureCore(
                id = fixtureId,
                uid = fixtureUid,
                kickoff = kickoff,
                available = false,
            )

        given(fixtureCoreQueryService.findById(fixtureId)).willReturn(fixtureCore)
        given(fixtureCoreRepository.save(fixtureCore)).willReturn(fixtureCore)
        // 킥오프 1시간 전이 이미 지났으므로 now()로 시작 시간이 계산됨
        // 시간 계산 때문에 정확한 값 매칭이 어려우므로 ArgumentCaptor 사용
        val fixtureUidCaptor = ArgumentCaptor.forClass(String::class.java)
        val startTimeCaptor = ArgumentCaptor.forClass(Instant::class.java)
        given(jobSchedulerService.addPreMatchJob(any(String::class.java), any(Instant::class.java))).willReturn(true)
        given(jobSchedulerService.addLiveMatchJob(fixtureUid, kickoff))
            .willReturn(true)

        val beforeExecution = Instant.now()

        // When
        facade.addAvailableFixture(fixtureId)

        // Then
        val afterExecution = Instant.now()
        verify(jobSchedulerService).addPreMatchJob(fixtureUidCaptor.capture(), startTimeCaptor.capture())
        assertThat(fixtureUidCaptor.value).isEqualTo(fixtureUid)
        // 실제 호출된 시간이 now()와 가까운지 확인 (beforeExecution ~ afterExecution 사이)
        assertThat(startTimeCaptor.value).isAfterOrEqualTo(beforeExecution.minusSeconds(1))
        assertThat(startTimeCaptor.value).isBeforeOrEqualTo(afterExecution.plusSeconds(1))
    }

    @Test
    fun `PreMatchJob 등록 실패 시 에러 반환`() {
        // Given
        val fixtureId = 1L
        val fixtureUid = "fixture_uid_123"
        val kickoff = Instant.now().plus(2, ChronoUnit.HOURS)

        val fixtureCore =
            createFixtureCore(
                id = fixtureId,
                uid = fixtureUid,
                kickoff = kickoff,
                available = false,
            )

        given(fixtureCoreQueryService.findById(fixtureId)).willReturn(fixtureCore)
        given(fixtureCoreRepository.save(fixtureCore)).willReturn(fixtureCore)
        given(jobSchedulerService.addPreMatchJob(fixtureUid, kickoff.atZone(ZoneId.systemDefault()).toInstant().minus(1, ChronoUnit.HOURS)))
            .willReturn(false)

        // When
        val result = facade.addAvailableFixture(fixtureId)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Fail::class.java)
        val fail = result as DomainResult.Fail
        assertThat(fail.error).isInstanceOf(DomainFail.Validation::class.java)
        val validation = fail.error as DomainFail.Validation
        assertThat(validation.errors.first().code).isEqualTo("PRE_MATCH_JOB_REGISTRATION_FAILED")

        verify(jobSchedulerService, never()).addLiveMatchJob(any(String::class.java), any(Instant::class.java))
        verify(jobSchedulerService, never()).removeAllJobsForFixture(any(String::class.java))
    }

    @Test
    fun `LiveMatchJob 등록 실패 시 PreMatchJob 롤백`() {
        // Given
        val fixtureId = 1L
        val fixtureUid = "fixture_uid_123"
        val kickoff = Instant.now().plus(2, ChronoUnit.HOURS)

        val fixtureCore =
            createFixtureCore(
                id = fixtureId,
                uid = fixtureUid,
                kickoff = kickoff,
                available = false,
            )

        given(fixtureCoreQueryService.findById(fixtureId)).willReturn(fixtureCore)
        given(fixtureCoreRepository.save(fixtureCore)).willReturn(fixtureCore)
        given(jobSchedulerService.addPreMatchJob(fixtureUid, kickoff.atZone(ZoneId.systemDefault()).toInstant().minus(1, ChronoUnit.HOURS)))
            .willReturn(true)
        given(jobSchedulerService.addLiveMatchJob(fixtureUid, kickoff))
            .willReturn(false)

        // When
        val result = facade.addAvailableFixture(fixtureId)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Fail::class.java)
        val fail = result as DomainResult.Fail
        assertThat(fail.error).isInstanceOf(DomainFail.Validation::class.java)
        val validation = fail.error as DomainFail.Validation
        assertThat(validation.errors.first().code).isEqualTo("LIVE_MATCH_JOB_REGISTRATION_FAILED")

        verify(jobSchedulerService).removeAllJobsForFixture(fixtureUid)
    }

    @Test
    fun `존재하지 않는 Fixture는 NotFound 반환`() {
        // Given
        val fixtureId = 999L

        given(fixtureCoreQueryService.findById(fixtureId)).willReturn(null)

        // When
        val result = facade.addAvailableFixture(fixtureId)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Fail::class.java)
        val fail = result as DomainResult.Fail
        assertThat(fail.error).isInstanceOf(DomainFail.NotFound::class.java)
        val notFound = fail.error as DomainFail.NotFound
        assertThat(notFound.resource).isEqualTo("FIXTURE_CORE")
        assertThat(notFound.id).isEqualTo("999")
    }

    @Test
    fun `Fixture unavailable 설정 및 모든 Job 삭제`() {
        // Given
        val fixtureId = 1L
        val fixtureUid = "fixture_uid_123"

        val fixtureCore =
            createFixtureCore(
                id = fixtureId,
                uid = fixtureUid,
                kickoff = Instant.now().plus(2, ChronoUnit.HOURS),
                available = true,
            )

        given(fixtureCoreQueryService.findById(fixtureId)).willReturn(fixtureCore)
        given(fixtureCoreRepository.save(fixtureCore)).willReturn(fixtureCore)
        given(jobSchedulerService.removeAllJobsForFixture(fixtureUid)).willReturn(2)

        // When
        val result = facade.removeAvailableFixture(fixtureId)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        assertThat((result as DomainResult.Success).value).isEqualTo(fixtureUid)
        assertThat(fixtureCore.available).isFalse()

        verify(fixtureCoreRepository).save(fixtureCore)
        verify(jobSchedulerService).removeAllJobsForFixture(fixtureUid)
    }

    @Test
    fun `이미 unavailable인 Fixture는 처리 안함`() {
        // Given
        val fixtureId = 1L
        val fixtureUid = "fixture_uid_123"

        val fixtureCore =
            createFixtureCore(
                id = fixtureId,
                uid = fixtureUid,
                kickoff = Instant.now().plus(2, ChronoUnit.HOURS),
                available = false,
            )

        given(fixtureCoreQueryService.findById(fixtureId)).willReturn(fixtureCore)

        // When
        val result = facade.removeAvailableFixture(fixtureId)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        assertThat((result as DomainResult.Success).value).isEqualTo(fixtureUid)

        verify(fixtureCoreRepository, never()).save(fixtureCore)
        verify(jobSchedulerService, never()).removeAllJobsForFixture(any())
    }

    @Test
    fun `Unavailable 설정 시 존재하지 않는 Fixture는 NotFound 반환`() {
        // Given
        val fixtureId = 999L

        given(fixtureCoreQueryService.findById(fixtureId)).willReturn(null)

        // When
        val result = facade.removeAvailableFixture(fixtureId)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Fail::class.java)
        val fail = result as DomainResult.Fail
        assertThat(fail.error).isInstanceOf(DomainFail.NotFound::class.java)
    }

    /**
     * 테스트용 FixtureCore 생성 헬퍼
     */
    private fun createFixtureCore(
        id: Long,
        uid: String,
        kickoff: Instant?,
        available: Boolean,
    ): FixtureCore {
        val league =
            LeagueCore(
                id = 1L,
                uid = "league_uid_123",
                name = "Test League",
                available = false,
                autoGenerated = true,
            )

        return FixtureCore(
            id = id,
            uid = uid,
            kickoff = kickoff,
            statusText = "Not Started",
            statusCode = FixtureStatusCode.NS,
            elapsedMin = null,
            league = league,
            homeTeam = null,
            awayTeam = null,
            goalsHome = null,
            goalsAway = null,
            finished = false,
            available = available,
            autoGenerated = false,
        )
    }
}
