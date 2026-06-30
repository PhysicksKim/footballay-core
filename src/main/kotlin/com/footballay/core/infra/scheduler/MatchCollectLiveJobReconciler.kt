package com.footballay.core.infra.scheduler

import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface MatchCollectLiveJobReconciler {
    fun reconcileLeague(leagueUid: String): ReconcileResult
}

@Component
class MatchCollectLiveJobReconcilerImpl(
    private val fixtureCoreRepository: FixtureCoreRepository,
    private val fixtureReconciler: MatchCollectLiveFixtureReconciler,
) : MatchCollectLiveJobReconciler {
    @Transactional
    override fun reconcileLeague(leagueUid: String): ReconcileResult {
        val fixtures = fixtureCoreRepository.findMatchCollectLiveJobReconcileFixturesByLeagueUid(leagueUid)
        if (fixtures.isEmpty()) {
            return ReconcileResult.empty(fixtureUid = null, leagueUid = leagueUid)
        }

        return combineResults(
            fixtureUid = null,
            leagueUid = leagueUid,
            results = fixtures.map(fixtureReconciler::reconcileFixture),
        )
    }

    private fun combineResults(
        fixtureUid: String?,
        leagueUid: String?,
        results: List<ReconcileResult>,
    ): ReconcileResult =
        ReconcileResult(
            fixtureUid = fixtureUid,
            leagueUid = leagueUid,
            success = results.all { it.success },
            planned = results.sumOf { it.planned },
            registered = results.sumOf { it.registered },
            replaced = results.sumOf { it.replaced },
            deleted = results.sumOf { it.deleted },
            skipped = results.sumOf { it.skipped },
            errors = results.flatMap { it.errors },
        )
}
