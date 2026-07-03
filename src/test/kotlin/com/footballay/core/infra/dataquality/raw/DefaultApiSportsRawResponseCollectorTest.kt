package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.infra.dataquality.raw.model.RawResponseCollectedEvent
import com.footballay.core.infra.dataquality.raw.model.RawResponseCollectionCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseDuplicateCheckCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseDuplicateCheckResult
import com.footballay.core.infra.dataquality.raw.model.RawResponseObjectKeyCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseRequestMetadata
import com.footballay.core.infra.dataquality.raw.model.RawResponseStoredObject
import com.footballay.core.infra.dataquality.raw.model.RawResponseUploadCommand
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.time.Instant
import java.util.concurrent.RejectedExecutionException

@ExtendWith(MockitoExtension::class)
class DefaultApiSportsRawResponseCollectorTest {
    @Mock
    private lateinit var canonicalHasher: RawResponseCanonicalHasher

    @Mock
    private lateinit var duplicateGate: RawResponseDuplicateGate

    @Mock
    private lateinit var objectKeyFactory: RawResponseObjectKeyFactory

    @Mock
    private lateinit var gzipCodec: RawResponseGzipCodec

    @Mock
    private lateinit var storage: RawResponseStorage

    @Mock
    private lateinit var publisher: RawResponsePublisher

    private lateinit var directCollector: DefaultApiSportsRawResponseCollector

    @BeforeEach
    fun setUp() {
        directCollector = collector(DirectThreadPoolTaskExecutor())
    }

    @Test
    fun `collect only submits async task and does not run pipeline on caller thread`() {
        val executor = CapturingExecutor()
        val collector = collector(executor)

        collector.collect(COMMAND)

        assertThat(executor.tasks).hasSize(1)
        verify(canonicalHasher, never()).hash(any())
        verify(duplicateGate, never()).checkAndStore(any())
        verify(storage, never()).upload(any())
        verify(publisher, never()).publish(any())
    }

    @Test
    fun `executor rejection is swallowed`() {
        val collector =
            collector(
                RejectingThreadPoolTaskExecutor(),
            )

        assertThatCode {
            collector.collect(COMMAND)
        }.doesNotThrowAnyException()
    }

    @Test
    fun `normal async job calls dependencies in order and publishes metadata after upload`() {
        stubSuccessfulPipeline()

        directCollector.collect(COMMAND)

        inOrder(canonicalHasher, duplicateGate, objectKeyFactory, gzipCodec, storage, publisher) {
            verify(canonicalHasher).hash(RAW_JSON)
            verify(duplicateGate)
                .checkAndStore(
                    RawResponseDuplicateCheckCommand(
                        provider = FootballDataProvider.API_SPORTS,
                        endpointKey = "fixture_single",
                        apiId = "1208397",
                        canonicalHash = CANONICAL_HASH,
                    ),
                )
            verify(objectKeyFactory)
                .create(
                    RawResponseObjectKeyCommand(
                        provider = FootballDataProvider.API_SPORTS,
                        endpointKey = "fixture_single",
                        apiId = "1208397",
                        collectedAt = COLLECTED_AT,
                        canonicalHash = CANONICAL_HASH,
                    ),
                )
            verify(gzipCodec).compress(RAW_JSON)
            verify(storage)
                .upload(
                    RawResponseUploadCommand(
                        rawJsonObjectKey = OBJECT_KEY,
                        gzipBytes = GZIP_BYTES,
                    ),
                )
            verify(publisher)
                .publish(
                    argThat<RawResponseCollectedEvent> {
                        schemaVersion == 1 &&
                            eventId.isNotBlank() &&
                            provider == FootballDataProvider.API_SPORTS &&
                            endpointKey == "fixture_single" &&
                            apiId == "1208397" &&
                            canonicalHash == CANONICAL_HASH &&
                            rawJsonObjectKey == OBJECT_KEY &&
                            collectedAt == COLLECTED_AT &&
                            request == COMMAND.request
                    },
                )
        }
    }

    @Test
    fun `duplicate response skips object key gzip storage and publish`() {
        whenever(canonicalHasher.hash(RAW_JSON)).thenReturn(CANONICAL_HASH)
        whenever(duplicateGate.checkAndStore(any())).thenReturn(RawResponseDuplicateCheckResult.Duplicate)

        directCollector.collect(COMMAND)

        verify(objectKeyFactory, never()).create(any())
        verify(gzipCodec, never()).compress(any())
        verify(storage, never()).upload(any())
        verify(publisher, never()).publish(any())
    }

    @Test
    fun `duplicate gate failure skips object key gzip storage and publish`() {
        whenever(canonicalHasher.hash(RAW_JSON)).thenReturn(CANONICAL_HASH)
        whenever(duplicateGate.checkAndStore(any())).thenReturn(RawResponseDuplicateCheckResult.Failed("redis down"))

        directCollector.collect(COMMAND)

        verify(objectKeyFactory, never()).create(any())
        verify(gzipCodec, never()).compress(any())
        verify(storage, never()).upload(any())
        verify(publisher, never()).publish(any())
    }

    @Test
    fun `hash failure is swallowed and skips remaining pipeline`() {
        whenever(canonicalHasher.hash(RAW_JSON)).thenThrow(IllegalArgumentException("invalid json"))

        assertThatCode {
            directCollector.collect(COMMAND)
        }.doesNotThrowAnyException()
        verify(duplicateGate, never()).checkAndStore(any())
        verify(storage, never()).upload(any())
        verify(publisher, never()).publish(any())
    }

    @Test
    fun `gzip failure is swallowed and skips storage and publish`() {
        whenever(canonicalHasher.hash(RAW_JSON)).thenReturn(CANONICAL_HASH)
        whenever(duplicateGate.checkAndStore(any())).thenReturn(RawResponseDuplicateCheckResult.New)
        whenever(objectKeyFactory.create(any())).thenReturn(OBJECT_KEY)
        whenever(gzipCodec.compress(RAW_JSON)).thenThrow(IllegalStateException("gzip failed"))

        directCollector.collect(COMMAND)

        verify(storage, never()).upload(any())
        verify(publisher, never()).publish(any())
    }

    @Test
    fun `storage failure is swallowed and skips publish`() {
        whenever(canonicalHasher.hash(RAW_JSON)).thenReturn(CANONICAL_HASH)
        whenever(duplicateGate.checkAndStore(any())).thenReturn(RawResponseDuplicateCheckResult.New)
        whenever(objectKeyFactory.create(any())).thenReturn(OBJECT_KEY)
        whenever(gzipCodec.compress(RAW_JSON)).thenReturn(GZIP_BYTES)
        whenever(storage.upload(any())).thenThrow(IllegalStateException("storage failed"))

        assertThatCode {
            directCollector.collect(COMMAND)
        }.doesNotThrowAnyException()
        verify(publisher, never()).publish(any())
    }

    @Test
    fun `publish failure is swallowed`() {
        stubSuccessfulPipeline()
        whenever(publisher.publish(any())).thenThrow(IllegalStateException("publish failed"))

        assertThatCode {
            directCollector.collect(COMMAND)
        }.doesNotThrowAnyException()
    }

    private fun stubSuccessfulPipeline() {
        whenever(canonicalHasher.hash(RAW_JSON)).thenReturn(CANONICAL_HASH)
        whenever(duplicateGate.checkAndStore(any())).thenReturn(RawResponseDuplicateCheckResult.New)
        whenever(objectKeyFactory.create(any())).thenReturn(OBJECT_KEY)
        whenever(gzipCodec.compress(RAW_JSON)).thenReturn(GZIP_BYTES)
        whenever(storage.upload(any())).thenReturn(RawResponseStoredObject(rawJsonObjectKey = OBJECT_KEY))
    }

    private fun collector(executor: ThreadPoolTaskExecutor): DefaultApiSportsRawResponseCollector =
        DefaultApiSportsRawResponseCollector(
            taskExecutor = executor,
            canonicalHasher = canonicalHasher,
            duplicateGate = duplicateGate,
            objectKeyFactory = objectKeyFactory,
            gzipCodec = gzipCodec,
            storage = storage,
            publisher = publisher,
        )

    private class DirectThreadPoolTaskExecutor : ThreadPoolTaskExecutor() {
        override fun execute(task: Runnable) {
            task.run()
        }
    }

    private class CapturingExecutor : ThreadPoolTaskExecutor() {
        val tasks = mutableListOf<Runnable>()

        override fun execute(task: Runnable) {
            tasks.add(task)
        }
    }

    private class RejectingThreadPoolTaskExecutor : ThreadPoolTaskExecutor() {
        override fun execute(task: Runnable) {
            throw RejectedExecutionException("queue full")
        }
    }

    private companion object {
        private const val RAW_JSON = """{"response":[{"fixture":{"id":1208397}}]}"""
        private const val CANONICAL_HASH = "hash"
        private const val OBJECT_KEY = "data-quality/raw/api-sports/fixture_single/2026/07/02/1208397/object.json.gz"
        private val GZIP_BYTES = byteArrayOf(1, 2, 3)
        private val COLLECTED_AT: Instant = Instant.parse("2026-07-02T08:00:00Z")
        private val COMMAND =
            RawResponseCollectionCommand(
                provider = FootballDataProvider.API_SPORTS,
                endpointKey = "fixture_single",
                apiId = "1208397",
                rawJson = RAW_JSON,
                collectedAt = COLLECTED_AT,
                request =
                    RawResponseRequestMetadata(
                        method = "GET",
                        path = "/fixtures",
                        query = mapOf("id" to "1208397"),
                    ),
            )
    }
}
