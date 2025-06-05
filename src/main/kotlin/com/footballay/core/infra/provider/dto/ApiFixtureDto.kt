package com.footballay.core.infra.provider.dto

import java.time.LocalDateTime

/**
 * ApiSports로부터 넘어오는 경기 정보 중 최소한으로 필요한 필드
 */
data class ApiFixtureDto(
    val apiId: Long,
    val referee: String?,
    val timezone: String?,
    val date: LocalDateTime,
    val timestamp: Long,
    
    // Venue
    val venueId: Long?,
    val venueName: String?,
    val venueCity: String?,
    
    // Status
    val statusLong: String,
    val statusShort: String,
    val statusElapsed: Int?,
    
    // League
    val leagueId: Long,
    val leagueName: String,
    val leagueCountry: String?,
    val leagueLogo: String?,
    val leagueFlag: String?,
    val season: Int,
    val round: String,
    
    // Teams
    val homeTeamId: Long,
    val homeTeamName: String,
    val homeTeamLogo: String?,
    val homeTeamWinner: Boolean?,
    
    val awayTeamId: Long,
    val awayTeamName: String,
    val awayTeamLogo: String?,
    val awayTeamWinner: Boolean?,
    
    // Goals
    val goalsHome: Int?,
    val goalsAway: Int?,
    
    // Score
    val halftimeHome: Int?,
    val halftimeAway: Int?,
    val fulltimeHome: Int?,
    val fulltimeAway: Int?,
    val extratimeHome: Int?,
    val extratimeAway: Int?,
    val penaltyHome: Int?,
    val penaltyAway: Int?
) 