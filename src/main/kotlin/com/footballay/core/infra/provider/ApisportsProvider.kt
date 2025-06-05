package com.footballay.core.infra.provider

import com.footballay.core.infra.provider.dto.*
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * ApiSports 기반 Provider 구현체
 */
@Component
class ApisportsProvider(
    private val objectMapper: ObjectMapper,
    @Value("\${rapidapi.football.key}") private val apiKey: String
) : FootballDataProvider {

    private val client = OkHttpClient()
    private val baseUrl = "https://v3.football.api-sports.io"

    override fun fetchAllCurrentLeagues(): List<ApiLeagueDto> {
        val url = "$baseUrl/leagues?current=true"
        val request = buildRequest(url)

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("ApiSports: 리그 조회 실패 ${response.code}")
            }
            val body = response.body?.string()
                ?: throw RuntimeException("ApiSports: Body is null")

            val root = objectMapper.readTree(body)
            val list = mutableListOf<ApiLeagueDto>()
            
            root["response"].forEach { node ->
                val leagueNode = node["league"]
                val seasonsArray = node["seasons"]
                val curSeason = seasonsArray.firstOrNull { it["current"].asBoolean() }?.get("year")?.asInt()
                    ?: seasonsArray.lastOrNull()?.get("year")?.asInt() ?: 2024

                list.add(
                    ApiLeagueDto(
                        apiId = leagueNode["id"].asLong(),
                        name = leagueNode["name"].asText(),
                        countryName = node["country"]["name"].asText(),
                        countryCode = node["country"]["code"].asText(),
                        logo = leagueNode["logo"].asText(),
                        currentSeason = curSeason
                    )
                )
            }
            return list
        }
    }

    override fun fetchLeagueById(leagueId: Long): ApiLeagueDto {
        val url = "$baseUrl/leagues?id=$leagueId&current=true"
        val request = buildRequest(url)

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("ApiSports: 리그 상세 조회 실패 ${response.code}")
            }
            val body = response.body?.string()
                ?: throw RuntimeException("ApiSports: Body is null for leagueId=$leagueId")

            val root = objectMapper.readTree(body)
            val node = root["response"].firstOrNull()
                ?: throw RuntimeException("ApiSports: 리그 정보를 찾을 수 없습니다. leagueId=$leagueId")
            
            val leagueNode = node["league"]
            val seasonsArray = node["seasons"]
            val curSeason = seasonsArray.firstOrNull { it["current"].asBoolean() }?.get("year")?.asInt()
                ?: seasonsArray.lastOrNull()?.get("year")?.asInt() ?: 2024

            return ApiLeagueDto(
                apiId = leagueNode["id"].asLong(),
                name = leagueNode["name"].asText(),
                countryName = node["country"]["name"].asText(),
                countryCode = node["country"]["code"].asText(),
                logo = leagueNode["logo"].asText(),
                currentSeason = curSeason
            )
        }
    }

    override fun fetchTeamsOfLeague(leagueId: Long, season: Int): List<ApiTeamDto> {
        val url = "$baseUrl/teams?league=$leagueId&season=$season"
        val request = buildRequest(url)

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("ApiSports: 팀 조회 실패 ${response.code}")
            }
            val body = response.body?.string()
                ?: throw RuntimeException("ApiSports: Body is null")

            val root = objectMapper.readTree(body)
            val list = mutableListOf<ApiTeamDto>()
            
            root["response"].forEach { node ->
                val teamNode = node["team"]
                val venueNode = node["venue"]

                list.add(
                    ApiTeamDto(
                        apiId = teamNode["id"].asLong(),
                        name = teamNode["name"].asText(),
                        code = teamNode["code"]?.asText(),
                        country = teamNode["country"]?.asText(),
                        founded = teamNode["founded"]?.asInt(),
                        national = teamNode["national"]?.asBoolean() ?: false,
                        logo = teamNode["logo"]?.asText(),
                        venueId = venueNode["id"]?.asLong(),
                        venueName = venueNode["name"]?.asText(),
                        venueAddress = venueNode["address"]?.asText(),
                        venueCity = venueNode["city"]?.asText(),
                        venueCapacity = venueNode["capacity"]?.asInt()
                    )
                )
            }
            return list
        }
    }

    override fun fetchPlayersOfTeam(teamId: Long): List<ApiPlayerDto> {
        val url = "$baseUrl/players?team=$teamId&season=2024"
        val request = buildRequest(url)

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("ApiSports: 선수 조회 실패 ${response.code}")
            }
            val body = response.body?.string()
                ?: throw RuntimeException("ApiSports: Body is null")

            val root = objectMapper.readTree(body)
            val list = mutableListOf<ApiPlayerDto>()
            
            root["response"].forEach { node ->
                val playerNode = node["player"]
                val statisticsArray = node["statistics"]
                val firstStat = statisticsArray.firstOrNull()

                list.add(
                    ApiPlayerDto(
                        apiId = playerNode["id"].asLong(),
                        name = playerNode["name"].asText(),
                        firstname = playerNode["firstname"]?.asText(),
                        lastname = playerNode["lastname"]?.asText(),
                        age = playerNode["age"]?.asInt(),
                        birthDate = playerNode["birth"]["date"]?.asText()?.let { 
                            LocalDateTime.parse(it + "T00:00:00").toLocalDate()
                        },
                        birthPlace = playerNode["birth"]["place"]?.asText(),
                        birthCountry = playerNode["birth"]["country"]?.asText(),
                        nationality = playerNode["nationality"]?.asText(),
                        height = playerNode["height"]?.asText(),
                        weight = playerNode["weight"]?.asText(),
                        injured = playerNode["injured"]?.asBoolean() ?: false,
                        photo = playerNode["photo"]?.asText(),
                        position = firstStat?.get("games")?.get("position")?.asText(),
                        number = firstStat?.get("games")?.get("number")?.asInt()
                    )
                )
            }
            return list
        }
    }

    override fun fetchFixturesOfLeagueSeason(leagueId: Long, season: Int): List<ApiFixtureDto> {
        val url = "$baseUrl/fixtures?league=$leagueId&season=$season"
        val request = buildRequest(url)

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("ApiSports: 경기 조회 실패 ${response.code}")
            }
            val body = response.body?.string()
                ?: throw RuntimeException("ApiSports: Body is null")

            val root = objectMapper.readTree(body)
            val list = mutableListOf<ApiFixtureDto>()
            
            root["response"].forEach { node ->
                val fixtureNode = node["fixture"]
                val leagueNode = node["league"]
                val teamsNode = node["teams"]
                val goalsNode = node["goals"]
                val scoreNode = node["score"]

                list.add(
                    ApiFixtureDto(
                        apiId = fixtureNode["id"].asLong(),
                        referee = fixtureNode["referee"]?.asText(),
                        timezone = fixtureNode["timezone"]?.asText(),
                        date = LocalDateTime.parse(fixtureNode["date"].asText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        timestamp = fixtureNode["timestamp"].asLong(),
                        
                        venueId = fixtureNode["venue"]["id"]?.asLong(),
                        venueName = fixtureNode["venue"]["name"]?.asText(),
                        venueCity = fixtureNode["venue"]["city"]?.asText(),
                        
                        statusLong = fixtureNode["status"]["long"].asText(),
                        statusShort = fixtureNode["status"]["short"].asText(),
                        statusElapsed = fixtureNode["status"]["elapsed"]?.asInt(),
                        
                        leagueId = leagueNode["id"].asLong(),
                        leagueName = leagueNode["name"].asText(),
                        leagueCountry = leagueNode["country"]?.asText(),
                        leagueLogo = leagueNode["logo"]?.asText(),
                        leagueFlag = leagueNode["flag"]?.asText(),
                        season = leagueNode["season"].asInt(),
                        round = leagueNode["round"].asText(),
                        
                        homeTeamId = teamsNode["home"]["id"].asLong(),
                        homeTeamName = teamsNode["home"]["name"].asText(),
                        homeTeamLogo = teamsNode["home"]["logo"]?.asText(),
                        homeTeamWinner = teamsNode["home"]["winner"]?.asBoolean(),
                        
                        awayTeamId = teamsNode["away"]["id"].asLong(),
                        awayTeamName = teamsNode["away"]["name"].asText(),
                        awayTeamLogo = teamsNode["away"]["logo"]?.asText(),
                        awayTeamWinner = teamsNode["away"]["winner"]?.asBoolean(),
                        
                        goalsHome = goalsNode["home"]?.asInt(),
                        goalsAway = goalsNode["away"]?.asInt(),
                        
                        halftimeHome = scoreNode["halftime"]["home"]?.asInt(),
                        halftimeAway = scoreNode["halftime"]["away"]?.asInt(),
                        fulltimeHome = scoreNode["fulltime"]["home"]?.asInt(),
                        fulltimeAway = scoreNode["fulltime"]["away"]?.asInt(),
                        extratimeHome = scoreNode["extratime"]["home"]?.asInt(),
                        extratimeAway = scoreNode["extratime"]["away"]?.asInt(),
                        penaltyHome = scoreNode["penalty"]["home"]?.asInt(),
                        penaltyAway = scoreNode["penalty"]["away"]?.asInt()
                    )
                )
            }
            return list
        }
    }

    private fun buildRequest(url: String): Request {
        return Request.Builder()
            .url(url)
            .get()
            .addHeader("X-RapidAPI-Host", "v3.football.api-sports.io")
            .addHeader("X-RapidAPI-Key", apiKey)
            .build()
    }
} 