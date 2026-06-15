package com.footballay.core.infra.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.league.MatchCollect
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.scheduler.MatchCollectLiveJobReconciler
import com.footballay.core.infra.scheduler.ReconcileResult
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class LeagueMatchCollectUpdateResult(
    val leagueCoreUid: String,
    val matchCollect: MatchCollect,
    val reconcileResult: ReconcileResult,
)

@Service
class LeagueMatchCollectFacade(
    private val leagueCoreRepository: LeagueCoreRepository,
    private val matchCollectLiveJobReconciler: MatchCollectLiveJobReconciler,
) {
    private val log = logger()

    @Transactional
    fun setLeagueMatchCollectByCoreUid(
        leagueCoreUid: String,
        matchCollect: MatchCollect,
    ): DomainResult<LeagueMatchCollectUpdateResult, DomainFail> {
        val league =
            leagueCoreRepository.findByUid(leagueCoreUid)
                ?: return leagueCoreNotFound(leagueCoreUid)

        val previousMatchCollect = league.matchCollect
        league.matchCollect = matchCollect

        val reconcileResult = matchCollectLiveJobReconciler.reconcileLeague(league.uid)
        if (!reconcileResult.success) {
            log.error(
                "League matchCollect reconcile failed - leagueCoreUid={}, requested={}, previous={}, result={}",
                league.uid,
                matchCollect,
                previousMatchCollect,
                reconcileResult,
            )
            league.matchCollect = previousMatchCollect
            restoreMatchCollectJobState(league.uid)
            return DomainResult.Fail(DomainFail.Unknown("리그 matchCollect 변경 후 job reconcile 에 실패했습니다: ${league.uid}"))
        }

        log.info("League matchCollect updated - leagueCoreUid={}, matchCollect={}", league.uid, matchCollect)
        return DomainResult.Success(
            LeagueMatchCollectUpdateResult(
                leagueCoreUid = league.uid,
                matchCollect = matchCollect,
                reconcileResult = reconcileResult,
            ),
        )
    }

    private fun restoreMatchCollectJobState(leagueCoreUid: String) {
        val compensationResult = matchCollectLiveJobReconciler.reconcileLeague(leagueCoreUid)
        if (!compensationResult.success) {
            log.error(
                "Best-effort matchCollect job compensation failed - leagueCoreUid={}, result={}",
                leagueCoreUid,
                compensationResult,
            )
        }
    }

    private fun leagueCoreNotFound(leagueCoreUid: String): DomainResult.Fail<DomainFail.NotFound> {
        log.warn("LeagueCore not found - leagueCoreUid={}", leagueCoreUid)
        return DomainResult.Fail(
            DomainFail.NotFound(
                resource = "LEAGUE_CORE",
                id = leagueCoreUid,
            ),
        )
    }
}
