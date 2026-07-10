package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrlCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseUploadCommand
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class AWSRawResponseStorageTest {
    private val storage = AWSRawResponseStorage()

    @Test
    fun `upload is not implemented yet`() {
        assertThatThrownBy {
            storage.upload(
                RawResponseUploadCommand(
                    rawJsonObjectKey = "data-quality/raw/object.json.gz",
                    gzipBytes = byteArrayOf(1, 2, 3),
                ),
            )
        }.isInstanceOf(UnsupportedOperationException::class.java)
            .hasMessage("AWS raw response storage is not implemented yet")
    }

    @Test
    fun `create download url is not implemented yet`() {
        assertThatThrownBy {
            storage.createDownloadUrl(
                RawResponseDownloadUrlCommand(rawJsonObjectKey = "data-quality/raw/object.json.gz"),
            )
        }.isInstanceOf(UnsupportedOperationException::class.java)
            .hasMessage("AWS raw response storage is not implemented yet")
    }
}
