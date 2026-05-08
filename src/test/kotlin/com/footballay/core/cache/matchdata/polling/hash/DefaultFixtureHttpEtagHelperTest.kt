package com.footballay.core.cache.matchdata.polling.hash

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultFixtureHttpEtagHelperTest {
    private lateinit var helper: FixtureHttpEtagHelper

    @BeforeEach
    fun setUp() {
        helper = DefaultFixtureHttpEtagHelper()
    }

    @Test
    fun `toWeakEtag - weak etag 헤더 포맷으로 감싼다`() {
        val weakEtag = helper.toWeakEtag("abc123")

        assertThat(weakEtag).isEqualTo("""W/"abc123"""")
    }

    @Test
    fun `matchesIfNoneMatch - weak etag 헤더와 etagHash 를 비교한다`() {
        val matched = helper.matchesIfNoneMatch("""W/"abc123"""", "abc123")

        assertThat(matched).isTrue()
    }

    @Test
    fun `matchesIfNoneMatch - strong etag 형식이나 raw hash 도 비교할 수 있다`() {
        assertThat(helper.matchesIfNoneMatch(""""abc123"""", "abc123")).isTrue()
        assertThat(helper.matchesIfNoneMatch("abc123", "abc123")).isTrue()
    }

    @Test
    fun `matchesIfNoneMatch - 여러 etag 값 중 하나만 일치해도 true 를 반환한다`() {
        val matched = helper.matchesIfNoneMatch("""W/"nope", "abc123", W/"other"""", "abc123")

        assertThat(matched).isTrue()
    }

    @Test
    fun `matchesIfNoneMatch - wildcard 는 항상 일치한다`() {
        val matched = helper.matchesIfNoneMatch("*", "abc123")

        assertThat(matched).isTrue()
    }

    @Test
    fun `matchesIfNoneMatch - 값이 없거나 다르면 false 를 반환한다`() {
        assertThat(helper.matchesIfNoneMatch(null, "abc123")).isFalse()
        assertThat(helper.matchesIfNoneMatch("", "abc123")).isFalse()
        assertThat(helper.matchesIfNoneMatch("""W/"other"""", "abc123")).isFalse()
    }
}
