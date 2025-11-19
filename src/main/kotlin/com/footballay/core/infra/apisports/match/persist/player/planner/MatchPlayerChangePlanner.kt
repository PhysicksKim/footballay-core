package com.footballay.core.infra.apisports.match.persist.player.planner

import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerKeyGenerator
import com.footballay.core.infra.apisports.match.plan.dto.MatchPlayerDto
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import com.footballay.core.infra.util.UidGenerator
import com.footballay.core.logger

/**
 * MatchPlayer 변경 작업 계획기
 *
 * DTO와 기존 엔티티를 비교하여 데이터베이스 변경 작업을 계획합니다.
 *
 * **처리 로직:**
 * 1. DTO와 엔티티를 키로 매칭하여 분류
 * 2. 새로운 DTO → 엔티티 생성 계획
 * 3. 매칭된 DTO-엔티티 → 변경사항 확인 후 업데이트 계획
 * 4. 고아 엔티티 → 삭제 계획
 *
 * **사용 예시:**
 * ```kotlin
 * val dtoMap = mapOf("mp_id_123" to playerDto)
 * val entityMap = MatchPlayerChangePlanner.entitiesToKeyMap(existingEntities)
 * val changeSet = MatchPlayerChangePlanner.planChanges(dtoMap, entityMap, uidGenerator)
 * ```
 */
object MatchPlayerChangePlanner {
    private val log = logger()

    /**
     * 기존 엔티티들을 MatchPlayerKey 기반 맵으로 변환합니다.
     *
     * @param entities DB에서 조회된 기존 MatchPlayer 엔티티들
     * @return MatchPlayerKey를 키로 하는 엔티티 맵
     */
    fun entitiesToKeyMap(entities: List<ApiSportsMatchPlayer>): Map<String, ApiSportsMatchPlayer> {
        val createMpKey = MatchPlayerKeyGenerator::generateMatchPlayerKey

        return entities
            .filter { it.name.isNotBlank() } // 비정상 엔티티 제외
            .associateBy { entity ->
                createMpKey(entity.playerApiSports?.apiId, entity.name!!)
            }.also { resultMap ->
                log.info("Converted {} valid entities from {} total entities", resultMap.size, entities.size)
            }
    }

    /**
     * DTO와 엔티티를 분석하여 변경 작업을 계획합니다.
     *
     * @param dtoPlayers 새로운 DTO 선수들 (key: MatchPlayerKey)
     * @param entityPlayers 기존 저장된 엔티티 선수들 (key: MatchPlayerKey)
     * @param uidGenerator 새로운 엔티티 UID 생성기
     * @param homeTeam 홈팀 MatchTeam (연결용)
     * @param awayTeam 원정팀 MatchTeam (연결용)
     * @return 데이터베이스 변경 작업 명세서
     */
    fun planChanges(
        dtoPlayers: Map<String, MatchPlayerDto>,
        entityPlayers: Map<String, ApiSportsMatchPlayer>,
        uidGenerator: UidGenerator,
        homeTeam: ApiSportsMatchTeam?,
        awayTeam: ApiSportsMatchTeam?,
    ): MatchPlayerChangeSet {
        log.info(
            "Starting change planning - DTOs: {}, Entities: {}",
            dtoPlayers.size,
            entityPlayers.size,
        )

        // Step 1: 키 기반 정확 매칭
        val exactMatches = mutableMapOf<String, MatchedPair>()
        val unmatchedDtos = dtoPlayers.toMutableMap()
        val unmatchedEntities = entityPlayers.toMutableMap()

        dtoPlayers.forEach { (dtoKey, dto) ->
            if (entityPlayers.containsKey(dtoKey)) {
                exactMatches[dtoKey] = MatchedPair(dto, entityPlayers[dtoKey]!!)
                unmatchedDtos.remove(dtoKey)
                unmatchedEntities.remove(dtoKey)
                log.debug("Exact match found: key={}, name={}", dtoKey, dto.name)
            }
        }

        log.info("Exact matches: {}", exactMatches.size)

        // Step 2: Name 기반 Fallback 매칭 (apiId 불일치 시)
        val fallbackMatches = mutableMapOf<String, MatchedPair>()
        val nameToEntityMap = unmatchedEntities.values.associateBy { it.name!! }

        unmatchedDtos.entries.toList().forEach { (dtoKey, dto) ->
            val matchedEntity = nameToEntityMap[dto.name]
            if (matchedEntity != null) {
                fallbackMatches[dtoKey] = MatchedPair(dto, matchedEntity)
                unmatchedDtos.remove(dtoKey)
                unmatchedEntities.remove(
                    MatchPlayerKeyGenerator.generateMatchPlayerKey(
                        matchedEntity.playerApiSports?.apiId,
                        matchedEntity.name!!,
                    ),
                )
                log.warn(
                    "Fallback match by name: dto_key={}, entity_apiId={}, name={}",
                    dtoKey,
                    matchedEntity.playerApiSports?.apiId,
                    dto.name,
                )
            }
        }

        log.info("Fallback matches: {}", fallbackMatches.size)

        // Step 3: 변경 작업 계획
        val allMatches = exactMatches + fallbackMatches

        val toCreate =
            buildNewEntities(
                unmatchedDtos.values,
                uidGenerator,
                homeTeam,
                awayTeam,
            )

        val toRetain = buildRetainedEntities(allMatches)

        val toDelete = unmatchedEntities.values.toList()

        // Step 4: 안전장치 - 의심스러운 대량 삭제 감지
        val totalEntities = entityPlayers.size
        val deleteRatio = if (totalEntities > 0) toDelete.size.toDouble() / totalEntities else 0.0
        if (deleteRatio > 0.5 && totalEntities >= 10) {
            log.error(
                "⚠️ SUSPICIOUS MASS DELETION DETECTED! Deleting {}/{} players ({}%). " +
                    "This might indicate apiId mismatch or data corruption. " +
                    "Please verify before proceeding.",
                toDelete.size,
                totalEntities,
                String.format("%.1f", deleteRatio * 100),
            )
            log.error("Unmatched DTO keys: {}", unmatchedDtos.keys.take(10))
            log.error("Unmatched Entity keys: {}", unmatchedEntities.keys.take(10))
        }

        val changeSet =
            MatchPlayerChangeSet(
                toCreate = toCreate,
                toRetain = toRetain,
                toDelete = toDelete,
            )

        log.info(
            "Change planning completed - Create: {}, Retain: {}, Delete: {}",
            changeSet.createCount,
            changeSet.retainedCount,
            changeSet.deleteCount,
        )

        log.info(
            """
            DEBUG INFO:
            create : {}
            retain : {}
            delete : {}
            """.trimIndent(),
            toCreate.map { it.name },
            toRetain.map { it.name },
            toDelete.map { it.name },
        )

        return changeSet
    }

    /**
     * 새로운 DTO들로부터 엔티티를 생성합니다.
     */
    private fun buildNewEntities(
        unmatchedDtos: Collection<MatchPlayerDto>,
        uidGenerator: UidGenerator,
        homeTeam: ApiSportsMatchTeam?,
        awayTeam: ApiSportsMatchTeam?,
    ): List<ApiSportsMatchPlayer> =
        unmatchedDtos.map { dto ->
            val uid = dto.matchPlayerUid ?: uidGenerator.generateUid()

            // MatchTeam 연결 로직
            val matchTeam =
                when {
                    dto.teamApiId == homeTeam?.teamApiSports?.apiId -> homeTeam
                    dto.teamApiId == awayTeam?.teamApiSports?.apiId -> awayTeam
                    else -> null
                }

            ApiSportsMatchPlayer(
                matchPlayerUid = uid,
                playerApiSports = null, // TODO: PlayerApiSports 연결 로직 필요
                name = dto.name,
                number = dto.number,
                position = dto.position ?: "Unknown",
                grid = dto.grid,
                substitute = dto.substitute,
                matchTeam = matchTeam,
            ).also {
                log.debug("Planned creation: {}", dto.name)
            }
        }

    /**
     * 매칭된 쌍의 엔티티를 반환합니다.
     * 변경사항이 있으면 업데이트하고, 없으면 그대로 유지합니다.
     */
    private fun buildRetainedEntities(matched: Map<String, MatchedPair>): List<ApiSportsMatchPlayer> =
        matched.values.map { pair ->
            if (hasFieldChanges(pair.entity, pair.dto)) {
                // 직접 필드 업데이트
                pair.entity
                    .apply {
                        name = pair.dto.name
                        number = pair.dto.number
                        position = pair.dto.position ?: position
                        grid = pair.dto.grid
                        substitute = pair.dto.substitute
                    }.also {
                        log.debug("Planned update: {}", pair.dto.name)
                    }
            } else {
                // 변경사항 없어도 엔티티 유지
                log.debug("No changes detected, keeping entity: {}", pair.dto.name)
                pair.entity
            }
        }

    /**
     * 엔티티와 DTO 간 필드 변경사항 확인
     */
    private fun hasFieldChanges(
        entity: ApiSportsMatchPlayer,
        dto: MatchPlayerDto,
    ): Boolean =
        entity.name != dto.name ||
            entity.number != dto.number ||
            entity.position != (dto.position ?: entity.position) ||
            entity.grid != dto.grid ||
            entity.substitute != dto.substitute

    /**
     * 매칭된 DTO-엔티티 쌍
     */
    private data class MatchedPair(
        val dto: MatchPlayerDto,
        val entity: ApiSportsMatchPlayer,
    )
}
