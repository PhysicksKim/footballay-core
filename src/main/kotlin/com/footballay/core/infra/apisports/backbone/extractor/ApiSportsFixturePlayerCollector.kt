package com.footballay.core.infra.apisports.backbone.extractor

import com.footballay.core.infra.apisports.backbone.sync.PlayerApiSportsCreateDto
import com.footballay.core.infra.apisports.match.ApiSportsFixtureSingle
import com.footballay.core.logger
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * ApiSports Fixture 응답에서 선수 정보를 추출하는 컬렉터
 *
 * Fixture의 라인업과 통계 데이터에서 선수 정보를 팀별로 추출합니다.
 * ApiId가 존재하는 선수만 추출하며, 라인업과 통계에서 중복을 제거합니다.
 *
 * **주의:** ApiId가 null인 선수는 Core-Api 구조에 저장되지 않으며,
 * Match 데이터 동기화 시 별도로 처리됩니다.
 */
@Component
class ApiSportsFixturePlayerCollector {
    private val log = logger()

    @Transactional
    fun extractPlayersByTeam(response: ApiSportsFixtureSingle): Map<Long, List<PlayerApiSportsCreateDto>> =
        try {
            val home = extractTeamPlayers(response, isHome = true)
            val away = extractTeamPlayers(response, isHome = false)
            listOfNotNull(home, away).toMap()
        } catch (e: IllegalArgumentException) {
            log.info("선수 추출 실패: {}", e.message)
            emptyMap()
        } catch (e: Exception) {
            log.error("선수 추출 중 예상치 못한 오류: {}", e.message, e)
            throw e
        }

    private fun extractTeamPlayers(
        response: ApiSportsFixtureSingle,
        isHome: Boolean,
    ): Pair<Long, List<PlayerApiSportsCreateDto>>? {
        val teamApiId = extractTeamApiId(response, isHome)
        requireNotNull(teamApiId) {
            "팀 API ID가 null입니다. teams=${response.response[0].teams}"
        }

        val lineupPlayers = extractPlayersFromLineup(response, teamApiId).filter { it.apiId != null }
        val lineupPlayerApiIds = lineupPlayers.mapNotNull { it.apiId }.toSet()

        val statsPlayers =
            extractPlayersInStatsNotExistInLineup(response, teamApiId, lineupPlayerApiIds).filter {
                it.apiId !=
                    null
            }
        if (statsPlayers.isNotEmpty()) {
            log.warn(
                "라인업과 통계에서 선수 불일치 발견 - lineup: {}, stats: {}\n라인업: {}\n통계: {}",
                lineupPlayers.size,
                statsPlayers.size,
                lineupPlayers.joinToString { it.name ?: "NO-NAME" },
                statsPlayers.joinToString { it.name ?: "NO-NAME" },
            )
        }

        val allPlayers = lineupPlayers + statsPlayers
        log.info("선수 추출 완료 - {}명 (teamApiId={})", allPlayers.size, teamApiId)
        return teamApiId to allPlayers
    }

    private fun extractPlayersFromLineup(
        response: ApiSportsFixtureSingle,
        teamApiId: Long,
    ): List<PlayerApiSportsCreateDto> {
        val lineups = response.response[0].lineups
        val teamLineup = lineups.find { it.team.id == teamApiId }
        val allLineupPlayers = (teamLineup?.startXI ?: emptyList()) + (teamLineup?.substitutes ?: emptyList())

        return allLineupPlayers.map {
            PlayerApiSportsCreateDto(
                apiId = it.player.id,
                name = it.player.name,
                position = it.player.pos,
            )
        }
    }

    private fun extractPlayersInStatsNotExistInLineup(
        response: ApiSportsFixtureSingle,
        teamApiId: Long,
        lineupPlayerApiIds: Set<Long>,
    ): List<PlayerApiSportsCreateDto> =
        response.response[0]
            .players
            .filter { it.team.id == teamApiId }
            .flatMap { teamStats ->
                teamStats.players
                    .filter { it.player.id == null || it.player.id !in lineupPlayerApiIds }
                    .map { player ->
                        PlayerApiSportsCreateDto(
                            apiId = player.player.id,
                            name = player.player.name,
                        )
                    }
            }

    private fun extractTeamApiId(
        response: ApiSportsFixtureSingle,
        isHome: Boolean,
    ): Long? {
        val teams = response.response[0].teams
        return if (isHome) teams.home.id else teams.away.id
    }
}
