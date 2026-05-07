package com.footballay.core.matchdata.facade

import com.footballay.core.matchdata.sync.dispatcher.JobContext
import com.footballay.core.matchdata.sync.dispatcher.MatchDataSyncDispatcher
import com.footballay.core.matchdata.sync.dispatcher.MatchDataSyncResult
import org.springframework.stereotype.Service

@Service
class MatchDataSyncFacadeImpl(
    private val dispatcher: MatchDataSyncDispatcher,
) : MatchDataSyncFacade {
    override fun syncByFixtureUid(fixtureUid: String): MatchDataSyncResult =
        dispatcher.syncByFixtureUid(fixtureUid)

    override fun syncByFixtureUid(
        fixtureUid: String,
        jobContext: JobContext,
    ): MatchDataSyncResult =
        dispatcher.syncByFixtureUid(fixtureUid, jobContext)
}
