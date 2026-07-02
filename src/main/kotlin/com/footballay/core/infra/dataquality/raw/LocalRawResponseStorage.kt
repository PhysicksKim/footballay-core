package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrl
import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrlCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseStoredObject
import com.footballay.core.infra.dataquality.raw.model.RawResponseUploadCommand
import java.nio.file.Files
import java.nio.file.Path
import java.time.Clock
import java.time.Duration
import java.time.Instant

class LocalRawResponseStorage(
    baseDir: Path,
    private val downloadUrlTtl: Duration,
    private val clock: Clock = Clock.systemUTC(),
) : RawResponseStorage {
    private val baseDir: Path = baseDir.toAbsolutePath().normalize()

    override fun upload(command: RawResponseUploadCommand): RawResponseStoredObject {
        val objectPath = resolveObjectPath(command.rawJsonObjectKey)
        Files.createDirectories(objectPath.parent)
        Files.write(objectPath, command.gzipBytes)
        return RawResponseStoredObject(rawJsonObjectKey = command.rawJsonObjectKey)
    }

    override fun createDownloadUrl(command: RawResponseDownloadUrlCommand): RawResponseDownloadUrl {
        val objectPath = resolveObjectPath(command.rawJsonObjectKey)
        return RawResponseDownloadUrl(
            downloadUrl = objectPath.toUri().toString(),
            expiresAt = Instant.now(clock).plus(downloadUrlTtl),
        )
    }

    private fun resolveObjectPath(rawJsonObjectKey: String): Path {
        require(rawJsonObjectKey.isNotBlank()) {
            "rawJsonObjectKey must not be blank"
        }
        val objectPath = baseDir.resolve(rawJsonObjectKey).normalize()
        require(objectPath.startsWith(baseDir)) {
            "rawJsonObjectKey must not escape local storage base directory"
        }
        return objectPath
    }
}
