package com.footballay.core.web.football.cache

import com.footballay.core.localization.SupportedLocale

/** Fixture web cache representation을 식별합니다. */
data class FixtureWebCacheIdentity(
    val fixtureUid: String,
    val endpoint: FixturePollingEndpoint,
    /**
     * Cache identity의 locale dimension입니다.
     * null은 이 cache representation이 locale dimension을 가지지 않는 경우에만 사용합니다.
     * 현재 request-time에서는 STATUS만 null을 사용합니다.
     * Request locale 미지정은 이 단계 전에 EN으로 resolve되며,
     * 전체 locale refresh를 null identity로 표현하지 않습니다.
     */
    val locale: SupportedLocale?,
)
