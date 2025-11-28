package com.footballay.core.infra.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.scheduler.JobSchedulerService
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

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
    private val fixtureCoreRepository: FixtureCoreRepository,
    private val fixtureApiSportsRepository: FixtureApiSportsRepository,
    private val jobSchedulerService: JobSchedulerService,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val log = logger()

    /**
     * Fixture를 Available로 설정하고 PreMatchJob, LiveMatchJob 등록
     *
     * **요구사항:**
     * - Fixture의 kickoff 시간이 확정되어야 함 (null 불가)
     * - 토너먼트 경기 등에서 kickoff가 미정인 경우 available 설정 불가
     * - 추후 kickoff 시간이 확정되면 다시 available 설정 시도 가능
     *
     * **Job 등록 전략:**
     * - PreMatchJob: kickoff 1시간 전 시작 (최소 킥오프 1분 전 종료)
     * - LiveMatchJob: kickoff 시간에 시작 (사전 등록)
     *
     * **개선 가능성:**
     * - 추후 kickoff 시간 변경 이벤트 처리
     * - 미확정 경기에 대한 대기열 시스템 도입
     *
     * @param fixtureApiId FixtureApiSports API ID
     * @return 성공 시 fixture UID, 실패 시 DomainFail
     */
    @Transactional
    fun addAvailableFixture(fixtureApiId: Long): DomainResult<String, DomainFail> {
        log.info("Adding available fixture - fixtureApiId={}", fixtureApiId)

        // 1. FixtureApiSports 조회
        val fixtureApiSports =
            fixtureApiSportsRepository.findByApiId(fixtureApiId)
                ?: return DomainResult.Fail(
                    DomainFail.NotFound(
                        resource = "FIXTURE_API_SPORTS",
                        id = fixtureApiId.toString(),
                    ),
                )

        // 2. FixtureCore 조회 (연관관계로 접근)
        val fixtureCore =
            fixtureApiSports.core
                ?: return DomainResult.Fail(
                    DomainFail.NotFound(
                        resource = "FIXTURE_CORE",
                        id = "core not linked to apiId=$fixtureApiId",
                    ),
                )

        // 3. 이미 available인지 확인
        if (fixtureCore.available) {
            log.warn("Fixture is already available - fixtureApiId={}, uid={}", fixtureApiId, fixtureCore.uid)
            return DomainResult.Success(fixtureCore.uid)
        }

        // 4. kickoff 시간이 확정되지 않은 경우 Job 등록 불가
        val kickoff = fixtureCore.kickoff
        if (kickoff == null) {
            log.warn("Cannot make fixture available - kickoff time is not set - fixtureApiId={}, uid={}", fixtureApiId, fixtureCore.uid)
            return DomainResult.Fail(
                DomainFail.Validation.single(
                    code = "KICKOFF_TIME_NOT_SET",
                    message = "경기 시작 시간이 미정입니다. 킥오프 시간 확정 후 다시 시도해주세요.",
                    field = "kickoff",
                ),
            )
        }

        val now = Instant.now(clock)

        // 5. FixtureCore available 플래그 설정
        setFixtureAvailableFlag(fixtureCore, fixtureApiSports, true)
        log.info(
            "FixtureApiSports available updated - fixtureApiId={}, uid={}, available=true, kickoff={}, now={}",
            fixtureApiId,
            fixtureCore.uid,
            kickoff,
            now,
        )

        // 6. PreMatchJob 조건부 등록 (킥오프 이전인 경우에만)
        if (kickoff.isAfter(now)) {
            val preMatchStartTime = calculatePreMatchJobStartTime(kickoff)
            val preJobAdded = jobSchedulerService.addPreMatchJob(fixtureCore.uid, preMatchStartTime)

            if (!preJobAdded) {
                log.error(
                    "Failed to add PreMatchJob - fixtureApiId={}, uid={}, kickoff={}, preMatchStart={}",
                    fixtureApiId,
                    fixtureCore.uid,
                    kickoff,
                    preMatchStartTime,
                )

                // available 플래그 롤백
                setFixtureAvailableFlag(fixtureCore, fixtureApiSports, false)

                return DomainResult.Fail(
                    DomainFail.Validation.single(
                        code = "PRE_MATCH_JOB_REGISTRATION_FAILED",
                        message = "Failed to register PreMatchJob for fixture ${fixtureCore.uid}",
                        field = "fixtureApiId",
                    ),
                )
            }
        } else {
            log.info(
                "Kickoff already passed, skipping PreMatchJob - fixtureApiId={}, uid={}, kickoff={}, now={}",
                fixtureApiId,
                fixtureCore.uid,
                kickoff,
                now,
            )
        }

        // 7. LiveMatchJob 사전 등록 (킥오프 시간에 시작)
        val liveJobAdded = jobSchedulerService.addLiveMatchJob(fixtureCore.uid, kickoff)

        if (!liveJobAdded) {
            log.error("Failed to add LiveMatchJob - fixtureApiId={}, uid={}, rolling back PreMatchJob", fixtureApiId, fixtureCore.uid)
            // PreMatchJob 롤백
            jobSchedulerService.removeAllJobsForFixture(fixtureCore.uid)

            // available 플래그 롤백
            setFixtureAvailableFlag(fixtureCore, fixtureApiSports, false)

            return DomainResult.Fail(
                DomainFail.Validation.single(
                    code = "LIVE_MATCH_JOB_REGISTRATION_FAILED",
                    message = "Failed to register LiveMatchJob for fixture ${fixtureCore.uid}",
                    field = "fixtureApiId",
                ),
            )
        }

        log.info(
            "Available fixture added successfully - fixtureApiId={}, uid={}, kickoff={}",
            fixtureApiId,
            fixtureCore.uid,
            kickoff,
        )
        return DomainResult.Success(fixtureCore.uid)
    }

    private fun setFixtureAvailableFlag(
        fixtureCore: FixtureCore,
        fixtureApiSports: FixtureApiSports,
        available: Boolean,
    ) {
        fixtureCore.available = available
        fixtureApiSports.available = available
        fixtureCoreRepository.save(fixtureCore)
        fixtureApiSportsRepository.save(fixtureApiSports)
    }

    /**
     * Fixture를 Unavailable로 설정하고 모든 Job 삭제
     *
     * @param fixtureApiId FixtureApiSports API ID
     * @return 성공 시 fixture UID, 실패 시 DomainFail
     */
    @Transactional
    fun removeAvailableFixture(fixtureApiId: Long): DomainResult<String, DomainFail> {
        log.info("Removing available fixture - fixtureApiId={}", fixtureApiId)

        // 1. FixtureApiSports 조회
        val fixtureApiSports =
            fixtureApiSportsRepository.findByApiId(fixtureApiId)
                ?: return DomainResult.Fail(
                    DomainFail.NotFound(
                        resource = "FIXTURE_API_SPORTS",
                        id = fixtureApiId.toString(),
                    ),
                )

        // 2. FixtureCore 조회 (연관관계로 접근)
        val fixtureCore =
            fixtureApiSports.core
                ?: return DomainResult.Fail(
                    DomainFail.NotFound(
                        resource = "FIXTURE_CORE",
                        id = "core not linked to apiId=$fixtureApiId",
                    ),
                )

        // 3. 이미 unavailable인지 확인
        if (!fixtureCore.available) {
            log.warn("Fixture is already unavailable - fixtureApiId={}, uid={}", fixtureApiId, fixtureCore.uid)
            return DomainResult.Success(fixtureCore.uid)
        }

        // 4. FixtureCore available 플래그 해제
        setFixtureAvailableFlag(fixtureCore, fixtureApiSports, false)
        log.info("FixtureApiSports available updated - fixtureApiId={}, uid={}, available=false", fixtureApiId, fixtureCore.uid)

        // 6. 모든 Job 삭제
        val deletedCount = jobSchedulerService.removeAllJobsForFixture(fixtureCore.uid)

        log.info(
            "Available fixture removed successfully - fixtureApiId={}, uid={}, deletedJobs={}",
            fixtureApiId,
            fixtureCore.uid,
            deletedCount,
        )

        return DomainResult.Success(fixtureCore.uid)
    }

    /**
     * PreMatchJob 시작 시각 계산
     *
     * - 킥오프 1시간 전부터 시작 (권장)
     * - 이미 킥오프 1시간 전이 지났으면 즉시 시작
     *
     * @param kickoff 경기 킥오프 시각 (UTC, non-null, addAvailableFixture에서 이미 검증됨)
     * @return PreMatchJob 시작 시각 (Instant, UTC)
     */
    private fun calculatePreMatchJobStartTime(kickoff: Instant): Instant {
        val oneHourBeforeKickoff = kickoff.minusSeconds(3600) // 1시간 = 3600초
        val now = Instant.now(clock)

        return if (oneHourBeforeKickoff.isBefore(now)) {
            // 이미 킥오프 1시간 전이 지났으면 즉시 시작
            now
        } else {
            // 킥오프 1시간 전부터 시작
            oneHourBeforeKickoff
        }
    }
}
