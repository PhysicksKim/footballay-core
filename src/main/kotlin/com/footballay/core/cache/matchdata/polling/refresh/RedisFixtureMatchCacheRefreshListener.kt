package com.footballay.core.cache.matchdata.polling.refresh

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.common.logging.logger
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Component

@Component
class RedisFixtureMatchCacheRefreshListener(
    private val objectMapper: ObjectMapper,
    private val refreshUseCase: FixtureMatchCacheRefreshUseCase,
) : MessageListener {
    private val log = logger()

    override fun onMessage(
        message: Message,
        pattern: ByteArray?,
    ) {
        val payload = message.body.toString(Charsets.UTF_8)

        runCatching {
            objectMapper.readValue(payload, FixtureMatchCacheRefreshTrigger::class.java)
        }.onSuccess { trigger ->
            refreshUseCase.handle(trigger)
        }.onFailure { ex ->
            log.error("Failed to consume fixture cache refresh trigger. payload={}", payload, ex)
        }
    }
}
