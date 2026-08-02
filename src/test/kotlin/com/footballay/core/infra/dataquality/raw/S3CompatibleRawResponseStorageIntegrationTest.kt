package com.footballay.core.infra.dataquality.raw

import com.footballay.core.infra.dataquality.raw.model.RawResponseUploadCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.zip.GZIPOutputStream

/** SeaweedFS `weed mini` endpoint에서 S3 path-style presigned GET 계약을 검증합니다. */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class S3CompatibleRawResponseStorageIntegrationTest {
    private val now = Instant.now()
    private val gzipBytes = gzip("{\"response\":[{\"fixture\":{\"id\":1}}]}")

    @BeforeAll
    fun createBucket() {
        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build())
    }

    @Test
    @DisplayName("raw response storage가 SeaweedFS presigned GET으로 gzip byte를 보존한다.")
    fun `uploads gzip bytes and downloads identical bytes through a presigned GET URL`() {
        val storage =
            S3CompatibleRawResponseStorage(
                s3Client = s3Client,
                s3Presigner = s3Presigner,
                bucket = BUCKET,
                downloadUrlTtl = TTL,
                clock = Clock.fixed(now, ZoneOffset.UTC),
            )

        storage.upload(RawResponseUploadCommand(OBJECT_KEY, gzipBytes))
        val download = storage.createDownloadUrl(OBJECT_KEY)
        val response =
            HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(download.uri).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray(),
            )

        assertThat(response.statusCode()).isEqualTo(200)
        assertThat(response.body()).containsExactly(*gzipBytes)
        assertThat(download.expiresAt).isEqualTo(now.plus(TTL))
        assertThat(download.expiresAt).isAfter(now)
    }

    private fun gzip(value: String): ByteArray =
        ByteArrayOutputStream().use { bytes ->
            GZIPOutputStream(bytes).use { it.write(value.toByteArray()) }
            bytes.toByteArray()
        }

    private companion object {
        const val BUCKET = "footballay-data-quality-test"
        const val OBJECT_KEY = "data-quality/raw/api-sports/fixture-single/test.json.gz"
        val TTL: Duration = Duration.ofMinutes(5)

        @Container
        @JvmStatic
        val seaweedFs = SeaweedFsMiniContainer()

        val endpoint: URI
            get() = URI.create("http://${seaweedFs.host}:${seaweedFs.getMappedPort(8333)}")

        val credentials = StaticCredentialsProvider.create(AwsBasicCredentials.create("seaweedfsadmin", "seaweedfsadmin"))

        val s3Client: S3Client by lazy {
            S3Client
                .builder()
                .endpointOverride(endpoint)
                .region(Region.of("ap-northeast-2"))
                .credentialsProvider(credentials)
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build()
        }

        val s3Presigner: S3Presigner by lazy {
            S3Presigner
                .builder()
                .endpointOverride(endpoint)
                .region(Region.of("ap-northeast-2"))
                .credentialsProvider(credentials)
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build()
        }
    }
}

private class SeaweedFsMiniContainer :
    GenericContainer<SeaweedFsMiniContainer>(
        DockerImageName.parse("chrislusf/seaweedfs:4.29"),
    ) {
    init {
        withEnv("AWS_ACCESS_KEY_ID", "seaweedfsadmin")
        withEnv("AWS_SECRET_ACCESS_KEY", "seaweedfsadmin")
        withCommand("mini", "-dir=/data")
        withExposedPorts(8333)
        waitingFor(Wait.forHttp("/").forPort(8333).forStatusCode(403))
    }
}
