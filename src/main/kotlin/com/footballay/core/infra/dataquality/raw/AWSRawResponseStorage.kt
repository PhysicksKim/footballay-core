package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrl
import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrlCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseStoredObject
import com.footballay.core.infra.dataquality.raw.model.RawResponseUploadCommand

class AWSRawResponseStorage(
    private val uploader: S3RawResponseUploader,
    private val signer: CloudFrontRawResponseSigner,
) : RawResponseStorage {
    override fun upload(command: RawResponseUploadCommand): RawResponseStoredObject =
        uploader.upload(command)

    override fun createDownloadUrl(command: RawResponseDownloadUrlCommand): RawResponseDownloadUrl =
        signer.createDownloadUrl(command)
}
