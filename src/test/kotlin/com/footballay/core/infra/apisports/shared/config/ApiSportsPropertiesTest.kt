package com.footballay.core.infra.apisports.shared.config

import com.footballay.core.infra.apisports.shared.config.ApiSportsProperties
import com.footballay.core.logger
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.condition.DisabledIf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.StringUtils
import kotlin.test.Test

@ActiveProfiles("devrealapi")
@SpringBootTest
@Disabled("실제 API를 호출하므로 기본 테스트에서는 제외합니다")
class ApiSportsPropertiesTest {
    val log = logger()

    @Autowired
    private lateinit var apiSportsProperties: ApiSportsProperties

    @Test
    fun `프로퍼티 로딩 테스트`() {
        val scheme = apiSportsProperties.scheme
        val host = apiSportsProperties.host
        val headers = apiSportsProperties.headers
        val xRapidapiHostName = headers.xRapidapiHostName
        val xRapidapiHostVal = headers.xRapidapiHostValue

        log.info("Scheme: $scheme")
        log.info("Host: $host")
        log.info("header : $xRapidapiHostName : $xRapidapiHostVal")

        assertThat(StringUtils.hasText(scheme)).isTrue()
        assertThat(StringUtils.hasText(host)).isTrue()
        assertThat(StringUtils.hasText(xRapidapiHostName)).isTrue()
        assertThat(StringUtils.hasText(xRapidapiHostVal)).isTrue()
    }
}
