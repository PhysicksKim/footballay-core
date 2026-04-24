package com.footballay.core.web.football.cache.hash

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets

class DefaultFixtureEtagHasherTest {
    private lateinit var hasher: FixtureEtagHasher

    @BeforeEach
    fun setUp() {
        hasher = DefaultFixtureEtagHasher()
    }

    @Test
    fun `hash - canonical bytes 를 sha256 base64url no padding 으로 변환한다`() {
        val bytes = "abc".toByteArray(StandardCharsets.UTF_8)

        val hash = hasher.hash(bytes)

        assertThat(hash).isEqualTo("ungWv48Bz-pBQUDeXa4iI7ADYaOWF3qctBD_YfIAFa0")
        assertThat(hash).doesNotContain("+", "/", "=")
    }

    @Test
    fun `hash - 같은 bytes 는 같은 hash 를 생성한다`() {
        val bytes = """{"fixtureUid":"fixture-1","liveStatus":{"elapsed":77}}""".toByteArray(StandardCharsets.UTF_8)

        val firstHash = hasher.hash(bytes)
        val secondHash = hasher.hash(bytes)

        assertThat(firstHash).isEqualTo(secondHash)
    }

    @Test
    fun `hash - bytes 가 달라지면 hash 도 달라진다`() {
        val firstBytes = """{"fixtureUid":"fixture-1","liveStatus":{"elapsed":77}}""".toByteArray(StandardCharsets.UTF_8)
        val secondBytes = """{"fixtureUid":"fixture-1","liveStatus":{"elapsed":78}}""".toByteArray(StandardCharsets.UTF_8)

        val firstHash = hasher.hash(firstBytes)
        val secondHash = hasher.hash(secondBytes)

        assertThat(firstHash).isNotEqualTo(secondHash)
    }

    @Test
    fun `toWeakEtag - weak etag 포맷으로 감싼다`() {
        val weakEtag = hasher.toWeakEtag("abc123")

        assertThat(weakEtag).isEqualTo("""W/"abc123"""")
    }

    @Test
    fun `hashToWeakEtag - bytes 를 바로 weak etag 로 변환한다`() {
        val bytes = "abc".toByteArray(StandardCharsets.UTF_8)

        val weakEtag = hasher.hashToWeakEtag(bytes)

        assertThat(weakEtag).isEqualTo("""W/"ungWv48Bz-pBQUDeXa4iI7ADYaOWF3qctBD_YfIAFa0"""")
    }
}
