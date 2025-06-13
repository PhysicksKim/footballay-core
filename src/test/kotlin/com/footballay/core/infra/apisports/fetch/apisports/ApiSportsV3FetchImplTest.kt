package com.footballay.core.infra.apisports.fetch.apisports

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.infra.apisports.APISPORTS_FIXTURE_ID_MAU_FUL
import com.footballay.core.logger
import com.footballay.core.prettyPrintJson
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("dev", "devrealapi")
class ApiSportsV3FetchImplTest {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var apiSportsV3FetchImpl: ApiSportsV3FetchImpl

    private val log = logger()

    @Test
    fun getStatus() {
        log.info("Testing getStatus")
        val requestStatus = apiSportsV3FetchImpl.fetchStatus()
        log.info("Response: ${prettyPrintJson(objectMapper, requestStatus)}")
        assertThat(requestStatus)
            .withFailMessage("Response status is null")
            .isNotNull()
    }

    @Test
    fun getLeaguesCurrent() {
        log.info("Testing getLeagues")
        val requestLeaguesCurrent = apiSportsV3FetchImpl.fetchLeaguesCurrent()
        log.info("Response: ${prettyPrintJson(objectMapper, requestLeaguesCurrent)}")
        assertThat(requestLeaguesCurrent.response)
            .withFailMessage("Response leagues current is empty")
            .isNotEmpty()
    }

    @Test
    fun getTeamsOfLeague() {
        log.info("Testing getTeamsOfLeague")
        val requestTeamsOfLeague = apiSportsV3FetchImpl.fetchTeamsOfLeague(leagueApiId = 39, season = 2023)
        log.info("Response: ${prettyPrintJson(objectMapper, requestTeamsOfLeague)}")
        assertThat(requestTeamsOfLeague.response)
            .withFailMessage("Response teams of league is empty")
            .isNotEmpty()
    }

    @Test
    fun getSquadOfTeam() {
        log.info("Testing getSquadOfTeam")
        val requestSquadOfTeam = apiSportsV3FetchImpl.fetchSquadOfTeam(teamApiId = 40)
        log.info("Response: ${prettyPrintJson(objectMapper, requestSquadOfTeam)}")
        assertThat(requestSquadOfTeam.response)
            .withFailMessage("Response squad of team is empty")
            .isNotEmpty()
    }

    @Test
    fun getFixturesOfLeague() {
        log.info("Testing getFixturesOfLeague")
        val requestFixturesOfLeague = apiSportsV3FetchImpl.fetchFixturesOfLeague(leagueApiId = 39, season = "2023")
        log.info("Response: ${prettyPrintJson(objectMapper, requestFixturesOfLeague)}")
        assertThat(requestFixturesOfLeague.response)
            .withFailMessage("Response fixtures of league is empty")
            .isNotEmpty()
    }

    @Test
    fun getFixtureSingle() {
        log.info("Testing getFixtureSingle")
        val requestFixtureSingle = apiSportsV3FetchImpl.fetchFixtureSingle(APISPORTS_FIXTURE_ID_MAU_FUL)
        log.info("Response: ${prettyPrintJson(objectMapper, requestFixtureSingle)}")
        assertThat(requestFixtureSingle.response)
            .withFailMessage("Response fixture single is empty")
            .isNotEmpty()
    }
}
