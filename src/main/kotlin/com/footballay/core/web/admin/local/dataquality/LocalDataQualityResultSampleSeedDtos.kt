package com.footballay.core.web.admin.local.dataquality

data class LocalDataQualityResultSampleSeedResponse(
    val collection: String,
    val seededCount: Int,
    val resultIds: List<String>,
)
