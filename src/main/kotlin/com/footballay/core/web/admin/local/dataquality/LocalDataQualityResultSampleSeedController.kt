package com.footballay.core.web.admin.local.dataquality

// local profile에서 Data Quality Admin API 검증용 MongoDB sample result seed endpoint를 제공한다.
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Local - Data Quality Result Seed",
    description = "local profile 에서만 열리는 Data Quality Result MongoDB sample seed API. 운영 admin API 가 아닙니다.",
)
@SecurityRequirement(name = "cookieAuth")
@RestController
@Profile("local")
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/local/data-quality/results")
class LocalDataQualityResultSampleSeedController(
    private val localDataQualityResultSampleSeedService: LocalDataQualityResultSampleSeedService,
) {
    @Operation(
        summary = "Local quality_results sample seed",
        description = "Admin Data Quality list/detail API 검증을 위해 local MongoDB quality_results sample document 를 upsert 합니다.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Seeded sample result IDs",
        content = [Content(schema = Schema(implementation = LocalDataQualityResultSampleSeedResponse::class))],
    )
    @PostMapping("/sample-seed")
    fun seed() = ResponseEntity.ok(localDataQualityResultSampleSeedService.seed())
}
