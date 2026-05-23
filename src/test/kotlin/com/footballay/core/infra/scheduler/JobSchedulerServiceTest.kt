package com.footballay.core.infra.scheduler

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.impl.matchers.GroupMatcher
import org.mockito.Mock
import java.time.Instant
import java.util.Date

@ExtendWith(MockitoExtension::class)
class JobSchedulerServiceTest {
    @Mock
    private lateinit var scheduler: Scheduler

    @Test
    fun `MatchJobIdentity는 group job trigger key를 생성한다`() {
        val identity =
            MatchJobIdentity(
                owner = MatchJobOwner.AVAILABLE,
                phase = MatchJobPhase.PRE,
                leagueUid = "league-1",
                fixtureUid = "fixture-1",
            )

        assertThat(identity.groupName).isEqualTo("league:match:league-1")
        assertThat(identity.jobName).isEqualTo("available:pre:fixture-1")
        assertThat(identity.jobKey).isEqualTo(JobKey.jobKey("available:pre:fixture-1", "league:match:league-1"))
        assertThat(identity.triggerKey.name).isEqualTo("available:pre:fixture-1:trigger")
        assertThat(identity.triggerKey.group).isEqualTo("league:match:league-1")
        assertThat(MatchJobIdentity.leagueUidFromGroup(identity.groupName)).isEqualTo("league-1")
    }

    @Test
    fun `신규 match job을 등록한다`() {
        val service = JobSchedulerService(scheduler)
        val identity = availableIdentity(MatchJobPhase.PRE)
        val schedule = preSchedule(Instant.parse("2026-05-14T10:00:00Z"))
        val jobCaptor = argumentCaptor<JobDetail>()
        val triggerCaptor = argumentCaptor<Trigger>()
        whenever(scheduler.checkExists(identity.jobKey)).thenReturn(false)
        whenever(scheduler.scheduleJob(any<JobDetail>(), any<Trigger>())).thenReturn(Date.from(schedule.startAt))

        val result = service.registerOrReplace(identity, schedule)

        assertThat(result).isTrue()
        verify(scheduler).scheduleJob(jobCaptor.capture(), triggerCaptor.capture())

        val job = jobCaptor.firstValue
        val trigger = triggerCaptor.firstValue as SimpleTrigger
        assertThat(job.key).isEqualTo(identity.jobKey)
        assertThat(job.jobClass).isEqualTo(PreMatchJob::class.java)
        assertThat(job.jobDataMap.getString(JobSchedulerService.KEY_FIXTURE_UID)).isEqualTo(identity.fixtureUid)
        assertThat(trigger.key).isEqualTo(identity.triggerKey)
        assertThat(trigger.startTime).isEqualTo(Date.from(schedule.startAt))
        assertThat(trigger.repeatInterval).isEqualTo(schedule.repeatIntervalMillis)
        assertThat(trigger.repeatCount).isEqualTo(schedule.repeatCount)
    }

    @Test
    fun `기존 job spec이 같으면 재등록하지 않는다`() {
        val service = JobSchedulerService(scheduler)
        val identity = availableIdentity(MatchJobPhase.PRE)
        val schedule = preSchedule(Instant.parse("2026-05-14T10:00:00Z"))
        whenever(scheduler.checkExists(identity.jobKey)).thenReturn(true)
        whenever(scheduler.getJobDetail(identity.jobKey)).thenReturn(existingJob(identity, schedule))
        whenever(scheduler.getTriggersOfJob(identity.jobKey)).thenReturn(listOf(existingTrigger(identity, schedule)))

        val result = service.registerOrReplace(identity, schedule)

        assertThat(result).isTrue()
        verify(scheduler, never()).deleteJob(any())
        verify(scheduler, never()).scheduleJob(any<JobDetail>(), any<Trigger>())
    }

    @Test
    fun `기존 job spec이 다르면 삭제 후 재등록한다`() {
        val service = JobSchedulerService(scheduler)
        val identity = availableIdentity(MatchJobPhase.PRE)
        val schedule = preSchedule(Instant.parse("2026-05-14T10:00:00Z"))
        val oldSchedule = schedule.copy(repeatIntervalSeconds = 30)
        whenever(scheduler.checkExists(identity.jobKey)).thenReturn(true)
        whenever(scheduler.getJobDetail(identity.jobKey)).thenReturn(existingJob(identity, schedule))
        whenever(scheduler.getTriggersOfJob(identity.jobKey)).thenReturn(listOf(existingTrigger(identity, oldSchedule)))
        whenever(scheduler.deleteJob(identity.jobKey)).thenReturn(true)
        whenever(scheduler.scheduleJob(any<JobDetail>(), any<Trigger>())).thenReturn(Date.from(schedule.startAt))

        val result = service.registerOrReplace(identity, schedule)

        assertThat(result).isTrue()
        verify(scheduler).deleteJob(identity.jobKey)
        verify(scheduler).scheduleJob(any<JobDetail>(), any<Trigger>())
    }

    @Test
    fun `startAt 비교를 끄면 trigger start time이 달라도 같은 spec으로 본다`() {
        val service = JobSchedulerService(scheduler)
        val identity = availableIdentity(MatchJobPhase.POST)
        val schedule =
            preSchedule(Instant.parse("2026-05-14T10:00:00Z"))
                .copy(jobClass = PostMatchJob::class.java, compareStartAt = false)
        val oldSchedule = schedule.copy(startAt = Instant.parse("2026-05-14T09:00:00Z"))
        whenever(scheduler.checkExists(identity.jobKey)).thenReturn(true)
        whenever(scheduler.getJobDetail(identity.jobKey)).thenReturn(existingJob(identity, schedule))
        whenever(scheduler.getTriggersOfJob(identity.jobKey)).thenReturn(listOf(existingTrigger(identity, oldSchedule)))

        val result = service.registerOrReplaceDetailed(identity, schedule)

        assertThat(result).isEqualTo(MatchJobRegistrationResult.Unchanged)
        verify(scheduler, never()).deleteJob(any())
        verify(scheduler, never()).scheduleJob(any<JobDetail>(), any<Trigger>())
    }

    @Test
    fun `available pre live post job을 fixture 기준으로 삭제한다`() {
        val service = JobSchedulerService(scheduler)
        whenever(scheduler.deleteJob(any())).thenReturn(true)

        val deletedCount =
            service.deleteFixtureJobs(
                leagueUid = "league-1",
                fixtureUid = "fixture-1",
                owner = MatchJobOwner.AVAILABLE,
            )

        assertThat(deletedCount).isEqualTo(3)
        verify(scheduler).deleteJob(JobKey.jobKey("available:pre:fixture-1", "league:match:league-1"))
        verify(scheduler).deleteJob(JobKey.jobKey("available:live:fixture-1", "league:match:league-1"))
        verify(scheduler).deleteJob(JobKey.jobKey("available:post:fixture-1", "league:match:league-1"))
    }

    @Test
    fun `리그 match job 목록을 조회한다`() {
        val service = JobSchedulerService(scheduler)
        val jobs = setOf(availableIdentity(MatchJobPhase.PRE).jobKey, availableIdentity(MatchJobPhase.LIVE).jobKey)
        whenever(scheduler.getJobKeys(any<GroupMatcher<JobKey>>())).thenReturn(jobs)

        val result = service.listLeagueMatchJobs("league-1")

        assertThat(result).isEqualTo(jobs)
    }

    @Test
    fun `available pre wrapper는 새 identity 규칙으로 등록한다`() {
        val service = JobSchedulerService(scheduler)
        val startAt = Instant.parse("2026-05-14T10:00:00Z")
        val jobCaptor = argumentCaptor<JobDetail>()
        whenever(scheduler.checkExists(any<JobKey>())).thenReturn(false)
        whenever(scheduler.scheduleJob(any<JobDetail>(), any<Trigger>())).thenReturn(Date.from(startAt))

        val result = service.addPreMatchJob("league-1", "fixture-1", startAt)

        assertThat(result).isTrue()
        verify(scheduler).scheduleJob(jobCaptor.capture(), any<Trigger>())
        assertThat(jobCaptor.firstValue.key).isEqualTo(JobKey.jobKey("available:pre:fixture-1", "league:match:league-1"))
    }

    @Test
    fun `startup cleanup은 old available group과 current available owner job만 삭제한다`() {
        val service = JobSchedulerService(scheduler)
        val oldPreJob = JobKey.jobKey("pre-match-fixture-1", "pre-match")
        val oldLiveJob = JobKey.jobKey("live-match-fixture-1", "live-match")
        val currentAvailableJob = JobKey.jobKey("available:pre:fixture-2", "league:match:league-1")
        val currentMatchCollectJob = JobKey.jobKey("matchcollect:live:fixture-3", "league:match:league-1")
        val legacyFootballJob = JobKey.jobKey("live-match-100", "football-fixture")

        whenever(scheduler.getJobKeys(any<GroupMatcher<JobKey>>()))
            .thenReturn(setOf(oldPreJob))
            .thenReturn(setOf(oldLiveJob))
            .thenReturn(emptySet())
            .thenReturn(setOf(currentAvailableJob, currentMatchCollectJob))
        whenever(scheduler.getJobGroupNames()).thenReturn(listOf("league:match:league-1", "football-fixture"))
        whenever(scheduler.deleteJob(any())).thenReturn(true)

        val result = service.deleteStartupAvailableMatchJobs()

        assertThat(result.success).isTrue()
        assertThat(result.deleted).isEqualTo(3)
        assertThat(result.skipped).isEqualTo(1)
        verify(scheduler).deleteJob(oldPreJob)
        verify(scheduler).deleteJob(oldLiveJob)
        verify(scheduler).deleteJob(currentAvailableJob)
        verify(scheduler, never()).deleteJob(currentMatchCollectJob)
        verify(scheduler, never()).deleteJob(legacyFootballJob)
    }

    @Test
    fun `startup cleanup은 삭제할 job이 없어도 성공한다`() {
        val service = JobSchedulerService(scheduler)
        whenever(scheduler.getJobKeys(any<GroupMatcher<JobKey>>())).thenReturn(emptySet())
        whenever(scheduler.getJobGroupNames()).thenReturn(emptyList())

        val result = service.deleteStartupAvailableMatchJobs()

        assertThat(result.success).isTrue()
        assertThat(result.deleted).isZero()
        assertThat(result.skipped).isZero()
        verify(scheduler, never()).deleteJob(any())
    }

    @Test
    fun `startup cleanup은 group 조회 실패를 error로 기록한다`() {
        val service = JobSchedulerService(scheduler)
        whenever(scheduler.getJobKeys(any<GroupMatcher<JobKey>>()))
            .thenThrow(RuntimeException("quartz failed"))
        whenever(scheduler.getJobGroupNames()).thenReturn(emptyList())

        val result = service.deleteStartupAvailableMatchJobs()

        assertThat(result.success).isFalse()
        assertThat(result.errors).isNotEmpty
        assertThat(result.errors.first().operation).isEqualTo("list-jobs:legacy-available-group")
    }

    private fun availableIdentity(phase: MatchJobPhase): MatchJobIdentity =
        MatchJobIdentity(
            owner = MatchJobOwner.AVAILABLE,
            phase = phase,
            leagueUid = "league-1",
            fixtureUid = "fixture-1",
        )

    private fun preSchedule(startAt: Instant): MatchJobSchedule =
        MatchJobSchedule(
            jobClass = PreMatchJob::class.java,
            startAt = startAt,
            repeatIntervalSeconds = 60,
            repeatCount = 300,
        )

    private fun existingJob(
        identity: MatchJobIdentity,
        schedule: MatchJobSchedule,
    ): JobDetail =
        org.quartz.JobBuilder
            .newJob(schedule.jobClass)
            .withIdentity(identity.jobKey)
            .usingJobData(JobSchedulerService.KEY_FIXTURE_UID, identity.fixtureUid)
            .build()

    private fun existingTrigger(
        identity: MatchJobIdentity,
        schedule: MatchJobSchedule,
    ): SimpleTrigger =
        TriggerBuilder
            .newTrigger()
            .withIdentity(identity.triggerKey)
            .forJob(identity.jobKey)
            .startAt(Date.from(schedule.startAt))
            .withSchedule(
                SimpleScheduleBuilder
                    .simpleSchedule()
                    .withIntervalInSeconds(schedule.repeatIntervalSeconds)
                    .withRepeatCount(schedule.repeatCount),
            ).build() as SimpleTrigger
}
