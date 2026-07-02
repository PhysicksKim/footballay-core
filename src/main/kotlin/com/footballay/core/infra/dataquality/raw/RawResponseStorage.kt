package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrl
import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrlCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseStoredObject
import com.footballay.core.infra.dataquality.raw.model.RawResponseUploadCommand
import java.time.Instant

interface RawResponseStorage {
    fun upload(command: RawResponseUploadCommand): RawResponseStoredObject

    fun createDownloadUrl(command: RawResponseDownloadUrlCommand): RawResponseDownloadUrl
}

class NoopRawResponseStorage : RawResponseStorage {
    override fun upload(command: RawResponseUploadCommand): RawResponseStoredObject =
        RawResponseStoredObject(rawJsonObjectKey = command.rawJsonObjectKey)

    override fun createDownloadUrl(command: RawResponseDownloadUrlCommand): RawResponseDownloadUrl =
        RawResponseDownloadUrl(
            downloadUrl = "",
            expiresAt = Instant.EPOCH,
        )
}
