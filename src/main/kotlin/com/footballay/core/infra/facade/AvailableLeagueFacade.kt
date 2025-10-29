package com.footballay.core.infra.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.logger
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Available League 관리 Facade
 *
 * League의 available 상태를 관리하는 Domain Facade입니다.
 * Available 리그는 해당 리그의 경기들이 실시간 동기화 대상이 됩니다.
 * (Fixture 개별 available 설정과는 별개로 작동)
 */
@Service
class AvailableLeagueFacade(
    private val leagueCoreRepository: LeagueCoreRepository,
) {
    private val log = logger()

    /**
     * 리그의 available 상태를 설정합니다.
     *
     * @param leagueId LeagueCore ID
     * @param available Available 상태 (true: 활성화, false: 비활성화)
     * @return 성공 시 league UID, 실패 시 DomainFail
     */
    @Transactional
    fun setLeagueAvailable(
        leagueId: Long,
        available: Boolean,
    ): DomainResult<String, DomainFail> {
        log.info("Setting league available - leagueId={}, available={}", leagueId, available)

        // 1. LeagueCore 조회
        val leagueCore =
            leagueCoreRepository.findByIdOrNull(leagueId)
                ?: return DomainResult.Fail(
                    DomainFail.NotFound(
                        resource = "LEAGUE_CORE",
                        id = leagueId.toString(),
                    ),
                )

        // 2. Available 플래그 설정
        leagueCore.available = available
        leagueCoreRepository.save(leagueCore)

        log.info(
            "League available status updated - leagueId={}, uid={}, available={}",
            leagueId,
            leagueCore.uid,
            available,
        )

        return DomainResult.Success(leagueCore.uid)
    }
}

