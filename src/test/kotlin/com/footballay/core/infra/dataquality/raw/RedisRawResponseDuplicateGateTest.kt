package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.infra.dataquality.raw.model.RawResponseDuplicateCheckCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseDuplicateCheckResult
import com.footballay.core.infra.dataquality.raw.model.RawResponseParameter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration

@ExtendWith(MockitoExtension::class)
class RedisRawResponseDuplicateGateTest {
    @Mock
    private lateinit var stringRedisTemplate: StringRedisTemplate

    @Mock
    private lateinit var valueOperations: ValueOperations<String, String>

    private lateinit var duplicateGate: RedisRawResponseDuplicateGate

    @BeforeEach
    fun setUp() {
        whenever(stringRedisTemplate.opsForValue()).thenReturn(valueOperations)
        duplicateGate =
            RedisRawResponseDuplicateGate(
                stringRedisTemplate = stringRedisTemplate,
                ttl = TTL,
            )
    }

    @Test
    fun `missing hash returns new and stores hash with ttl`() {
        whenever(valueOperations.get(REDIS_KEY)).thenReturn(null)

        val result = duplicateGate.checkAndStore(command(canonicalHash = "new-hash"))

        assertThat(result).isEqualTo(RawResponseDuplicateCheckResult.New)
        verify(valueOperations).set(REDIS_KEY, "new-hash", TTL)
    }

    @Test
    fun `same hash returns duplicate without storing hash`() {
        whenever(valueOperations.get(REDIS_KEY)).thenReturn("same-hash")

        val result = duplicateGate.checkAndStore(command(canonicalHash = "same-hash"))

        assertThat(result).isEqualTo(RawResponseDuplicateCheckResult.Duplicate)
        verify(valueOperations, never()).set(REDIS_KEY, "same-hash", TTL)
    }

    @Test
    fun `changed hash returns new and updates hash with ttl`() {
        whenever(valueOperations.get(REDIS_KEY)).thenReturn("old-hash")

        val result = duplicateGate.checkAndStore(command(canonicalHash = "changed-hash"))

        assertThat(result).isEqualTo(RawResponseDuplicateCheckResult.New)
        verify(valueOperations).set(REDIS_KEY, "changed-hash", TTL)
    }

    @Test
    fun `redis get exception returns failed without storing hash`() {
        whenever(valueOperations.get(REDIS_KEY)).thenThrow(IllegalStateException("redis unavailable"))

        val result = duplicateGate.checkAndStore(command(canonicalHash = "new-hash"))

        assertThat(result).isInstanceOf(RawResponseDuplicateCheckResult.Failed::class.java)
        assertThat((result as RawResponseDuplicateCheckResult.Failed).reason).contains("redis unavailable")
        verify(valueOperations, never()).set(REDIS_KEY, "new-hash", TTL)
    }

    @Test
    fun `redis set exception returns failed`() {
        whenever(valueOperations.get(REDIS_KEY)).thenReturn(null)
        whenever(valueOperations.set(REDIS_KEY, "new-hash", TTL)).thenThrow(IllegalStateException("set failed"))

        val result = duplicateGate.checkAndStore(command(canonicalHash = "new-hash"))

        assertThat(result).isInstanceOf(RawResponseDuplicateCheckResult.Failed::class.java)
        assertThat((result as RawResponseDuplicateCheckResult.Failed).reason).contains("set failed")
    }

    private fun command(canonicalHash: String): RawResponseDuplicateCheckCommand =
        RawResponseDuplicateCheckCommand(
            provider = FootballDataProvider.API_SPORTS,
            endpointKey = "fixtureSingle",
            parameters = PARAMETERS,
            canonicalHash = canonicalHash,
        )

    private companion object {
        private val TTL: Duration = Duration.ofDays(7)
        private const val REDIS_KEY = "footballay:data-quality:raw-response:API_SPORTS:fixtureSingle:fixtureId=1208397"
        private val PARAMETERS = listOf(RawResponseParameter(name = "fixtureId", value = "1208397"))
    }
}
