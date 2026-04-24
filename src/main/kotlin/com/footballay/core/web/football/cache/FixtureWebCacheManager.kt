package com.footballay.core.web.football.cache

import com.footballay.core.logger
import com.footballay.core.web.football.cache.hash.FixtureResponseCacheDocument
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

interface FixtureWebCacheManager {
    fun find(
        fixtureUid: String,
        endpoint: FixturePollingEndpoint,
    ): FixtureWebCacheEntry?

    fun save(
        fixtureUid: String,
        endpoint: FixturePollingEndpoint,
        document: FixtureResponseCacheDocument,
    )
}

data class FixtureWebCacheEntry(
    val snapshotJson: String,
    val etagHash: String,
    val updatedAt: Instant,
)

@Component
class RedisFixtureWebCacheManager(
    private val stringRedisTemplate: StringRedisTemplate,
) : FixtureWebCacheManager {
    private val log = logger()

    override fun find(
        fixtureUid: String,
        endpoint: FixturePollingEndpoint,
    ): FixtureWebCacheEntry? {
        val key = key(fixtureUid, endpoint)

        return runCatching {
            val values = stringRedisTemplate.opsForHash<String, String>().entries(key)
            if (values.isEmpty()) {
                return null
            }

            val snapshotJson = values[SNAPSHOT_JSON_FIELD] ?: return null
            val etagHash = values[ETAG_HASH_FIELD] ?: return null
            val updatedAtRaw = values[UPDATED_AT_FIELD] ?: return null

            FixtureWebCacheEntry(
                snapshotJson = snapshotJson,
                etagHash = etagHash,
                updatedAt = Instant.parse(updatedAtRaw),
            )
        }.onFailure { ex ->
            log.warn("Failed to read fixture web cache. fixtureUid={}, endpoint={}, key={}", fixtureUid, endpoint, key, ex)
        }.getOrNull()
    }

    override fun save(
        fixtureUid: String,
        endpoint: FixturePollingEndpoint,
        document: FixtureResponseCacheDocument,
    ) {
        val key = key(fixtureUid, endpoint)

        runCatching {
            stringRedisTemplate
                .opsForHash<String, String>()
                .putAll(
                    key,
                    mapOf(
                        SNAPSHOT_JSON_FIELD to document.snapshotJson,
                        ETAG_HASH_FIELD to document.etagHash,
                        UPDATED_AT_FIELD to Instant.now().toString(),
                    ),
                )
            stringRedisTemplate.expire(key, CACHE_TTL)
        }.onFailure { ex ->
            log.warn("Failed to write fixture web cache. fixtureUid={}, endpoint={}, key={}", fixtureUid, endpoint, key, ex)
        }
    }

    private fun key(
        fixtureUid: String,
        endpoint: FixturePollingEndpoint,
    ): String = "$KEY_PREFIX:${endpoint.keySegment}:$fixtureUid"

    private companion object {
        const val KEY_PREFIX = "footballay:fixture:web"
        const val SNAPSHOT_JSON_FIELD = "snapshotJson"
        const val ETAG_HASH_FIELD = "etagHash"
        const val UPDATED_AT_FIELD = "updatedAt"
        val CACHE_TTL: Duration = Duration.ofSeconds(10)
    }
}
