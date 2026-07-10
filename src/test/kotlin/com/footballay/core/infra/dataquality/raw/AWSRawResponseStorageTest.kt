package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrl
import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrlCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseStoredObject
import com.footballay.core.infra.dataquality.raw.model.RawResponseUploadCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

class AWSRawResponseStorageTest {
    private val uploader = mock<S3RawResponseUploader>()
    private val signer = mock<CloudFrontRawResponseSigner>()
    private val storage = AWSRawResponseStorage(uploader, signer)

    @Test
    fun `upload delegates to s3 uploader`() {
        val command =
            RawResponseUploadCommand(
                rawJsonObjectKey = "data-quality/raw/object.json.gz",
                gzipBytes = byteArrayOf(1, 2, 3),
            )
        val storedObject = RawResponseStoredObject(rawJsonObjectKey = command.rawJsonObjectKey)
        whenever(uploader.upload(command)).thenReturn(storedObject)

        val result = storage.upload(command)

        assertThat(result).isEqualTo(storedObject)
        verify(uploader).upload(command)
    }

    @Test
    fun `create download url delegates to cloudfront signer`() {
        val command = RawResponseDownloadUrlCommand(rawJsonObjectKey = "data-quality/raw/object.json.gz")
        val downloadUrl =
            RawResponseDownloadUrl(
                downloadUrl = "https://static.example.com/data-quality/raw/object.json.gz",
                expiresAt = Instant.parse("2026-07-10T00:00:00Z"),
            )
        whenever(signer.createDownloadUrl(command)).thenReturn(downloadUrl)

        val result = storage.createDownloadUrl(command)

        assertThat(result).isEqualTo(downloadUrl)
        verify(signer).createDownloadUrl(command)
    }
}
