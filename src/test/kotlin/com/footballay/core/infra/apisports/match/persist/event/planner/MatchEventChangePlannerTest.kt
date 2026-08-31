package com.footballay.core.infra.apisports.match.persist.event.planner

import com.footballay.core.infra.apisports.match.persist.event.planner.MatchEventChangePlanner
import com.footballay.core.infra.apisports.match.plan.dto.MatchEventDto
import com.footballay.core.infra.apisports.match.plan.dto.MatchEventPlanDto
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsSeason
import com.footballay.core.infra.persistence.apisports.entity.PlayerApiSports
import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import com.footballay.core.infra.apisports.match.plan.context.MatchPlayerKeyGenerator.generateMatchPlayerKey as createMpKey

@DisplayName("MatchEventChangePlanner 테스트")
class MatchEventChangePlannerTest {
    @Test
    @DisplayName("기존 엔티티들을 sequence 기반으로 올바르게 맵핑합니다")
    fun `entitiesToSequenceMap_정상적인_엔티티들이_올바르게_맵핑된다`() {
        // given
        val entities =
            listOf(
                createMockMatchEvent(sequence = 1),
                createMockMatchEvent(sequence = 2),
                createMockMatchEvent(sequence = 3),
            )

        // when
        val result = MatchEventChangePlanner.entitiesToSequenceMap(entities)

        // then
        assertThat(result).hasSize(3)
        assertThat(result).containsKeys(1, 2, 3)
        assertThat(result[1]?.sequence).isEqualTo(1)
        assertThat(result[2]?.sequence).isEqualTo(2)
        assertThat(result[3]?.sequence).isEqualTo(3)
    }

    @Test
    @DisplayName("sequence가 0 이하인 엔티티들은 필터링되어 맵핑에서 제외됩니다")
    fun `entitiesToSequenceMap_비정상_엔티티들이_필터링된다`() {
        // given
        val entities =
            listOf(
                createMockMatchEvent(sequence = 1),
                createMockMatchEvent(sequence = 0), // 필터링 대상
                createMockMatchEvent(sequence = -1), // 필터링 대상
                createMockMatchEvent(sequence = 2),
            )

        // when
        val result = MatchEventChangePlanner.entitiesToSequenceMap(entities)

        // then
        assertThat(result).hasSize(3)
        assertThat(result).containsKeys(0, 1, 2)
        assertThat(result).doesNotContainKeys(-1)
    }

    @Test
    @DisplayName("새로운 이벤트 DTO는 생성 계획에 포함됩니다")
    fun `planChanges_새로운_이벤트는_생성_계획에_포함된다`() {
        // given
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(sequence = 1, playerMpKey = "mp_id_123"),
                        createMockEventDto(sequence = 2, playerMpKey = "mp_name_Player B"),
                    ),
            )
        val entityEvents = mapOf<Int, ApiSportsMatchEvent>() // 빈 맵
        val allMatchPlayers =
            listOf(
                createMockMatchPlayer(apiId = 123L, name = "Player A"),
                createMockMatchPlayer(apiId = null, name = "Player B"),
            )

        // when
        val result =
            MatchEventChangePlanner.planChanges(
                eventDto,
                entityEvents,
                createMockFixture(),
                createMockHomeTeam(),
                createMockAwayTeam(),
                allMatchPlayers.associate { player ->
                    val key = createMpKey(player.playerApiSports?.apiId, player.name)
                    key to player
                },
            )

        // then
        assertThat(result.createCount).isEqualTo(2)
        assertThat(result.retainedCount).isEqualTo(0)
        assertThat(result.deleteCount).isEqualTo(0)

        // 생성된 엔티티 검증
        val createdEvents = result.toCreate
        assertThat(createdEvents).hasSize(2)
        assertThat(createdEvents[0].sequence).isEqualTo(1)
        assertThat(createdEvents[0].player?.name).isEqualTo("Player A")
        assertThat(createdEvents[1].sequence).isEqualTo(2)
        assertThat(createdEvents[1].player?.name).isEqualTo("Player B")
    }

    @Test
    @DisplayName("기존 이벤트와 매칭되는 DTO는 변경사항이 있을 때만 업데이트 계획에 포함됩니다")
    fun `planChanges_기존_이벤트는_변경사항이_있을_때만_업데이트된다`() {
        // given
        val existingEvent = createMockMatchEvent(sequence = 1, elapsedTime = 10)
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(sequence = 1, elapsedTime = 15), // 변경됨
                    ),
            )
        val entityEvents = mapOf(1 to existingEvent)
        val allMatchPlayers =
            listOf(
                createMockMatchPlayer(apiId = 123L, name = "Player A"),
            )

        // when
        val result =
            MatchEventChangePlanner.planChanges(
                eventDto,
                entityEvents,
                createMockFixture(),
                createMockHomeTeam(),
                createMockAwayTeam(),
                allMatchPlayers.associate { player ->
                    val key = createMpKey(player.playerApiSports?.apiId, player.name)
                    key to player
                },
            )

        // then
        assertThat(result.createCount).isEqualTo(0)
        assertThat(result.retainedCount).isEqualTo(1)
        assertThat(result.deleteCount).isEqualTo(0)

        val updatedEvent = result.toRetain[0]
        assertThat(updatedEvent.sequence).isEqualTo(1)
        assertThat(updatedEvent.elapsedTime).isEqualTo(15)
    }

    @Test
    @DisplayName("같은 sequence의 이벤트 팀이 변경되면 최신 팀으로 업데이트합니다")
    fun `planChanges_updates_match_team_when_team_api_id_changes`() {
        // given
        val homeTeam = createMockHomeTeam()
        val awayTeam = createMockAwayTeam()
        val existingEvent = createMockMatchEvent(sequence = 1, matchTeam = homeTeam)
        val eventDto = MatchEventPlanDto(events = listOf(createMockEventDto(sequence = 1, teamApiId = 2L)))

        // when
        val result =
            MatchEventChangePlanner.planChanges(
                eventDto,
                mapOf(1 to existingEvent),
                createMockFixture(),
                homeTeam,
                awayTeam,
                emptyMap(),
            )

        // then
        assertThat(result.toRetain).containsExactly(existingEvent)
        assertThat(existingEvent.matchTeam).isEqualTo(awayTeam)
    }

    @Test
    @DisplayName("중간 이벤트 추가로 sequence가 밀리면 기존 슬롯을 현재 snapshot 값으로 덮어씁니다")
    fun `planChanges_overwrites_shifted_sequence_slots_from_current_snapshot`() {
        // given
        val homeTeam = createMockHomeTeam()
        val awayTeam = createMockAwayTeam()
        val playerA = createMockMatchPlayer(101L, "Player A")
        val playerB = createMockMatchPlayer(102L, "Player B")
        val playerC = createMockMatchPlayer(103L, "Player C")
        val playerX = createMockMatchPlayer(104L, "Player X")
        val allMatchPlayers =
            listOf(playerA, playerB, playerC, playerX).associateBy { createMpKey(it.playerApiSports?.apiId, it.name) }
        val existingEventA =
            createMockMatchEvent(0, 10, homeTeam, playerA, null, 0, "Goal", "A", "A comment")
        val existingEventB =
            createMockMatchEvent(1, 20, homeTeam, playerB, playerA, 1, "Card", "B", "B comment")
        val existingEventC =
            createMockMatchEvent(2, 30, awayTeam, playerC, playerB, 2, "VAR", "C", "C comment")
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(0, 10, 1L, createMpKey(101L, "Player A"), null, 0, "Goal", "A", "A comment"),
                        createMockEventDto(
                            1,
                            25,
                            2L,
                            createMpKey(104L, "Player X"),
                            createMpKey(101L, "Player A"),
                            3,
                            "Subst",
                            "X",
                            "X comment",
                        ),
                        createMockEventDto(
                            2,
                            20,
                            1L,
                            createMpKey(102L, "Player B"),
                            createMpKey(104L, "Player X"),
                            1,
                            "Card",
                            "B moved",
                            "B moved comment",
                        ),
                        createMockEventDto(
                            3,
                            30,
                            2L,
                            createMpKey(103L, "Player C"),
                            createMpKey(102L, "Player B"),
                            2,
                            "VAR",
                            "C moved",
                            "C moved comment",
                        ),
                    ),
            )

        // when
        val result =
            MatchEventChangePlanner.planChanges(
                eventDto,
                mapOf(0 to existingEventA, 1 to existingEventB, 2 to existingEventC),
                createMockFixture(),
                homeTeam,
                awayTeam,
                allMatchPlayers,
            )

        // then
        assertThat(result.toRetain).containsExactly(existingEventB, existingEventC)
        assertThat(result.toCreate).hasSize(1)
        assertThat(result.toDelete).isEmpty()
        assertEvent(existingEventB, awayTeam, playerX, playerA, 25, 3, "Subst", "X", "X comment")
        assertEvent(existingEventC, homeTeam, playerB, playerX, 20, 1, "Card", "B moved", "B moved comment")
        assertEvent(result.toCreate.single(), awayTeam, playerC, playerB, 30, 2, "VAR", "C moved", "C moved comment")
    }

    @Test
    @DisplayName("변경사항이 없는 기존 이벤트는 업데이트 계획에 포함되지 않습니다")
    fun `planChanges_변경사항이_없는_이벤트는_업데이트되지_않는다`() {
        // given
        val existingEvent = createMockMatchEvent(sequence = 1, elapsedTime = 10)
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(sequence = 1, elapsedTime = 10), // 동일
                    ),
            )
        val entityEvents = mapOf(1 to existingEvent)
        val allMatchPlayers =
            listOf(
                createMockMatchPlayer(apiId = 123L, name = "Player A"),
            )

        // when
        val result =
            MatchEventChangePlanner.planChanges(
                eventDto,
                entityEvents,
                createMockFixture(),
                createMockHomeTeam(),
                createMockAwayTeam(),
                allMatchPlayers.associate { player ->
                    val key = createMpKey(player.playerApiSports?.apiId, player.name)
                    key to player
                },
            )

        // then
        assertThat(result.createCount).isEqualTo(0)
        assertThat(result.retainedCount).isEqualTo(0)
        assertThat(result.deleteCount).isEqualTo(0)
    }

    @Test
    @DisplayName("DTO에 없는 기존 이벤트는 삭제 계획에 포함됩니다")
    fun `planChanges_고아_엔티티는_삭제_계획에_포함된다`() {
        // given
        val existingEvent1 = createMockMatchEvent(sequence = 1)
        val existingEvent2 = createMockMatchEvent(sequence = 2)
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(sequence = 1), // sequence 2는 DTO에 없음
                    ),
            )
        val entityEvents =
            mapOf(
                1 to existingEvent1,
                2 to existingEvent2,
            )
        val allMatchPlayers =
            listOf(
                createMockMatchPlayer(apiId = 123L, name = "Player A"),
            )

        // when
        val result =
            MatchEventChangePlanner.planChanges(
                eventDto,
                entityEvents,
                createMockFixture(),
                createMockHomeTeam(),
                createMockAwayTeam(),
                allMatchPlayers.associate { player ->
                    val key = createMpKey(player.playerApiSports?.apiId, player.name)
                    key to player
                },
            )

        // then
        assertThat(result.createCount).isEqualTo(0)
        assertThat(result.retainedCount).isEqualTo(0)
        assertThat(result.deleteCount).isEqualTo(1)
        assertThat(result.toDelete[0].sequence).isEqualTo(2)
    }

    @Test
    @DisplayName("player와 assist 필드가 올바르게 MatchPlayer와 연결됩니다")
    fun `planChanges_player_assist_필드가_올바르게_연결된다`() {
        // given
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(
                            sequence = 1,
                            playerMpKey = "mp_id_123",
                            assistMpKey = "mp_name_Player B",
                        ),
                    ),
            )
        val entityEvents = mapOf<Int, ApiSportsMatchEvent>()
        val allMatchPlayers =
            listOf(
                createMockMatchPlayer(apiId = 123L, name = "Player A"),
                createMockMatchPlayer(apiId = null, name = "Player B"),
            )

        // when
        val result =
            MatchEventChangePlanner.planChanges(
                eventDto,
                entityEvents,
                createMockFixture(),
                createMockHomeTeam(),
                createMockAwayTeam(),
                allMatchPlayers.associate { player ->
                    val key = createMpKey(player.playerApiSports?.apiId, player.name)
                    key to player
                },
            )

        // then
        val createdEvent = result.toCreate[0]
        assertThat(createdEvent.player?.name).isEqualTo("Player A")
        assertThat(createdEvent.player?.playerApiSports?.apiId).isEqualTo(123L)
        assertThat(createdEvent.assist?.name).isEqualTo("Player B")
        assertThat(createdEvent.assist?.playerApiSports).isNull()
    }

    @Test
    @DisplayName("MatchPlayerKey가 null인 경우 player/assist 필드도 null로 설정됩니다")
    fun `planChanges_null_키는_null_필드로_설정된다`() {
        // given
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(
                            sequence = 1,
                            playerMpKey = null,
                            assistMpKey = null,
                        ),
                    ),
            )
        val entityEvents = mapOf<Int, ApiSportsMatchEvent>()
        val allMatchPlayers =
            listOf(
                createMockMatchPlayer(apiId = 123L, name = "Player A"),
            )

        // when
        val result =
            MatchEventChangePlanner.planChanges(
                eventDto,
                entityEvents,
                createMockFixture(),
                createMockHomeTeam(),
                createMockAwayTeam(),
                allMatchPlayers.associate { player ->
                    val key = createMpKey(player.playerApiSports?.apiId, player.name)
                    key to player
                },
            )

        // then
        val createdEvent = result.toCreate[0]
        assertThat(createdEvent.player).isNull()
        assertThat(createdEvent.assist).isNull()
    }

    @Test
    @DisplayName("존재하지 않는 MatchPlayerKey는 null로 처리됩니다")
    fun `planChanges_존재하지_않는_키는_null로_처리된다`() {
        // given
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(
                            sequence = 1,
                            playerMpKey = "mp_id_999", // 존재하지 않는 키
                            assistMpKey = "mp_name_Unknown Player", // 존재하지 않는 키
                        ),
                    ),
            )
        val entityEvents = mapOf<Int, ApiSportsMatchEvent>()
        val allMatchPlayers =
            listOf(
                createMockMatchPlayer(apiId = 123L, name = "Player A"),
            )

        // when
        val result =
            MatchEventChangePlanner.planChanges(
                eventDto,
                entityEvents,
                createMockFixture(),
                createMockHomeTeam(),
                createMockAwayTeam(),
                allMatchPlayers.associate { player ->
                    val key = createMpKey(player.playerApiSports?.apiId, player.name)
                    key to player
                },
            )

        // then
        val createdEvent = result.toCreate[0]
        assertThat(createdEvent.player).isNull()
        assertThat(createdEvent.assist).isNull()
    }

    @Test
    @DisplayName("sequence 검증 - 중복된 sequence가 있으면 경고를 로그에 기록합니다")
    fun `planChanges_duplicate_sequences_should_log_warning`() {
        // given
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(sequence = 1),
                        createMockEventDto(sequence = 1), // 중복
                        createMockEventDto(sequence = 2),
                    ),
            )
        val entityEvents = mapOf<Int, ApiSportsMatchEvent>()
        val allMatchPlayers =
            listOf(
                createMockMatchPlayer(apiId = 123L, name = "Player A"),
            )

        // when
        val result =
            MatchEventChangePlanner.planChanges(
                eventDto,
                entityEvents,
                createMockFixture(),
                createMockHomeTeam(),
                createMockAwayTeam(),
                allMatchPlayers.associate { player ->
                    val key = createMpKey(player.playerApiSports?.apiId, player.name)
                    key to player
                },
            )

        // then
        assertThat(result.createCount).isEqualTo(3) // 중복이어도 모두 생성됨
    }

    @Test
    @DisplayName("sequence 검증 - 누락된 sequence가 있으면 경고를 로그에 기록합니다")
    fun `planChanges_missing_sequences_should_log_warning`() {
        // given
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(sequence = 0),
                        createMockEventDto(sequence = 2), // 1이 누락됨
                        createMockEventDto(sequence = 3),
                    ),
            )
        val entityEvents = mapOf<Int, ApiSportsMatchEvent>()
        val allMatchPlayers =
            listOf(
                createMockMatchPlayer(apiId = 123L, name = "Player A"),
            )

        // when
        val result =
            MatchEventChangePlanner.planChanges(
                eventDto,
                entityEvents,
                createMockFixture(),
                createMockHomeTeam(),
                createMockAwayTeam(),
                allMatchPlayers.associate { player ->
                    val key = createMpKey(player.playerApiSports?.apiId, player.name)
                    key to player
                },
            )

        // then
        assertThat(result.createCount).isEqualTo(3) // 누락이어도 모두 생성됨
    }

    @Test
    @DisplayName("sequence 검증 - 시작점이 0이 아니면 경고를 로그에 기록합니다")
    fun `planChanges_non_zero_start_sequence_should_log_warning`() {
        // given
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(sequence = 1), // 0이 아닌 시작점
                        createMockEventDto(sequence = 2),
                        createMockEventDto(sequence = 3),
                    ),
            )
        val entityEvents = mapOf<Int, ApiSportsMatchEvent>()
        val allMatchPlayers =
            listOf(
                createMockMatchPlayer(apiId = 123L, name = "Player A"),
            )

        // when
        val result =
            MatchEventChangePlanner.planChanges(
                eventDto,
                entityEvents,
                createMockFixture(),
                createMockHomeTeam(),
                createMockAwayTeam(),
                allMatchPlayers.associate { player ->
                    val key = createMpKey(player.playerApiSports?.apiId, player.name)
                    key to player
                },
            )

        // then
        assertThat(result.createCount).isEqualTo(3) // 경고가 있어도 모두 생성됨
    }

    @Test
    @DisplayName("sequence 검증 - DTO와 Entity 시작점이 다르면 경고를 로그에 기록합니다")
    fun `planChanges_different_start_points_should_log_warning`() {
        // given
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(sequence = 0),
                        createMockEventDto(sequence = 1),
                    ),
            )
        val existingEvent = createMockMatchEvent(sequence = 1) // Entity는 1부터 시작
        existingEvent.eventType = "TYPE_NEW" // update 가 일어나도록 하기 위함
        val entityEvents = mapOf(1 to existingEvent)
        val allMatchPlayers =
            listOf(
                createMockMatchPlayer(apiId = 123L, name = "Player A"),
            )

        // when
        val result =
            MatchEventChangePlanner.planChanges(
                eventDto,
                entityEvents,
                createMockFixture(),
                createMockHomeTeam(),
                createMockAwayTeam(),
                allMatchPlayers.associate { player ->
                    val key = createMpKey(player.playerApiSports?.apiId, player.name)
                    key to player
                },
            )

        // then
        assertThat(result.createCount).isEqualTo(1) // sequence 0은 새로 생성
        assertThat(result.retainedCount).isEqualTo(1) // sequence 1은 업데이트
    }

    @Test
    @DisplayName("효율적인 삭제 - 이벤트 순서가 변경되면 해당 sequence만 삭제합니다")
    fun `planChanges_efficient_deletion_when_event_order_changes`() {
        // given
        val existingEvent1 = createMockMatchEvent(sequence = 0)
        val existingEvent2 = createMockMatchEvent(sequence = 1)
        val existingEvent3 = createMockMatchEvent(sequence = 2)
        existingEvent1.eventType = "TYPE_OLD" // update 가 일어나도록 하기 위함
        existingEvent3.eventType = "TYPE_OLD" // update 가 일어나도록 하기 위함

        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(sequence = 0),
                        createMockEventDto(sequence = 2), // sequence 1이 삭제되고 2가 남음
                        createMockEventDto(sequence = 3), // 새로운 sequence
                    ),
            )
        val entityEvents =
            mapOf(
                0 to existingEvent1,
                1 to existingEvent2,
                2 to existingEvent3,
            )
        val allMatchPlayers =
            listOf(
                createMockMatchPlayer(apiId = 123L, name = "Player A"),
            )

        // when
        val result =
            MatchEventChangePlanner.planChanges(
                eventDto,
                entityEvents,
                createMockFixture(),
                createMockHomeTeam(),
                createMockAwayTeam(),
                allMatchPlayers.associate { player ->
                    val key = createMpKey(player.playerApiSports?.apiId, player.name)
                    key to player
                },
            )

        // then
        assertThat(result.createCount).isEqualTo(1) // sequence 3만 새로 생성
        assertThat(result.retainedCount).isEqualTo(2) // sequence 0, 2는 업데이트
        assertThat(result.deleteCount).isEqualTo(1) // sequence 1만 삭제
        assertThat(result.toDelete[0].sequence).isEqualTo(1)
    }

    @Test
    @DisplayName("빈 엔티티 맵에서 새로운 이벤트들을 생성할 수 있습니다")
    fun `planChanges_empty_entities_should_create_all_events`() {
        // given
        val eventDto =
            MatchEventPlanDto(
                events =
                    listOf(
                        createMockEventDto(sequence = 0),
                        createMockEventDto(sequence = 1),
                        createMockEventDto(sequence = 2),
                    ),
            )
        val entityEvents = mapOf<Int, ApiSportsMatchEvent>()
        val allMatchPlayers =
            listOf(
                createMockMatchPlayer(apiId = 123L, name = "Player A"),
            )

        // when
        val result =
            MatchEventChangePlanner.planChanges(
                eventDto,
                entityEvents,
                createMockFixture(),
                createMockHomeTeam(),
                createMockAwayTeam(),
                allMatchPlayers.associate { player ->
                    val key = createMpKey(player.playerApiSports?.apiId, player.name)
                    key to player
                },
            )

        // then
        assertThat(result.createCount).isEqualTo(3)
        assertThat(result.retainedCount).isEqualTo(0)
        assertThat(result.deleteCount).isEqualTo(0)
    }

    @Test
    @DisplayName("빈 DTO에서 모든 엔티티를 삭제할 수 있습니다")
    fun `planChanges_empty_dto_should_delete_all_entities`() {
        // given
        val eventDto = MatchEventPlanDto(events = emptyList())
        val existingEvent1 = createMockMatchEvent(sequence = 0)
        val existingEvent2 = createMockMatchEvent(sequence = 1)
        val entityEvents =
            mapOf(
                0 to existingEvent1,
                1 to existingEvent2,
            )
        val allMatchPlayers =
            listOf(
                createMockMatchPlayer(apiId = 123L, name = "Player A"),
            )

        // when
        val result =
            MatchEventChangePlanner.planChanges(
                eventDto,
                entityEvents,
                createMockFixture(),
                createMockHomeTeam(),
                createMockAwayTeam(),
                allMatchPlayers.associate { player ->
                    val key = createMpKey(player.playerApiSports?.apiId, player.name)
                    key to player
                },
            )

        // then
        assertThat(result.createCount).isEqualTo(0)
        assertThat(result.retainedCount).isEqualTo(0)
        assertThat(result.deleteCount).isEqualTo(2)
    }

    @Test
    @DisplayName("entitiesToSequenceMap - 유효한 엔티티만 변환합니다")
    fun `entitiesToSequenceMap_should_filter_valid_entities`() {
        // given
        val validEvent1 = createMockMatchEvent(sequence = 0)
        val validEvent2 = createMockMatchEvent(sequence = 1)
        val invalidEvent = createMockMatchEvent(sequence = -1) // 유효하지 않은 sequence
        val entities = listOf(validEvent1, validEvent2, invalidEvent)

        // when
        val result = MatchEventChangePlanner.entitiesToSequenceMap(entities)

        // then
        assertThat(result).hasSize(2)
        assertThat(result[0]).isEqualTo(validEvent1)
        assertThat(result[1]).isEqualTo(validEvent2)
        assertThat(result).doesNotContainKey(-1)
    }

    @Test
    @DisplayName("entitiesToSequenceMap - 빈 리스트를 처리할 수 있습니다")
    fun `entitiesToSequenceMap_empty_list_should_return_empty_map`() {
        // given
        val entities = emptyList<ApiSportsMatchEvent>()

        // when
        val result = MatchEventChangePlanner.entitiesToSequenceMap(entities)

        // then
        assertThat(result).isEmpty()
    }

    // Helper methods
    private fun createMockEventDto(
        sequence: Int,
        elapsedTime: Int = 10,
        teamApiId: Long? = null,
        playerMpKey: String? = null,
        assistMpKey: String? = null,
        extraTime: Int? = 0,
        eventType: String = "Goal",
        detail: String? = "Normal Goal",
        comments: String? = null,
    ): MatchEventDto =
        MatchEventDto(
            sequence = sequence,
            elapsedTime = elapsedTime,
            extraTime = extraTime,
            eventType = eventType,
            detail = detail,
            comments = comments,
            teamApiId = teamApiId,
            playerMpKey = playerMpKey,
            assistMpKey = assistMpKey,
        )

    private fun createMockMatchEvent(
        sequence: Int,
        elapsedTime: Int = 10,
        matchTeam: ApiSportsMatchTeam? = null,
        player: ApiSportsMatchPlayer? = null,
        assist: ApiSportsMatchPlayer? = null,
        extraTime: Int? = 0,
        eventType: String = "Goal",
        detail: String? = "Normal Goal",
        comments: String? = null,
    ): ApiSportsMatchEvent =
        ApiSportsMatchEvent(
            fixtureApi = createMockFixture(),
            matchTeam = matchTeam,
            player = player,
            assist = assist,
            sequence = sequence,
            elapsedTime = elapsedTime,
            extraTime = extraTime,
            eventType = eventType,
            detail = detail,
            comments = comments,
        )

    private fun assertEvent(
        event: ApiSportsMatchEvent,
        matchTeam: ApiSportsMatchTeam,
        player: ApiSportsMatchPlayer,
        assist: ApiSportsMatchPlayer?,
        elapsedTime: Int,
        extraTime: Int?,
        eventType: String,
        detail: String,
        comments: String,
    ) {
        assertThat(event.matchTeam).isSameAs(matchTeam)
        assertThat(event.player).isSameAs(player)
        assertThat(event.assist).isSameAs(assist)
        assertThat(event.elapsedTime).isEqualTo(elapsedTime)
        assertThat(event.extraTime).isEqualTo(extraTime)
        assertThat(event.eventType).isEqualTo(eventType)
        assertThat(event.detail).isEqualTo(detail)
        assertThat(event.comments).isEqualTo(comments)
    }

    private fun createMockMatchPlayer(
        apiId: Long?,
        name: String,
    ): ApiSportsMatchPlayer =
        ApiSportsMatchPlayer(
            matchPlayerUid = "mp_$name",
            playerApiSports = apiId?.let { createMockPlayerApiSports(it) },
            name = name,
            number = 10,
            position = "F",
            grid = "10:10",
            substitute = false,
            matchTeam = null,
        )

    private fun createMockPlayerApiSports(apiId: Long): PlayerApiSports =
        PlayerApiSports(
            apiId = apiId,
            name = "Player $apiId",
            firstname = "First",
            lastname = "Last",
            age = 25,
            nationality = "Korea",
            height = "180",
            weight = "75",
            photo = "photo.jpg",
        )

    private fun createMockFixture(): FixtureApiSports =
        FixtureApiSports(
            apiId = 1L,
            referee = null,
            date = null,
            round = null,
            status = null,
            score = null,
            venue = null,
            season = createMockSeason(),
        )

    private fun createMockHomeTeam(): ApiSportsMatchTeam =
        ApiSportsMatchTeam(
            teamApiSports = createMockTeamApiSports(1L),
            formation = "4-4-2",
            playerColor = null,
            goalkeeperColor = null,
            winner = null,
            teamStatistics = null,
        )

    private fun createMockAwayTeam(): ApiSportsMatchTeam =
        ApiSportsMatchTeam(
            teamApiSports = createMockTeamApiSports(2L),
            formation = "4-3-3",
            playerColor = null,
            goalkeeperColor = null,
            winner = null,
            teamStatistics = null,
        )

    private fun createMockSeason(): LeagueApiSportsSeason =
        LeagueApiSportsSeason(
            seasonYear = 2024,
            seasonStart = null,
            seasonEnd = null,
            coverage = null,
            leagueApiSports = null,
        )

    private fun createMockTeamApiSports(apiId: Long): TeamApiSports =
        TeamApiSports(
            apiId = apiId,
            name = "Team $apiId",
            code = "T$apiId",
            country = "Korea",
            founded = 1900,
            national = false,
            logo = "logo.jpg",
        )
}
