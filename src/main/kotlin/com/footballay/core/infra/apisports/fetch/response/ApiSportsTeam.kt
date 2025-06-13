package com.footballay.core.infra.apisports.fetch.response

object ApiSportsTeam {

    data class OfLeague(
        val team: TeamDetail,
        val venue: VenueDetail
    ) : TeamResponse {
        data class TeamDetail(
            val id: Int,
            val name: String,
            val code: String,
            val country: String,
            val founded: Int,
            val national: Boolean,
            val logo: String
        )
        data class VenueDetail(
            val id: Int,
            val name: String,
            val address: String,
            val city: String,
            val capacity: Int,
            val surface: String,
            val image: String
        )
    }
}