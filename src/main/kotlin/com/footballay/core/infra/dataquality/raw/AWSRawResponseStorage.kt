package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrl
import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrlCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseStoredObject
import com.footballay.core.infra.dataquality.raw.model.RawResponseUploadCommand

class AWSRawResponseStorage : RawResponseStorage {
    override fun upload(command: RawResponseUploadCommand): RawResponseStoredObject =
        throw UnsupportedOperationException("AWS raw response storage is not implemented yet")

    override fun createDownloadUrl(command: RawResponseDownloadUrlCommand): RawResponseDownloadUrl =
        throw UnsupportedOperationException("AWS raw response storage is not implemented yet")
}
