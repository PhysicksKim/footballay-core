package com.footballay.core.web.admin.core.controller

import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.matchcollect.MatchCollectStatus
import com.footballay.core.web.admin.core.dto.AdminMatchCollectExecutionResponse
import com.footballay.core.web.admin.core.dto.MatchCollectLeagueResponse
import com.footballay.core.web.admin.core.dto.MatchCollectLeagueStatePageResponse
import com.footballay.core.web.admin.core.dto.MatchCollectStatePageResponse
import com.footballay.core.web.admin.core.service.AdminLeagueMatchCollectWebService
import com.footballay.core.web.admin.core.service.AdminMatchCollectQueryWebService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminMatchCollectControllerTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockitoBean
    private lateinit var adminMatchCollectQueryWebService: AdminMatchCollectQueryWebService

    @MockitoBean
    private lateinit var adminLeagueMatchCollectWebService: AdminLeagueMatchCollectWebService

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("global state 조회는 incompleteOnly와 status query parameter를 webservice에 전달한다")
    fun getStates_withIncompleteOnlyAndStatus_delegatesToWebService() {
        given(
            adminMatchCollectQueryWebService.findStates(
                leagueUid = eq(null),
                fixtureUid = eq(null),
                status = eq(MatchCollectStatus.FAIL_END),
                incompleteOnly = eq(true),
                page = eq(0),
                size = eq(50),
            ),
        ).willReturn(emptyStatePage())

        mockMvc
            .get("/api/v1/admin/match-collect/states") {
                param("status", "FAIL_END")
                param("incompleteOnly", "true")
                param("page", "0")
                param("size", "50")
            }.andExpect {
                status { isOk() }
                jsonPath("$.content") { isArray() }
            }

        verify(adminMatchCollectQueryWebService).findStates(
            leagueUid = eq(null),
            fixtureUid = eq(null),
            status = eq(MatchCollectStatus.FAIL_END),
            incompleteOnly = eq(true),
            page = eq(0),
            size = eq(50),
        )
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("제거된 states incomplete path는 정상 endpoint로 처리되지 않는다")
    fun removedIncompletePath_isRejected() {
        mockMvc
            .get("/api/v1/admin/match-collect/states/incomplete")
            .andExpect {
                status { isBadRequest() }
            }
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("league state 조회는 incompleteOnly query parameter를 webservice에 전달한다")
    fun getLeagueStates_withIncompleteOnly_delegatesToWebService() {
        given(
            adminMatchCollectQueryWebService.findLeagueStates(
                leagueUid = eq("league-1"),
                status = eq(null),
                incompleteOnly = eq(true),
                page = eq(0),
                size = eq(50),
            ),
        ).willReturn(emptyLeagueStatePage())

        mockMvc
            .get("/api/v1/admin/leagues/{leagueCoreUid}/match-collect/states", "league-1") {
                param("incompleteOnly", "true")
            }.andExpect {
                status { isOk() }
                jsonPath("$.league.leagueUid") { value("league-1") }
                jsonPath("$.content") { isArray() }
            }

        verify(adminMatchCollectQueryWebService).findLeagueStates(
            leagueUid = eq("league-1"),
            status = eq(null),
            incompleteOnly = eq(true),
            page = eq(0),
            size = eq(50),
        )
    }

    @WithMockUser(roles = ["ADMIN"])
    @Test
    @DisplayName("fixtureUid 기반 단건 match collect endpoint는 webservice로 위임한다")
    fun collectMatchFixture_delegatesToWebService() {
        val fixtureUid = "fixture-1"
        given(adminLeagueMatchCollectWebService.collectMatchByFixtureUid(fixtureUid))
            .willReturn(
                DomainResult.Success(
                    AdminMatchCollectExecutionResponse(
                        resultType = "COLLECTED",
                        fixtureUid = fixtureUid,
                        status = MatchCollectStatus.SUCCESS,
                        collectedAt = Instant.parse("2026-06-25T00:00:00Z"),
                        reason = null,
                        message = null,
                        syncResult = null,
                    ),
                ),
            )

        mockMvc
            .post("/api/v1/admin/leagues/match-collect/fixtures/{fixtureUid}", fixtureUid)
            .andExpect {
                status { isOk() }
                jsonPath("$.resultType") { value("COLLECTED") }
                jsonPath("$.fixtureUid") { value(fixtureUid) }
                jsonPath("$.status") { value("SUCCESS") }
            }

        verify(adminLeagueMatchCollectWebService).collectMatchByFixtureUid(fixtureUid)
    }

    private fun emptyStatePage() =
        MatchCollectStatePageResponse(
            content = emptyList(),
            page = 0,
            size = 50,
            totalElements = 0,
            totalPages = 0,
        )

    private fun emptyLeagueStatePage() =
        MatchCollectLeagueStatePageResponse(
            league =
                MatchCollectLeagueResponse(
                    leagueUid = "league-1",
                    name = "League",
                    available = true,
                    matchCollect = com.footballay.core.domain.league.MatchCollect.LIVE,
                ),
            content = emptyList(),
            page = 0,
            size = 50,
            totalElements = 0,
            totalPages = 0,
        )
}
