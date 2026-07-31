package com.footballay.core.infra.dataquality.raw

import java.net.URI
import java.time.Instant

data class RawResponseDownloadUrl(
    val uri: URI,
    val expiresAt: Instant,
)

/** 관리자 원본 응답 다운로드 URL을 생성하는 포트입니다. */
fun interface RawResponseDownloadUrlGenerator {
    fun createDownloadUrl(rawJsonObjectKey: String): RawResponseDownloadUrl
}

class NoopRawResponseDownloadUrlGenerator : RawResponseDownloadUrlGenerator {
    override fun createDownloadUrl(rawJsonObjectKey: String): RawResponseDownloadUrl =
        RawResponseDownloadUrl(URI.create("about:blank"), Instant.EPOCH)
}
