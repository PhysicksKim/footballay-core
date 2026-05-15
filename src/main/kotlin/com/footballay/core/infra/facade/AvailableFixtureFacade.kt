package com.footballay.core.infra.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.scheduler.AvailableFixtureJobReconciler
import com.footballay.core.infra.scheduler.ReconcileResult
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Available Fixture 관리 Facade
 *
 * Fixture의 available 상태와 관련 Job 생명주기를 관리하는 Domain Facade입니다.
 *
 * **동작 흐름:**
 * 1. Fixture available = true 설정
 * 2. AvailableFixtureJobReconciler 로 desired Quartz Job 적용
 * 3. PreMatchJob → LiveMatchJob → PostMatchJob 자동 전환
 * 4. Fixture available = false 설정 → 모든 Job 삭제
 */
@Service
class AvailableFixtureFacade(
    private val fixtureCoreRepository: FixtureCoreRepository,
    private val fixtureApiSportsRepository: FixtureApiSportsRepository,
    private val availableFixtureJobReconciler: AvailableFixtureJobReconciler,
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
     * - DB 상태를 먼저 available=true 로 바꾼 뒤 reconciler 가 desired Job 을 계산/적용
     * - Quartz apply 실패 시 available flag 를 rollback 하고 best-effort compensation 수행
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

        // 5. FixtureCore available 플래그 설정
        setFixtureAvailableFlag(fixtureCore, fixtureApiSports, true)
        log.info(
            "FixtureApiSports available updated - fixtureApiId={}, uid={}, available=true, kickoff={}",
            fixtureApiId,
            fixtureCore.uid,
            kickoff,
        )

        val reconcileResult = availableFixtureJobReconciler.reconcileFixture(fixtureCore)
        if (!reconcileResult.success) {
            log.error(
                "Available fixture job reconcile failed - fixtureApiId={}, uid={}, result={}",
                fixtureApiId,
                fixtureCore.uid,
                reconcileResult,
            )
            setFixtureAvailableFlag(fixtureCore, fixtureApiSports, false)
            compensateAfterAvailableFlagRollback(fixtureCore.uid, reconcileResult)
            return DomainResult.Fail(
                DomainFail.Validation.single(
                    code = "AVAILABLE_FIXTURE_JOB_RECONCILE_FAILED",
                    message = "Failed to reconcile available fixture jobs for fixture ${fixtureCore.uid}",
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

        // 5. 모든 available fixture Job 정리
        val reconcileResult = availableFixtureJobReconciler.reconcileFixture(fixtureCore)
        if (!reconcileResult.success) {
            log.error(
                "Available fixture job cleanup reconcile failed - fixtureApiId={}, uid={}, result={}",
                fixtureApiId,
                fixtureCore.uid,
                reconcileResult,
            )
            setFixtureAvailableFlag(fixtureCore, fixtureApiSports, true)
            compensateAfterAvailableFlagRollback(fixtureCore.uid, reconcileResult)
            return DomainResult.Fail(
                DomainFail.Validation.single(
                    code = "AVAILABLE_FIXTURE_JOB_RECONCILE_FAILED",
                    message = "Failed to reconcile available fixture jobs for fixture ${fixtureCore.uid}",
                    field = "fixtureApiId",
                ),
            )
        }

        log.info(
            "Available fixture removed successfully - fixtureApiId={}, uid={}, reconcileResult={}",
            fixtureApiId,
            fixtureCore.uid,
            reconcileResult,
        )

        return DomainResult.Success(fixtureCore.uid)
    }

    private fun compensateAfterAvailableFlagRollback(
        fixtureUid: String,
        originalFailure: ReconcileResult,
    ) {
        val compensationResult = availableFixtureJobReconciler.reconcileFixture(fixtureUid)
        if (!compensationResult.success) {
            log.error(
                "Best-effort available fixture job compensation failed - fixtureUid={}, originalFailure={}, compensationResult={}",
                fixtureUid,
                originalFailure,
                compensationResult,
            )
        }
    }
}
