package com.footballay.core.infra.apisports.match

import com.footballay.core.infra.MatchDataSyncer
import com.footballay.core.infra.apisports.FixtureApiSportsQueryService
import com.footballay.core.infra.apisports.backbone.extractor.FixturePlayerExtractor
import com.footballay.core.infra.apisports.backbone.extractor.PlayerSyncExecutor
import com.footballay.core.infra.apisports.backbone.sync.player.PlayerApiSportsSyncer
import com.footballay.core.infra.apisports.match.dto.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.live.deprecated.ApiSportsFixtureSingle
import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.infra.apisports.match.sync.MatchApiSportsSyncer
import com.footballay.core.infra.apisports.shared.fetch.ApiSportsV3Fetcher

import org.springframework.stereotype.Component

// for docs
import com.footballay.core.infra.persistence.core.entity.*
import com.footballay.core.infra.persistence.apisports.entity.*

/**
 * [FixtureCore.uid] 를 받아서 ApiSports Match data 를 저장하는 facade 입니다.
 * [FullMatchSyncDto] 로 Fixture 전체 데이터를 받아서 저장합니다.
 *
 * 사전에 미리 아래와 같은 엔티티가 저장되고 연관관계가 맺어져 있음을 가정합니다.
 * - [FixtureCore]
 * - [LeagueCore]
 * - [TeamCore]
 * - [FixtureApiSports]
 * - [LeagueApiSports]
 * - [TeamApiSports]
 *
 * [PlayerCore] 와 [PlayerApiSports] 는 [FullMatchSyncDto] 를 바탕으로 자동 생성하여 저장합니다.
 *
 *
 * **핵심 책임:**
 * - ApiSports API 에서 라이브 데이터 조회
 * - 선수 사전 저장 (PlayerCore, PlayerApiSports)
 * - 전체 매치 데이터 동기화
 *
 * **동기화 과정:**
 * 1. `ApiSportsV3Fetcher`를 통해 라이브 데이터 조회
 * 2. `FixturePlayerExtractor`로 선수 정보 추출
 * 3. `PlayerSyncExecutor`로 선수 사전 저장
 * 4. `MatchApiSportsSyncer`로 매치 엔티티 동기화
 *
 * **특징:**
 * - 선수 사전 저장으로 Event, PlayerStats에서의 복잡한 선수 처리 로직 단순화
 * - 인터페이스 기반 의존성으로 Provider별 구분과 버전 관리 용이
 * - 트랜잭션 분리로 안정성 보장
 *
 * @see com.footballay.core.infra.MatchDataSyncer
 * @see com.footballay.core.infra.apisports.match.sync.MatchApiSportsSyncer
 *
 * AI가 작성한 주석
 */
@Component
class ApiSportsMatchSyncFacade(
    private val fetcher: ApiSportsV3Fetcher,
    private val playerExtractor: FixturePlayerExtractor,
    private val playerApiSportsSyncer: PlayerApiSportsSyncer,
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
    override fun syncMatchData(uid: String): MatchDataSyncResult {
        val response = fetchLiveResponse(uid)

        syncPlayersBeforeMatchSync(response)

        val fullMatchSyncDto = FullMatchSyncDto.of(response)
        return matchSyncService.syncFixtureMatchEntities(fullMatchSyncDto)
    }

    private fun fetchLiveResponse(fixtureUid: String): ApiSportsFixtureSingle {
        val apiId = extractApiIdFromUid(fixtureUid)
        val fetchFixtureSingle = fetcher.fetchFixtureSingle(apiId)
        return fetchFixtureSingle
    }

    private fun syncPlayersBeforeMatchSync(response: ApiSportsFixtureSingle) {
        val playersByTeam = playerExtractor.extractPlayersByTeam(response)
        playersByTeam.forEach {
                (teamId, dtos) -> playerApiSportsSyncer.syncPlayersOfTeam(teamId, dtos)
        }
    }

    private fun extractApiIdFromUid(uid: String): Long {
        val apiSportsFixture = fixtureQueryService.getApiIdByFixtureUid(uid)
        if(apiSportsFixture?.apiId == null) {
            throw IllegalArgumentException("Fixture with UID $uid does not have a valid ApiSports ID")
        }
        return apiSportsFixture.apiId
    }
}