package com.footballay.core.domain.model

sealed interface FixtureExtra

data class ApiSportsFixtureExtra(
    val refereeName: String? = null,
) : FixtureExtra
