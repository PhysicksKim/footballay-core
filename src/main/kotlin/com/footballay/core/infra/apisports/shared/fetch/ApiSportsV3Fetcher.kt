package com.footballay.core.infra.apisports.shared.fetch

import com.footballay.core.infra.apisports.shared.fetch.response.ApiSportsFixture
import com.footballay.core.infra.apisports.shared.fetch.response.ApiSportsLeague
import com.footballay.core.infra.apisports.shared.fetch.response.ApiSportsPlayer
import com.footballay.core.infra.apisports.shared.fetch.response.ApiSportsAccountStatus
import com.footballay.core.infra.apisports.shared.fetch.response.ApiSportsTeam
import com.footballay.core.infra.apisports.shared.fetch.response.ApiSportsV3Envelope
import com.footballay.core.infra.apisports.shared.fetch.response.ApiSportsV3LiveStatusEnvelope

interface ApiSportsV3Fetcher {

    fun fetchStatus() : ApiSportsV3LiveStatusEnvelope<ApiSportsAccountStatus>

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
        season: Int,
    ) : ApiSportsV3Envelope<ApiSportsFixture.OfLeague>

    fun fetchFixtureSingle(
        fixtureApiId: Long,
    ) : ApiSportsV3Envelope<ApiSportsFixture.Single>

}