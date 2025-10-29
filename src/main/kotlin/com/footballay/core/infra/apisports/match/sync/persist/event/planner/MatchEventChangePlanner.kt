package com.footballay.core.infra.apisports.match.sync.persist.event.planner

import com.footballay.core.infra.apisports.match.sync.dto.MatchEventDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchEventSyncDto
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import com.footballay.core.logger

/**
 * MatchEvent 변경 작업 계획기
 *
 * Event DTO와 기존 엔티티를 비교하여 데이터베이스 변경 작업을 계획합니다.
 *
 * **처리 로직:**
 * 1. **Sequence 검증**: DTO와 Entity의 sequence 무결성 검사
 * 2. **DTO와 엔티티를 sequence로 매칭하여 분류**
 * 3. **새로운 DTO → 엔티티 생성 계획**
 * 4. **매칭된 DTO-엔티티 → 변경사항 확인 후 업데이트 계획**
 * 5. **효율적인 고아 엔티티 → 삭제 계획**
 *
 * **특별 고려사항:**
 * - Event는 sequence 기반으로 매칭
 * - Player/Assist는 MatchPlayerKey로 매칭
 * - DTO는 이미 정규화된 상태 (EventSyncer에서 처리됨)
 * - Sequence 검증으로 데이터 무결성 보장
 * - 효율적인 삭제 로직으로 성능 최적화
 *
 * **Sequence 검증 항목:**
 * - 중복된 sequence 검사
 * - 누락된 sequence 검사 (연속성 확인)
 * - 시작점이 0인지 검사
 * - DTO와 Entity sequence 시작점 일치 검사
 *
 * **성능 최적화:**
 * - 기존: O(n×m) 복잡도 (전체 순회)
 * - 현재: O(n log n) 복잡도 (정렬 기반)
 */
object MatchEventChangePlanner {
    private val log = logger()

    /**
     * 기존 엔티티들을 sequence 기반 맵으로 변환합니다.
     *
     * **처리 과정:**
     * 1. sequence >= 0 조건으로 유효한 엔티티만 필터링
     * 2. sequence를 키로 하는 맵으로 변환
     * 3. 변환 결과를 로그로 기록
     *
     * **필터링 기준:**
     * - sequence >= 0: sequence는 0부터 시작하므로 0 이상인 것만 유효
     *
     * @param entities DB에서 조회된 기존 MatchEvent 엔티티들
     * @return sequence를 키로 하는 엔티티 맵
     */
    fun entitiesToSequenceMap(entities: List<ApiSportsMatchEvent>): Map<Int, ApiSportsMatchEvent> =
        entities
            .filter { it.sequence >= 0 } // sequence는 0부터 시작하므로 0 이상
            .associateBy { it.sequence }
            .also { resultMap ->
                log.info("Converted ${resultMap.size} valid events from ${entities.size} total events")
            }

    /**
     * DTO와 엔티티를 분석하여 변경 작업을 계획합니다.
     *
     * **처리 단계:**
     * 1. **Sequence 검증**: DTO와 Entity의 sequence 무결성 검사
     * 2. **새로운 DTO → 엔티티 생성 계획**: sequence가 Entity에 없는 DTO들
     * 3. **매칭된 DTO-엔티티 → 변경사항 확인 후 업데이트 계획**: sequence가 일치하는 쌍들
     * 4. **효율적인 고아 엔티티 → 삭제 계획**: Entity에만 있고 DTO에 없는 엔티티들
     *
     * **Sequence 검증 항목:**
     * - 중복된 sequence 검사
     * - 누락된 sequence 검사 (연속성 확인)
     * - 시작점이 0인지 검사
     * - DTO와 Entity sequence 시작점 일치 검사
     *
     * @param eventDto 새로운 Event DTO들 (이미 정규화됨)
     * @param entityEvents 기존 저장된 엔티티 이벤트들 (key: sequence)
     * @param fixtureApi FixtureApiSports (연결용)
     * @param homeTeam 홈팀 MatchTeam (연결용)
     * @param awayTeam 원정팀 MatchTeam (연결용)
     * @param allMatchPlayers 모든 MatchPlayer 엔티티들 (key: MatchPlayerKey, value: ApiSportsMatchPlayer)
     * @return 데이터베이스 변경 작업 명세서
     */
    fun planChanges(
        eventDto: MatchEventSyncDto,
        entityEvents: Map<Int, ApiSportsMatchEvent>,
        fixtureApi: FixtureApiSports,
        homeTeam: ApiSportsMatchTeam?,
        awayTeam: ApiSportsMatchTeam?,
        allMatchPlayers: Map<String, ApiSportsMatchPlayer>,
    ): MatchEventChangeSet {
        val dtoEvents = eventDto.events

        // 1단계: Sequence 검증 - DTO와 Entity의 sequence 무결성 검사
        validateSequences(dtoEvents, entityEvents)

        // 2단계: 새로운 DTO → 엔티티 생성 계획 - sequence가 Entity에 없는 DTO들
        val toCreate =
            buildNewEntities(
                dtoEvents.filter { !entityEvents.containsKey(it.sequence) },
                fixtureApi,
                homeTeam,
                awayTeam,
                allMatchPlayers,
            )

        // 3단계: 매칭된 DTO-엔티티 → 변경사항 확인 후 업데이트 계획 - sequence가 일치하는 쌍들
        val matchedPairs =
            dtoEvents
                .filter { entityEvents.containsKey(it.sequence) }
                .associate { dto ->
                    dto.sequence to MatchedPair(dto, entityEvents[dto.sequence]!!)
                }
        val toUpdate = buildUpdatedEntities(matchedPairs, allMatchPlayers)

        // 4단계: 효율적인 고아 엔티티 → 삭제 계획 - Entity에만 있고 DTO에 없는 엔티티들
        val toDelete = calculateOrphanEntities(dtoEvents, entityEvents)

        val changeSet =
            MatchEventChangeSet(
                toCreate = toCreate,
                toUpdate = toUpdate,
                toDelete = toDelete,
            )

        log.info(
            "Event change planning completed - Create: ${changeSet.createCount}, Update: ${changeSet.updateCount}, Delete: ${changeSet.deleteCount}",
        )

        return changeSet
    }

    /**
     * Sequence 검증을 수행합니다.
     *
     * **검증 항목:**
     * - **중복 검사**: 같은 sequence가 여러 번 나타나는지 확인
     * - **연속성 검사**: sequence가 연속적으로 증가하는지 확인 (누락된 sequence 검사)
     * - **시작점 검사**: sequence가 0부터 시작하는지 확인
     * - **DTO와 Entity 비교**: 시작점이 일치하는지 확인
     *
     * **경고 로그:**
     * - 중복된 sequence 발견 시 경고 로그 출력
     * - 누락된 sequence 발견 시 경고 로그 출력
     * - 시작점이 0이 아닌 경우 경고 로그 출력
     * - DTO와 Entity 시작점이 다른 경우 경고 로그 출력
     *
     * @param dtoEvents 검증할 Event DTO 목록
     * @param entityEvents 검증할 Entity 이벤트 맵
     */
    private fun validateSequences(
        dtoEvents: List<MatchEventDto>,
        entityEvents: Map<Int, ApiSportsMatchEvent>,
    ) {
        // DTO sequence 검증 - 중복, 연속성, 시작점 검사
        val dtoSequences = dtoEvents.map { it.sequence }.sorted()
        validateSequenceOrder("DTO", dtoSequences)

        // Entity sequence 검증 - 중복, 연속성, 시작점 검사
        val entitySequences = entityEvents.keys.sorted()
        validateSequenceOrder("Entity", entitySequences)

        // DTO와 Entity sequence 비교 - 시작점 일치 검사
        if (dtoSequences.isNotEmpty() && entitySequences.isNotEmpty()) {
            val dtoMin = dtoSequences.first()
            val entityMin = entitySequences.first()

            if (dtoMin != entityMin) {
                log.warn("Sequence 시작점 불일치 - DTO 시작: $dtoMin, Entity 시작: $entityMin")
            }
        }
    }

    /**
     * Sequence 순서를 검증합니다.
     *
     * **검증 항목:**
     * - **중복 검사**: 같은 sequence가 여러 번 나타나는지 확인
     * - **연속성 검사**: sequence가 연속적으로 증가하는지 확인 (누락된 sequence 검사)
     * - **시작점 검사**: sequence가 0부터 시작하는지 확인
     *
     * **경고 로그:**
     * - 중복된 sequence 발견 시 경고 로그 출력
     * - 누락된 sequence 발견 시 경고 로그 출력
     * - 시작점이 0이 아닌 경우 경고 로그 출력
     *
     * @param source 검증 대상 소스 ("DTO" 또는 "Entity")
     * @param sequences 검증할 sequence 목록 (정렬됨)
     */
    private fun validateSequenceOrder(
        source: String,
        sequences: List<Int>,
    ) {
        if (sequences.isEmpty()) return

        // 중복 검사 - 같은 sequence가 여러 번 나타나는지 확인
        val duplicates = sequences.groupingBy { it }.eachCount().filter { it.value > 1 }
        if (duplicates.isNotEmpty()) {
            log.warn("$source 에서 중복된 sequence 발견: ${duplicates.keys}")
        }

        // 연속성 검사 - sequence가 연속적으로 증가하는지 확인
        val expectedSequences = (sequences.first()..sequences.last()).toList()
        val missingSequences = expectedSequences - sequences.toSet()
        if (missingSequences.isNotEmpty()) {
            log.warn("$source 에서 누락된 sequence 발견: $missingSequences")
        }

        // 시작점 검사 - sequence가 0부터 시작하는지 확인
        if (sequences.first() != 0) {
            log.warn("$source sequence가 0부터 시작하지 않음. 시작점: ${sequences.first()}")
        }
    }

    /**
     * 효율적인 고아 엔티티 계산
     *
     * **처리 로직:**
     * 1. **개수 비교**: DTO sequence 개수가 Entity보다 적으면 마지막부터 삭제
     * 2. **순서 변경**: 개수가 같지만 sequence가 다른 경우 (이벤트 순서 변경)
     *
     * **성능 최적화:**
     * - 기존: O(n×m) 복잡도 (전체 순회)
     * - 현재: O(n log n) 복잡도 (정렬 기반)
     *
     * **삭제 전략:**
     * - 개수 감소 시: 마지막 sequence부터 삭제
     * - 순서 변경 시: DTO에 없는 sequence만 삭제
     *
     * @param dtoEvents 새로운 Event DTO 목록
     * @param entityEvents 기존 Entity 이벤트 맵
     * @return 삭제할 Event 엔티티 목록
     */
    private fun calculateOrphanEntities(
        dtoEvents: List<MatchEventDto>,
        entityEvents: Map<Int, ApiSportsMatchEvent>,
    ): List<ApiSportsMatchEvent> {
        if (entityEvents.isEmpty()) return emptyList()

        val dtoSequences = dtoEvents.map { it.sequence }.toSet()
        val entitySequences = entityEvents.keys.sorted()

        // DTO sequence 개수가 entity보다 적으면 마지막부터 삭제 - 개수 감소 시나리오
        if (dtoSequences.size < entitySequences.size) {
            val deleteCount = entitySequences.size - dtoSequences.size
            val sequencesToDelete = entitySequences.takeLast(deleteCount)

            log.info("Event 개수 감소 감지 - 삭제 대상 sequence: $sequencesToDelete")
            return sequencesToDelete.mapNotNull { entityEvents[it] }
        }

        // 개수가 같지만 sequence가 다른 경우 (이벤트 순서 변경) - 순서 변경 시나리오
        val orphanSequences = entitySequences.filter { it !in dtoSequences }
        if (orphanSequences.isNotEmpty()) {
            log.info("Event 순서 변경 감지 - 삭제 대상 sequence: $orphanSequences")
            return orphanSequences.mapNotNull { entityEvents[it] }
        }

        return emptyList()
    }

    /**
     * 새로운 DTO들로부터 엔티티를 생성합니다.
     *
     * **처리 과정:**
     * 1. DTO의 teamApiId로 MatchTeam 찾기
     * 2. DTO의 playerMpKey로 MatchPlayer 찾기
     * 3. DTO의 assistMpKey로 MatchPlayer 찾기
     * 4. 새로운 ApiSportsMatchEvent 엔티티 생성
     *
     * @param unmatchedDtos Entity에 없는 새로운 DTO 목록
     * @param fixtureApi FixtureApiSports (연결용)
     * @param homeTeam 홈팀 MatchTeam (연결용)
     * @param awayTeam 원정팀 MatchTeam (연결용)
     * @param allMatchPlayers 모든 MatchPlayer 엔티티들 (key: MatchPlayerKey, value: ApiSportsMatchPlayer)
     * @return 생성할 Event 엔티티 목록
     */
    private fun buildNewEntities(
        unmatchedDtos: List<MatchEventDto>,
        fixtureApi: FixtureApiSports,
        homeTeam: ApiSportsMatchTeam?,
        awayTeam: ApiSportsMatchTeam?,
        allMatchPlayers: Map<String, ApiSportsMatchPlayer>,
    ): List<ApiSportsMatchEvent> =
        unmatchedDtos.map { dto ->
            val matchTeam = findMatchTeam(dto.teamApiId, homeTeam, awayTeam)
            val player = findMatchPlayer(dto.playerMpKey, allMatchPlayers)
            val assist = findMatchPlayer(dto.assistMpKey, allMatchPlayers)

            ApiSportsMatchEvent(
                fixtureApi = fixtureApi,
                matchTeam = matchTeam,
                player = player,
                assist = assist,
                sequence = dto.sequence,
                elapsedTime = dto.elapsedTime,
                extraTime = dto.extraTime,
                eventType = dto.eventType,
                detail = dto.detail,
                comments = dto.comments,
            ).also {
                log.debug("Planned event creation: sequence=${dto.sequence}, type=${dto.eventType}")
            }
        }

    /**
     * 매칭된 쌍에서 변경사항이 있는 엔티티만 업데이트 목록에 추가합니다.
     *
     * **처리 과정:**
     * 1. 매칭된 DTO-Entity 쌍에서 필드 변경사항 확인
     * 2. 변경사항이 있는 경우에만 업데이트 목록에 추가
     * 3. 변경사항이 없는 경우 null 반환하여 제외
     *
     * **변경사항 확인 항목:**
     * - elapsedTime, extraTime, eventType, detail, comments
     * - player, assist (MatchPlayerKey 기반)
     *
     * @param matched 매칭된 DTO-Entity 쌍 맵
     * @param allMatchPlayers 모든 MatchPlayer 엔티티들 (key: MatchPlayerKey, value: ApiSportsMatchPlayer)
     * @return 업데이트할 Event 엔티티 목록
     */
    private fun buildUpdatedEntities(
        matched: Map<Int, MatchedPair>,
        allMatchPlayers: Map<String, ApiSportsMatchPlayer>,
    ): List<ApiSportsMatchEvent> =
        matched.values.mapNotNull { pair ->
            if (hasFieldChanges(pair.entity, pair.dto, allMatchPlayers)) {
                // 직접 필드 업데이트 - 변경사항이 있는 경우에만 업데이트
                pair.entity
                    .apply {
                        elapsedTime = pair.dto.elapsedTime
                        extraTime = pair.dto.extraTime
                        eventType = pair.dto.eventType
                        detail = pair.dto.detail
                        comments = pair.dto.comments
                        player = findMatchPlayer(pair.dto.playerMpKey, allMatchPlayers)
                        assist = findMatchPlayer(pair.dto.assistMpKey, allMatchPlayers)
                    }.also {
                        log.debug("Planned event update: sequence=${pair.dto.sequence}, type=${pair.dto.eventType}")
                    }
            } else {
                log.debug("No changes detected: sequence=${pair.dto.sequence}")
                null
            }
        }

    /**
     * 엔티티와 DTO 간 필드 변경사항 확인
     *
     * **확인 항목:**
     * - elapsedTime, extraTime, eventType, detail, comments
     * - player, assist (MatchPlayerKey 기반으로 찾은 MatchPlayer)
     *
     * **성능 최적화:**
     * - MatchPlayerKey 기반 O(1) 조회로 성능 향상
     *
     * @param entity 기존 Event 엔티티
     * @param dto 새로운 Event DTO
     * @param allMatchPlayers 모든 MatchPlayer 엔티티들 (key: MatchPlayerKey, value: ApiSportsMatchPlayer)
     * @return 변경사항이 있으면 true, 없으면 false
     */
    private fun hasFieldChanges(
        entity: ApiSportsMatchEvent,
        dto: MatchEventDto,
        allMatchPlayers: Map<String, ApiSportsMatchPlayer>,
    ): Boolean {
        val currentPlayer = findMatchPlayer(dto.playerMpKey, allMatchPlayers)
        val currentAssist = findMatchPlayer(dto.assistMpKey, allMatchPlayers)

        return entity.elapsedTime != dto.elapsedTime ||
            entity.extraTime != dto.extraTime ||
            entity.eventType != dto.eventType ||
            entity.detail != dto.detail ||
            entity.comments != dto.comments ||
            entity.player != currentPlayer ||
            entity.assist != currentAssist
    }

    /**
     * teamApiId로 MatchTeam을 찾습니다.
     *
     * **매칭 로직:**
     * - homeTeam의 teamApiSports.apiId와 일치하면 homeTeam 반환
     * - awayTeam의 teamApiSports.apiId와 일치하면 awayTeam 반환
     * - 일치하는 팀이 없으면 null 반환
     *
     * @param teamApiId 찾을 팀의 API ID
     * @param homeTeam 홈팀 MatchTeam
     * @param awayTeam 원정팀 MatchTeam
     * @return 매칭되는 MatchTeam 또는 null
     */
    private fun findMatchTeam(
        teamApiId: Long?,
        homeTeam: ApiSportsMatchTeam?,
        awayTeam: ApiSportsMatchTeam?,
    ): ApiSportsMatchTeam? =
        when {
            teamApiId == homeTeam?.teamApiSports?.apiId -> homeTeam
            teamApiId == awayTeam?.teamApiSports?.apiId -> awayTeam
            else -> null
        }

    /**
     * playerMpKey로 MatchPlayer를 찾습니다.
     *
     * **성능 최적화:**
     * - 엔티티 번들에서 효율적으로 찾기 위해 키 기반 검색을 사용
     * - O(1) 시간 복잡도로 빠른 조회
     *
     * @param playerMpKey 찾을 선수의 MatchPlayerKey
     * @param allMatchPlayers 모든 MatchPlayer 엔티티들 (key: MatchPlayerKey, value: ApiSportsMatchPlayer)
     * @return 매칭되는 MatchPlayer 또는 null
     */
    private fun findMatchPlayer(
        playerMpKey: String?,
        allMatchPlayers: Map<String, ApiSportsMatchPlayer>,
    ): ApiSportsMatchPlayer? {
        if (playerMpKey == null) return null

        return allMatchPlayers[playerMpKey]
    }

    /**
     * 매칭된 DTO-엔티티 쌍
     *
     * 변경사항 확인을 위해 DTO와 Entity를 함께 관리합니다.
     *
     * @property dto 새로운 Event DTO
     * @property entity 기존 Event 엔티티
     */
    private data class MatchedPair(
        val dto: MatchEventDto,
        val entity: ApiSportsMatchEvent,
    )
}
