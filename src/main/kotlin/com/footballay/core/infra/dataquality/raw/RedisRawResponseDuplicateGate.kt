package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseDuplicateCheckCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseDuplicateCheckResult
import com.footballay.core.infra.dataquality.raw.model.RawResponseParameter
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
                "Data quality duplicate gate failed. provider={}, endpointKey={}, parameters={}, redisKey={}",
                command.provider,
                command.endpointKey,
                command.parameters,
                key,
                ex,
            )
        }.getOrElse { ex ->
            RawResponseDuplicateCheckResult.Failed(
                ex.message ?: ex::class.simpleName ?: "Redis duplicate gate failed",
            )
        }
    }

    private fun createKey(command: RawResponseDuplicateCheckCommand): String = "$KEY_PREFIX:${command.provider.name}:${command.endpointKey}:${parameterKey(command.parameters)}"

    private fun parameterKey(parameters: List<RawResponseParameter>): String {
        require(parameters.isNotEmpty()) {
            "parameters must not be empty"
        }
        return parameters.joinToString("&") { "${it.name}=${it.value}" }
    }

    private companion object {
        private const val KEY_PREFIX = "footballay:data-quality:raw-response"
    }
}
