package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseStoredObject
import com.footballay.core.infra.dataquality.raw.model.RawResponseUploadCommand
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

interface S3RawResponseUploader {
    fun upload(command: RawResponseUploadCommand): RawResponseStoredObject
}

class DefaultS3RawResponseUploader(
    private val s3Client: S3Client,
    private val bucket: String,
) : S3RawResponseUploader {
    override fun upload(command: RawResponseUploadCommand): RawResponseStoredObject {
        require(command.rawJsonObjectKey.isNotBlank()) {
            "rawJsonObjectKey must not be blank"
        }

        val request =
            PutObjectRequest
                .builder()
                .bucket(bucket)
                .key(command.rawJsonObjectKey)
                .contentType(command.contentType)
                .build()

        s3Client.putObject(request, RequestBody.fromBytes(command.gzipBytes))
        return RawResponseStoredObject(rawJsonObjectKey = command.rawJsonObjectKey)
    }
}
