package com.footballay.core.web.football.service

import com.footballay.core.domain.facade.MockDataReadOption
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MockDataReadOptionResolverTest {
    @Test
    fun `헤더 값이 없으면 기본 option을 반환한다`() {
        val option = MockDataReadOptionResolver.resolve(null)

        assertThat(option).isEqualTo(MockDataReadOption.DEFAULT)
    }

    @Test
    fun `include 헤더 값이면 mock data 포함 option을 반환한다`() {
        val option = MockDataReadOptionResolver.resolve("include")

        assertThat(option).isEqualTo(MockDataReadOption(includeMockData = true))
    }

    @Test
    fun `include 헤더 값은 공백과 대소문자를 무시한다`() {
        val option = MockDataReadOptionResolver.resolve(" Include ")

        assertThat(option).isEqualTo(MockDataReadOption(includeMockData = true))
    }

    @Test
    fun `알 수 없는 헤더 값이면 기본 option을 반환한다`() {
        val option = MockDataReadOptionResolver.resolve("enabled")

        assertThat(option).isEqualTo(MockDataReadOption.DEFAULT)
    }
}
