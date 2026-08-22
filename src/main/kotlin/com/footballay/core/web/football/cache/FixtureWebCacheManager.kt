package com.footballay.core.web.football.cache

import com.footballay.core.logger
import com.footballay.core.web.football.cache.hash.FixtureResponseCacheDocument
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

interface FixtureWebCacheManager {
    fun find(identity: FixtureWebCacheIdentity): FixtureWebCacheEntry?

    fun findSnapshot(identity: FixtureWebCacheIdentity): FixtureWebCacheSnapshot?

    fun findEtagHash(identity: FixtureWebCacheIdentity): String?

    fun save(
        identity: FixtureWebCacheIdentity,
        document: FixtureResponseCacheDocument,
    )
}

data class FixtureWebCacheEntry(
    val snapshotJson: String,
    val etagHash: String,
    val updatedAt: Instant,
)

data class FixtureWebCacheSnapshot(
    val snapshotJson: String,
    val etagHash: String,
)

@Component
class RedisFixtureWebCacheManager(
    private val stringRedisTemplate: StringRedisTemplate,
) : FixtureWebCacheManager {
    private val log = logger()
    private val hashOperations by lazy { stringRedisTemplate.opsForHash<String, String>() }

    override fun find(identity: FixtureWebCacheIdentity): FixtureWebCacheEntry? {
        val key = key(identity)

        return runCatching {
            val values = hashOperations.entries(key)
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
            log.warn("Failed to read fixture web cache. fixtureUid={}, endpoint={}, locale={}, key={}", identity.fixtureUid, identity.endpoint, identity.locale, key, ex)
        }.getOrNull()
    }

    override fun findSnapshot(identity: FixtureWebCacheIdentity): FixtureWebCacheSnapshot? {
        val key = key(identity)

        return runCatching {
            val values = hashOperations.multiGet(key, SNAPSHOT_FIELDS)
            val snapshotJson = values?.getOrNull(0) ?: return null
            val etagHash = values.getOrNull(1) ?: return null

            FixtureWebCacheSnapshot(
                snapshotJson = snapshotJson,
                etagHash = etagHash,
            )
        }.onFailure { ex ->
            log.warn("Failed to read fixture web cache snapshot. fixtureUid={}, endpoint={}, locale={}, key={}", identity.fixtureUid, identity.endpoint, identity.locale, key, ex)
        }.getOrNull()
    }

    override fun findEtagHash(identity: FixtureWebCacheIdentity): String? {
        val key = key(identity)

        return runCatching {
            hashOperations.get(key, ETAG_HASH_FIELD)
        }.onFailure { ex ->
            log.warn("Failed to read fixture web cache etag. fixtureUid={}, endpoint={}, locale={}, key={}", identity.fixtureUid, identity.endpoint, identity.locale, key, ex)
        }.getOrNull()
    }

    override fun save(
        identity: FixtureWebCacheIdentity,
        document: FixtureResponseCacheDocument,
    ) {
        val key = key(identity)

        runCatching {
            hashOperations.putAll(
                key,
                mapOf(
                    SNAPSHOT_JSON_FIELD to document.snapshotJson,
                    ETAG_HASH_FIELD to document.etagHash,
                    UPDATED_AT_FIELD to Instant.now().toString(),
                ),
            )
            stringRedisTemplate.expire(key, CACHE_TTL)
        }.onFailure { ex ->
            log.warn("Failed to write fixture web cache. fixtureUid={}, endpoint={}, locale={}, key={}", identity.fixtureUid, identity.endpoint, identity.locale, key, ex)
        }
    }

    private fun key(identity: FixtureWebCacheIdentity): String {
        val prefix = "$KEY_PREFIX:${identity.fixtureUid}:${identity.endpoint.keySegment}"
        return identity.locale?.let { "$prefix:${it.code}" } ?: prefix
    }

    private companion object {
        const val KEY_PREFIX = "footballay:fixture:web"
        const val SNAPSHOT_JSON_FIELD = "snapshotJson"
        const val ETAG_HASH_FIELD = "etagHash"
        const val UPDATED_AT_FIELD = "updatedAt"
        val SNAPSHOT_FIELDS: List<String> = listOf(SNAPSHOT_JSON_FIELD, ETAG_HASH_FIELD)
        /**
         * Fixture web cache TTL.
         *
         * 이 TTL은 "경기 중에 캐시가 자주 만료되도록" 잡는 값이 아니다.
         * MatchDataSync 흐름이 데이터 제공자 polling 이후 snapshot/etag를 계속 갱신하므로,
         * 경기 진행 중에는 sync가 정상 동작하는 한 캐시가 유지되는 것이 기대 동작이다.
         *
         * TTL의 실제 목적은 다음 두 가지다.
         * 1. 더 이상 활발히 갱신되지 않는 지난 경기의 Redis 캐시를 자연스럽게 정리한다.
         * 2. 일시적인 scheduler 지연이나 sync 간격 흔들림이 있어도 경기 중 캐시 miss를 과도하게 만들지 않는다.
         *
         * 현재 job 주기는 다음과 같다.
         * - LiveMatchJob: 17초 간격
         * - PreMatchJob: 60초 간격
         * - PostMatchJob: 60초 간격
         *
         * 따라서 TTL은 가장 긴 정상 갱신 주기(60초)보다 충분히 길어야 한다.
         * 기존 10초 TTL은 pre/post-match 구간에서 정상 동작 중에도 캐시가 빈번히 만료될 수 있어
         * ETag 기반 304 응답 기회를 불필요하게 잃는다.
         *
         * 3분(180초)은 60초 주기에 대해 충분한 버퍼를 제공해 경기 중 cache miss를 줄이면서도,
         * 종료된 경기 캐시를 Redis에 과도하게 오래 남기지 않는 균형점이다.
         */
        val CACHE_TTL: Duration = Duration.ofMinutes(3)
    }
}
