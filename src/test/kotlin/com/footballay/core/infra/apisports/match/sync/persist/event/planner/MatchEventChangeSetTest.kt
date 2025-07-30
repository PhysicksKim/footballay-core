package com.footballay.core.infra.apisports.match.sync.persist.event.planner

import com.footballay.core.infra.apisports.match.sync.persist.event.planner.MatchEventChangeSet
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * MatchEventChangeSet 테스트
 * 
 * MatchEventChangeSet의 편의 메서드들과 기능들을 테스트합니다.
 * 
 * **테스트 대상:**
 * - 편의 메서드들 (totalCount, createCount, updateCount, deleteCount)
 * - hasChanges() 메서드
 * - empty() 팩토리 메서드
 */
class MatchEventChangeSetTest {

    @Test
    @DisplayName("편의 메서드들이 올바른 개수를 반환합니다")
    fun `convenience_methods_should_return_correct_counts`() {
        // given
        val toCreate = listOf(createMockEvent(0), createMockEvent(1))
        val toUpdate = listOf(createMockEvent(2))
        val toDelete = listOf(createMockEvent(3), createMockEvent(4), createMockEvent(5))
        
        val changeSet = MatchEventChangeSet(toCreate, toUpdate, toDelete)
        
        // when & then
        assertThat(changeSet.totalCount).isEqualTo(6) // 2 + 1 + 3
        assertThat(changeSet.createCount).isEqualTo(2)
        assertThat(changeSet.updateCount).isEqualTo(1)
        assertThat(changeSet.deleteCount).isEqualTo(3)
    }

    @Test
    @DisplayName("빈 ChangeSet의 경우 모든 개수가 0입니다")
    fun `empty_changeset_should_have_zero_counts`() {
        // given
        val changeSet = MatchEventChangeSet.empty()
        
        // when & then
        assertThat(changeSet.totalCount).isEqualTo(0)
        assertThat(changeSet.createCount).isEqualTo(0)
        assertThat(changeSet.updateCount).isEqualTo(0)
        assertThat(changeSet.deleteCount).isEqualTo(0)
    }

    @Test
    @DisplayName("hasChanges() - 변경사항이 있으면 true를 반환합니다")
    fun `hasChanges_should_return_true_when_there_are_changes`() {
        // given
        val changeSet = MatchEventChangeSet(
            toCreate = listOf(createMockEvent(0)),
            toUpdate = emptyList(),
            toDelete = emptyList()
        )
        
        // when & then
        assertThat(changeSet.hasChanges()).isTrue()
    }

    @Test
    @DisplayName("hasChanges() - 변경사항이 없으면 false를 반환합니다")
    fun `hasChanges_should_return_false_when_there_are_no_changes`() {
        // given
        val changeSet = MatchEventChangeSet.empty()
        
        // when & then
        assertThat(changeSet.hasChanges()).isFalse()
    }

    @Test
    @DisplayName("empty() 팩토리 메서드가 빈 ChangeSet을 생성합니다")
    fun `empty_factory_method_should_create_empty_changeset`() {
        // when
        val changeSet = MatchEventChangeSet.empty()
        
        // then
        assertThat(changeSet.toCreate).isEmpty()
        assertThat(changeSet.toUpdate).isEmpty()
        assertThat(changeSet.toDelete).isEmpty()
        assertThat(changeSet.hasChanges()).isFalse()
    }

    @Test
    @DisplayName("부분적으로만 변경사항이 있는 경우에도 hasChanges()가 true를 반환합니다")
    fun `hasChanges_should_return_true_for_partial_changes`() {
        // given - 생성만 있는 경우
        val createOnly = MatchEventChangeSet(
            toCreate = listOf(createMockEvent(0)),
            toUpdate = emptyList(),
            toDelete = emptyList()
        )
        
        // given - 수정만 있는 경우
        val updateOnly = MatchEventChangeSet(
            toCreate = emptyList(),
            toUpdate = listOf(createMockEvent(1)),
            toDelete = emptyList()
        )
        
        // given - 삭제만 있는 경우
        val deleteOnly = MatchEventChangeSet(
            toCreate = emptyList(),
            toUpdate = emptyList(),
            toDelete = listOf(createMockEvent(2))
        )
        
        // when & then
        assertThat(createOnly.hasChanges()).isTrue()
        assertThat(updateOnly.hasChanges()).isTrue()
        assertThat(deleteOnly.hasChanges()).isTrue()
    }

    @Test
    @DisplayName("데이터 클래스의 구조가 올바르게 동작합니다")
    fun `data_class_should_work_correctly`() {
        // given
        val toCreate = listOf(createMockEvent(0))
        val toUpdate = listOf(createMockEvent(1))
        val toDelete = listOf(createMockEvent(2))
        
        val changeSet1 = MatchEventChangeSet(toCreate, toUpdate, toDelete)
        val changeSet2 = MatchEventChangeSet(toCreate, toUpdate, toDelete)
        
        // when & then
        assertThat(changeSet1).isEqualTo(changeSet2)
        assertThat(changeSet1.hashCode()).isEqualTo(changeSet2.hashCode())
        
        // 구조 분해 할당 테스트
        val (create, update, delete) = changeSet1
        assertThat(create).isEqualTo(toCreate)
        assertThat(update).isEqualTo(toUpdate)
        assertThat(delete).isEqualTo(toDelete)
    }

    // Helper methods
    private fun createMockEvent(sequence: Int): ApiSportsMatchEvent {
        return ApiSportsMatchEvent(
            fixtureApi = mockk(relaxed = true),
            matchTeam = mockk(relaxed = true),
            player = null,
            assist = null,
            sequence = sequence,
            elapsedTime = 45,
            extraTime = null,
            eventType = "Goal",
            detail = "Test goal",
            comments = "Test comment"
        )
    }
} 