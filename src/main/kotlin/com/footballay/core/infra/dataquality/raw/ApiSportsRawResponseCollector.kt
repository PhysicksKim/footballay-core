package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseCollectedEvent
import com.footballay.core.infra.dataquality.raw.model.RawResponseCollectionCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseDuplicateCheckCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseDuplicateCheckResult
import com.footballay.core.infra.dataquality.raw.model.RawResponseObjectKeyCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseStoredObject
import com.footballay.core.infra.dataquality.raw.model.RawResponseUploadCommand
import com.footballay.core.logger
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.UUID

interface ApiSportsRawResponseCollector {
    fun collect(command: RawResponseCollectionCommand)
}

class NoopApiSportsRawResponseCollector : ApiSportsRawResponseCollector {
    override fun collect(command: RawResponseCollectionCommand) = Unit
}

class DefaultApiSportsRawResponseCollector(
    private val taskExecutor: ThreadPoolTaskExecutor,
    private val canonicalHasher: RawResponseCanonicalHasher,
    private val duplicateGate: RawResponseDuplicateGate,
    private val objectKeyFactory: RawResponseObjectKeyFactory,
    private val gzipCodec: RawResponseGzipCodec,
    private val storage: RawResponseStorage,
    private val publisher: RawResponsePublisher,
) : ApiSportsRawResponseCollector {
    private val log = logger()

    override fun collect(command: RawResponseCollectionCommand) {
        try {
            taskExecutor.execute {
                collectAsync(command)
            }
        } catch (ex: Exception) {
            log.warn(
                "Failed to submit data quality raw collection task. provider={}, endpointKey={}, apiId={}",
                command.provider,
                command.endpointKey,
                command.apiId,
                ex,
            )
        }
    }

    private fun collectAsync(command: RawResponseCollectionCommand) {
        val canonicalHash = hashOrNull(command) ?: return
        if (!isNewResponse(command, canonicalHash)) return

        val objectKey = objectKeyOrNull(command, canonicalHash) ?: return
        val gzipBytes = gzipOrNull(command) ?: return
        val storedObject = uploadOrNull(command, objectKey, gzipBytes) ?: return

        publishSafely(command, canonicalHash, storedObject.rawJsonObjectKey)
    }

    private fun hashOrNull(command: RawResponseCollectionCommand): String? =
        try {
            canonicalHasher.hash(command.rawJson)
        } catch (ex: Exception) {
            log.warn(
                "Failed to hash data quality raw response. provider={}, endpointKey={}, apiId={}",
                command.provider,
                command.endpointKey,
                command.apiId,
                ex,
            )
            null
        }

    private fun isNewResponse(
        command: RawResponseCollectionCommand,
        canonicalHash: String,
    ): Boolean {
        when (
            val duplicateResult =
                duplicateGate.checkAndStore(
                    RawResponseDuplicateCheckCommand(
                        provider = command.provider,
                        endpointKey = command.endpointKey,
                        apiId = command.apiId,
                        canonicalHash = canonicalHash,
                    ),
                )
        ) {
            RawResponseDuplicateCheckResult.New -> {
                return true
            }

            RawResponseDuplicateCheckResult.Duplicate -> {
                return false
            }

            is RawResponseDuplicateCheckResult.Failed -> {
                log.warn(
                    "Skipping data quality raw collection because duplicate gate failed. provider={}, endpointKey={}, apiId={}, reason={}",
                    command.provider,
                    command.endpointKey,
                    command.apiId,
                    duplicateResult.reason,
                )
                return false
            }
        }
    }

    private fun objectKeyOrNull(
        command: RawResponseCollectionCommand,
        canonicalHash: String,
    ): String? =
        try {
            objectKeyFactory.create(
                RawResponseObjectKeyCommand(
                    provider = command.provider,
                    endpointKey = command.endpointKey,
                    apiId = command.apiId,
                    collectedAt = command.collectedAt,
                    canonicalHash = canonicalHash,
                ),
            )
        } catch (ex: Exception) {
            log.warn(
                "Failed to create data quality raw response object key. provider={}, endpointKey={}, apiId={}",
                command.provider,
                command.endpointKey,
                command.apiId,
                ex,
            )
            null
        }

    private fun gzipOrNull(command: RawResponseCollectionCommand): ByteArray? =
        try {
            gzipCodec.compress(command.rawJson)
        } catch (ex: Exception) {
            log.warn(
                "Failed to compress data quality raw response. provider={}, endpointKey={}, apiId={}",
                command.provider,
                command.endpointKey,
                command.apiId,
                ex,
            )
            null
        }

    private fun uploadOrNull(
        command: RawResponseCollectionCommand,
        objectKey: String,
        gzipBytes: ByteArray,
    ): RawResponseStoredObject? =
        try {
            storage.upload(
                RawResponseUploadCommand(
                    rawJsonObjectKey = objectKey,
                    gzipBytes = gzipBytes,
                ),
            )
        } catch (ex: Exception) {
            log.warn(
                "Failed to upload data quality raw response. provider={}, endpointKey={}, apiId={}, rawJsonObjectKey={}",
                command.provider,
                command.endpointKey,
                command.apiId,
                objectKey,
                ex,
            )
            null
        }

    private fun publishSafely(
        command: RawResponseCollectionCommand,
        canonicalHash: String,
        objectKey: String,
    ) {
        try {
            publisher.publish(
                RawResponseCollectedEvent(
                    eventId = UUID.randomUUID().toString(),
                    provider = command.provider,
                    endpointKey = command.endpointKey,
                    apiId = command.apiId,
                    canonicalHash = canonicalHash,
                    rawJsonObjectKey = objectKey,
                    collectedAt = command.collectedAt,
                    request = command.request,
                ),
            )
        } catch (ex: Exception) {
            log.warn(
                "Failed to publish data quality raw collected event. provider={}, endpointKey={}, apiId={}, rawJsonObjectKey={}",
                command.provider,
                command.endpointKey,
                command.apiId,
                objectKey,
                ex,
            )
        }
    }
}
