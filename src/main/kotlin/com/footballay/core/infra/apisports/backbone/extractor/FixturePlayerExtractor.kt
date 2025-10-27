package com.footballay.core.infra.apisports.backbone.extractor

import com.footballay.core.infra.apisports.backbone.sync.PlayerApiSportsCreateDto
import com.footballay.core.infra.apisports.match.live.deprecated.ApiSportsFixtureSingle
import com.footballay.core.logger
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class FixturePlayerExtractor{

    val log = logger()

    /**
     * 모든 신규 선수를 캐싱합니다.
     * 하나의 트랜잭션으로 처리하여 효율성과 일관성을 보장합니다.
     *
     * 주의: Core-Api 구조에는 id가 있는 선수만 저장합니다.
     * id=null인 선수는 ApiSportsLiveMatchSyncService에서 Match 구조로 처리됩니다.
     */
    @Transactional
    fun extractPlayersByTeam(response: ApiSportsFixtureSingle) : Map<Long, List<PlayerApiSportsCreateDto>> {
        try {
            val home = extractTeamPlayers(response, isHome = true)
            val away = extractTeamPlayers(response, isHome = false)
            return listOfNotNull(home, away).toMap()
        } catch (e: IllegalArgumentException) {
            log.info("Fixture Response 에서 선수 추출 중 오류 발생: ${e.message}")
            return emptyMap()
        } catch (e: Exception) {
            log.error("예상치 못한 오류 발생: ${e.message}", e)
            throw e
        }
    }

    /**
     * 특정 팀의 신규 선수를 캐싱합니다.
     * 라인업과 선수 통계에서 중복 제거하여 처리하며, id가 있는 선수만 Core-Api 구조에 저장합니다.
     */
    private fun extractTeamPlayers(response: ApiSportsFixtureSingle, isHome: Boolean) : Pair<Long, List<PlayerApiSportsCreateDto>>? {
        val teamApiId = extractTeamApiId(response, isHome)
        if (teamApiId == null) {
            log.warn("Fixture Response 의 팀 API ID가 null 입니다. 응답: ${response.response[0].teams}")
            throw IllegalArgumentException("팀 API ID가 null입니다. 응답: ${response.response[0].teams}")
        }

        val lineupPlayers = extractPlayersFromLineup(response, teamApiId).filter { it.apiId != null }
        val lineupPlayerApiIds = lineupPlayers.mapNotNull { it.apiId }.toSet()

        val statisticsPlayers = extractPlayersInStatsNotExistInLineup(response, teamApiId, lineupPlayerApiIds).filter { it.apiId != null }
        if(statisticsPlayers.isNotEmpty()) {
            log.warn("lineup 과 statistics 에서 선수 불일치가 있습니다 lineup: ${lineupPlayers.size}, statistics: ${statisticsPlayers.size}\n" +
                    "라인업 선수: ${lineupPlayers.joinToString(separator = ",", transform = { it.name ?: "NO-NAME" })}\n" +
                    "통계 선수: ${statisticsPlayers.joinToString(separator = ",", transform = { it.name ?: "NO-NAME" })}")
        }

        val allPlayers = lineupPlayers + statisticsPlayers
        if (allPlayers.isNotEmpty()) {
            log.info("라인업에서 추출한 신규 선수 수: ${allPlayers.size} (teamApiId=$teamApiId)")
        } else {
            log.info("라인업에 신규 선수가 없습니다. teamApiId=$teamApiId")
        }
        return teamApiId to allPlayers
    }

    private fun extractPlayersFromLineup(
        response: ApiSportsFixtureSingle,
        teamApiId: Long
    ): List<PlayerApiSportsCreateDto> {
        val lineups = response.response[0].lineups
        val startXI = lineups.find { it.team.id == teamApiId }?.startXI ?: emptyList()
        val substitutes = lineups.find { it.team.id == teamApiId }?.substitutes ?: emptyList()
        val lineupPlayers = startXI + substitutes

        return lineupPlayers.map {
            PlayerApiSportsCreateDto(
                apiId = it.player.id,  // id가 null일 수 있음 - 상위에서 필터링됨
                name = it.player.name,
                position = it.player.pos
            )
        }
    }

    private fun extractPlayersInStatsNotExistInLineup(
        response: ApiSportsFixtureSingle,
        teamApiId: Long,
        lineupPlayerApiIds: Set<Long>
    ): List<PlayerApiSportsCreateDto> {
        val playerStatistics = response.response[0].players

        return playerStatistics
            .filter { it.team.id == teamApiId }
            .flatMap { teamStats ->
                teamStats.players
                    .filter {
                        // 라인업에 없는 선수만 추출 (id가 null이면 어차피 상위에서 필터링됨)
                        it.player.id == null || it.player.id !in lineupPlayerApiIds
                    }
                    .map { player ->
                        PlayerApiSportsCreateDto(
                            apiId = player.player.id,  // id가 null일 수 있음 - 상위에서 필터링됨
                            name = player.player.name,
                        )
                    }
            }
    }

    private fun extractTeamApiId(
        response: ApiSportsFixtureSingle,
        isHome: Boolean
    ): Long? {
        val teams = response.response[0].teams
        return if (isHome) teams.home.id else teams.away.id
    }
}