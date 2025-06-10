package com.footballay.core.domain.live

import com.footballay.core.domain.live.fetcher.LiveMatchDataSyncer
import com.footballay.core.domain.live.fetcher.LiveMatchSyncInstruction
import org.springframework.stereotype.Component

/**
 * 등록된 {@link LiveDataFetchable} 구현체를 순회하며 fixture 별로 해당 Fetcher 가 동작해야 하는 경우 fetch 를 수행합니다. <br>
 * 각 Fetcher 는 fixtureUid 를 통해 해당 fixture 가 지원하는지 여부를 판단합니다. <br>
 */
@Component
class FetcherProviderResolver (
    private val fetchers: List<LiveMatchDataSyncer>
) {

    fun fetchLiveData(
        fixtureUid: String,
    ): LiveMatchSyncInstruction? {
        for (fetcher in fetchers) {
            if (fetcher.isSupport(fixtureUid)) {
                return fetcher.syncLiveMatchData(fixtureUid)
            }
        }
        return null
    }
}