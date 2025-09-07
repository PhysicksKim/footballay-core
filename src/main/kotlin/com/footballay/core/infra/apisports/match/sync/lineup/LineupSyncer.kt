package com.footballay.core.infra.apisports.match.sync.lineup

import com.footballay.core.infra.apisports.match.dto.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerKeyGenerator
import com.footballay.core.infra.apisports.match.sync.dto.LineupSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchPlayerDto
import com.footballay.core.logger
import org.springframework.stereotype.Component

/**
 * 라인업 데이터를 동기화합니다.
 * 
 * 책임:
 * - Lineup 선수 정보 (substitute 여부와 id=null 여부 포함) 처리
 * - Context를 통한 O(1) 조회로 성능 최적화
 * - 새로운 선수는 자동으로 Context에 추가
 */
@Component
class LineupSyncer : MatchLineupDtoExtractor {

    private val log = logger()

    override fun extractLineup(dto: FullMatchSyncDto, context: MatchPlayerContext): LineupSyncDto {
        val homeId = dto.teams.home.id
        val awayId = dto.teams.away.id

        if (homeId == null || awayId == null) {
            log.info("홈 또는 어웨이 팀 ID가 비어 있습니다. 홈: $homeId, 어웨이: $awayId")
            return LineupSyncDto.Companion.EMPTY
        }
        if (dto.lineups.isEmpty()) {
            log.info("라인업 정보가 비어 있습니다. 홈: $homeId, 어웨이: $awayId")
            return LineupSyncDto.Companion.EMPTY
        }

        val homeLineupDto = dto.lineups.find { it.team.id == homeId }
        val awayLineupDto = dto.lineups.find { it.team.id == awayId }
        if (homeLineupDto == null || awayLineupDto == null) {
            log.warn("라인업에서 home away 팀을 매칭할 수 없습니다. 홈: $homeId, 어웨이: $awayId")
            return LineupSyncDto.Companion.EMPTY
        }

        val homeStartMpMap = createLineupMpMap(homeLineupDto, homeId, substitute = false)
        val homeSubMpMap = createLineupMpMap(homeLineupDto, homeId, substitute = true)
        val awayStartMpMap = createLineupMpMap(awayLineupDto, awayId, substitute = false)
        val awaySubMpMap = createLineupMpMap(awayLineupDto, awayId, substitute = true)

        if(homeStartMpMap.size != 11 || awayStartMpMap.size != 11) {
            logLineupAnomaly(homeLineupDto, awayLineupDto, homeStartMpMap.size, awayStartMpMap.size)
        }

        context.putAllLineup(homeStartMpMap)
        context.putAllLineup(homeSubMpMap)
        context.putAllLineup(awayStartMpMap)
        context.putAllLineup(awaySubMpMap)

        val homeLineupSyncDto = toLineupSyncDto(homeLineupDto, homeStartMpMap, homeSubMpMap)
        val awayLineupSyncDto = toLineupSyncDto(awayLineupDto, awayStartMpMap, awaySubMpMap)

        return LineupSyncDto(
            home = homeLineupSyncDto,
            away = awayLineupSyncDto
        )
    }

    private fun toLineupSyncDto(
        lineupDto: FullMatchSyncDto.LineupDto,
        startMpMap: Map<String, MatchPlayerDto>,
        subMpMap: Map<String, MatchPlayerDto>
    ): LineupSyncDto.Lineup = LineupSyncDto.Lineup(
        teamApiId = lineupDto.team.id,
        teamName = lineupDto.team.name,
        teamLogo = lineupDto.team.logo,
        playerColor = LineupSyncDto.Color(
            primary = lineupDto.team.colors?.player?.primary,
            number = lineupDto.team.colors?.player?.number,
            border = lineupDto.team.colors?.player?.border
        ),
        goalkeeperColor = LineupSyncDto.Color(
            primary = lineupDto.team.colors?.goalkeeper?.primary,
            number = lineupDto.team.colors?.goalkeeper?.number,
            border = lineupDto.team.colors?.goalkeeper?.border
        ),
        formation = lineupDto.formation,
        startMpKeys = startMpMap.keys.toList(),
        subMpKeys = subMpMap.keys.toList()
    )

    private fun createLineupMpMap(
        lineupDto: FullMatchSyncDto.LineupDto,
        teamId: Long,
        substitute: Boolean,
    ): Map<String, MatchPlayerDto> {
        val players = if (substitute) lineupDto.substitutes else lineupDto.startXI
        
        return players
            .filter { it.player.name != null && it.player.name.isNotBlank() }
            .associate { player ->
                val key = MatchPlayerKeyGenerator.generateMatchPlayerKey(player.player.id, player.player.name!!)
                val dto = lineupPlayerToMatchPlayerDto(player, teamApiId = teamId, substitute = substitute)
                key to dto
            }
    }

    private fun lineupPlayerToMatchPlayerDto(
        player: FullMatchSyncDto.LineupDto.LineupPlayerDto,
        teamApiId: Long,
        substitute: Boolean
    ): MatchPlayerDto = MatchPlayerDto(
        matchPlayerUid = null,
        apiId = player.player.id,
        name = player.player.name!!,
        number = player.player.number,
        position = player.player.pos ?: "Unknown",
        grid = player.player.grid,
        substitute = substitute,
        teamApiId = teamApiId,
        playerApiSportsInfo = null
    )

    private fun logLineupAnomaly(
        homeLineupDto: FullMatchSyncDto.LineupDto,
        awayLineupDto: FullMatchSyncDto.LineupDto,
        homeStartCount: Int,
        awayStartCount: Int
    ) {
        val sb = StringBuilder()
        sb.append("라인업 MatchPlayerDto 생성 결과 시작 선수 수가 11명이 아닙니다. 홈: ${homeStartCount}, 어웨이: ${awayStartCount}\n")

        sb.append("\tHomeTeam {id=${homeLineupDto.team.id},name=${homeLineupDto.team.name}} lineup\n")
        val homePlayers = homeLineupDto.startXI.joinToString(", ") { "{id=${it.player.id},name=${it.player.name}}" }
        sb.append(homePlayers)
        sb.append("\n")

        sb.append("\tAwayTeam {id=${awayLineupDto.team.id},name=${awayLineupDto.team.name}} lineup\n")
        val awayPlayers = awayLineupDto.startXI.joinToString(", ") { "{id=${it.player.id},name=${it.player.name}}" }
        sb.append(awayPlayers)
        sb.append("\n")

        log.warn(sb.toString())
    }
}
