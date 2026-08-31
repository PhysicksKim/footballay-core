package com.footballay.core.web.football.cache.refresh

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.logger
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.stereotype.Component

/**
 * Redis Pub/Sub을 통해 fixture/match 캐시 갱신 이벤트를 수신한다.
 *
 * 캐시 갱신 작업은 idempotent하며, 이벤트의 영속성, 재처리(replay),
 * ACK 또는 consumer group 기능이 필요하지 않으므로 Redis Streams 대신
 * 단순한 Pub/Sub 방식을 의도적으로 사용한다.
 *
 * 블루/그린 배포 중에는 여러 애플리케이션 인스턴스가 동일한 갱신 이벤트를
 * 동시에 수신할 수 있다. 캐시 갱신 작업은 idempotent하므로 이러한 중복 실행은
 * 정상적인 동작이며 데이터 정합성에 영향을 주지 않는다.
 */
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
