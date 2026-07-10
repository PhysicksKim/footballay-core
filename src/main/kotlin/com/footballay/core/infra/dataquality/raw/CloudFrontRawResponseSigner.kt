package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrl
import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrlCommand
import java.time.Instant

interface CloudFrontRawResponseSigner {
    fun createDownloadUrl(command: RawResponseDownloadUrlCommand): RawResponseDownloadUrl
}

class NoopCloudFrontRawResponseSigner : CloudFrontRawResponseSigner {
    override fun createDownloadUrl(command: RawResponseDownloadUrlCommand): RawResponseDownloadUrl {
        require(command.rawJsonObjectKey.isNotBlank()) {
            "rawJsonObjectKey must not be blank"
        }

        return RawResponseDownloadUrl(
            downloadUrl = "",
            expiresAt = Instant.EPOCH,
        )
    }
}
