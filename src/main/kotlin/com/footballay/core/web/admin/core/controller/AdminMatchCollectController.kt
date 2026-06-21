package com.footballay.core.web.admin.core.controller

import com.footballay.core.common.result.toResponseEntity
import com.footballay.core.domain.matchcollect.MatchCollectStatus
import com.footballay.core.web.admin.core.dto.MatchCollectStatePageResponse
import com.footballay.core.web.admin.core.dto.MatchCollectUpdateRequest
import com.footballay.core.web.admin.core.dto.MatchCollectUpdateResponse
import com.footballay.core.web.admin.core.service.AdminLeagueMatchCollectWebService
import com.footballay.core.web.admin.core.service.AdminMatchCollectQueryWebService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin")
class AdminMatchCollectController(
    private val adminMatchCollectQueryWebService: AdminMatchCollectQueryWebService,
    private val adminLeagueMatchCollectWebService: AdminLeagueMatchCollectWebService,
) {
    @PutMapping("/leagues/{leagueCoreUid}/match-collect")
    fun setLeagueMatchCollect(
        @PathVariable leagueCoreUid: String,
        @RequestBody @Valid request: MatchCollectUpdateRequest,
    ): ResponseEntity<MatchCollectUpdateResponse> =
        adminLeagueMatchCollectWebService
            .setLeagueMatchCollect(leagueCoreUid, request.matchCollect)
            .toResponseEntity()

    @GetMapping("/leagues/{leagueCoreUid}/match-collect/states")
    fun getLeagueStates(
        @PathVariable leagueCoreUid: String,
        @RequestParam(required = false) fixtureUid: String?,
        @RequestParam(required = false) status: MatchCollectStatus?,
        @RequestParam(required = false, defaultValue = "false") incompleteOnly: Boolean,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "50") size: Int,
    ): ResponseEntity<MatchCollectStatePageResponse> =
        ResponseEntity.ok(
            adminMatchCollectQueryWebService.findStates(
                leagueUid = leagueCoreUid,
                fixtureUid = fixtureUid,
                status = status,
                incompleteOnly = incompleteOnly,
                page = page,
                size = size,
            ),
        )

    @GetMapping("/leagues/{leagueCoreUid}/match-collect/states/incomplete")
    fun getLeagueIncompleteStates(
        @PathVariable leagueCoreUid: String,
        @RequestParam(required = false) fixtureUid: String?,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "50") size: Int,
    ): ResponseEntity<MatchCollectStatePageResponse> =
        ResponseEntity.ok(
            adminMatchCollectQueryWebService.findIncompleteStates(
                leagueUid = leagueCoreUid,
                fixtureUid = fixtureUid,
                page = page,
                size = size,
            ),
        )

    @GetMapping("/match-collect/states")
    fun getStates(
        @RequestParam(required = false) leagueUid: String?,
        @RequestParam(required = false) fixtureUid: String?,
        @RequestParam(required = false) status: MatchCollectStatus?,
        @RequestParam(required = false, defaultValue = "false") incompleteOnly: Boolean,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "50") size: Int,
    ): ResponseEntity<MatchCollectStatePageResponse> =
        ResponseEntity.ok(
            adminMatchCollectQueryWebService.findStates(
                leagueUid = leagueUid,
                fixtureUid = fixtureUid,
                status = status,
                incompleteOnly = incompleteOnly,
                page = page,
                size = size,
            ),
        )

    @GetMapping("/match-collect/states/incomplete")
    fun getIncompleteStates(
        @RequestParam(required = false) leagueUid: String?,
        @RequestParam(required = false) fixtureUid: String?,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "50") size: Int,
    ): ResponseEntity<MatchCollectStatePageResponse> =
        ResponseEntity.ok(
            adminMatchCollectQueryWebService.findIncompleteStates(
                leagueUid = leagueUid,
                fixtureUid = fixtureUid,
                page = page,
                size = size,
            ),
        )
}
