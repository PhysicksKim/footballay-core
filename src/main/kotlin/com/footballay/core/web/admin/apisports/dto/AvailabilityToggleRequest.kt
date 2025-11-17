package com.footballay.core.web.admin.apisports.dto

import jakarta.validation.constraints.NotNull

data class AvailabilityToggleRequest(
    @field:NotNull
    val available: Boolean,
)
