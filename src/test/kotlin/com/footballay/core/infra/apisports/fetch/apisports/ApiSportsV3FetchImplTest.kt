package com.footballay.core.infra.apisports.fetch.apisports

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.infra.apisports.APISPORTS_FIXTURE_ID_MAU_FUL
import com.footballay.core.logger
import com.footballay.core.prettyPrintJson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@SpringBootTest
@ActiveProfiles("dev","devrealapi")
class ApiSportsV3FetchImplTest {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var apiSportsV3FetchImpl: ApiSportsV3FetchImpl

    private val log = logger()

    @Test
    fun getLeaguesCurrent() {
        log.info("Testing getLeagues")
        val requestLeaguesCurrent = apiSportsV3FetchImpl.fetchLeaguesCurrent()
        log.info("Response: ${prettyPrintJson(objectMapper, requestLeaguesCurrent)}")
        assert(requestLeaguesCurrent.response.isNotEmpty()) { "Response leagues current is empty" }
    }

    @Test
    fun getFixtureSingle() {
        log.info("Testing getFixtureSingle")
        val requestLeaguesCurrent = apiSportsV3FetchImpl.fetchFixtureSingle(APISPORTS_FIXTURE_ID_MAU_FUL)
        log.info("Response: ${prettyPrintJson(objectMapper, requestLeaguesCurrent)}")
        assert(requestLeaguesCurrent.response.isNotEmpty()) { "Response fixture single is empty" }
    }

}