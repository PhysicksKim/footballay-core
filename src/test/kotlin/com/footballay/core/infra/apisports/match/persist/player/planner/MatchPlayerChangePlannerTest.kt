package com.footballay.core.infra.apisports.match.persist.player.planner

import com.footballay.core.infra.apisports.match.plan.dto.MatchPlayerDto
import com.footballay.core.infra.persistence.apisports.entity.PlayerApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.infra.util.UidGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@DisplayName("MatchPlayerChangePlanner 테스트")
class MatchPlayerChangePlannerTest {
    @Test
    @DisplayName("MatchPlayer 엔티티들을 apiId 또는 name 기반 키로 올바르게 맵핑하여 키-엔티티 Map을 생성합니다")
    fun `정상적인 엔티티들이 올바르게 맵핑된다`() {
        // given
        val entities =
            listOf(
                createMockMatchPlayerEntity(apiId = 123L, name = "Player A"),
                createMockMatchPlayerEntity(apiId = null, name = "Player B"), // ID null
                createMockMatchPlayerEntity(apiId = 456L, name = "Player C"),
            )

        // when
        val result = MatchPlayerChangePlanner.entitiesToKeyMap(entities)

        // then
        assertThat(result).hasSize(3)
        assertThat(result).containsKeys("mp_id_123", "mp_name_Player B", "mp_id_456")
    }

    @Test
    @DisplayName("name이 빈 문자열이거나 공백인 엔티티들은 필터링되어 맵핑에서 제외됩니다")
    fun `name이 없는 비정상 엔티티는 제외된다`() {
        // given
        val entities =
            listOf(
                createMockMatchPlayerEntity(apiId = 123L, name = "Valid Player"),
                createMockMatchPlayerEntity(apiId = 789L, name = ""), // name empty
                createMockMatchPlayerEntity(apiId = 999L, name = "  "), // name blank
            )

        // when
        val result = MatchPlayerChangePlanner.entitiesToKeyMap(entities)

        // then
        assertThat(result).hasSize(1)
        assertThat(result).containsKeys("mp_id_123")
    }

    @Test
    @DisplayName("기존 엔티티와 매칭되지 않는 새로운 DTO들은 모두 생성 대상으로 분류되고 새로운 UID가 할당됩니다")
    fun `unmatchedDtos가 모두 생성 대상으로 분류된다`() {
        // given
        val mockUidGenerator = createMockUidGenerator()

        val dtoPlayers =
            mapOf(
                "mp_id_123" to createMockPlayerDto(apiId = 123L, name = "New Player 1"),
                "mp_name_NewPlayer2" to createMockPlayerDto(apiId = null, name = "New Player 2"),
            )
        val entityPlayers = emptyMap<String, ApiSportsMatchPlayer>()

        // when
        val changeSet = MatchPlayerChangePlanner.planChanges(dtoPlayers, entityPlayers, mockUidGenerator, null, null)

        // then
        assertThat(changeSet.createCount).isEqualTo(2)
        assertThat(changeSet.retainedCount).isEqualTo(0)
        assertThat(changeSet.deleteCount).isEqualTo(0)

        val createEntities = changeSet.toCreate
        assertThat(createEntities).hasSize(2)
        assertThat(createEntities.map { it.name }).containsExactlyInAnyOrder("New Player 1", "New Player 2")
        assertThat(createEntities.all { it.matchPlayerUid.startsWith("test-uid-") }).isTrue()
    }

    @Test
    @DisplayName("기존 엔티티와 매칭되는 DTO들 중에서 실제 변경사항이 있는 엔티티만 업데이트 대상으로 분류됩니다")
    fun `변경사항이 있는 matched 엔티티만 업데이트 대상에 포함된다`() {
        // given
        val mockUidGenerator = createMockUidGenerator()

        // 변경사항이 있는 Pair
        val changedEntity = createMockMatchPlayerEntity(apiId = 123L, name = "Old Name", position = "Defender")
        val changedDto = createMockPlayerDto(apiId = 123L, name = "New Name", position = "Forward") // 이름과 포지션 변경

        // 변경사항이 없는 Pair
        val unchangedEntity = createMockMatchPlayerEntity(apiId = 456L, name = "Same Player", position = "Midfielder")
        val unchangedDto = createMockPlayerDto(apiId = 456L, name = "Same Player", position = "Midfielder") // 동일

        val dtoPlayers =
            mapOf(
                "mp_id_123" to changedDto,
                "mp_id_456" to unchangedDto,
            )
        val entityPlayers =
            mapOf(
                "mp_id_123" to changedEntity,
                "mp_id_456" to unchangedEntity,
            )

        // when
        val changeSet = MatchPlayerChangePlanner.planChanges(dtoPlayers, entityPlayers, mockUidGenerator, null, null)

        // then
        assertThat(changeSet.createCount).isEqualTo(0)
        assertThat(changeSet.retainedCount).isEqualTo(2) // 변경사항이 있는 1개만
        assertThat(changeSet.deleteCount).isEqualTo(0)

        val updateEntity = changeSet.toRetain.find { it.playerApiSports?.apiId == 123L }!!
        assertThat(updateEntity.name).isEqualTo("New Name")
        assertThat(updateEntity.position).isEqualTo("Forward")
    }

    @Test
    @DisplayName("새로운 DTO에 매칭되지 않는 기존 엔티티들은 모두 삭제 대상으로 분류됩니다")
    fun `orphanedEntities가 모두 삭제 대상으로 분류된다`() {
        // given
        val mockUidGenerator = createMockUidGenerator()

        val dtoPlayers = emptyMap<String, MatchPlayerDto>()
        val entityPlayers =
            mapOf(
                "mp_id_789" to createMockMatchPlayerEntity(apiId = 789L, name = "Orphaned Player 1"),
                "mp_id_999" to createMockMatchPlayerEntity(apiId = 999L, name = "Orphaned Player 2"),
            )

        // when
        val changeSet = MatchPlayerChangePlanner.planChanges(dtoPlayers, entityPlayers, mockUidGenerator, null, null)

        // then
        assertThat(changeSet.createCount).isEqualTo(0)
        assertThat(changeSet.retainedCount).isEqualTo(0)
        assertThat(changeSet.deleteCount).isEqualTo(2)

        val deleteEntities = changeSet.toDelete
        assertThat(deleteEntities).hasSize(2)
        assertThat(deleteEntities.map { it.name }).contains("Orphaned Player 1", "Orphaned Player 2")
    }

    @Test
    @DisplayName("복합 시나리오에서 생성, 업데이트, 삭제 작업이 모두 올바르게 분류되고 계획됩니다")
    fun `모든 타입의 변경사항이 올바르게 분류된다`() {
        // given
        val mockUidGenerator = createMockUidGenerator()

        val dtoPlayers =
            mapOf(
                "mp_id_100" to createMockPlayerDto(apiId = 100L, name = "Updated Player", number = 10), // 업데이트
                "mp_id_200" to createMockPlayerDto(apiId = 200L, name = "Same Player"), // 변경사항 없음
                "mp_id_300" to createMockPlayerDto(apiId = 300L, name = "New Player"), // 새로운 선수
            )

        val entityPlayers =
            mapOf(
                "mp_id_100" to createMockMatchPlayerEntity(apiId = 100L, name = "Old Player", number = 9), // 변경사항 있음
                "mp_id_200" to createMockMatchPlayerEntity(apiId = 200L, name = "Same Player"), // 변경사항 없음
                "mp_id_400" to createMockMatchPlayerEntity(apiId = 400L, name = "Deleted Player"), // orphaned
            )

        // when
        val changeSet = MatchPlayerChangePlanner.planChanges(dtoPlayers, entityPlayers, mockUidGenerator, null, null)

        // then
        assertThat(changeSet.createCount).isEqualTo(1) // unmatchedDto 1개
        assertThat(changeSet.retainedCount).isEqualTo(2) // 업데이트1개 + 변경사항1개 = 2개
        assertThat(changeSet.deleteCount).isEqualTo(1) // orphaned 1개
        assertThat(changeSet.totalCount).isEqualTo(4)

        assertThat(changeSet.toCreate.first().name).isEqualTo("New Player")
        assertThat(changeSet.toRetain).hasSize(2)
        assertThat(changeSet.toRetain.map { it.name }).contains("Updated Player", "Same Player")
        assertThat(changeSet.toRetain.find { it.number == 10 }?.name)
            .isEqualTo("Updated Player") // 업데이트된 선수 확인
        assertThat(changeSet.toDelete.first().name).isEqualTo("Deleted Player")
    }

    @Test
    @DisplayName("matchPlayerUid가 null인 DTO에 대해서는 새로운 UID를 생성하고, 기존 UID가 있는 DTO는 기존 UID를 유지합니다")
    fun `DTO에 matchPlayerUid가 없으면 새로운 UID가 생성된다`() {
        // given
        val mockUidGenerator = createMockUidGenerator()

        val dtoWithoutUid = createMockPlayerDto(apiId = 123L, name = "Player Without UID").copy(matchPlayerUid = null)
        val dtoWithUid =
            createMockPlayerDto(
                apiId = 456L,
                name = "Player With UID",
            ).copy(matchPlayerUid = "existing-uid-456")

        val dtoPlayers =
            mapOf(
                "mp_id_123" to dtoWithoutUid,
                "mp_id_456" to dtoWithUid,
            )
        val entityPlayers = emptyMap<String, ApiSportsMatchPlayer>()

        // when
        val changeSet = MatchPlayerChangePlanner.planChanges(dtoPlayers, entityPlayers, mockUidGenerator, null, null)

        // then
        val createdEntities = changeSet.toCreate
        assertThat(createdEntities).hasSize(2)

        // UID가 없었던 엔티티는 새로운 UID를 받아야 함
        val entityWithoutOriginalUid = createdEntities.find { it.name == "Player Without UID" }!!
        assertThat(entityWithoutOriginalUid.matchPlayerUid).startsWith("test-uid-")

        // UID가 있었던 엔티티는 기존 UID를 유지해야 함
        val entityWithOriginalUid = createdEntities.find { it.name == "Player With UID" }!!
        assertThat(entityWithOriginalUid.matchPlayerUid).isEqualTo("existing-uid-456")
    }

    @Test
    @DisplayName("빈 DTO Map과 빈 엔티티 Map이 주어졌을 때 빈 변경 계획을 반환하고 아무 작업도 수행하지 않습니다")
    fun `빈 입력에 대해 빈 변경사항을 반환한다`() {
        // given
        val mockUidGenerator = createMockUidGenerator()
        val emptyDtos = emptyMap<String, MatchPlayerDto>()
        val emptyEntities = emptyMap<String, ApiSportsMatchPlayer>()

        // when
        val changeSet = MatchPlayerChangePlanner.planChanges(emptyDtos, emptyEntities, mockUidGenerator, null, null)

        // then
        assertThat(changeSet.createCount).isEqualTo(0)
        assertThat(changeSet.retainedCount).isEqualTo(0)
        assertThat(changeSet.deleteCount).isEqualTo(0)
        assertThat(changeSet.totalCount).isEqualTo(0)
        assertThat(changeSet.hasRetained()).isFalse()
    }

    // 테스트 헬퍼 메서드들
    private fun createMockPlayerDto(
        apiId: Long? = 123L,
        name: String = "Test Player",
        number: Int? = null,
        position: String? = null,
        substitute: Boolean = false,
    ): MatchPlayerDto =
        MatchPlayerDto(
            matchPlayerUid = null,
            apiId = apiId,
            name = name,
            number = number,
            position = position,
            substitute = substitute,
            nonLineupPlayer = false,
            teamApiId = 1L,
            playerApiSportsInfo = null,
        )

    private fun createMockMatchPlayerEntity(
        apiId: Long? = 123L,
        name: String = "Test Player",
        position: String = "Forward",
        number: Int? = null,
    ): ApiSportsMatchPlayer {
        val playerApiSports =
            if (apiId != null) {
                PlayerApiSports().apply { this.apiId = apiId }
            } else {
                null
            }

        return ApiSportsMatchPlayer(
            matchPlayerUid = "test-uid-123",
            playerApiSports = playerApiSports,
            name = name,
            number = number,
            position = position,
            substitute = false,
            matchTeam = null,
        )
    }

    private fun createMockUidGenerator(): UidGenerator {
        val mockGenerator = mock<UidGenerator>()
        var counter = 0
        whenever(mockGenerator.generateUid()).thenAnswer { "test-uid-${++counter}" }
        return mockGenerator
    }
}
