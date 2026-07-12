package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrl
import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrlCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseStoredObject
import com.footballay.core.infra.dataquality.raw.model.RawResponseUploadCommand
import com.footballay.core.logger
import java.time.Instant

interface RawResponseStorage {
    fun upload(command: RawResponseUploadCommand): RawResponseStoredObject

    fun createDownloadUrl(command: RawResponseDownloadUrlCommand): RawResponseDownloadUrl
}

class NoopRawResponseStorage : RawResponseStorage {
    private val log = logger()

    override fun upload(command: RawResponseUploadCommand): RawResponseStoredObject {
        log.info("No-op Raw Response upload")
        return RawResponseStoredObject(rawJsonObjectKey = command.rawJsonObjectKey)
    }

    override fun createDownloadUrl(command: RawResponseDownloadUrlCommand): RawResponseDownloadUrl {
        log.info("No-op raw response createDownloadUrl")
        return RawResponseDownloadUrl(
            downloadUrl = "",
            expiresAt = Instant.EPOCH,
        )
    }
}
