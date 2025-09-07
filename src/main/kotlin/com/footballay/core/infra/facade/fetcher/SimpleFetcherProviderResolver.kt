package com.footballay.core.infra.facade.fetcher

import com.footballay.core.infra.facade.fetcher.ActionAfterMatchSync
import com.footballay.core.infra.apisports.match.MatchDataSyncer
import com.footballay.core.logger
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

/**
 * 기본 FetcherProviderResolver 구현체
 * 
 * 등록된 [MatchDataSyncer] 구현체들을 순회하며 fixture별로 해당 Provider가 동작해야 하는 경우
 * 동기화를 수행합니다. 각 Provider는 `fixtureUid`를 통해 해당 fixture가 지원되는지 여부를 판단합니다.
 * 
 * **동작 방식:**
 * 1. 등록된 모든 `MatchDataSyncer` 구현체를 순회
 * 2. 각 Provider의 `isSupport(fixtureUid)` 호출하여 지원 여부 확인
 * 3. 지원하는 Provider를 찾으면 `syncMatchData(fixtureUid)` 호출
 * 4. 지원하는 Provider가 없으면 기본 폴링 액션 반환
 * 
 * **특징:**
 * - Spring의 의존성 주입을 통해 모든 `MatchDataSyncer` 구현체를 자동 수집
 * - Provider별로 독립적인 지원 로직 구현 가능
 * - Fallback 메커니즘으로 안정성 보장
 * 
 * @see MatchDataSyncer
 * @see FetcherProviderResolver
 * 
 * AI가 작성한 주석
 */
@Component
class SimpleFetcherProviderResolver(
    private val fetchers: List<MatchDataSyncer>
) : FetcherProviderResolver {

    val log = logger()

    override fun fetchMatchData(
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