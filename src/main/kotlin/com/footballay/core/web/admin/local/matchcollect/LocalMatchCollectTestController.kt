package com.footballay.core.web.admin.local.matchcollect

import com.footballay.core.infra.matchcollect.FinishedMatchCollectBatchResult
import com.footballay.core.infra.scheduler.ReconcileResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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

@Tag(
    name = "Local - MatchCollect Test",
    description = "local profile 에서만 열리는 MatchCollect 로컬 디버깅 API. 운영 admin API 가 아닙니다.",
)
@SecurityRequirement(name = "cookieAuth")
@Validated
@RestController
@Profile("local")
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/local/match-collect")
class LocalMatchCollectTestController(
    private val localMatchCollectTestService: LocalMatchCollectTestService,
) {
    companion object {
        private const val OP_DIAGNOSTICS =
            "특정 league 또는 fixture 의 MatchCollect 관련 상태를 한 번에 조회합니다. " +
                "local profile 전용이며 league/fixture/state/quartz 상태를 로컬 smoke test 목적으로 확인합니다."

        private const val OP_RECONCILE_LIVE =
            "DB truth 기준으로 특정 리그의 MatchCollect LIVE Quartz job reconcile 을 즉시 실행합니다. " +
                "schedule update 를 기다리지 않고 로컬에서 job 등록/삭제 결과를 확인하기 위한 API 입니다."

        private const val OP_FINISHED_SCANNER =
            "FINISHED scanner batch 를 Quartz cron 없이 1회 실행합니다. " +
                "실제 ApiSports request 가 발생할 수 있으므로 local smoke test 용도로만 사용합니다."

        private const val OP_COLLECT_FINISHED =
            "특정 fixture 에 대해 scanner 용 FINISHED collect 경로를 직접 호출합니다. " +
                "checkpoint/due 조건을 존중하며 local profile 에서만 사용할 수 있습니다."

        private const val OP_COLLECT_PRE =
            "특정 fixture 에 대해 MatchCollect PRE phase executor 를 직접 호출합니다. local profile 전용입니다."

        private const val OP_COLLECT_LIVE =
            "특정 fixture 에 대해 MatchCollect LIVE phase executor 를 직접 호출합니다. local profile 전용입니다."

        private const val OP_COLLECT_POST =
            "특정 fixture 에 대해 MatchCollect POST phase executor 를 직접 호출합니다. local profile 전용입니다."

        private const val OP_QUARTZ_FIRE =
            "등록된 Quartz job 을 즉시 trigger 요청합니다. 실행 완료가 아니라 trigger 요청 성공 여부만 반환합니다."
    }

    @Operation(summary = "Local MatchCollect diagnostics", description = OP_DIAGNOSTICS)
    @ApiResponse(
        responseCode = "200",
        description = "Local MatchCollect diagnostics snapshot",
        content = [Content(schema = Schema(implementation = LocalMatchCollectDiagnosticsResponse::class))],
    )
    @GetMapping("/diagnostics")
    fun diagnostics(
        @Parameter(description = "LeagueCore UID", example = "league_core_abcd1234")
        @RequestParam(required = false) leagueUid: String?,
        @Parameter(description = "FixtureCore UID", example = "fixture_core_abcd1234")
        @RequestParam(required = false) fixtureUid: String?,
        @Parameter(description = "Quartz job snapshot 포함 여부", example = "true")
        @RequestParam(required = false, defaultValue = "true") includeQuartz: Boolean,
        @Parameter(description = "MatchCollect state 포함 여부", example = "true")
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

    @Operation(summary = "Local LIVE job reconcile 실행", description = OP_RECONCILE_LIVE)
    @ApiResponse(responseCode = "200", description = "ReconcileResult")
    @PostMapping("/leagues/{leagueCoreUid}/reconcile-live-jobs")
    fun reconcileLiveJobs(
        @Parameter(description = "LeagueCore UID", example = "league_core_abcd1234")
        @PathVariable leagueCoreUid: String,
    ): ResponseEntity<ReconcileResult> =
        ResponseEntity.ok(localMatchCollectTestService.reconcileLiveJobs(leagueCoreUid))

    @Operation(summary = "Local FINISHED scanner 1회 실행", description = OP_FINISHED_SCANNER)
    @ApiResponse(responseCode = "200", description = "FINISHED scanner batch result")
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

    @Operation(summary = "Local fixture FINISHED collect 직접 실행", description = OP_COLLECT_FINISHED)
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Local MatchCollect execution result",
            content = [Content(schema = Schema(implementation = LocalMatchCollectExecutionResponse::class))],
        ),
    )
    @PostMapping("/fixtures/{fixtureUid}/collect-finished")
    fun collectFinished(
        @Parameter(description = "FixtureCore UID", example = "fixture_core_abcd1234")
        @PathVariable fixtureUid: String,
        @RequestBody(required = false) @Valid request: LocalMatchCollectNowRequest?,
    ): ResponseEntity<LocalMatchCollectExecutionResponse> =
        ResponseEntity.ok(localMatchCollectTestService.collectFinished(fixtureUid, request?.now))

    @Operation(summary = "Local fixture PRE collect 직접 실행", description = OP_COLLECT_PRE)
    @ApiResponse(
        responseCode = "200",
        description = "Local MatchCollect execution result",
        content = [Content(schema = Schema(implementation = LocalMatchCollectExecutionResponse::class))],
    )
    @PostMapping("/fixtures/{fixtureUid}/collect-pre")
    fun collectPre(
        @Parameter(description = "FixtureCore UID", example = "fixture_core_abcd1234")
        @PathVariable fixtureUid: String,
        @RequestBody(required = false) @Valid request: LocalMatchCollectNowRequest?,
    ): ResponseEntity<LocalMatchCollectExecutionResponse> =
        ResponseEntity.ok(localMatchCollectTestService.collectPre(fixtureUid, request?.now))

    @Operation(summary = "Local fixture LIVE collect 직접 실행", description = OP_COLLECT_LIVE)
    @ApiResponse(
        responseCode = "200",
        description = "Local MatchCollect execution result",
        content = [Content(schema = Schema(implementation = LocalMatchCollectExecutionResponse::class))],
    )
    @PostMapping("/fixtures/{fixtureUid}/collect-live")
    fun collectLive(
        @Parameter(description = "FixtureCore UID", example = "fixture_core_abcd1234")
        @PathVariable fixtureUid: String,
        @RequestBody(required = false) @Valid request: LocalMatchCollectNowRequest?,
    ): ResponseEntity<LocalMatchCollectExecutionResponse> =
        ResponseEntity.ok(localMatchCollectTestService.collectLive(fixtureUid, request?.now))

    @Operation(summary = "Local fixture POST collect 직접 실행", description = OP_COLLECT_POST)
    @ApiResponse(
        responseCode = "200",
        description = "Local MatchCollect execution result",
        content = [Content(schema = Schema(implementation = LocalMatchCollectExecutionResponse::class))],
    )
    @PostMapping("/fixtures/{fixtureUid}/collect-post")
    fun collectPost(
        @Parameter(description = "FixtureCore UID", example = "fixture_core_abcd1234")
        @PathVariable fixtureUid: String,
        @RequestBody(required = false) @Valid request: LocalMatchCollectNowRequest?,
    ): ResponseEntity<LocalMatchCollectExecutionResponse> =
        ResponseEntity.ok(localMatchCollectTestService.collectPost(fixtureUid, request?.now))

    @Operation(summary = "Local Quartz job manual fire", description = OP_QUARTZ_FIRE)
    @ApiResponse(
        responseCode = "200",
        description = "Quartz trigger request result",
        content = [Content(schema = Schema(implementation = LocalQuartzFireResponse::class))],
    )
    @PostMapping("/quartz/fire")
    fun fireQuartzJob(
        @RequestBody @Valid request: LocalQuartzFireRequest,
    ): ResponseEntity<LocalQuartzFireResponse> =
        ResponseEntity.ok(localMatchCollectTestService.fireQuartzJob(request))
}
