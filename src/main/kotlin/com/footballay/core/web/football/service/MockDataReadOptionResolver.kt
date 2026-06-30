package com.footballay.core.web.football.service

import com.footballay.core.domain.facade.MockDataReadOption

object MockDataReadOptionResolver {
    const val HEADER_NAME = "X-Footballay-Dev-Data"

    fun resolve(headerValue: String?): MockDataReadOption =
        if (headerValue?.trim()?.equals("include", ignoreCase = true) == true) {
            MockDataReadOption(includeMockData = true)
        } else {
            MockDataReadOption.DEFAULT
        }
}
