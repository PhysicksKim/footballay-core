package com.footballay.core.infra.backbone.mock.resource

import org.springframework.stereotype.Component
import java.time.Clock
import java.time.format.DateTimeFormatter
import java.util.UUID

@Component
class MockUidFactory(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val clock: Clock = Clock.systemUTC(),
) {
    fun scenarioUid(): String = uid("mock_scenario")

    fun leagueMockUid(): String = uid("mock_league_ref")

    fun teamMockUid(): String = uid("mock_team_ref")

    fun fixtureMockUid(): String = uid("mock_fixture_ref")

    fun leagueCoreUid(): String = uid("mock_league")

    fun teamCoreUid(): String = uid("mock_team")

    fun fixtureCoreUid(): String = uid("mock_fixture")

    private fun uid(prefix: String): String {
        val timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(clock.zone).format(clock.instant())
        val randomId = UUID.randomUUID().toString().replace("-", "").take(8)
        return "${prefix}_${timestamp}_$randomId"
    }
}
