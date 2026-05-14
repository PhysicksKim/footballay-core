package com.footballay.core.infra.dispatcher.match

import com.footballay.core.infra.match.MatchSyncOrchestrator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

/**
 * SimpleMatchDataSyncDispatcher 단위 테스트
 *
 * Dispatcher는 지원 가능한 MatchSyncOrchestrator를 선택하고 sync 결과를 그대로 반환합니다.
 */
@ExtendWith(MockitoExtension::class)
class SimpleMatchDataSyncDispatcherTest {
    @Mock
    private lateinit var orchestrator: MatchSyncOrchestrator

    @Mock
    private lateinit var anotherOrchestrator: MatchSyncOrchestrator

    private lateinit var dispatcher: SimpleMatchDataSyncDispatcher

    @BeforeEach
    fun setup() {
        dispatcher = SimpleMatchDataSyncDispatcher(orchestrators = listOf(orchestrator))
    }

    @Test
    fun `지원 orchestrator가 있으면 sync 결과를 반환한다`() {
        val fixtureUid = "testfixture0001"
        val expected =
            MatchDataSyncResult.PreMatch(
                lineupCached = true,
                kickoffTime = Instant.now(),
                shouldTerminatePreMatchJob = false,
            )

        whenever(orchestrator.isSupport(fixtureUid)).thenReturn(true)
        whenever(orchestrator.syncMatchData(fixtureUid)).thenReturn(expected)

        val result = dispatcher.syncByFixtureUid(fixtureUid)

        assertThat(result).isEqualTo(expected)
        verify(orchestrator).isSupport(fixtureUid)
        verify(orchestrator).syncMatchData(fixtureUid)
    }

    @Test
    fun `지원 orchestrator가 없으면 Error를 반환한다`() {
        val fixtureUid = "testfixture0002"

        whenever(orchestrator.isSupport(fixtureUid)).thenReturn(false)

        val result = dispatcher.syncByFixtureUid(fixtureUid)

        assertThat(result).isInstanceOf(MatchDataSyncResult.Error::class.java)
        val error = result as MatchDataSyncResult.Error
        assertThat(error.message).isEqualTo("No orchestrator found for fixtureUid=$fixtureUid")
        assertThat(error.kickoffTime).isNull()
        verify(orchestrator).isSupport(fixtureUid)
        verify(orchestrator, never()).syncMatchData(any())
    }

    @Test
    fun `여러 orchestrator 중 지원하는 것을 호출한다`() {
        val fixtureUid = "testfixture0003"
        val expected =
            MatchDataSyncResult.Live(
                kickoffTime = Instant.now(),
                isMatchFinished = false,
                elapsedMin = 35,
                statusShort = "1H",
            )
        dispatcher = SimpleMatchDataSyncDispatcher(orchestrators = listOf(orchestrator, anotherOrchestrator))

        whenever(orchestrator.isSupport(fixtureUid)).thenReturn(false)
        whenever(anotherOrchestrator.isSupport(fixtureUid)).thenReturn(true)
        whenever(anotherOrchestrator.syncMatchData(fixtureUid)).thenReturn(expected)

        val result = dispatcher.syncByFixtureUid(fixtureUid)

        assertThat(result).isEqualTo(expected)
        verify(orchestrator).isSupport(fixtureUid)
        verify(orchestrator, never()).syncMatchData(any())
        verify(anotherOrchestrator).isSupport(fixtureUid)
        verify(anotherOrchestrator).syncMatchData(fixtureUid)
    }

    @Test
    fun `orchestrator의 Error result도 그대로 반환한다`() {
        val fixtureUid = "testfixture0004"
        val expected =
            MatchDataSyncResult.Error(
                message = "API error",
                kickoffTime = null,
            )

        whenever(orchestrator.isSupport(fixtureUid)).thenReturn(true)
        whenever(orchestrator.syncMatchData(fixtureUid)).thenReturn(expected)

        val result = dispatcher.syncByFixtureUid(fixtureUid)

        assertThat(result).isEqualTo(expected)
        verify(orchestrator).isSupport(fixtureUid)
        verify(orchestrator).syncMatchData(fixtureUid)
    }
}
