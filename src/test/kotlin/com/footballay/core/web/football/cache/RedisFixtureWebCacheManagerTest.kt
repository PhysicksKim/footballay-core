package com.footballay.core.web.football.cache

import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.football.cache.hash.FixtureResponseCacheDocument
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration

class RedisFixtureWebCacheManagerTest {
    private lateinit var stringRedisTemplate: StringRedisTemplate
    private lateinit var hashOperations: HashOperations<String, String, String>
    private lateinit var cacheManager: FixtureWebCacheManager

    @BeforeEach
    fun setUp() {
        stringRedisTemplate = mockk()
        hashOperations = mockk()

        every { stringRedisTemplate.opsForHash<String, String>() } returns hashOperations

        cacheManager =
            RedisFixtureWebCacheManager(
                stringRedisTemplate = stringRedisTemplate,
            )
    }

    @Test
    fun `find - hash 필드가 모두 있으면 cache entry 를 반환한다`() {
        every { hashOperations.entries("footballay:fixture:web:fixture-1:status") } returns
            mapOf(
                "snapshotJson" to """{"fixtureUid":"fixture-1"}""",
                "etagHash" to "etag-1",
                "updatedAt" to "2026-04-24T00:00:00Z",
            )

        val result = cacheManager.find(FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.STATUS, null))

        assertThat(result).isNotNull
        assertThat(result!!.snapshotJson).isEqualTo("""{"fixtureUid":"fixture-1"}""")
        assertThat(result.etagHash).isEqualTo("etag-1")
        assertThat(result.updatedAt.toString()).isEqualTo("2026-04-24T00:00:00Z")
    }

    @Test
    fun `findSnapshot - snapshot 과 etag 만 가져온다`() {
        every {
            hashOperations.multiGet(
                "footballay:fixture:web:fixture-1:status",
                listOf("snapshotJson", "etagHash"),
            )
        } returns listOf("""{"fixtureUid":"fixture-1"}""", "etag-1")

        val result = cacheManager.findSnapshot(FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.STATUS, null))

        assertThat(result).isNotNull
        assertThat(result!!.snapshotJson).isEqualTo("""{"fixtureUid":"fixture-1"}""")
        assertThat(result.etagHash).isEqualTo("etag-1")
    }

    @Test
    fun `findEtagHash - etag 필드만 가져온다`() {
        every { hashOperations.get("footballay:fixture:web:fixture-1:status", "etagHash") } returns "etag-1"

        val result = cacheManager.findEtagHash(FixtureWebCacheIdentity("fixture-1", FixturePollingEndpoint.STATUS, null))

        assertThat(result).isEqualTo("etag-1")
    }

    @Test
    fun `save - snapshot 과 etag 와 updatedAt 을 같은 key 에 저장하고 ttl 을 건다`() {
        every { hashOperations.putAll(any(), any<Map<String, String>>()) } just runs
        every { stringRedisTemplate.expire("footballay:fixture:web:fixture-2:events:en", Duration.ofMinutes(3)) } returns true

        cacheManager.save(
            identity = FixtureWebCacheIdentity("fixture-2", FixturePollingEndpoint.EVENTS, SupportedLocale.EN),
            document =
                FixtureResponseCacheDocument(
                    snapshotJson = """{"fixtureUid":"fixture-2"}""",
                    etagHash = "etag-2",
                ),
        )

        verify {
            hashOperations.putAll(
                "footballay:fixture:web:fixture-2:events:en",
                match<Map<String, String>> {
                    it["snapshotJson"] == """{"fixtureUid":"fixture-2"}""" &&
                        it["etagHash"] == "etag-2" &&
                        it["updatedAt"] != null
                },
            )
        }
        verify { stringRedisTemplate.expire("footballay:fixture:web:fixture-2:events:en", Duration.ofMinutes(3)) }
    }

    @Test
    fun `localized endpoint - locale별 key를 사용한다`() {
        every { hashOperations.get(any(), "etagHash") } returns "etag"

        for (endpoint in listOf(FixturePollingEndpoint.EVENTS, FixturePollingEndpoint.LINEUP, FixturePollingEndpoint.STATISTICS)) {
            cacheManager.findEtagHash(FixtureWebCacheIdentity("fixture-1", endpoint, SupportedLocale.EN))
            cacheManager.findEtagHash(FixtureWebCacheIdentity("fixture-1", endpoint, SupportedLocale.KO))

            verify { hashOperations.get("footballay:fixture:web:fixture-1:${endpoint.keySegment}:en", "etagHash") }
            verify { hashOperations.get("footballay:fixture:web:fixture-1:${endpoint.keySegment}:ko", "etagHash") }
        }
    }
}
