package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseDownloadUrlCommand
import com.footballay.core.infra.dataquality.raw.model.RawResponseUploadCommand
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class LocalRawResponseStorageTest {
    @TempDir
    lateinit var tempDir: Path

    private val fixedClock = Clock.fixed(Instant.parse("2026-07-02T08:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `upload writes bytes under object key`() {
        val storage = storage()
        val objectKey = "data-quality/raw/api-sports/fixture_single/2026/07/02/1208397/object.json.gz"
        val gzipBytes = byteArrayOf(1, 2, 3, 4)

        val stored =
            storage.upload(
                RawResponseUploadCommand(
                    rawJsonObjectKey = objectKey,
                    gzipBytes = gzipBytes,
                ),
            )

        assertThat(stored.rawJsonObjectKey).isEqualTo(objectKey)
        assertThat(Files.readAllBytes(tempDir.resolve(objectKey))).isEqualTo(gzipBytes)
    }

    @Test
    fun `upload creates parent directories`() {
        val storage = storage()
        val objectKey = "nested/path/object.json.gz"

        storage.upload(
            RawResponseUploadCommand(
                rawJsonObjectKey = objectKey,
                gzipBytes = byteArrayOf(1),
            ),
        )

        assertThat(Files.exists(tempDir.resolve("nested/path"))).isTrue()
        assertThat(Files.exists(tempDir.resolve(objectKey))).isTrue()
    }

    @Test
    fun `create download url returns file uri and expiry`() {
        val storage = storage()
        val objectKey = "data-quality/raw/object.json.gz"

        val downloadUrl =
            storage.createDownloadUrl(
                RawResponseDownloadUrlCommand(rawJsonObjectKey = objectKey),
            )

        assertThat(downloadUrl.downloadUrl).isEqualTo(tempDir.resolve(objectKey).toUri().toString())
        assertThat(downloadUrl.expiresAt).isEqualTo(Instant.parse("2026-07-02T08:10:00Z"))
    }

    @Test
    fun `rejects path traversal object key`() {
        val storage = storage()

        assertThatThrownBy {
            storage.upload(
                RawResponseUploadCommand(
                    rawJsonObjectKey = "../outside.json.gz",
                    gzipBytes = byteArrayOf(1),
                ),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("rawJsonObjectKey must not escape local storage base directory")
    }

    @Test
    fun `rejects blank object key`() {
        val storage = storage()

        assertThatThrownBy {
            storage.upload(
                RawResponseUploadCommand(
                    rawJsonObjectKey = " ",
                    gzipBytes = byteArrayOf(1),
                ),
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("rawJsonObjectKey must not be blank")
    }

    private fun storage(): LocalRawResponseStorage =
        LocalRawResponseStorage(
            baseDir = tempDir,
            downloadUrlTtl = Duration.ofMinutes(10),
            clock = fixedClock,
        )
}
