package com.footballay.core.domain.fixture.model

import java.time.Instant

data class TeamSide(
    val uid: String,
    val name: String,
)

data class Score(
    val home: Int?,
    val away: Int?,
)

data class FixtureModel(
    val uid: String,
    val kickoffAt: Instant,
    val homeTeam: TeamSide,
    val awayTeam: TeamSide,
    val status: String,
    val score: Score,
    val available: Boolean,
    val extra: FixtureExtra? = null,
)


