package com.footballay.core.infra.apisports.fetch.apisports

import com.footballay.core.infra.apisports.fetch.response.*

interface ApiSportsV3FetchService {

    fun fetchStatus() : ApiSportsV3LiveStatusEnvelope<ApiSportsStatus>

    fun fetchLeaguesCurrent() : ApiSportsV3Envelope<ApiSportsLeague.Current>

    fun fetchTeamsOfLeague(
        leagueApiId: Long,
        season: Int
    ) : ApiSportsV3Envelope<ApiSportsTeam.OfLeague>

    fun fetchSquadOfTeam(
        teamApiId: Long,
    ) : ApiSportsV3Envelope<ApiSportsPlayer.OfTeam>

    fun fetchFixturesOfLeague(
        leagueApiId: Long,
        season: String,
    ) : ApiSportsV3Envelope<ApiSportsFixture.OfLeague>

    fun fetchFixtureSingle(
        fixtureApiId: Long,
    ) : ApiSportsV3Envelope<ApiSportsFixture.Single>

}