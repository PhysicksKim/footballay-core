package com.footballay.core.infra.apisports.live

import com.footballay.core.domain.live.fetcher.CreatePlayerCoreDto
import com.footballay.core.domain.live.fetcher.LiveLineupSyncTemplate
import com.footballay.core.domain.live.fetcher.LiveMatchSyncInstruction
import com.footballay.core.domain.live.fetcher.Side
import com.footballay.core.infra.apisports.fetch.response.ApiSportsFixture
import com.footballay.core.infra.apisports.fetch.response.ApiSportsV3Envelope
import com.footballay.core.infra.persistence.apisports.entity.PlayerApiSports
import com.footballay.core.infra.persistence.core.entity.PlayerCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import org.springframework.stereotype.Component

typealias ApiSportsFixtureSingle = ApiSportsV3Envelope<ApiSportsFixture.Single>
typealias ApiSportsPlayer          = PlayerApiSports

@Component
class ApiSportsLiveMatchDataSyncer(
) : LiveLineupSyncTemplate<ApiSportsFixtureSingle, ApiSportsPlayer>() {

    /**
     * 현재는 ApiSports 만 지원하므로 항상 true 를 반환합니다.
     */
    override fun isSupport(uid: String): Boolean {
        return true
    }

    override fun fetchLiveResponse(fixtureUid: String): ApiSportsFixtureSingle {
        // ApiSportFetchService
        TODO("Api Sports 에다가 fixtureUid 를 넣어서 데이터 호출하는 로직을 구현")
    }

    override fun extractNewApiPlayers(
        resp: ApiSportsFixtureSingle,
        side: Side
    ): Map<CreatePlayerCoreDto, ApiSportsPlayer> {
        TODO("Not yet implemented")
    }

    override fun resolveTeam(resp: ApiSportsFixtureSingle, side: Side): TeamCore {
        TODO("Not yet implemented")
    }

    override fun executeFullSync(resp: ApiSportsFixtureSingle): LiveMatchSyncInstruction {
        // ApiSportsLiveMatchSyncer
        TODO("Not yet implemented")
    }

    override fun linkPlayersToCoreAndTeam(coreMap: Map<PlayerCore, ApiSportsPlayer>, team: TeamCore) {

        TODO("Not yet implemented")
    }

    override fun generateCorePlayers(apiNew: Map<CreatePlayerCoreDto, ApiSportsPlayer>): Map<PlayerCore, ApiSportsPlayer> {
        TODO("Not yet implemented")
    }

}