package com.footballay.core.infra.apisports.match.sync.persist.player.planner

import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerKeyGenerator
import com.footballay.core.infra.apisports.match.sync.dto.MatchPlayerDto
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
                log.info("Converted ${resultMap.size} valid entities from ${entities.size} total entities")
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
        val toCreate =
            buildNewEntities(
                dtoPlayers.filter { !entityPlayers.containsKey(it.key) }.values,
                uidGenerator,
                homeTeam,
                awayTeam,
            )

        val toUpdate =
            buildUpdatedEntities(
                dtoPlayers
                    .filter { entityPlayers.containsKey(it.key) }
                    .mapValues { (key, dto) ->
                        MatchedPair(dto, entityPlayers[key]!!)
                    },
            )

        val toDelete = entityPlayers.filter { !dtoPlayers.containsKey(it.key) }.values.toList()

        val changeSet =
            MatchPlayerChangeSet(
                toCreate = toCreate,
                toUpdate = toUpdate,
                toDelete = toDelete,
            )

        log.info(
            "Change planning completed - Create: ${changeSet.createCount}, Update: ${changeSet.updateCount}, Delete: ${changeSet.deleteCount}",
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
                log.debug("Planned creation: ${dto.name}")
            }
        }

    /**
     * 매칭된 쌍에서 변경사항이 있는 엔티티만 업데이트 목록에 추가합니다.
     */
    private fun buildUpdatedEntities(matched: Map<String, MatchedPair>): List<ApiSportsMatchPlayer> =
        matched.values.mapNotNull { pair ->
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
                        log.debug("Planned update: ${pair.dto.name}")
                    }
            } else {
                log.debug("No changes detected: ${pair.dto.name}")
                null
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
