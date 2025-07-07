package com.footballay.core.infra.apisports.syncer.match.event

import com.footballay.core.infra.apisports.live.FullMatchSyncDto
import com.footballay.core.infra.apisports.live.FullMatchSyncDto.LineupDto
import com.footballay.core.infra.apisports.syncer.match.context.MatchPlayerContext
import com.footballay.core.infra.apisports.syncer.match.context.MatchPlayerKeyGenerator.generateMatchPlayerKey as generateMpKey
import com.footballay.core.infra.apisports.syncer.match.dto.MatchEventDto
import com.footballay.core.infra.apisports.syncer.match.dto.MatchEventSyncDto
import com.footballay.core.infra.apisports.syncer.match.dto.MatchPlayerDto
import com.footballay.core.logger
import org.springframework.stereotype.Component

/**
 * 이벤트 데이터를 동기화합니다.
 * 
 * 책임:
 * - Event 데이터 처리 (골, 카드, 교체 등)
 * - Context를 통한 선수 매칭
 * - 새로운 이벤트 전용 선수 생성
 * - Subst 이벤트의 player/assist 정규화 (player=subin, assist=subout)
 *
 */
@Component
class EventSyncer : MatchEventSync {

    private val log = logger()

    /*
    이벤트에서는 뭘 조심해야할까?
    일단 이벤트에서는 2개의 MatchPlayer 대응되는 필드가 있는데 event.player와 event.assist가 MatchPlayer 대응 필드에 해당한다.
    다만 이 경우 Goal 의 경우 명확하지만 Card, Subst 의 경우 매우 복잡해지는게 문제다.
    특히 Subst 의 경우 player 가 교체 선수일 수도 있고, assist 가 교체 선수일 수도 있다.
     */
    /**
     * ApiSports 의 데이터 문제로 인해, Subst Event 에서 player/assist 필드 중 어떤 필드가 Sub in/out 인지 경기마다 달라집니다.
     *
     * 따라서 FullMatchSyncDto 에서 라인업 데이터를 추출하고, 시작 lineup 부터 순차적 subst event 적용하면서 in/out 선수를 시뮬레이션 해야 합니다.
     * 또한 같은 경기에서도 subst event 의 player/assist - sub in/out 정보가 이벤트별로 다르고 동일 이벤트도 경기 중에 달라질 수 있기 때문에
     * 이전에 저장된 이벤트 정보를 통해서 유추하면 안됩니다. 매 시행마다 새롭게 시뮬레이션 해야합니다.
     */
    override fun syncEvents(dto: FullMatchSyncDto, context: MatchPlayerContext): MatchEventSyncDto {
        if (dto.events.isEmpty()) {
            log.info("이벤트 데이터가 비어 있습니다.")
            return MatchEventSyncDto()
        }

        try {
            // 1. sub in/out 시뮬레이션을 위한 라인업 선발/후보 정보 추출
            val lineupForSubstSimulation = extractLineupInfo(dto)
            if(validateLineupInfo(lineupForSubstSimulation)) {
                log.info("lineup 정보가 존재하지 않아 match event 저장을 건너뜁니다")
                return MatchEventSyncDto()
            }
            
            // 2. 이벤트를 순서대로 처리하면서 subst in/out 정규화
            val normalizedEvents = createNormalizedEvents(dto, lineupForSubstSimulation)

            // 3. 정규화된 이벤트를 MatchEventDto로 변환
            val eventDtos = createMatchEventDtos(normalizedEvents)

            // 4. Context에 이벤트 전용 선수 추가 (라인업에 없는 선수)
            addEventOnlyPlayers(normalizedEvents, context)

            log.info("이벤트 동기화 완료: ${eventDtos.size}개 이벤트 처리")
            return MatchEventSyncDto(events = eventDtos)
        } catch (e: Exception) {
            log.error("이벤트 동기화 중 오류 발생: ${e.message}", e)
            return MatchEventSyncDto()
        }
    }

    private fun createMatchEventDtos(normalizedEvents: List<FullMatchSyncDto.EventDto>): List<MatchEventDto> =
        normalizedEvents.mapIndexed { index, event ->
            toMatchEventDto(event, index + 1)
        }

    private fun createNormalizedEvents(
        dto: FullMatchSyncDto,
        lineupForSubstSimulation: LineupInfo
    ): List<FullMatchSyncDto.EventDto> = dto.events.mapIndexed { index, event ->
        when (event.type.lowercase()) {
            "subst" -> normalizeSubstEvent(event, lineupForSubstSimulation)
            else -> event // 다른 이벤트는 그대로 유지
        }
    }

    private fun validateLineupInfo(lineupInfo: LineupInfo): Boolean {
        if(lineupInfo.homeTeamId == -1L || lineupInfo.awayTeamId == -1L) {
            log.info("팀 아이디를 정보가 없어서 match event 저장을 위한 라인업을 추출할 수 없습니다.")
            return false
        }
        if(lineupInfo.homeStartPlayers.isEmpty() ||
            lineupInfo.homeSubPlayers.isEmpty() ||
            lineupInfo.awayStartPlayers.isEmpty() ||
            lineupInfo.awaySubPlayers.isEmpty()
        ) {
            log.info("라인업 정보가 없습니다. \n" +
                    "홈팀 선발 수: ${lineupInfo.homeStartPlayers.size}, " +
                    "홈팀 후보 수: ${lineupInfo.homeSubPlayers.size}, " +
                    "어웨이팀 선발 수: ${lineupInfo.awayStartPlayers.size}, " +
                    "어웨이팀 후보 수: ${lineupInfo.awaySubPlayers.size}")
            return false
        }
        return true
    }

    /**
     * 라인업 정보에서 선발/후보 선수 정보를 추출합니다.
     * 완전한 관심사 분리를 위해 dto에서 직접 추출합니다.
     */
    private fun extractLineupInfo(dto: FullMatchSyncDto): LineupInfo {
        val homeId = dto.teams.home.id
        val awayId = dto.teams.away.id
        
        if (homeId == null || awayId == null) {
            return LineupInfo.wrong()
        }

        val homeLineup = dto.lineups.find { it.team.id == homeId }
        val awayLineup = dto.lineups.find { it.team.id == awayId }

        if (homeLineup == null || awayLineup == null) {
            return LineupInfo.empty(homeId, awayId)
        }

        val homeStartPlayers = homeLineup.startXI.filter{ it.player.name != null }
                .associate { generateMpKey(it.player.id, it.player.name!!) to it }.toMutableMap()
        val homeSubPlayers = homeLineup.substitutes.filter{ it.player.name != null }
                .associate { generateMpKey(it.player.id, it.player.name!!) to it }.toMutableMap()
        val awayStartPlayers = awayLineup.startXI.filter{ it.player.name != null }
                .associate { generateMpKey(it.player.id, it.player.name!!) to it }.toMutableMap()
        val awaySubPlayers = awayLineup.substitutes.filter{ it.player.name != null }
                .associate { generateMpKey(it.player.id, it.player.name!!) to it }.toMutableMap()

        return LineupInfo(
            homeTeamId = homeId,
            awayTeamId = awayId,
            homeStartPlayers = homeStartPlayers,
            homeSubPlayers = homeSubPlayers,
            awayStartPlayers = awayStartPlayers,
            awaySubPlayers = awaySubPlayers
        )
    }

    /**
     * subst 이벤트의 player/assist를 정규화합니다.
     * ApiSports 에서는 subst 이벤트에서 sub in/out 이 명확히 구분되지 않기 때문에 해당 메서드를 통해서
     * player = sub-in 선수, assist = sub-out 선수로 일관되게 설정합니다.
     */
    private fun normalizeSubstEvent(
        event: FullMatchSyncDto.EventDto,
        lineupInfo: LineupInfo,
    ): FullMatchSyncDto.EventDto {
        val teamId = event.team.id
        if (teamId == null) {
            log.warn("Subst 이벤트의 팀 ID가 null입니다: ${event}")
            return event
        }

        val playerId = event.player?.id
        val playerName = event.player?.name
        val assistId = event.assist?.id
        val assistName = event.assist?.name
        if (playerName == null || assistName == null) {
            log.warn("Subst 이벤트의 player 또는 assist ID가 null입니다: ${event}")
            return event
        }

        // 홈팀인지 어웨이팀인지 판단
        val isHomeTeam = teamId == lineupInfo.homeTeamId

        val startPlayers = if (isHomeTeam) lineupInfo.homeStartPlayers else lineupInfo.awayStartPlayers
        val subPlayers = if (isHomeTeam) lineupInfo.homeSubPlayers else lineupInfo.awaySubPlayers

        // player가 선발 선수인지 후보 선수인지 판단
        val playerIsSub = subPlayers.containsKey(generateMpKey(playerId, playerName))
        val assistIsSub = subPlayers.containsKey(generateMpKey(assistId, assistName))

        // 정규화: player = sub-in, assist = sub-out
        val (subInPlayer, subOutPlayer) = when {
            playerIsSub && !assistIsSub -> {
                // player가 후보, assist가 선발 -> player가 sub-in, assist가 sub-out
                event.player to event.assist
            }
            !playerIsSub && assistIsSub -> {
                // player가 선발, assist가 후보 -> assist가 sub-in, player가 sub-out
                event.assist to event.player
            }
            else -> {
                // 둘 다 후보이거나 둘 다 선발인 경우 (비정상적인 상황)
                log.warn("비정상적인 subst 이벤트: player와 assist가 모두 후보이거나 모두 선발입니다. ${event}")
                event.player to event.assist
            }
        }

        // lineup 의 map 을 수정해서 "현재까지 이벤트 적용시켜서 경기장에서 뛰는 선수" 를 업데이트 해야한다
        // 왜냐하면 sub-in 선수가 다시 또 sub out 될 수 있기 때문이다.
        // 예를 들어 수비수 중 한 자리가 [ 선발수비수 -> 교체 수비수 -> 또 다른 선수 ] 로 교체될 수 있다.
        val outPlayerMpKey = generateMpKey(subOutPlayer.id, subOutPlayer.name!!)
        val inPlayerMpKey = generateMpKey(subInPlayer.id, subInPlayer.name!!)
        val outMp = startPlayers[outPlayerMpKey]
        val inMp = subPlayers[inPlayerMpKey]
        if(outMp == null || inMp == null) {
            log.warn("Subst 이벤트에서 선수 매칭 실패: outMp=$outMp, inMp=$inMp, event=$event")
            return event
        }
        startPlayers.remove(outPlayerMpKey)
        subPlayers.remove(inPlayerMpKey)
        startPlayers[inPlayerMpKey] = inMp
        subPlayers[outPlayerMpKey] = outMp

        return event.copy(player = subInPlayer, assist = subOutPlayer)
    }

    /**
     * 이벤트를 MatchEventDto로 변환합니다.
     */
    private fun toMatchEventDto(event: FullMatchSyncDto.EventDto, sequence: Int): MatchEventDto {
        return MatchEventDto(
            sequence = sequence,
            elapsedTime = event.time.elapsed,
            extraTime = event.time.extra,
            eventType = event.type,
            detail = event.detail,
            comments = event.comments,
            teamApiId = event.team.id,
            playerMpKey = if(event.player?.name != null) generateMpKey(event.player.id, event.player.name) else null,
            assistMpKey = if(event.assist?.name != null) generateMpKey(event.assist.id, event.assist.name) else null
        )
    }

    /**
     * 라인업에 없는 이벤트 전용 선수를 Context에 추가합니다.
     */
    private fun addEventOnlyPlayers(
        events: List<FullMatchSyncDto.EventDto>,
        context: MatchPlayerContext
    ) {
        events.forEach { event ->
            // player가 라인업에 없는 경우 추가
            event.player?.let { player ->
                createEventMatchPlayerIfNeed(player, context, event)
            }

            // assist가 라인업에 없는 경우 추가
            event.assist?.let { assist ->
                createEventMatchPlayerIfNeed(assist, context, event)
            }
        }
    }

    // todo : key 생성 조건 및 생성 로직과 context 포함 여부 로직을 분리해야 합니다
    private fun createEventMatchPlayerIfNeed(
        player: FullMatchSyncDto.EventDto.EventPlayerDto,
        context: MatchPlayerContext,
        event: FullMatchSyncDto.EventDto
    ) {
        if (player.name != null) {
            val key = generateMpKey(player.id, player.name)
            if (!context.lineupMpDtoMap.containsKey(key) && !context.eventMpDtoMap.containsKey(key)) {
                val dto = MatchPlayerDto(
                    matchPlayerUid = null,
                    apiId = player.id,
                    name = player.name,
                    number = null,
                    position = null,
                    grid = null,
                    substitute = true, // 이벤트용 선수는 선발 조회에 등장하면 안됨
                    nonLineupPlayer = true, // 라인업에 없는 이벤트 전용 선수
                    teamApiId = event.team.id,
                    playerApiSportsInfo = null,
                )
                context.eventMpDtoMap[key] = dto
            }
        }
    }

    /**
     * 라인업 정보를 담는 데이터 클래스
     */
    private data class LineupInfo(
        val homeTeamId: Long,
        val awayTeamId: Long,
        val homeStartPlayers: MutableMap<String, LineupDto.LineupPlayerDto>,
        val homeSubPlayers: MutableMap<String, LineupDto.LineupPlayerDto>,
        val awayStartPlayers: MutableMap<String, LineupDto.LineupPlayerDto>,
        val awaySubPlayers: MutableMap<String, LineupDto.LineupPlayerDto>,
    ) {
        companion object {
            fun wrong() = LineupInfo(
                homeTeamId = -1L,
                awayTeamId = -1L,
                homeStartPlayers = mutableMapOf(),
                homeSubPlayers = mutableMapOf(),
                awayStartPlayers = mutableMapOf(),
                awaySubPlayers = mutableMapOf()
            )
            fun empty(homeId: Long, awayId: Long) = LineupInfo(
                homeTeamId = homeId,
                awayTeamId = awayId,
                homeStartPlayers = mutableMapOf(),
                homeSubPlayers = mutableMapOf(),
                awayStartPlayers = mutableMapOf(),
                awaySubPlayers = mutableMapOf()
            )
        }
    }
} 