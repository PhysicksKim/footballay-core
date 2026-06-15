package com.footballay.core.infra.scheduler

import com.footballay.core.logger
import org.springframework.stereotype.Component

interface MatchCollectLiveJobReconciler {
    fun reconcileLeague(leagueUid: String): ReconcileResult
}

@Component
class NoopMatchCollectLiveJobReconciler : MatchCollectLiveJobReconciler {
    private val log = logger()

    override fun reconcileLeague(leagueUid: String): ReconcileResult {
        log.info("MatchCollect LIVE job reconcile is not implemented yet - leagueUid={}", leagueUid)
        return ReconcileResult.empty(fixtureUid = null, leagueUid = leagueUid)
    }
}
