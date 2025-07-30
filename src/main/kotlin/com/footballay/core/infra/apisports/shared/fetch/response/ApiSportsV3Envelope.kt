package com.footballay.core.infra.apisports.shared.fetch.response

data class ApiSportsV3Envelope<T : ApiSportsResponse>(
    val get: String,
    val parameters: Map<String, String>,
    val errors: List<String>,
    val results: Int,
    val paging: Paging,
    val response: List<T>
)

data class Paging(
    val current: Int,
    val total: Int
)

data class ApiSportsV3LiveStatusEnvelope<T : ApiSportsResponse> (
    val get: String,
    val parameters: List<Any>,
    val errors: List<String>,
    val results: Int,
    val paging: Paging,
    val response: T
)