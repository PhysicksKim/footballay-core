package com.footballay.core.infra.apisports.match.sync.persist.player.collector

import com.footballay.core.infra.apisports.match.sync.persist.player.collector.MatchPlayerDtoCollector
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.sync.dto.MatchPlayerDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

@DisplayName("MatchPlayerDtoCollector 테스트")
class MatchPlayerDtoCollectorTest {

    @Test
    @DisplayName("lineup, event, stat에서 동일한 선수가 있을 때 lineup이 가장 높은 우선순위를 가지며 중복이 제거됩니다")
    fun `lineup 선수가 event, stat보다 우선순위가 높다`() {
        // given
        val context = MatchPlayerContext()
        val playerKey = "mp_id_123"
        val lineupPlayer = createMockPlayerDto(apiId = 123L, name = "Lineup Player", substitute = false)
        val eventPlayer = createMockPlayerDto(apiId = 123L, name = "Event Player", substitute = true)  
        val statPlayer = createMockPlayerDto(apiId = 123L, name = "Stat Player", substitute = true)
        
        context.lineupMpDtoMap[playerKey] = lineupPlayer
        context.eventMpDtoMap[playerKey] = eventPlayer
        context.statMpDtoMap[playerKey] = statPlayer
        
        // when
        val result = MatchPlayerDtoCollector.collectFrom(context)
        
        // then
        assertThat(result).hasSize(1)
        assertThat(result[playerKey]).isEqualTo(lineupPlayer)
        assertThat(result[playerKey]?.name).isEqualTo("Lineup Player")
        assertThat(result[playerKey]?.substitute).isFalse()
    }

    @Test
    @DisplayName("event에만 존재하는 선수는 nonLineupPlayer 플래그가 true로 설정되어 수집됩니다")
    fun `event에만 있는 선수는 nonLineupPlayer가 true가 된다`() {
        // given
        val context = MatchPlayerContext()
        val eventOnlyPlayer = createMockPlayerDto(apiId = 456L, name = "Event Only", nonLineupPlayer = false)
        context.eventMpDtoMap["mp_id_456"] = eventOnlyPlayer
        
        // when
        val result = MatchPlayerDtoCollector.collectFrom(context)
        
        // then
        assertThat(result).hasSize(1)
        assertThat(result["mp_id_456"]?.nonLineupPlayer).isTrue()
    }

    @Test
    @DisplayName("lineup, event, stat 세 소스에서 선수들을 수집하고 우선순위에 따라 중복을 제거하여 통합된 결과를 반환합니다")
    fun `lineup, event, stat에서 선수들이 올바르게 수집된다`() {
        // given
        val context = MatchPlayerContext()
        
        // lineup 선수 2명
        context.lineupMpDtoMap["mp_id_1"] = createMockPlayerDto(apiId = 1L, name = "Lineup 1")
        context.lineupMpDtoMap["mp_id_2"] = createMockPlayerDto(apiId = 2L, name = "Lineup 2")
        
        // event 선수 (1명은 lineup과 중복, 1명은 새로움)
        context.eventMpDtoMap["mp_id_1"] = createMockPlayerDto(apiId = 1L, name = "Event 1") // 중복
        context.eventMpDtoMap["mp_name_EventOnly"] = createMockPlayerDto(apiId = null, name = "Event Only") // 새로움
        
        // stat 선수 (모두 새로움)
        context.statMpDtoMap["mp_id_3"] = createMockPlayerDto(apiId = 3L, name = "Stat 3")
        
        // when
        val result = MatchPlayerDtoCollector.collectFrom(context)
        
        // then
        assertThat(result).hasSize(4) // lineup 2명 + event 1명(중복제외) + stat 1명
        assertThat(result["mp_id_1"]?.name).isEqualTo("Lineup 1") // lineup이 우선
        assertThat(result["mp_id_2"]?.name).isEqualTo("Lineup 2")
        assertThat(result["mp_name_EventOnly"]?.nonLineupPlayer).isTrue()
        assertThat(result["mp_id_3"]?.nonLineupPlayer).isTrue()
    }

    @Test
    @DisplayName("빈 Context가 주어졌을 때 빈 Map을 반환하고 아무 작업도 수행하지 않습니다")
    fun `빈 컨텍스트에서 빈 결과를 반환한다`() {
        // given
        val emptyContext = MatchPlayerContext()
        
        // when
        val result = MatchPlayerDtoCollector.collectFrom(emptyContext)
        
        // then
        assertThat(result).isEmpty()
    }
    
    private fun createMockPlayerDto(
        apiId: Long? = 123L,
        name: String = "Test Player",
        substitute: Boolean = false,
        nonLineupPlayer: Boolean = false
    ): MatchPlayerDto {
        return MatchPlayerDto(
            matchPlayerUid = null,
            apiId = apiId,
            name = name,
            substitute = substitute,
            nonLineupPlayer = nonLineupPlayer,
            teamApiId = 1L,
            playerApiSportsInfo = null
        )
    }
} 