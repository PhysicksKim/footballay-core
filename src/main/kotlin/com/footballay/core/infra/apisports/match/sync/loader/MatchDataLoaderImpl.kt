package com.footballay.core.infra.apisports.match.sync.loader

import com.footballay.core.infra.apisports.match.sync.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerKeyGenerator
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.logger
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class MatchDataLoaderImpl (
    val entityQueryService: MatchEntityQueryService
) : MatchDataLoader {

    val log = logger()

    @Transactional(readOnly = true)
    override fun loadContext(fixtureApiId: Long, context: MatchPlayerContext, entityBundle: MatchEntityBundle) {
        val homeTeam = entityQueryService.loadHomeTeamWithPlayersAndStats(fixtureApiId)
        val awayTeam = entityQueryService.loadAwayTeamWithPlayersAndStats(fixtureApiId)
        val fixtureWithEvent = entityQueryService.loadFixtureWithEvents(fixtureApiId)

        log.info("Loading match data for fixtureApiId: $fixtureApiId, homeTeam: ${homeTeam?.teamApiSports?.name}, awayTeam: ${awayTeam?.teamApiSports?.name}, events: ${fixtureWithEvent?.events?.size ?: 0}")
        log.info("fixture api sports: $fixtureWithEvent.id")

        val allMatchPlayers = mutableListOf<ApiSportsMatchPlayer>()
        homeTeam?.players?.let { players ->
            allMatchPlayers.addAll(players)
        }
        awayTeam?.players?.let { players ->
            allMatchPlayers.addAll(players)
        }

        fixtureWithEvent?.events?.forEach { event ->
            event.player?.let { player ->
                if (!allMatchPlayers.any { it.id == player.id }) {
                    allMatchPlayers.add(player)
                }
            }
            event.assist?.let { assist ->
                if (!allMatchPlayers.any { it.id == assist.id }) {
                    allMatchPlayers.add(assist)
                }
            }
        }

        // 5-4. EntityBundle 직접 할당
        entityBundle.fixture = fixtureWithEvent
        entityBundle.homeTeam = homeTeam
        entityBundle.awayTeam = awayTeam
        entityBundle.allMatchPlayers = allMatchPlayers.associateBy { MatchPlayerKeyGenerator.generateMatchPlayerKey(it.id, it.name) }
        entityBundle.allEvents = fixtureWithEvent?.events ?: emptyList()
    }
}