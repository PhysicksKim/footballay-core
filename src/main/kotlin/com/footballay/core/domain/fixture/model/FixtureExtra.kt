package com.footballay.core.domain.fixture.model

sealed interface FixtureExtra

data class ApiSportsFixtureExtra(
    val refereeName: String? = null,
) : FixtureExtra


