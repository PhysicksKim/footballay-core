package com.footballay.core.infra.apisports.fetch.response

data class ApiSportsAccountStatus (
    val account: Account,
    val subscription: Subscription,
    val requests: Requests
) : StatusResponse {

    data class Account(
        val firstname: String,
        val lastname: String,
        val email: String
    )

    data class Subscription(
        val plan: String,
        val end: String,
        val active: Boolean
    )

    data class Requests(
        val current: Int,
        val limit_day: Int
    )
}
