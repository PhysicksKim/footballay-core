package com.footballay.core.infra.apisports.fetch.apisports

import com.footballay.core.infra.apisports.fetch.response.ApiSportsV3Response
import com.footballay.core.infra.apisports.fetch.response.fixtures.ApiSportsFixtureSingle
import com.footballay.core.infra.apisports.fetch.response.leagues.ApiSportsLeaguesCurrent

interface ApiSportsV3FetchService {

    fun fetchLeaguesCurrent() : ApiSportsV3Response<ApiSportsLeaguesCurrent>

    fun fetchFixtureSingle(
        fixtureApiId: Long,
    ) : ApiSportsV3Response<ApiSportsFixtureSingle>

}