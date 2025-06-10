package com.footballay.core.infra.apisports.live

import com.footballay.core.domain.live.fetcher.CreatePlayerCoreDto
import com.footballay.core.domain.live.fetcher.LiveLineupSyncTemplate
import com.footballay.core.domain.live.fetcher.LiveMatchSyncInstruction
import com.footballay.core.domain.live.fetcher.Side
import com.footballay.core.infra.apisports.fetch.response.ApiSportsV3Response
import com.footballay.core.infra.apisports.fetch.response.fixtures.ApiSportsFixtureSingle
import com.footballay.core.infra.persistence.apisports.entity.PlayerApiSports
import com.footballay.core.infra.persistence.core.entity.PlayerCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import org.springframework.stereotype.Component

typealias ApiSportsFixtureResponse = ApiSportsV3Response<ApiSportsFixtureSingle>
typealias ApiSportsPlayer          = PlayerApiSports

@Component
class ApiSportsLiveMatchDataSyncer(
) : LiveLineupSyncTemplate<ApiSportsFixtureResponse, ApiSportsPlayer>() {

    /**
     * 현재는 ApiSports 만 지원하므로 항상 true 를 반환합니다.
     */
    override fun isSupport(uid: String): Boolean {
        return true
    }

    override fun fetchLiveResponse(fixtureUid: String): ApiSportsFixtureResponse {
        // ApiSportFetchService
        TODO("Api Sports 에다가 fixtureUid 를 넣어서 데이터 호출하는 로직을 구현")
    }

    override fun executeFullSync(resp: ApiSportsFixtureResponse): LiveMatchSyncInstruction {
        // ApiSportsLiveMatchSyncer
        TODO("Not yet implemented")
    }

    override fun linkPlayersToCoreAndTeam(coreMap: Map<PlayerCore, ApiSportsPlayer>, team: TeamCore) {

        TODO("Not yet implemented")
    }

    override fun generateCorePlayers(apiNew: Map<CreatePlayerCoreDto, ApiSportsPlayer>): Map<PlayerCore, ApiSportsPlayer> {
        TODO("Not yet implemented")
    }

    override fun extractNewApiPlayers(
        resp: ApiSportsFixtureResponse,
        side: Side
    ): Map<CreatePlayerCoreDto, ApiSportsPlayer> {
        TODO("Not yet implemented")
    }

    override fun resolveTeam(resp: ApiSportsFixtureResponse, side: Side): TeamCore {
        TODO("Not yet implemented")
    }

}