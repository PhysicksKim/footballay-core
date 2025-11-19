package com.footballay.core.infra.apisports.match.persist

import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.plan.dto.MatchPlayerDto
import com.footballay.core.infra.apisports.match.persist.player.collector.MatchPlayerDtoCollector
import com.footballay.core.infra.apisports.match.persist.player.planner.MatchPlayerChangePlanner
import com.footballay.core.infra.persistence.apisports.entity.PlayerApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.infra.util.UidGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@DisplayName("MatchPlayer 통합 테스트")
class MatchPlayerIntegrationTest {
    @Test
    @DisplayName("실제 라이브 매치 시나리오에서 라인업, 이벤트, 통계 데이터를 우선순위에 따라 수집하고 모든 변경 타입을 처리합니다")
    fun `전체 MatchPlayer 처리 파이프라인이 올바르게 작동한다`() {
        // given: 복잡한 라이브 매치 상황 설정
        val context = MatchPlayerContext()
        val mockUidGenerator = createMockUidGenerator()

        // 라인업: 홈팀 2명 + 어웨이팀 2명
        context.lineupMpDtoMap["mp_id_101"] = createMockPlayerDto(apiId = 101L, name = "Home Player 1", number = 10)
        context.lineupMpDtoMap["mp_id_102"] = createMockPlayerDto(apiId = 102L, name = "Home Player 2", number = 11)
        context.lineupMpDtoMap["mp_id_201"] = createMockPlayerDto(apiId = 201L, name = "Away Player 1", number = 21)
        context.lineupMpDtoMap["mp_id_202"] = createMockPlayerDto(apiId = 202L, name = "Away Player 2", number = 22)

        // 이벤트: 교체 선수 + 심판 (라인업 중복 없음)
        context.eventMpDtoMap["mp_id_103"] = createMockPlayerDto(apiId = 103L, name = "Sub Player") // 새로운 교체 선수
        context.eventMpDtoMap["mp_name_Referee"] = createMockPlayerDto(apiId = null, name = "Referee") // ID 없는 심판

        // 통계: 기존 라인업 선수 중복 + 코치
        context.statMpDtoMap["mp_id_101"] = createMockPlayerDto(apiId = 101L, name = "Home Player 1") // 중복
        context.statMpDtoMap["mp_name_Coach"] = createMockPlayerDto(apiId = null, name = "Coach") // ID 없는 코치

        // 기존 저장된 엔티티들 (일부는 업데이트, 일부는 orphaned)
        val existingEntities =
            listOf(
                createMockMatchPlayerEntity(apiId = 101L, name = "Home Player 1", number = 9), // 업데이트될 것 (번호 변경)
                createMockMatchPlayerEntity(apiId = 999L, name = "Old Removed Player"), // orphaned
            )

        // when: 통합된 파이프라인 실행
        val collectedDtos = MatchPlayerDtoCollector.collectFrom(context) // 1단계: 우선순위 수집
        val entityKeyMap = MatchPlayerChangePlanner.entitiesToKeyMap(existingEntities) // 2단계: 엔티티 키 맵핑
        // 3단계: 변경 계획
        val changeSet = MatchPlayerChangePlanner.planChanges(collectedDtos, entityKeyMap, mockUidGenerator, null, null)

        // then: 각 단계별 결과 검증

        // 1단계 결과: 우선순위 수집 검증
        assertThat(collectedDtos).hasSize(7) // 라인업 4명 + 이벤트 2명(중복제외) + 통계 1명(중복제외)
        assertThat(collectedDtos.values.count { !it.nonLineupPlayer }).isEqualTo(4) // 라인업 선수들
        assertThat(collectedDtos.values.count { it.nonLineupPlayer }).isEqualTo(3) // non-lineup 선수들
        assertThat(collectedDtos["mp_id_101"]?.name).isEqualTo("Home Player 1") // lineup 우선순위 확인
        assertThat(collectedDtos["mp_name_Referee"]?.nonLineupPlayer).isTrue() // event 전용 선수 마킹
        assertThat(collectedDtos["mp_name_Coach"]?.nonLineupPlayer).isTrue() // stat 전용 선수 마킹

        // 2단계 결과: 엔티티 키 맵핑 검증
        assertThat(entityKeyMap).hasSize(2) // 기존 엔티티 2개

        // 3단계 결과: 변경 계획 검증
        assertThat(changeSet.createCount).isEqualTo(6) // 새로운 선수 6명
        assertThat(changeSet.updateCount).isEqualTo(1) // 번호 변경된 선수 1명
        assertThat(changeSet.deleteCount).isEqualTo(1) // 고아 선수 1명
        assertThat(changeSet.totalCount).isEqualTo(8)

        // 생성될 선수들 확인
        val createdNames = changeSet.toCreate.map { it.name }
        assertThat(createdNames).containsExactlyInAnyOrder(
            "Home Player 2",
            "Away Player 1",
            "Away Player 2",
            "Sub Player",
            "Referee",
            "Coach",
        )

        // 업데이트될 선수 확인
        val updatedPlayer = changeSet.toUpdate.first()
        assertThat(updatedPlayer.name).isEqualTo("Home Player 1")
        assertThat(updatedPlayer.number).isEqualTo(10) // 새로운 번호로 업데이트

        // 삭제될 선수 확인
        val deletedPlayer = changeSet.toDelete.first()
        assertThat(deletedPlayer.name).isEqualTo("Old Removed Player")

        // UID 생성 확인
        assertThat(changeSet.toCreate.all { it.matchPlayerUid.startsWith("test-uid-") }).isTrue()
    }

    @Test
    @DisplayName("단순한 시나리오에서 생성, 업데이트, 삭제 작업이 모두 올바르게 계획되고 처리됩니다")
    fun `단순한 시나리오에서 생성_업데이트_삭제가 모두 올바르게 처리된다`() {
        // given
        val context = MatchPlayerContext()
        val mockUidGenerator = createMockUidGenerator()

        // 라인업: 1명 (기존 엔티티와 매칭될 예정)
        context.lineupMpDtoMap["mp_id_123"] =
            createMockPlayerDto(
                apiId = 123L,
                name = "Updated Player",
                number = 10,
                position = "Forward",
            )

        // 이벤트: 1명 (새로운 선수)
        context.eventMpDtoMap["mp_id_456"] =
            createMockPlayerDto(
                apiId = 456L,
                name = "New Player",
            )

        // 기존 엔티티: 1명 매칭 + 1명 고아
        val existingEntities =
            listOf(
                createMockMatchPlayerEntity(apiId = 123L, name = "Old Player", number = 9, position = "Defender"),
                createMockMatchPlayerEntity(apiId = 789L, name = "Orphaned Player"), // 삭제
            )

        // when
        val collectedDtos = MatchPlayerDtoCollector.collectFrom(context)
        val entityKeyMap = MatchPlayerChangePlanner.entitiesToKeyMap(existingEntities)
        val changeSet = MatchPlayerChangePlanner.planChanges(collectedDtos, entityKeyMap, mockUidGenerator, null, null)

        // then
        assertThat(changeSet.createCount).isEqualTo(1) // New Player
        assertThat(changeSet.updateCount).isEqualTo(1) // Updated Player (name, number, position 변경)
        assertThat(changeSet.deleteCount).isEqualTo(1) // Orphaned Player

        // 업데이트 내용 상세 확인
        val updatedPlayer = changeSet.toUpdate.first()
        assertThat(updatedPlayer.name).isEqualTo("Updated Player")
        assertThat(updatedPlayer.number).isEqualTo(10)
        assertThat(updatedPlayer.position).isEqualTo("Forward")
    }

    @Test
    @DisplayName("기존 엔티티와 새로운 DTO가 동일한 정보를 가지고 있을 때 변경 계획이 생성되지 않습니다")
    fun `변경사항이 없는 경우 빈 변경 계획이 반환된다`() {
        // given: 변경사항이 없는 상황
        val context = MatchPlayerContext()
        val mockUidGenerator = createMockUidGenerator()

        context.lineupMpDtoMap["mp_id_123"] =
            createMockPlayerDto(
                apiId = 123L,
                name = "Same Player",
                number = 10,
                position = "Forward",
            )

        val existingEntities =
            listOf(
                createMockMatchPlayerEntity(apiId = 123L, name = "Same Player", number = 10, position = "Forward"),
            )

        // when
        val collectedDtos = MatchPlayerDtoCollector.collectFrom(context)
        val entityKeyMap = MatchPlayerChangePlanner.entitiesToKeyMap(existingEntities)
        val changeSet = MatchPlayerChangePlanner.planChanges(collectedDtos, entityKeyMap, mockUidGenerator, null, null)

        // then: 변경사항 없음
        assertThat(changeSet.createCount).isEqualTo(0)
        assertThat(changeSet.updateCount).isEqualTo(0) // 변경사항 없어서 업데이트 안됨
        assertThat(changeSet.deleteCount).isEqualTo(0)
        assertThat(changeSet.hasChanges()).isFalse()
    }

    @Test
    @DisplayName("빈 Context와 빈 엔티티 리스트가 주어졌을 때 빈 변경 계획을 반환하고 아무 작업도 수행하지 않습니다")
    fun `빈 입력에 대해 빈 변경 계획이 반환된다`() {
        // given
        val emptyContext = MatchPlayerContext()
        val emptyEntities = emptyList<ApiSportsMatchPlayer>()
        val mockUidGenerator = createMockUidGenerator()

        // when
        val collectedDtos = MatchPlayerDtoCollector.collectFrom(emptyContext)
        val entityKeyMap = MatchPlayerChangePlanner.entitiesToKeyMap(emptyEntities)
        val changeSet = MatchPlayerChangePlanner.planChanges(collectedDtos, entityKeyMap, mockUidGenerator, null, null)

        // then
        assertThat(collectedDtos).isEmpty()
        assertThat(entityKeyMap).isEmpty()
        assertThat(changeSet.createCount).isEqualTo(0)
        assertThat(changeSet.updateCount).isEqualTo(0)
        assertThat(changeSet.deleteCount).isEqualTo(0)
        assertThat(changeSet.hasChanges()).isFalse()
    }

    // 테스트 헬퍼 메서드들
    private fun createMockPlayerDto(
        apiId: Long? = 123L,
        name: String = "Test Player",
        number: Int? = null,
        position: String? = null,
        substitute: Boolean = false,
        nonLineupPlayer: Boolean = false,
    ): MatchPlayerDto =
        MatchPlayerDto(
            matchPlayerUid = null,
            apiId = apiId,
            name = name,
            number = number,
            position = position,
            substitute = substitute,
            nonLineupPlayer = nonLineupPlayer,
            teamApiId = 1L,
            playerApiSportsInfo = null,
        )

    private fun createMockMatchPlayerEntity(
        apiId: Long? = 123L,
        name: String = "Test Player",
        number: Int? = null,
        position: String = "Forward",
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
