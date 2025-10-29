package com.footballay.core.infra.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.infra.core.FixtureCoreQueryService
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.scheduler.JobSchedulerService
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/**
 * Available Fixture 관리 Facade
 *
 * Fixture의 available 상태와 관련 Job 생명주기를 관리하는 Domain Facade입니다.
 *
 * **동작 흐름:**
 * 1. Fixture available = true 설정
 * 2. PreMatchJob 등록 (킥오프 1시간 전부터 시작 권장)
 * 3. PreMatchJob → LiveMatchJob → PostMatchJob 자동 전환 (Dispatcher가 관리)
 * 4. Fixture available = false 설정 → 모든 Job 삭제
 */
@Service
class AvailableFixtureFacade(
    private val fixtureCoreQueryService: FixtureCoreQueryService,
    private val fixtureCoreRepository: FixtureCoreRepository,
    private val jobSchedulerService: JobSchedulerService,
) {
    private val log = logger()

    /**
     * Fixture를 Available로 설정하고 PreMatchJob 등록
     *
     * @param fixtureId FixtureCore ID
     * @return 성공 시 fixture UID, 실패 시 DomainFail
     */
    @Transactional
    fun addAvailableFixture(fixtureId: Long): DomainResult<String, DomainFail> {
        log.info("Adding available fixture - fixtureId={}", fixtureId)

        // 1. FixtureCore 조회
        val fixtureCore =
            fixtureCoreQueryService.findById(fixtureId)
                ?: return DomainResult.Fail(
                    DomainFail.NotFound(
                        resource = "FIXTURE_CORE",
                        id = fixtureId.toString(),
                    ),
                )

        // 2. 이미 available인지 확인
        if (fixtureCore.available) {
            log.warn("Fixture is already available - fixtureId={}, uid={}", fixtureId, fixtureCore.uid)
            return DomainResult.Success(fixtureCore.uid)
        }

        // 3. available 플래그 설정
        fixtureCore.available = true
        fixtureCoreRepository.save(fixtureCore)

        // 4. PreMatchJob 등록
        val startTime = calculatePreMatchJobStartTime(fixtureCore.kickoff)
        val jobAdded = jobSchedulerService.addPreMatchJob(fixtureCore.uid, startTime)

        if (!jobAdded) {
            log.error("Failed to add PreMatchJob - fixtureId={}, uid={}", fixtureId, fixtureCore.uid)
            return DomainResult.Fail(
                DomainFail.Validation.single(
                    code = "JOB_REGISTRATION_FAILED",
                    message = "Failed to register PreMatchJob for fixture ${fixtureCore.uid}",
                    field = "fixtureId",
                ),
            )
        }

        log.info(
            "Available fixture added successfully - fixtureId={}, uid={}, kickoff={}, jobStartTime={}",
            fixtureId,
            fixtureCore.uid,
            fixtureCore.kickoff,
            startTime,
        )

        return DomainResult.Success(fixtureCore.uid)
    }

    /**
     * Fixture를 Unavailable로 설정하고 모든 Job 삭제
     *
     * @param fixtureId FixtureCore ID
     * @return 성공 시 fixture UID, 실패 시 DomainFail
     */
    @Transactional
    fun removeAvailableFixture(fixtureId: Long): DomainResult<String, DomainFail> {
        log.info("Removing available fixture - fixtureId={}", fixtureId)

        // 1. FixtureCore 조회
        val fixtureCore =
            fixtureCoreQueryService.findById(fixtureId)
                ?: return DomainResult.Fail(
                    DomainFail.NotFound(
                        resource = "FIXTURE_CORE",
                        id = fixtureId.toString(),
                    ),
                )

        // 2. 이미 unavailable인지 확인
        if (!fixtureCore.available) {
            log.warn("Fixture is already unavailable - fixtureId={}, uid={}", fixtureId, fixtureCore.uid)
            return DomainResult.Success(fixtureCore.uid)
        }

        // 3. available 플래그 해제
        fixtureCore.available = false
        fixtureCoreRepository.save(fixtureCore)

        // 4. 모든 Job 삭제
        val deletedCount = jobSchedulerService.removeAllJobsForFixture(fixtureCore.uid)

        log.info(
            "Available fixture removed successfully - fixtureId={}, uid={}, deletedJobs={}",
            fixtureId,
            fixtureCore.uid,
            deletedCount,
        )

        return DomainResult.Success(fixtureCore.uid)
    }

    /**
     * PreMatchJob 시작 시각 계산
     *
     * - 킥오프 1시간 전부터 시작 (권장)
     * - 킥오프 시각이 없으면 즉시 시작
     */
    private fun calculatePreMatchJobStartTime(kickoff: OffsetDateTime?): OffsetDateTime {
        if (kickoff == null) {
            return OffsetDateTime.now()
        }

        val oneHourBeforeKickoff = kickoff.minusHours(1)
        val now = OffsetDateTime.now()

        return if (oneHourBeforeKickoff.isBefore(now)) {
            // 이미 킥오프 1시간 전이 지났으면 즉시 시작
            now
        } else {
            // 킥오프 1시간 전부터 시작
            oneHourBeforeKickoff
        }
    }
}

