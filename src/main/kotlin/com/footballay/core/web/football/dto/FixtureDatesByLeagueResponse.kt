package com.footballay.core.web.football.dto

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "요청한 기간 안에서 해당 리그 경기가 있는 날짜 목록입니다.")
data class FixtureDatesByLeagueResponse(
    @field:ArraySchema(
        arraySchema = Schema(description = "경기가 있는 날짜 목록입니다."),
        schema = Schema(type = "string", format = "date", example = "2026-08-01"),
    )
    val dates: List<String>,
)
