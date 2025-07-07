package com.footballay.core.infra.facade

import com.footballay.core.infra.apisports.FixtureApiSportsQueryService
import com.footballay.core.infra.apisports.fetch.ApiSportsV3Fetcher
import com.footballay.core.infra.apisports.live.ActionAfterMatchSync
import com.footballay.core.infra.apisports.live.ApiSportsFixtureSingle
import com.footballay.core.infra.apisports.live.FixturePlayerExtractor
import com.footballay.core.infra.apisports.live.FullMatchSyncDto
import com.footballay.core.infra.apisports.live.PlayerSyncExecutor
import com.footballay.core.infra.apisports.syncer.match.MatchApiSportsSyncer
import com.footballay.core.infra.apisports.syncer.MatchDataSyncer
import org.springframework.stereotype.Component

/**
 * ApiSports 라이브 매치 데이터 동기화 구현체
 *
 * - PlayerSyncService 대신 ApiSportsPlayerSyncService에 의존
 * - LiveMatchSyncer 대신 ApiSportsLiveMatchSyncService에 의존
 * - 구현체가 아닌 인터페이스에만 의존하여 Provider별 구분과 버전 관리 용이
 */
@Component
class ApiSportsMatchSyncFacade(
    private val fetcher: ApiSportsV3Fetcher,
    private val playerExtractor: FixturePlayerExtractor,
    private val playerSyncExecutor: PlayerSyncExecutor,
    private val matchSyncService: MatchApiSportsSyncer,
    private val fixtureQueryService: FixtureApiSportsQueryService,
) : MatchDataSyncer {

    /**
     * 현재는 ApiSports 만 지원하므로 항상 true 를 반환합니다.
     */
    override fun isSupport(uid: String): Boolean {
        return true
    }

    /**
     * ApiSports 라이브 경기 데이터 동기화
     * 1. 신규 선수 캐싱 (통합 트랜잭션)
     * 2. 전체 라이브 데이터 동기화
     */
    override fun syncMatchData(uid: String): ActionAfterMatchSync {
        val response = fetchLiveResponse(uid)

        // 선수 추출 및 미리 [PlayerCore] [PlayerApiSports] 저장
        val playerSyncDtoList = playerExtractor.extractAndSyncPlayers(response)
        playerSyncExecutor.syncPlayers(playerSyncDtoList)

        val fullMatchSyncDto = FullMatchSyncDto.of(response)
        return matchSyncService.syncFixtureMatchEntities(fullMatchSyncDto)
    }

    private fun fetchLiveResponse(fixtureUid: String): ApiSportsFixtureSingle {
        val apiId = extractApiIdFromUid(fixtureUid)
        val fetchFixtureSingle = fetcher.fetchFixtureSingle(apiId)
        return fetchFixtureSingle
    }

    private fun extractApiIdFromUid(uid: String): Long {
        val apiSportsFixture = fixtureQueryService.getApiIdByFixtureUid(uid)
        if(apiSportsFixture?.apiId == null) {
            throw IllegalArgumentException("Fixture with UID $uid does not have a valid ApiSports ID")
        }
        return apiSportsFixture.apiId
    }
}