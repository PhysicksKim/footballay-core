package com.footballay.core.infra.backbone.mock.resource

data class MockTeamCreateCommand(
    val name: String,
    val scenarioUid: String? = null,
    val scenarioName: String? = null,
)
