package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseDuplicateCheckCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseDuplicateCheckResult
import com.footballay.core.logger
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration

class RedisRawResponseDuplicateGate(
    private val stringRedisTemplate: StringRedisTemplate,
    private val ttl: Duration,
) : RawResponseDuplicateGate {
    private val log = logger()

    override fun checkAndStore(command: RawResponseDuplicateCheckCommand): RawResponseDuplicateCheckResult {
        val key = createKey(command)

        return runCatching {
            val valueOperations = stringRedisTemplate.opsForValue()
            val currentHash = valueOperations.get(key)
            if (currentHash == command.canonicalHash) {
                RawResponseDuplicateCheckResult.Duplicate
            } else {
                valueOperations.set(key, command.canonicalHash, ttl)
                RawResponseDuplicateCheckResult.New
            }
        }.onFailure { ex ->
            log.warn(
                "Data quality duplicate gate failed. provider={}, endpointKey={}, apiId={}, redisKey={}",
                command.provider,
                command.endpointKey,
                command.apiId,
                key,
                ex,
            )
        }.getOrElse { ex ->
            RawResponseDuplicateCheckResult.Failed(
                ex.message ?: ex::class.simpleName ?: "Redis duplicate gate failed",
            )
        }
    }

    private fun createKey(command: RawResponseDuplicateCheckCommand): String =
        "$KEY_PREFIX:${command.provider.name}:${command.endpointKey}:${command.apiId}"

    private companion object {
        private const val KEY_PREFIX = "footballay:data-quality:raw-response"
    }
}
