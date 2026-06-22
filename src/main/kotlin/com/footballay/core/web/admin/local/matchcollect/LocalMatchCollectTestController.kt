package com.footballay.core.web.admin.local.matchcollect

import com.footballay.core.infra.matchcollect.FinishedMatchCollectBatchResult
import com.footballay.core.infra.scheduler.ReconcileResult
import jakarta.validation.Valid
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@Profile("local")
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/local/match-collect")
class LocalMatchCollectTestController(
    private val localMatchCollectTestService: LocalMatchCollectTestService,
) {
    @GetMapping("/diagnostics")
    fun diagnostics(
        @RequestParam(required = false) leagueUid: String?,
        @RequestParam(required = false) fixtureUid: String?,
        @RequestParam(required = false, defaultValue = "true") includeQuartz: Boolean,
        @RequestParam(required = false, defaultValue = "true") includeState: Boolean,
    ): ResponseEntity<LocalMatchCollectDiagnosticsResponse> =
        ResponseEntity.ok(
            localMatchCollectTestService.diagnostics(
                leagueUid = leagueUid,
                fixtureUid = fixtureUid,
                includeQuartz = includeQuartz,
                includeState = includeState,
            ),
        )

    @PostMapping("/leagues/{leagueCoreUid}/reconcile-live-jobs")
    fun reconcileLiveJobs(
        @PathVariable leagueCoreUid: String,
    ): ResponseEntity<ReconcileResult> =
        ResponseEntity.ok(localMatchCollectTestService.reconcileLiveJobs(leagueCoreUid))

    @PostMapping("/finished-scanner/run-once")
    fun runFinishedScannerOnce(
        @RequestBody(required = false) @Valid request: LocalFinishedScannerRunRequest?,
    ): ResponseEntity<FinishedMatchCollectBatchResult> =
        ResponseEntity.ok(
            localMatchCollectTestService.runFinishedScannerOnce(
                now = request?.now,
                batchSize = request?.batchSize ?: 100,
            ),
        )

    @PostMapping("/fixtures/{fixtureUid}/collect-finished")
    fun collectFinished(
        @PathVariable fixtureUid: String,
        @RequestBody(required = false) @Valid request: LocalMatchCollectNowRequest?,
    ): ResponseEntity<LocalMatchCollectExecutionResponse> =
        ResponseEntity.ok(localMatchCollectTestService.collectFinished(fixtureUid, request?.now))

    @PostMapping("/fixtures/{fixtureUid}/collect-pre")
    fun collectPre(
        @PathVariable fixtureUid: String,
        @RequestBody(required = false) @Valid request: LocalMatchCollectNowRequest?,
    ): ResponseEntity<LocalMatchCollectExecutionResponse> =
        ResponseEntity.ok(localMatchCollectTestService.collectPre(fixtureUid, request?.now))

    @PostMapping("/fixtures/{fixtureUid}/collect-live")
    fun collectLive(
        @PathVariable fixtureUid: String,
        @RequestBody(required = false) @Valid request: LocalMatchCollectNowRequest?,
    ): ResponseEntity<LocalMatchCollectExecutionResponse> =
        ResponseEntity.ok(localMatchCollectTestService.collectLive(fixtureUid, request?.now))

    @PostMapping("/fixtures/{fixtureUid}/collect-post")
    fun collectPost(
        @PathVariable fixtureUid: String,
        @RequestBody(required = false) @Valid request: LocalMatchCollectNowRequest?,
    ): ResponseEntity<LocalMatchCollectExecutionResponse> =
        ResponseEntity.ok(localMatchCollectTestService.collectPost(fixtureUid, request?.now))

    @PostMapping("/quartz/fire")
    fun fireQuartzJob(
        @RequestBody @Valid request: LocalQuartzFireRequest,
    ): ResponseEntity<LocalQuartzFireResponse> =
        ResponseEntity.ok(localMatchCollectTestService.fireQuartzJob(request))
}
