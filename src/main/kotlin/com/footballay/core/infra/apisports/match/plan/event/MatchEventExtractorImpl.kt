package com.footballay.core.infra.apisports.match.plan.event

import com.footballay.core.infra.apisports.match.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.FullMatchSyncDto.LineupDto
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerKeyGenerator
import com.footballay.core.infra.apisports.match.plan.dto.MatchEventDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchEventPlanDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchPlayerDto
import com.footballay.core.logger
import org.springframework.stereotype.Component

/**
 * ApiSports Raw Response를 규칙에 따라 정리된 MatchEventPlanDto로 변환합니다.
 *
 * ## 처리 흐름
 * ```
 * FullMatchSyncDto (raw response)
 * [1] 라인업 정보 추출 및 검증
 * [2] Substitution 이벤트 정규화 (player=sub-in, assist=sub-out)
 * [3] 이벤트 타입 및 팀 필드 조정
 * [4] 이벤트 정렬 및 sequence 재할당
 * [5] Event-only 선수 추가 (라인업에 없는 선수)
 * MatchEventPlanDto (정리된 plan)
 * ```
 *
 * ## 예외 처리 및 변환 규칙
 *
 * ### 1. 조기 종료 조건 (빈 MatchEventPlanDto 반환)
 * - `dto.events`가 비어있는 경우
 * - 홈/어웨이 팀 ID가 null인 경우
 * - 라인업 정보가 없는 경우 (선발 또는 후보 선수가 비어있음)
 * - 처리 중 예외 발생 시 (로그 기록 후 빈 결과 반환)
 *
 * ### 2. Substitution 정규화 (normalizeSubstEvent)
 * **문제**: ApiSports는 Subst 이벤트에서 player/assist 필드가 누가 sub-in/out인지 일관성 없이 제공
 * - 같은 경기 내에서도 이벤트별로 달라질 수 있음
 * - 경기 중 API 응답이 업데이트되면서 순서가 바뀔 수 있음
 *
 * **해결책**: 라인업 시뮬레이션
 * 1. 시작 라인업(선발 11명, 후보 선수) 정보 추출
 * 2. 이벤트를 순서대로 처리하며 현재 필드 상태 추적
 * 3. player가 후보 && assist가 선발 → 정상 (player=sub-in, assist=sub-out)
 * 4. player가 선발 && assist가 후보 → 역순 (필드 교체하여 정규화)
 * 5. 둘 다 후보 또는 둘 다 선발 → 비정상 (경고 로그, 원본 유지)
 * 6. 교체 후 필드 상태 업데이트 (연속 교체 지원: sub-in 선수가 다시 sub-out 가능)
 *
 * ### 3. 이벤트 타입 조정 (toMatchEventDto)
 *
 * #### 3.1 패널티 실축 → ETC
 * - 조건: `type="Goal"` && `detail="Missed Penalty"`
 * - 변환: `eventType="ETC"`
 * - 이유: 골이 아닌 골 기회이므로 별도 분류.
 * - 추가설명: 이 이벤트는 경기 중에는 등장하지만 경기 후에는 제거됨
 *
 * #### 3.2 Own Goal → 상대 팀으로 득점 기록
 * - 조건: `type="Goal"` && `detail="Own Goal"`
 * - 변환: `teamApiId`를 상대 팀으로 교체
 *   - 홈팀 선수가 Own Goal → 어웨이팀(득점 획득 팀) ID로 변경
 *   - 어웨이팀 선수가 Own Goal → 홈팀 ID로 변경
 * - 예외: 팀 ID가 홈/어웨이와 일치하지 않으면 경고 로그 + 원본 유지
 *
 * #### 3.3 Substitution null → UNKNOWN
 * - 조건: `type="subst"` && `player.id=null` && `player.name=null` && `assist.id=null` && `assist.name=null`
 * - 변환: `eventType="UNKNOWN"`
 * - 주의: id만 null이고 name이 있는 경우는 정상 처리 (이름으로 매칭 가능)
 *
 * ### 4. 이벤트 정렬 및 Sequence 재할당 (createMatchEventDtos)
 * **문제**: ApiSports 응답 순서가 시간 순서와 일치하지 않을 수 있음
 *
 * **정렬 우선순위**:
 * 1. `elapsedTime` (ASC) - 경기 시간
 * 2. `extraTime` (ASC, null → 0으로 간주) - 추가 시간
 * 3. `teamApiId` (ASC) - 같은 시간대 이벤트는 팀별로 그룹핑
 * 4. Substitution 번호 (ASC) - detail에서 "Substitution N" 숫자 추출
 *    - 예: "Substitution 1" → 1, "Substitution 2" → 2
 *    - Substitution이 아니면 0 (정렬 영향 없음)
 *    - 숫자 없으면 Int.MAX_VALUE (뒤로 밀림)
 *
 * **Sequence 재할당**:
 * - 정렬 후 0부터 순차적으로 재할당 (연속성 보장)
 * - 원본 API 응답 순서는 무시됨
 *
 * ### 5. Event-only 선수 추가 (addEventOnlyPlayers)
 * **문제**: 일부 선수가 라인업에 없지만 이벤트에만 등장 (예: Coach Card Event, 긴급 유스 콜업으로 Data Provider가 처음 보는 선수)
 *
 * **처리**:
 * - 이벤트의 player/assist가 Context(lineupMpDtoMap, eventMpDtoMap)에 없으면 추가
 * - `substitute=true`, `nonLineupPlayer=true`로 표시
 * - 선수 ID는 event에서 가져오되, name이 null이면 추가하지 않음 (매칭 불가)
 *
 * ## 주요 데이터 구조
 * - **LineupInfo**: 홈/어웨이 팀의 선발/후보 선수 맵 (Substitution 시뮬레이션용)
 * - **MatchPlayerContext**: 전체 선수 정보 (라인업 + 이벤트 전용)
 * - **MatchEventDto**: 정리된 이벤트 DTO (sequence, type, team, players 등)
 *
 * ## 에러 처리 전략
 * - 치명적 오류 (팀 ID null, 라인업 없음) → 빈 결과 반환
 * - 데이터 이상 (비정상 subst, Own Goal 팀 불일치) → 경고 로그 + 원본 유지
 * - 예외 발생 → catch하여 로그 기록 후 빈 결과 반환
 */
@Component
class MatchEventExtractorImpl : MatchEventDtoExtractor {
    private val log = logger()

    /**
     * ApiSports 의 데이터 문제로 인해, Subst Event 에서 player/assist 필드 중 어떤 필드가 Sub in/out 인지 경기마다 달라집니다.
     *
     * 따라서 FullMatchSyncDto 에서 라인업 데이터를 추출하고, 시작 lineup 부터 순차적 subst event 적용하면서 in/out 선수를 시뮬레이션 해야 합니다.
     * 또한 같은 경기에서도 subst event 의 player/assist - sub in/out 정보가 이벤트별로 다르고 동일 이벤트도 경기 중에 달라질 수 있기 때문에
     * 이전에 저장된 이벤트 정보를 통해서 유추하면 안됩니다. 매 시행마다 새롭게 시뮬레이션 해야합니다.
     */
    override fun extractEvents(
        dto: FullMatchSyncDto,
        context: MatchPlayerContext,
    ): MatchEventPlanDto {
        if (dto.events.isEmpty()) {
            log.info("이벤트 데이터가 비어 있습니다.")
            return MatchEventPlanDto()
        }

        try {
            // 1. sub in/out 시뮬레이션을 위한 라인업 선발/후보 정보 추출
            val lineupForSubstSimulation = extractLineupInfo(dto)
            if (!validateLineupInfo(lineupForSubstSimulation)) {
                log.info("lineup 정보가 존재하지 않아 match event 계획을 건너뜁니다")
                return MatchEventPlanDto()
            }

            // 2. 이벤트를 순서대로 처리하면서 subst in/out 정규화
            val normalizedEvents = createNormalizedEvents(dto, lineupForSubstSimulation)

            // 3. 정규화된 이벤트를 MatchEventDto로 변환
            val eventDtos = createMatchEventDtos(normalizedEvents, lineupForSubstSimulation)

            // 4. Context에 이벤트 전용 선수 추가 (라인업에 없는 선수)
            addEventOnlyPlayers(normalizedEvents, context)

            log.info("이벤트 계획 완료: {}개 이벤트 처리", eventDtos.size)
            return MatchEventPlanDto(events = eventDtos)
        } catch (e: Exception) {
            log.error("이벤트 계획 중 오류 발생: {}", e.message, e)
            return MatchEventPlanDto()
        }
    }

    private fun createMatchEventDtos(
        normalizedEvents: List<FullMatchSyncDto.EventDto>,
        lineupInfo: LineupInfo,
    ): List<MatchEventDto> {
        // 먼저 DTO로 변환 (sequence는 임시로 원본 인덱스 사용)
        val unsortedDtos =
            normalizedEvents.mapIndexed { index, event ->
                toMatchEventDto(event, index, lineupInfo)
            }

        // 정렬 로직
        // 1. elapsedTime (ASC)
        // 2. extraTime (ASC, null을 0으로 간주)
        // 3. teamApiId (ASC) - 같은 시간대, 같은 팀 이벤트 그룹핑
        // 4. Substitution인 경우 detail에서 "Substitution N" 숫자 추출하여 정렬
        val sortedDtos =
            unsortedDtos.sortedWith(
                compareBy(
                    { it.elapsedTime },
                    { it.extraTime ?: 0 },
                    { it.teamApiId ?: Long.MAX_VALUE },
                    { extractSubstitutionNumber(it) },
                ),
            )

        // sequence를 0부터 순차적으로 재할당
        return sortedDtos.mapIndexed { index, dto ->
            dto.copy(sequence = index)
        }
    }

    /**
     * Substitution 이벤트의 detail에서 숫자를 추출합니다.
     * 예: "Substitution 1" -> 1, "Substitution 2" -> 2
     * Substitution이 아니거나 숫자를 찾을 수 없는 경우 Int.MAX_VALUE 반환 (정렬 시 뒤로 밀림)
     */
    private fun extractSubstitutionNumber(dto: MatchEventDto): Int {
        if (dto.eventType.lowercase() != "subst") {
            return 0 // Substitution이 아닌 경우 0 반환 (정렬에 영향 없음)
        }

        val detail = dto.detail ?: return Int.MAX_VALUE
        val regex = """Substitution\s+(\d+)""".toRegex(RegexOption.IGNORE_CASE)
        val matchResult = regex.find(detail)
        return matchResult?.groupValues?.get(1)?.toIntOrNull() ?: Int.MAX_VALUE
    }

    private fun createNormalizedEvents(
        dto: FullMatchSyncDto,
        lineupForSubstSimulation: LineupInfo,
    ): List<FullMatchSyncDto.EventDto> =
        dto.events.mapIndexed { index, event ->
            when (event.type.lowercase()) {
                "subst" -> normalizeSubstEvent(event, lineupForSubstSimulation)
                else -> event // 다른 이벤트는 그대로 유지
            }
        }

    private fun validateLineupInfo(lineupInfo: LineupInfo): Boolean {
        if (lineupInfo.homeTeamId == -1L || lineupInfo.awayTeamId == -1L) {
            log.info("팀 아이디를 정보가 없어서 match event 저장을 위한 라인업을 추출할 수 없습니다.")
            return false
        }
        if (lineupInfo.homeStartPlayers.isEmpty() ||
            lineupInfo.homeSubPlayers.isEmpty() ||
            lineupInfo.awayStartPlayers.isEmpty() ||
            lineupInfo.awaySubPlayers.isEmpty()
        ) {
            log.info(
                "라인업 정보가 없습니다. \n" +
                    "홈팀 선발 수: ${lineupInfo.homeStartPlayers.size}, " +
                    "홈팀 후보 수: ${lineupInfo.homeSubPlayers.size}, " +
                    "어웨이팀 선발 수: ${lineupInfo.awayStartPlayers.size}, " +
                    "어웨이팀 후보 수: ${lineupInfo.awaySubPlayers.size}",
            )
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

        val homeStartPlayers =
            homeLineup.startXI
                .filter { it.player.name != null }
                .associate { MatchPlayerKeyGenerator.generateMatchPlayerKey(it.player.id, it.player.name!!) to it }
                .toMutableMap()
        val homeSubPlayers =
            homeLineup.substitutes
                .filter { it.player.name != null }
                .associate { MatchPlayerKeyGenerator.generateMatchPlayerKey(it.player.id, it.player.name!!) to it }
                .toMutableMap()
        val awayStartPlayers =
            awayLineup.startXI
                .filter { it.player.name != null }
                .associate { MatchPlayerKeyGenerator.generateMatchPlayerKey(it.player.id, it.player.name!!) to it }
                .toMutableMap()
        val awaySubPlayers =
            awayLineup.substitutes
                .filter { it.player.name != null }
                .associate { MatchPlayerKeyGenerator.generateMatchPlayerKey(it.player.id, it.player.name!!) to it }
                .toMutableMap()

        return LineupInfo(
            homeTeamId = homeId,
            awayTeamId = awayId,
            homeStartPlayers = homeStartPlayers,
            homeSubPlayers = homeSubPlayers,
            awayStartPlayers = awayStartPlayers,
            awaySubPlayers = awaySubPlayers,
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
            log.warn("Subst 이벤트의 팀 ID가 null입니다: {}", event)
            return event
        }

        val playerId = event.player?.id
        val playerName = event.player?.name
        val assistId = event.assist?.id
        val assistName = event.assist?.name
        if (playerName == null || assistName == null) {
            log.warn("Subst 이벤트의 player 또는 assist ID가 null입니다: {}", event)
            return event
        }

        // 홈팀인지 어웨이팀인지 판단
        val isHomeTeam = teamId == lineupInfo.homeTeamId

        val startPlayers = if (isHomeTeam) lineupInfo.homeStartPlayers else lineupInfo.awayStartPlayers
        val subPlayers = if (isHomeTeam) lineupInfo.homeSubPlayers else lineupInfo.awaySubPlayers

        // player가 선발 선수인지 후보 선수인지 판단
        val playerIsSub = subPlayers.containsKey(MatchPlayerKeyGenerator.generateMatchPlayerKey(playerId, playerName))
        val assistIsSub = subPlayers.containsKey(MatchPlayerKeyGenerator.generateMatchPlayerKey(assistId, assistName))

        // 정규화: player = sub-in, assist = sub-out
        val (subInPlayer, subOutPlayer) =
            when {
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
                    log.warn("비정상적인 subst 이벤트: player와 assist가 모두 후보이거나 모두 선발입니다. {}", event)
                    event.player to event.assist
                }
            }

        // lineup 의 map 을 수정해서 "현재까지 이벤트 적용시켜서 경기장에서 뛰는 선수" 를 업데이트 해야한다
        // 왜냐하면 sub-in 선수가 다시 또 sub out 될 수 있기 때문이다.
        // 예를 들어 수비수 중 한 자리가 [ 선발수비수 -> 교체 수비수 -> 또 다른 선수 ] 로 교체될 수 있다.
        val outPlayerMpKey = MatchPlayerKeyGenerator.generateMatchPlayerKey(subOutPlayer.id, subOutPlayer.name!!)
        val inPlayerMpKey = MatchPlayerKeyGenerator.generateMatchPlayerKey(subInPlayer.id, subInPlayer.name!!)
        val outMp = startPlayers[outPlayerMpKey]
        val inMp = subPlayers[inPlayerMpKey]
        if (outMp == null || inMp == null) {
            log.warn("Subst 이벤트에서 선수 매칭 실패: outMp={}, inMp={}, event={}", outMp, inMp, event)
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
     *
     * 특수 케이스 처리:
     * 1. 패널티 실축: type="Goal", detail="Missed Penalty" → type="ETC"
     * 2. Own Goal: type="Goal", detail="Own Goal" → teamApiId를 상대 팀으로 변경
     * 3. Substitution null: player와 assist가 모두 완전히 null → type="UNKNOWN"
     */
    private fun toMatchEventDto(
        event: FullMatchSyncDto.EventDto,
        sequence: Int,
        lineupInfo: LineupInfo,
    ): MatchEventDto {
        // 1. 패널티 실축 처리
        val adjustedType =
            if (event.type.equals("Goal", ignoreCase = true) &&
                event.detail?.equals("Missed Penalty", ignoreCase = true) == true
            ) {
                "ETC"
            } else if (event.type.equals("subst", ignoreCase = true)) {
                // 3. Substitution null 처리
                val playerIsNull = event.player?.id == null && event.player?.name == null
                val assistIsNull = event.assist?.id == null && event.assist?.name == null
                if (playerIsNull && assistIsNull) {
                    log.warn("Substitution 이벤트에서 player와 assist가 모두 null입니다. UNKNOWN으로 처리: {}", event)
                    "UNKNOWN"
                } else {
                    event.type
                }
            } else {
                event.type
            }

        // 2. Own Goal 팀 변경
        val adjustedTeamApiId =
            if (event.type.equals("Goal", ignoreCase = true) &&
                event.detail?.equals("Own Goal", ignoreCase = true) == true
            ) {
                // Own Goal인 경우 상대 팀으로 변경
                val currentTeamId = event.team.id
                when (currentTeamId) {
                    lineupInfo.homeTeamId -> lineupInfo.awayTeamId
                    lineupInfo.awayTeamId -> lineupInfo.homeTeamId
                    else -> {
                        log.warn("Own Goal 이벤트의 팀 ID가 홈/어웨이 팀 ID와 일치하지 않습니다: currentTeamId={}, homeTeamId={}, awayTeamId={}", currentTeamId, lineupInfo.homeTeamId, lineupInfo.awayTeamId)
                        event.team.id
                    }
                }
            } else {
                event.team.id
            }

        return MatchEventDto(
            sequence = sequence,
            elapsedTime = event.time.elapsed,
            extraTime = event.time.extra,
            eventType = adjustedType,
            detail = event.detail,
            comments = event.comments,
            teamApiId = adjustedTeamApiId,
            playerMpKey =
                if (event.player?.name != null) {
                    MatchPlayerKeyGenerator.generateMatchPlayerKey(
                        event.player.id,
                        event.player.name,
                    )
                } else {
                    null
                },
            assistMpKey =
                if (event.assist?.name != null) {
                    MatchPlayerKeyGenerator.generateMatchPlayerKey(
                        event.assist.id,
                        event.assist.name,
                    )
                } else {
                    null
                },
        )
    }

    /**
     * 라인업에 없는 이벤트 전용 선수를 Context에 추가합니다.
     */
    private fun addEventOnlyPlayers(
        events: List<FullMatchSyncDto.EventDto>,
        context: MatchPlayerContext,
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
        event: FullMatchSyncDto.EventDto,
    ) {
        if (player.name != null) {
            val key = MatchPlayerKeyGenerator.generateMatchPlayerKey(player.id, player.name)
            if (!context.lineupMpDtoMap.containsKey(key) && !context.eventMpDtoMap.containsKey(key)) {
                val dto =
                    MatchPlayerDto(
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
            fun wrong() =
                LineupInfo(
                    homeTeamId = -1L,
                    awayTeamId = -1L,
                    homeStartPlayers = mutableMapOf(),
                    homeSubPlayers = mutableMapOf(),
                    awayStartPlayers = mutableMapOf(),
                    awaySubPlayers = mutableMapOf(),
                )

            fun empty(
                homeId: Long,
                awayId: Long,
            ) = LineupInfo(
                homeTeamId = homeId,
                awayTeamId = awayId,
                homeStartPlayers = mutableMapOf(),
                homeSubPlayers = mutableMapOf(),
                awayStartPlayers = mutableMapOf(),
                awaySubPlayers = mutableMapOf(),
            )
        }
    }
}
