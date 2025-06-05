package com.footballay.core.domain.sync.service.response

/*
    "get": "leagues",
    "parameters": {
        "current": "true"
    },
    "errors": [],
    "results": 1184,
    "paging": {
        "current": 1,
        "total": 1
    },
    "response": [
 */
data class ApiSportsV3Response<T>(
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