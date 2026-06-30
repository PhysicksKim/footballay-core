package com.footballay.core.infra.backbone.mock.resource

data class MockLeagueCreateCommand(
    val name: String,
    val available: Boolean = true,
    val scenarioUid: String? = null,
    val scenarioName: String? = null,
)
