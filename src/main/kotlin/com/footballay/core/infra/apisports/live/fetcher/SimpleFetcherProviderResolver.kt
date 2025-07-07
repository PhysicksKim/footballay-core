package com.footballay.core.infra.apisports.live.fetcher

import com.footballay.core.infra.apisports.live.ActionAfterMatchSync
import com.footballay.core.infra.apisports.syncer.MatchDataSyncer
import com.footballay.core.logger
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

/**
 * 기본 FetcherProviderResolver 구현체
 * 
 * 등록된 LiveMatchDataSyncer 구현체를 순회하며 fixture별로 해당 Fetcher가 동작해야 하는 경우 fetch를 수행합니다.
 * 각 Fetcher는 fixtureUid를 통해 해당 fixture가 지원하는지 여부를 판단합니다.
 */
@Component
class SimpleFetcherProviderResolver(
    private val fetchers: List<MatchDataSyncer>
) : FetcherProviderResolver {

    val log = logger()

    override fun fetchLiveData(
        fixtureUid: String,
    ): ActionAfterMatchSync? {
        for (fetcher in fetchers) {
            if (fetcher.isSupport(fixtureUid)) {
                return fetcher.syncMatchData(fixtureUid)
            }
        }
        log.warn("No fetcher found for fixtureUid: $fixtureUid")
        return ActionAfterMatchSync.Companion
            .ongoing(kickoffTime = OffsetDateTime.now().plusHours(1))
    }
} 