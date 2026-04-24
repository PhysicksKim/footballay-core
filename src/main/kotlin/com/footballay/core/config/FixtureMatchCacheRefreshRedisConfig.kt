package com.footballay.core.config

import com.footballay.core.web.football.cache.refresh.FixtureMatchCacheRefreshChannels
import com.footballay.core.web.football.cache.refresh.RedisFixtureMatchCacheRefreshListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer

@Configuration
@ConditionalOnProperty(
    prefix = "footballay.fixture-cache-refresh.redis-pubsub",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class FixtureMatchCacheRefreshRedisConfig {
    @Bean
    fun fixtureMatchCacheRefreshRedisMessageListenerContainer(
        redisConnectionFactory: RedisConnectionFactory,
        listener: RedisFixtureMatchCacheRefreshListener,
    ): RedisMessageListenerContainer =
        RedisMessageListenerContainer().apply {
            setConnectionFactory(redisConnectionFactory)
            addMessageListener(listener, ChannelTopic(FixtureMatchCacheRefreshChannels.FIXTURE_CACHE_REFRESH))
        }
}
