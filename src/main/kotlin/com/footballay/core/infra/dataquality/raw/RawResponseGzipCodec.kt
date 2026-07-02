package com.footballay.core.infra.dataquality.raw

import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

interface RawResponseGzipCodec {
    fun compress(rawJson: String): ByteArray

    fun decompress(gzipBytes: ByteArray): String
}

@Component
class DefaultRawResponseGzipCodec : RawResponseGzipCodec {
    override fun compress(rawJson: String): ByteArray {
        val rawBytes = rawJson.toByteArray(StandardCharsets.UTF_8)
        val output = ByteArrayOutputStream()

        GZIPOutputStream(output).use { gzip ->
            gzip.write(rawBytes)
        }

        return output.toByteArray()
    }

    override fun decompress(gzipBytes: ByteArray): String =
        GZIPInputStream(ByteArrayInputStream(gzipBytes)).use { gzip ->
            gzip.readBytes().toString(StandardCharsets.UTF_8)
        }
}
