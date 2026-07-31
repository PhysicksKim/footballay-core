package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseStoredObject
import com.footballay.core.infra.dataquality.raw.model.RawResponseUploadCommand
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.time.Clock
import java.time.Duration

/** 기본 S3 API로 원본을 저장하고 presigned GET URL을 생성합니다. */
class S3CompatibleRawResponseStorage(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    private val bucket: String,
    private val downloadUrlTtl: Duration,
    private val clock: Clock,
) : RawResponseStorage,
    RawResponseDownloadUrlGenerator {
    override fun upload(command: RawResponseUploadCommand): RawResponseStoredObject {
        s3Client.putObject(
            PutObjectRequest
                .builder()
                .bucket(bucket)
                .key(command.rawJsonObjectKey)
                .build(),
            RequestBody.fromBytes(command.gzipBytes),
        )
        return RawResponseStoredObject(command.rawJsonObjectKey)
    }

    override fun createDownloadUrl(rawJsonObjectKey: String): RawResponseDownloadUrl =
        RawResponseDownloadUrl(
            uri =
                s3Presigner
                    .presignGetObject(
                        GetObjectPresignRequest
                            .builder()
                            .signatureDuration(downloadUrlTtl)
                            .getObjectRequest(
                                GetObjectRequest
                                    .builder()
                                    .bucket(bucket)
                                    .key(rawJsonObjectKey)
                                    .build(),
                            ).build(),
                    ).url()
                    .toURI(),
            expiresAt = clock.instant().plus(downloadUrlTtl),
        )
}
