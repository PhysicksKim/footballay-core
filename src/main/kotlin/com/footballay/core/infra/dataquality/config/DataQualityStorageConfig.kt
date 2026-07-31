package com.footballay.core.infra.dataquality.config

import com.footballay.core.infra.dataquality.raw.NoopRawResponseDownloadUrlGenerator
import com.footballay.core.infra.dataquality.raw.NoopRawResponseStorage
import com.footballay.core.infra.dataquality.raw.RawResponseDownloadUrlGenerator
import com.footballay.core.infra.dataquality.raw.RawResponseStorage
import com.footballay.core.infra.dataquality.raw.S3CompatibleRawResponseStorage
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI
import java.time.Clock

@Configuration
@ConditionalOnProperty(
    prefix = "footballay.data-quality",
    name = ["enabled", "storage.enabled"],
    havingValue = "true",
)
class DataQualityStorageConfig {
    @Bean
    @ConditionalOnProperty(prefix = "footballay.data-quality.storage", name = ["type"], havingValue = "noop")
    fun noopRawResponseStorage(): RawResponseStorage = NoopRawResponseStorage()

    @Bean
    @ConditionalOnProperty(prefix = "footballay.data-quality.storage", name = ["type"], havingValue = "noop")
    fun noopRawResponseDownloadUrlGenerator(): RawResponseDownloadUrlGenerator = NoopRawResponseDownloadUrlGenerator()

    @Bean
    @ConditionalOnProperty(prefix = "footballay.data-quality.storage", name = ["type"], havingValue = "s3")
    fun s3CompatibleRawResponseStorage(
        properties: DataQualityProperties,
        dataQualityS3Client: S3Client,
        dataQualityS3Presigner: S3Presigner,
    ): S3CompatibleRawResponseStorage {
        validateS3Properties(properties.storage)
        return S3CompatibleRawResponseStorage(
            s3Client = dataQualityS3Client,
            s3Presigner = dataQualityS3Presigner,
            bucket = properties.storage.bucket,
            downloadUrlTtl = properties.storage.s3DownloadUrlTtl,
            clock = Clock.systemUTC(),
        )
    }

    @Bean
    @ConditionalOnProperty(prefix = "footballay.data-quality.storage", name = ["type"], havingValue = "s3")
    fun dataQualityS3Client(properties: DataQualityProperties): S3Client = createS3Client(properties.storage)

    @Bean
    @ConditionalOnProperty(prefix = "footballay.data-quality.storage", name = ["type"], havingValue = "s3")
    fun dataQualityS3Presigner(properties: DataQualityProperties): S3Presigner = createS3Presigner(properties.storage)

    private fun validateS3Properties(storage: DataQualityProperties.Storage) {
        require(storage.bucket.isNotBlank()) {
            "footballay.data-quality.storage.bucket must not be blank"
        }
        require(storage.region.isNotBlank()) {
            "footballay.data-quality.storage.region must not be blank"
        }
        require((storage.accessKey.isNullOrBlank()) == (storage.secretKey.isNullOrBlank())) {
            "footballay.data-quality.storage.access-key and secret-key must be configured together"
        }
        require(!storage.s3DownloadUrlTtl.isNegative && !storage.s3DownloadUrlTtl.isZero) {
            "footballay.data-quality.storage.s3-download-url-ttl must be greater than 0"
        }
    }

    private fun createS3Client(storage: DataQualityProperties.Storage): S3Client {
        val builder =
            S3Client
                .builder()
                .region(Region.of(storage.region))
                .credentialsProvider(credentialsProvider(storage))
                .serviceConfiguration(s3Configuration(storage))
        storage.endpoint?.takeIf(String::isNotBlank)?.let { builder.endpointOverride(URI.create(it)) }
        return builder.build()
    }

    private fun createS3Presigner(storage: DataQualityProperties.Storage): S3Presigner {
        val builder =
            S3Presigner
                .builder()
                .region(Region.of(storage.region))
                .credentialsProvider(credentialsProvider(storage))
                .serviceConfiguration(s3Configuration(storage))
        storage.endpoint?.takeIf(String::isNotBlank)?.let { builder.endpointOverride(URI.create(it)) }
        return builder.build()
    }

    private fun credentialsProvider(storage: DataQualityProperties.Storage) =
        if (storage.accessKey.isNullOrBlank()) {
            DefaultCredentialsProvider.create()
        } else {
            StaticCredentialsProvider.create(AwsBasicCredentials.create(storage.accessKey, storage.secretKey))
        }

    private fun s3Configuration(storage: DataQualityProperties.Storage): S3Configuration = S3Configuration.builder().pathStyleAccessEnabled(storage.pathStyleAccessEnabled).build()
}
