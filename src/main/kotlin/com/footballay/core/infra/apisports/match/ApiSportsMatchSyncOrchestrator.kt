package com.footballay.core.infra.apisports.match

import com.footballay.core.infra.MatchSyncOrchestrator
import com.footballay.core.infra.apisports.FixtureApiSportsQueryService
import com.footballay.core.infra.apisports.backbone.extractor.ApiSportsFixturePlayerCollector
import com.footballay.core.infra.apisports.backbone.sync.player.PlayerApiSportsSyncer
import com.footballay.core.infra.apisports.match.dto.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.sync.ApiSportsFixtureSingle
import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.infra.apisports.match.sync.ApiSportsMatchEntitySyncFacade
import com.footballay.core.infra.apisports.shared.fetch.ApiSportsV3Fetcher
import org.springframework.stereotype.Component

/**
 * ApiSports 기반 라이브 매치 데이터 동기화 오케스트레이터
 *
 * FixtureCore의 uid를 받아 ApiSports API로부터 라이브 매치 데이터를 조회하고 동기화합니다.
 *
 * **사전 조건:**
 * - FixtureCore, LeagueCore, TeamCore 및 대응되는 ApiSports 엔티티들이 이미 저장되어 있어야 함
 * - FixtureApiSports에 apiId가 바인딩되어 있어야 함
 *
 * **동기화 순서:**
 * 1. ApiSports Fixture API 호출
 * 2. 응답에서 선수 정보 추출 및 사전 저장 (PlayerCore, PlayerApiSports)
 * 3. 전체 매치 엔티티 동기화 (Event, PlayerStats 등)
 *
 * @see MatchSyncOrchestrator
 * @see ApiSportsMatchEntitySyncFacade
 */
@Component
class ApiSportsMatchSyncOrchestrator(
    private val fixtureQueryService: FixtureApiSportsQueryService,
    private val fetcher: ApiSportsV3Fetcher,
    private val playerExtractor: ApiSportsFixturePlayerCollector,
    private val playerApiSportsSyncer: PlayerApiSportsSyncer,
    private val matchSyncService: ApiSportsMatchEntitySyncFacade,
) : MatchSyncOrchestrator {

    override fun isSupport(uid: String): Boolean = true

    override fun syncMatchData(uid: String): MatchDataSyncResult {
        val apiId = extractApiIdFromUid(uid)
        val response = fetcher.fetchFixtureSingle(apiId)

        syncPlayersBeforeMatchSync(response)

        val fullMatchSyncDto = FullMatchSyncDto.of(response)
        return matchSyncService.syncFixtureMatchEntities(fullMatchSyncDto)
    }

    private fun syncPlayersBeforeMatchSync(response: ApiSportsFixtureSingle) {
        val playersByTeam = playerExtractor.extractPlayersByTeam(response)
        playersByTeam.forEach { (teamId, dtos) ->
            playerApiSportsSyncer.syncPlayersOfTeam(teamId, dtos)
        }
    }

    private fun extractApiIdFromUid(uid: String): Long {
        val apiSportsFixture = fixtureQueryService.getApiIdByFixtureUid(uid)
        requireNotNull(apiSportsFixture?.apiId) {
            "FixtureApiSports의 apiId가 바인딩되지 않았습니다. uid=$uid"
        }
        return apiSportsFixture.apiId
    }
}