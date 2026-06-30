package com.footballay.core.infra.apisports.match.plan.loader

import com.footballay.core.infra.apisports.match.plan.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerContext
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.logger
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerKeyGenerator.generateMatchPlayerKey as generateMpKey

@Component
class MatchDataLoaderImpl(
    val entityQueryService: MatchEntityQueryService,
) : MatchDataLoader {
    val log = logger()

    @Transactional(readOnly = true)
    override fun loadContext(
        fixtureApiId: Long,
        context: MatchPlayerContext,
        entityBundle: MatchEntityBundle,
    ) {
        log.info("starting to load match data for fixtureApiId: {}", fixtureApiId)
        val homeTeam = entityQueryService.loadHomeTeamWithPlayersAndStats(fixtureApiId)
        val awayTeam = entityQueryService.loadAwayTeamWithPlayersAndStats(fixtureApiId)
        val fixtureWithEvent = entityQueryService.loadFixtureWithEvents(fixtureApiId)

        log.info(
            "Loading match data for fixtureApiId: $fixtureApiId, homeTeam: ${homeTeam?.teamApiSports?.name}, awayTeam: ${awayTeam?.teamApiSports?.name}, events: ${fixtureWithEvent?.events?.size ?: 0}",
        )

        // 1. 모든 MatchPlayer를 수집하고 Map 으로 변환
        val allMatchPlayers = mutableListOf<ApiSportsMatchPlayer>()
        homeTeam?.players?.let { allMatchPlayers.addAll(it) }
        awayTeam?.players?.let { allMatchPlayers.addAll(it) }

        // 2. ID 기반 중복 체크를 위한 Set 생성
        val existingPlayerIds = allMatchPlayers.mapTo(mutableSetOf()) { it.id }

        // 3. 이벤트에만 존재하는 선수들 추가
        fixtureWithEvent?.events?.forEach { event ->
            event.player?.let { player ->
                if (player.id !in existingPlayerIds) {
                    allMatchPlayers.add(player)
                    existingPlayerIds.add(player.id)
                }
            }
            event.assist?.let { assist ->
                if (assist.id !in existingPlayerIds) {
                    allMatchPlayers.add(assist)
                    existingPlayerIds.add(assist.id)
                }
            }
        }

        // 4. 최종 Map 생성 (PlayerStats는 MatchPlayer.statistics 필드에 이미 포함됨)
        val allMatchPlayersMap = allMatchPlayers.associateBy { generateMpKey(it.id, it.name) }

        // 5. EntityBundle 할당
        entityBundle.fixture = fixtureWithEvent
        entityBundle.homeTeam = homeTeam
        entityBundle.awayTeam = awayTeam
        entityBundle.allMatchPlayers = allMatchPlayersMap
        entityBundle.allEvents = fixtureWithEvent?.events ?: emptyList()
        entityBundle.homeTeamStat = homeTeam?.teamStatistics
        entityBundle.awayTeamStat = awayTeam?.teamStatistics

        // 6. PlayerStats 로깅 (디버깅용)
        val playerStatsCount = entityBundle.getAllPlayerStats().size
        log.info("Loaded {} MatchPlayers with {} PlayerStats", allMatchPlayersMap.size, playerStatsCount)
    }
}
