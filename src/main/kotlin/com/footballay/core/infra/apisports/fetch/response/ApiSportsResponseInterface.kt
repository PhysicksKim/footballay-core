package com.footballay.core.infra.apisports.fetch.response

/**
 * ApiSports Response 맵핑 객체 namespace를 분리하기 위한 인터페이스입니다.
 */
sealed interface ApiSportsResponse

sealed interface FixtureResponse : ApiSportsResponse
sealed interface LeagueResponse : ApiSportsResponse
sealed interface TeamResponse : ApiSportsResponse
sealed interface PlayerResponse : ApiSportsResponse
sealed interface StatusResponse : ApiSportsResponse
