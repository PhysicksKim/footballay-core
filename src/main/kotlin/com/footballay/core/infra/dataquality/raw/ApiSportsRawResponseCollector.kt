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
import java.security.SecureRandom
import java.time.Instant

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
                "Failed to submit data quality raw collection task. provider={}, endpointKey={}, parameters={}",
                command.provider,
                command.endpointKey,
                command.parameters,
                ex,
            )
        }
    }

    private fun collectAsync(command: RawResponseCollectionCommand) {
        val canonicalHash = hashOrNull(command) ?: return
        if (isSamePrevious(command, canonicalHash)) return

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
                "Failed to hash data quality raw response. provider={}, endpointKey={}, parameters={}",
                command.provider,
                command.endpointKey,
                command.parameters,
                ex,
            )
            null
        }

    private fun isSamePrevious(
        command: RawResponseCollectionCommand,
        canonicalHash: String,
    ): Boolean {
        when (
            val duplicateResult =
                duplicateGate.checkAndStore(
                    RawResponseDuplicateCheckCommand(
                        provider = command.provider,
                        endpointKey = command.endpointKey,
                        parameters = command.parameters,
                        canonicalHash = canonicalHash,
                    ),
                )
        ) {
            RawResponseDuplicateCheckResult.New -> {
                return false
            }

            RawResponseDuplicateCheckResult.Duplicate -> {
                return true
            }

            is RawResponseDuplicateCheckResult.Failed -> {
                log.warn(
                    "Skipping data quality raw collection because duplicate gate failed. provider={}, endpointKey={}, parameters={}, reason={}",
                    command.provider,
                    command.endpointKey,
                    command.parameters,
                    duplicateResult.reason,
                )
                return true
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
                    parameters = command.parameters,
                    collectedAt = command.collectedAt,
                    canonicalHash = canonicalHash,
                ),
            )
        } catch (ex: Exception) {
            log.warn(
                "Failed to create data quality raw response object key. provider={}, endpointKey={}, parameters={}",
                command.provider,
                command.endpointKey,
                command.parameters,
                ex,
            )
            null
        }

    private fun gzipOrNull(command: RawResponseCollectionCommand): ByteArray? =
        try {
            gzipCodec.compress(command.rawJson)
        } catch (ex: Exception) {
            log.warn(
                "Failed to compress data quality raw response. provider={}, endpointKey={}, parameters={}",
                command.provider,
                command.endpointKey,
                command.parameters,
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
                "Failed to upload data quality raw response. provider={}, endpointKey={}, parameters={}, rawJsonObjectKey={}",
                command.provider,
                command.endpointKey,
                command.parameters,
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
                    rawEventId = UlidGenerator.generate(command.collectedAt),
                    provider = command.provider,
                    endpointKey = command.endpointKey,
                    parameters = command.parameters,
                    canonicalHash = canonicalHash,
                    rawJsonObjectKey = objectKey,
                    collectedAt = command.collectedAt,
                ),
            )
        } catch (ex: Exception) {
            log.warn(
                "Failed to publish data quality raw collected event. provider={}, endpointKey={}, parameters={}, rawJsonObjectKey={}",
                command.provider,
                command.endpointKey,
                command.parameters,
                objectKey,
                ex,
            )
        }
    }
}

private object UlidGenerator {
    private const val ENCODING = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
    private val random = SecureRandom()

    fun generate(now: Instant): String {
        val time = now.toEpochMilli()
        val bytes = ByteArray(16)
        bytes[0] = ((time ushr 40) and 0xFF).toByte()
        bytes[1] = ((time ushr 32) and 0xFF).toByte()
        bytes[2] = ((time ushr 24) and 0xFF).toByte()
        bytes[3] = ((time ushr 16) and 0xFF).toByte()
        bytes[4] = ((time ushr 8) and 0xFF).toByte()
        bytes[5] = (time and 0xFF).toByte()
        random.nextBytes(bytes, fromIndex = 6)
        return encode(bytes)
    }

    private fun SecureRandom.nextBytes(
        bytes: ByteArray,
        fromIndex: Int,
    ) {
        val randomBytes = ByteArray(bytes.size - fromIndex)
        nextBytes(randomBytes)
        randomBytes.copyInto(bytes, destinationOffset = fromIndex)
    }

    private fun encode(bytes: ByteArray): String {
        val chars = CharArray(26)
        chars[0] = ENCODING[((bytes[0].toInt() and 0xE0) ushr 5)]
        chars[1] = ENCODING[(bytes[0].toInt() and 0x1F)]
        chars[2] = ENCODING[((bytes[1].toInt() and 0xF8) ushr 3)]
        chars[3] = ENCODING[(((bytes[1].toInt() and 0x07) shl 2) or ((bytes[2].toInt() and 0xC0) ushr 6))]
        chars[4] = ENCODING[((bytes[2].toInt() and 0x3E) ushr 1)]
        chars[5] = ENCODING[(((bytes[2].toInt() and 0x01) shl 4) or ((bytes[3].toInt() and 0xF0) ushr 4))]
        chars[6] = ENCODING[(((bytes[3].toInt() and 0x0F) shl 1) or ((bytes[4].toInt() and 0x80) ushr 7))]
        chars[7] = ENCODING[((bytes[4].toInt() and 0x7C) ushr 2)]
        chars[8] = ENCODING[(((bytes[4].toInt() and 0x03) shl 3) or ((bytes[5].toInt() and 0xE0) ushr 5))]
        chars[9] = ENCODING[(bytes[5].toInt() and 0x1F)]

        var bitBuffer = 0
        var bitBufferLength = 0
        var charIndex = 10
        for (byteIndex in 6 until bytes.size) {
            bitBuffer = (bitBuffer shl 8) or (bytes[byteIndex].toInt() and 0xFF)
            bitBufferLength += 8
            while (bitBufferLength >= 5 && charIndex < chars.size) {
                bitBufferLength -= 5
                chars[charIndex++] = ENCODING[(bitBuffer ushr bitBufferLength) and 0x1F]
            }
        }
        if (charIndex < chars.size) {
            chars[charIndex] = ENCODING[(bitBuffer shl (5 - bitBufferLength)) and 0x1F]
        }
        return String(chars)
    }
}
