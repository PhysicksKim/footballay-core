package com.footballay.core.matchdata.facade

import com.footballay.core.matchdata.sync.dispatcher.JobContext
import com.footballay.core.matchdata.sync.dispatcher.MatchDataSyncResult

/**
 * Fixture UID 기반 match data sync 공식 진입점.
 *
 * Scheduler job, admin manual sync 같은 outer adapter는 내부 dispatcher/orchestrator 대신 이 facade를 바라본다.
 */
interface MatchDataSyncFacade {
    fun syncByFixtureUid(fixtureUid: String): MatchDataSyncResult

    fun syncByFixtureUid(
        fixtureUid: String,
        jobContext: JobContext,
    ): MatchDataSyncResult
}
