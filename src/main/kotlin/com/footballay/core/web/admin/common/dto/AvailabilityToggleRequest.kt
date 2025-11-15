package com.footballay.core.web.admin.common.dto

import jakarta.validation.constraints.NotNull

data class AvailabilityToggleRequest(
    @field:NotNull
    val available: Boolean,
)
