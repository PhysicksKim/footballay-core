package com.footballay.core.infra.dataquality.config

import com.footballay.core.infra.dataquality.raw.AWSRawResponseStorage
import com.footballay.core.infra.dataquality.raw.CloudFrontRawResponseSigner
import com.footballay.core.infra.dataquality.raw.DefaultS3RawResponseUploader
import com.footballay.core.infra.dataquality.raw.LocalRawResponseStorage
import com.footballay.core.infra.dataquality.raw.NoopCloudFrontRawResponseSigner
import com.footballay.core.infra.dataquality.raw.RawResponseStorage
import com.footballay.core.infra.dataquality.raw.S3RawResponseUploader
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.nio.file.Path

@Configuration
@ConditionalOnProperty(
    prefix = "footballay.data-quality",
    name = ["enabled", "storage.enabled"],
    havingValue = "true",
)
class DataQualityStorageConfig {
    @Bean
    @ConditionalOnProperty(
        prefix = "footballay.data-quality.storage",
        name = ["type"],
        havingValue = "local",
    )
    fun localRawResponseStorage(properties: DataQualityProperties): RawResponseStorage =
        LocalRawResponseStorage(
            baseDir = Path.of(properties.storage.localBaseDir),
            downloadUrlTtl = properties.storage.localDownloadUrlTtl,
        )

    @Bean
    @ConditionalOnProperty(
        prefix = "footballay.data-quality.storage",
        name = ["type"],
        havingValue = "s3",
    )
    fun dataQualityS3Client(properties: DataQualityProperties): S3Client {
        validateS3Properties(properties.storage)
        return S3Client
            .builder()
            .region(Region.of(properties.storage.region))
            .build()
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "footballay.data-quality.storage",
        name = ["type"],
        havingValue = "s3",
    )
    fun dataQualityS3RawResponseUploader(
        properties: DataQualityProperties,
        @Qualifier("dataQualityS3Client") s3Client: S3Client,
    ): S3RawResponseUploader =
        DefaultS3RawResponseUploader(
            s3Client = s3Client,
            bucket = properties.storage.bucket,
        )

    @Bean
    @ConditionalOnProperty(
        prefix = "footballay.data-quality.storage",
        name = ["type"],
        havingValue = "s3",
    )
    fun dataQualityCloudFrontRawResponseSigner(): CloudFrontRawResponseSigner =
        NoopCloudFrontRawResponseSigner()

    @Bean
    @ConditionalOnProperty(
        prefix = "footballay.data-quality.storage",
        name = ["type"],
        havingValue = "s3",
    )
    fun awsRawResponseStorage(
        uploader: S3RawResponseUploader,
        signer: CloudFrontRawResponseSigner,
    ): RawResponseStorage =
        AWSRawResponseStorage(
            uploader = uploader,
            signer = signer,
        )

    private fun validateS3Properties(storage: DataQualityProperties.Storage) {
        require(storage.bucket.isNotBlank()) {
            "footballay.data-quality.storage.bucket must not be blank"
        }
        require(storage.region.isNotBlank()) {
            "footballay.data-quality.storage.region must not be blank"
        }
        require(!storage.s3DownloadUrlTtl.isNegative && !storage.s3DownloadUrlTtl.isZero) {
            "footballay.data-quality.storage.s3-download-url-ttl must be greater than 0"
        }
        require(storage.preflight.keyPrefix.isNotBlank()) {
            "footballay.data-quality.storage.preflight.key-prefix must not be blank"
        }
    }
}
