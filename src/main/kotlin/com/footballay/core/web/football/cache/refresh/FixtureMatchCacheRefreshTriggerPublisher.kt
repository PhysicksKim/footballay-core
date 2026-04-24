package com.footballay.core.web.football.cache.refresh

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.logger
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

interface FixtureMatchCacheRefreshTriggerPublisher {
    fun publish(trigger: FixtureMatchCacheRefreshTrigger)
}

@Component
class RedisFixtureMatchCacheRefreshTriggerPublisher(
    private val stringRedisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) : FixtureMatchCacheRefreshTriggerPublisher {
    private val log = logger()

    override fun publish(trigger: FixtureMatchCacheRefreshTrigger) {
        val payload = objectMapper.writeValueAsString(trigger)
        stringRedisTemplate.convertAndSend(FixtureMatchCacheRefreshChannels.FIXTURE_CACHE_REFRESH, payload)
        log.info(
            "Published fixture cache refresh trigger. fixtureUid={}, source={}, jobPhase={}",
            trigger.fixtureUid,
            trigger.source,
            trigger.jobPhase,
        )
    }
}
