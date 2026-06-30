package com.footballay.core.infra.apisports.match.persist.player.collector

import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.plan.dto.MatchPlayerDto
import com.footballay.core.logger

/**
 * MatchPlayer DTO 우선순위 수집기
 *
 * MatchPlayerContext에서 여러 소스(lineup, event, stat)의 DTO를 우선순위 기반으로 수집합니다.
 *
 * **우선순위 규칙:**
 * 1. Lineup > Event > Stat (신뢰성 순서)
 * 2. 동일 선수가 여러 소스에 존재하면 높은 우선순위만 선택
 * 3. Event/Stat 전용 선수는 nonLineupPlayer로 마킹
 *
 * **사용 예시:**
 * ```kotlin
 * val context = MatchPlayerContext()
 * // context에 lineup, event, stat DTO들 추가
 * val collectedPlayers = MatchPlayerDtoCollector.collectFrom(context)
 * ```
 */
object MatchPlayerDtoCollector {
    private val log = logger()

    /**
     * 컨텍스트에서 우선순위 기반으로 MatchPlayer DTO를 수집합니다.
     *
     * @param context 라인업, 이벤트, 통계에서 추출된 MatchPlayer DTO들
     * @return 중복 제거되고 우선순위 적용된 MatchPlayer DTO 맵 (key: MatchPlayerKey)
     */
    fun collectFrom(context: MatchPlayerContext): Map<String, MatchPlayerDto> {
        val allPlayers = mutableMapOf<String, MatchPlayerDto>()

        // 1단계: Lineup 선수들 추가 (최고 우선순위)
        context.lineupMpDtoMap.forEach { (key, player) ->
            allPlayers[key] = player
            log.debug("Added lineup player: {}", key)
        }

        // 2단계: Event 선수들 추가 (lineup에 없는 경우만)
        context.eventMpDtoMap.forEach { (key, player) ->
            if (!allPlayers.containsKey(key)) {
                allPlayers[key] = player.copy(nonLineupPlayer = true)
                log.debug("Added event-only player: {}", key)
            } else {
                log.debug("Skipped event player (already in lineup): $key")
            }
        }

        // 3단계: Stat 선수들 추가 (lineup, event에 없는 경우만)
        context.statMpDtoMap.forEach { (key, player) ->
            if (!allPlayers.containsKey(key)) {
                allPlayers[key] = player.copy(nonLineupPlayer = true)
                log.debug("Added stat-only player: {}", key)
            } else {
                log.debug("Skipped stat player (already exists): $key")
            }
        }

        log.info(
            "Collected total ${allPlayers.size} unique players - Lineup: ${context.lineupMpDtoMap.size}, Event: ${context.eventMpDtoMap.size}, Stat: ${context.statMpDtoMap.size}",
        )

        return allPlayers.toMap()
    }
}
