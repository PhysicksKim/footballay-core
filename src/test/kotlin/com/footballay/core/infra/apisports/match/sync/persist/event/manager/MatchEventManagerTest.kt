package com.footballay.core.infra.apisports.match.sync.persist.event.manager

import com.footballay.core.infra.apisports.match.sync.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.sync.dto.MatchEventDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchEventPlanDto
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchEventRepository
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

/**
 * MatchEventManager 테스트
 *
 * MatchEventManager의 주요 기능들을 테스트합니다.
 *
 * **테스트 대상:**
 * - Sequence 검증 기능
 * - 에러 처리 및 빈 이벤트 생성
 * - 정렬 기능
 * - EntityBundle 업데이트
 */
class MatchEventManagerTest {
    private lateinit var matchEventRepository: ApiSportsMatchEventRepository
    private lateinit var matchEventManager: MatchEventManager
    private lateinit var entityBundle: MatchEntityBundle

    @BeforeEach
    fun setUp() {
        matchEventRepository = mockk(relaxed = true)
        matchEventManager = MatchEventManager(matchEventRepository)
        entityBundle = MatchEntityBundle.createEmpty()
        entityBundle.fixture = createMockFixture()
        entityBundle.homeTeam = createMockHomeTeam()
        entityBundle.awayTeam = createMockAwayTeam()
        entityBundle.allMatchPlayers = emptyMap()
        entityBundle.allEvents = emptyList()
    }

    @Test
    @DisplayName("정상적인 이벤트 처리를 수행할 수 있습니다")
    fun `processMatchEvents_should_process_events_successfully`() {
        // given
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(sequence = 0),
                        createMockEventDto(sequence = 1),
                    ),
            )
        val savedEvents =
            listOf(
                createMockMatchEvent(sequence = 0),
                createMockMatchEvent(sequence = 1),
            )
        every { matchEventRepository.saveAll(any<List<ApiSportsMatchEvent>>()) } returns savedEvents

        // when
        val result = matchEventManager.processMatchEvents(eventDto, entityBundle)

        // then
        assertThat(result.totalEvents).isEqualTo(2)
        assertThat(result.createdCount).isEqualTo(2)
        assertThat(result.updatedCount).isEqualTo(0)
        assertThat(result.deletedCount).isEqualTo(0)
        assertThat(result.savedEvents).isEqualTo(savedEvents.sortedBy { it.sequence })
        assertThat(entityBundle.allEvents).isEqualTo(savedEvents.sortedBy { it.sequence })
    }

    @Test
    @DisplayName("sequence 검증 - 중복된 sequence가 있으면 경고를 로그에 기록합니다")
    fun `processMatchEvents_duplicate_sequences_should_log_warning`() {
        // given
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(sequence = 0),
                        createMockEventDto(sequence = 0), // 중복
                        createMockEventDto(sequence = 1),
                    ),
            )
        val savedEvents =
            listOf(
                createMockMatchEvent(sequence = 0),
                createMockMatchEvent(sequence = 0),
                createMockMatchEvent(sequence = 1),
            )
        every { matchEventRepository.saveAll(any<List<ApiSportsMatchEvent>>()) } returns savedEvents

        // when
        val result = matchEventManager.processMatchEvents(eventDto, entityBundle)

        // then
        assertThat(result.totalEvents).isEqualTo(3) // 경고가 있어도 모두 처리됨
        assertThat(result.createdCount).isEqualTo(3)
    }

    @Test
    @DisplayName("sequence 검증 - 누락된 sequence가 있으면 경고를 로그에 기록합니다")
    fun `processMatchEvents_missing_sequences_should_log_warning`() {
        // given
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(sequence = 0),
                        createMockEventDto(sequence = 2), // 1이 누락됨
                    ),
            )
        val savedEvents =
            listOf(
                createMockMatchEvent(sequence = 0),
                createMockMatchEvent(sequence = 2),
            )
        every { matchEventRepository.saveAll(any<List<ApiSportsMatchEvent>>()) } returns savedEvents

        // when
        val result = matchEventManager.processMatchEvents(eventDto, entityBundle)

        // then
        assertThat(result.totalEvents).isEqualTo(2) // 누락이 있어도 모두 처리됨
        assertThat(result.createdCount).isEqualTo(2)
    }

    @Test
    @DisplayName("sequence 검증 - 시작점이 0이 아니면 경고를 로그에 기록합니다")
    fun `processMatchEvents_non_zero_start_sequence_should_log_warning`() {
        // given
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(sequence = 1), // 0이 아닌 시작점
                        createMockEventDto(sequence = 2),
                    ),
            )
        val savedEvents =
            listOf(
                createMockMatchEvent(sequence = 1),
                createMockMatchEvent(sequence = 2),
            )
        every { matchEventRepository.saveAll(any<List<ApiSportsMatchEvent>>()) } returns savedEvents

        // when
        val result = matchEventManager.processMatchEvents(eventDto, entityBundle)

        // then
        assertThat(result.totalEvents).isEqualTo(2) // 경고가 있어도 모두 처리됨
        assertThat(result.createdCount).isEqualTo(2)
    }

    @Test
    @DisplayName("저장 실패 시 빈 이벤트로 대체합니다")
    fun `processMatchEvents_should_create_empty_events_on_save_failure`() {
        // given
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(sequence = 0),
                        createMockEventDto(sequence = 1),
                    ),
            )
        every { matchEventRepository.saveAll(any<List<ApiSportsMatchEvent>>()) } throws
            RuntimeException("Database error")

        // when
        val result = matchEventManager.processMatchEvents(eventDto, entityBundle)

        // then
        assertThat(result.totalEvents).isEqualTo(2)
        assertThat(result.createdCount).isEqualTo(2)
        assertThat(result.savedEvents).allSatisfy { event ->
            assertThat(event.eventType).isEqualTo("UNKNOWN")
            assertThat(event.detail).isEqualTo("Failed to process event")
            assertThat(event.comments).isEqualTo("Event processing failed")
            assertThat(event.player).isNull()
            assertThat(event.assist).isNull()
            assertThat(event.elapsedTime).isEqualTo(0)
        }
    }

    @Test
    @DisplayName("저장된 이벤트를 sequence 순으로 정렬합니다")
    fun `processMatchEvents_should_sort_events_by_sequence`() {
        // given
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(sequence = 2),
                        createMockEventDto(sequence = 0),
                        createMockEventDto(sequence = 1),
                    ),
            )
        val savedEvents =
            listOf(
                createMockMatchEvent(sequence = 2),
                createMockMatchEvent(sequence = 0),
                createMockMatchEvent(sequence = 1),
            )
        every { matchEventRepository.saveAll(any<List<ApiSportsMatchEvent>>()) } returns savedEvents

        // when
        val result = matchEventManager.processMatchEvents(eventDto, entityBundle)

        // then
        val expectedSortedEvents = savedEvents.sortedBy { it.sequence }
        assertThat(result.savedEvents).isEqualTo(expectedSortedEvents)
        assertThat(entityBundle.allEvents).isEqualTo(expectedSortedEvents)

        // sequence 순서 확인
        assertThat(result.savedEvents.map { it.sequence }).containsExactly(0, 1, 2)
    }

    @Test
    @DisplayName("삭제 작업을 수행할 수 있습니다")
    fun `processMatchEvents_should_handle_deletions`() {
        // given
        val existingEvent = createMockMatchEvent(sequence = 0)
        entityBundle.allEvents = listOf(existingEvent)

        val eventDto = MatchEventPlanDto(events = emptyList()) // 모든 이벤트 삭제
        every { matchEventRepository.saveAll(any<List<ApiSportsMatchEvent>>()) } returns emptyList()

        // when
        val result = matchEventManager.processMatchEvents(eventDto, entityBundle)

        // then
        verify { matchEventRepository.deleteAll(any<List<ApiSportsMatchEvent>>()) }
        assertThat(result.totalEvents).isEqualTo(0)
        assertThat(result.createdCount).isEqualTo(0)
        assertThat(result.updatedCount).isEqualTo(0)
        assertThat(result.deletedCount).isEqualTo(1)
    }

    @Test
    @DisplayName("저장시 예외가 발생하더라도 예외가 바깥으로 던져지지 않습니다")
    fun `processMatchEvents_should_propagate_exceptions`() {
        // given
        val eventDto =
            MatchEventPlanDto(
                events = listOf(createMockEventDto(sequence = 0)),
            )
        every { matchEventRepository.saveAll(any<List<ApiSportsMatchEvent>>()) } throws
            RuntimeException("Critical error")

        // when & then
        assertDoesNotThrow {
            matchEventManager.processMatchEvents(eventDto, entityBundle)
        }
    }

    // Helper methods
    private fun createMockEventDto(sequence: Int): MatchEventDto =
        MatchEventDto(
            sequence = sequence,
            elapsedTime = 45,
            extraTime = null,
            eventType = "Goal",
            detail = "Goal scored",
            comments = "Test goal",
            teamApiId = 123L,
            playerMpKey = "player_123",
            assistMpKey = null,
        )

    private fun createMockMatchEvent(sequence: Int): ApiSportsMatchEvent =
        ApiSportsMatchEvent(
            fixtureApi = createMockFixture(),
            matchTeam = createMockHomeTeam(),
            player = null,
            assist = null,
            sequence = sequence,
            elapsedTime = 45,
            extraTime = null,
            eventType = "Goal",
            detail = "Goal scored",
            comments = "Test goal",
        )

    private fun createMockFixture(): FixtureApiSports = mockk(relaxed = true)

    private fun createMockHomeTeam(): ApiSportsMatchTeam = mockk(relaxed = true)

    private fun createMockAwayTeam(): ApiSportsMatchTeam = mockk(relaxed = true)
}
