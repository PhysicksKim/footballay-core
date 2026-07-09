package com.footballay.core.domain.dataquality.result

import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant

class QualityResultSearchConditionTest {
    @Test
    fun `checkedAtFrom은 checkedAtTo보다 늦을 수 없다`() {
        assertThatThrownBy {
            QualityResultSearchCondition(
                checkedAtFrom = Instant.parse("2026-07-07T12:02:00Z"),
                checkedAtTo = Instant.parse("2026-07-07T12:01:00Z"),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("checkedAtFrom must be before or equal to checkedAtTo")
    }

    @Test
    fun `parameterValue만 있는 조건은 허용하지 않는다`() {
        assertThatThrownBy {
            QualityResultSearchCondition(parameterValue = "1208397")
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("parameterName is required when parameterValue is provided")
    }

    @Test
    fun `parameterName만 있는 단순 조건은 허용한다`() {
        assertThatCode {
            QualityResultSearchCondition(parameterName = "fixtureId")
        }.doesNotThrowAnyException()
    }
}
