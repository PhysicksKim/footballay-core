package com.footballay.core.web.admin.core.controller

import com.footballay.core.common.result.toResponseEntity
import com.footballay.core.web.admin.apisports.dto.AvailabilityToggleRequest
import com.footballay.core.web.admin.apisports.dto.ToggleAvailableResponse
import com.footballay.core.web.admin.core.dto.MatchCollectUpdateRequest
import com.footballay.core.web.admin.core.dto.MatchCollectUpdateResponse
import com.footballay.core.web.admin.core.service.AdminCoreAvailableWebService
import com.footballay.core.web.admin.core.service.AdminLeagueMatchCollectWebService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin")
class AdminCoreAvailableController(
    private val adminCoreAvailableWebService: AdminCoreAvailableWebService,
    private val adminLeagueMatchCollectWebService: AdminLeagueMatchCollectWebService,
) {
    @PutMapping("/leagues/{leagueCoreUid}/available")
    fun setLeagueAvailable(
        @PathVariable leagueCoreUid: String,
        @RequestBody @Valid request: AvailabilityToggleRequest,
    ): ResponseEntity<ToggleAvailableResponse> =
        adminCoreAvailableWebService
            .setLeagueAvailable(leagueCoreUid, request.available)
            .toResponseEntity()

    @PutMapping("/leagues/{leagueCoreUid}/match-collect")
    fun setLeagueMatchCollect(
        @PathVariable leagueCoreUid: String,
        @RequestBody @Valid request: MatchCollectUpdateRequest,
    ): ResponseEntity<MatchCollectUpdateResponse> =
        adminLeagueMatchCollectWebService
            .setLeagueMatchCollect(leagueCoreUid, request.matchCollect)
            .toResponseEntity()

    @PutMapping("/fixtures/{fixtureCoreUid}/available")
    fun setFixtureAvailable(
        @PathVariable fixtureCoreUid: String,
        @RequestBody @Valid request: AvailabilityToggleRequest,
    ): ResponseEntity<ToggleAvailableResponse> =
        adminCoreAvailableWebService
            .setFixtureAvailable(fixtureCoreUid, request.available)
            .toResponseEntity()
}
