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
import java.util.UUID
import java.util.zip.GZIPOutputStream

/** SeaweedFS `weed mini`에서 기존 raw response S3 adapter의 다운로드 계약을 검증한다. */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SeaweedFsS3CompatibleRawResponseStorageIntegrationTest {
    private val bucket = "seaweedfs-adapter-${UUID.randomUUID().toString().replace("-", "")}".take(63)
    private val now = Instant.now()

    @BeforeAll
    fun createBucket() {
        s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build())
    }

    @Test
    @DisplayName("기존 raw response storage adapter가 SeaweedFS presigned GET으로 gzip byte를 보존한다.")
    fun uploadsAndDownloadsIdenticalGzipBytesThroughExistingAdapter() {
        val original = gzip("{\"response\":[{\"fixture\":{\"id\":1379192}}]}")
        val storage =
            S3CompatibleRawResponseStorage(
                s3Client = s3Client,
                s3Presigner = s3Presigner,
                bucket = bucket,
                downloadUrlTtl = Duration.ofMinutes(5),
                clock = Clock.fixed(now, ZoneOffset.UTC),
            )

        storage.upload(RawResponseUploadCommand("raw/seaweedfs.json.gz", original))
        val download = storage.createDownloadUrl("raw/seaweedfs.json.gz")
        val response =
            HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(download.uri).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray(),
            )

        assertThat(response.statusCode()).isEqualTo(200)
        assertThat(response.body()).containsExactly(*original)
    }

    private fun gzip(value: String): ByteArray =
        ByteArrayOutputStream().use { output ->
            GZIPOutputStream(output).use { it.write(value.toByteArray()) }
            output.toByteArray()
        }

    private companion object {
        private const val ACCESS_KEY = "seaweedfsadmin"
        private const val SECRET_KEY = "seaweedfsadmin"

        @Container
        @JvmStatic
        val seaweedFs = SeaweedFsMiniContainer()

        private val endpoint: URI
            get() = URI.create("http://${seaweedFs.host}:${seaweedFs.getMappedPort(8333)}")

        private val credentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY))

        private val s3Client: S3Client by lazy {
            S3Client.builder().endpointOverride(endpoint).region(Region.of("ap-northeast-2"))
                .credentialsProvider(credentials).serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()).build()
        }

        private val s3Presigner: S3Presigner by lazy {
            S3Presigner.builder().endpointOverride(endpoint).region(Region.of("ap-northeast-2"))
                .credentialsProvider(credentials).serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()).build()
        }
    }
}

private class SeaweedFsMiniContainer : GenericContainer<SeaweedFsMiniContainer>(DockerImageName.parse("chrislusf/seaweedfs:4.29")) {
    init {
        withEnv("AWS_ACCESS_KEY_ID", "seaweedfsadmin")
        withEnv("AWS_SECRET_ACCESS_KEY", "seaweedfsadmin")
        withCommand("mini", "-dir=/data")
        withExposedPorts(8333)
        waitingFor(Wait.forHttp("/").forPort(8333).forStatusCode(403))
    }
}
