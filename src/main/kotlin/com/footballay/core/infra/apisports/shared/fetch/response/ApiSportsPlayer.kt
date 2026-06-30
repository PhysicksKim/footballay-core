package com.footballay.core.infra.apisports.shared.fetch.response

object ApiSportsPlayer {
    data class OfTeam(
        val team: TeamInfo,
        val players: List<PlayerInfo>,
    ) : PlayerResponse {
        data class TeamInfo(
            val id: Long,
            val name: String,
            val logo: String,
        )

        data class PlayerInfo(
            val id: Long,
            val name: String,
            val age: Int,
            val number: Int?,
            val position: String,
            val photo: String,
        )
    }
}
