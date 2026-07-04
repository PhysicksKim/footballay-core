package com.footballay.core.web.admin.dataquality.service

import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.infra.persistence.dataquality.entity.DataQualityResultLog
import com.footballay.core.infra.persistence.dataquality.repository.DataQualityResultLogRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = ["ADMIN"])
class AdminDataQualityLogQueryServiceTest {
    @Autowired
    private lateinit var service: AdminDataQualityLogQueryWebService

    @Autowired
    private lateinit var repository: DataQualityResultLogRepository

    @Test
    fun `filters logs by provider endpoint api checkedAt range and has issue`() {
        val matched =
            repository.saveAndFlush(
                log(
                    resultEventId = "stage12-result-1",
                    rawEventId = "stage12-raw-1",
                    endpointKey = "fixture_single",
                    apiId = "1208397",
                    checkedAt = Instant.parse("2026-07-02T08:00:00Z"),
                    issueCount = 2,
                ),
            )
        repository.saveAndFlush(
            log(
                resultEventId = "stage12-result-2",
                rawEventId = "stage12-raw-2",
                endpointKey = "fixture_single",
                apiId = "1208397",
                checkedAt = Instant.parse("2026-07-02T09:00:00Z"),
                issueCount = 0,
            ),
        )
        repository.saveAndFlush(
            log(
                resultEventId = "stage12-result-3",
                rawEventId = "stage12-raw-3",
                endpointKey = "team_squad",
                apiId = "33",
                checkedAt = Instant.parse("2026-07-02T08:30:00Z"),
                issueCount = 3,
            ),
        )

        val result =
            service.findLogs(
                provider = FootballDataProvider.API_SPORTS,
                endpointKey = "fixture_single",
                apiId = "1208397",
                checkedAtFrom = Instant.parse("2026-07-02T07:59:00Z"),
                checkedAtTo = Instant.parse("2026-07-02T08:01:00Z"),
                hasIssue = true,
                page = 0,
                size = 50,
            )

        assertThat(result.totalElements).isEqualTo(1)
        assertThat(result.content).hasSize(1)
        assertThat(result.content.first().id).isEqualTo(matched.id)
        assertThat(result.content.first().hasIssue).isTrue()
        assertThat(result.content.first().rawJsonObjectKey).contains("stage12-result-1")
    }

    @Test
    fun `returns issue free logs when hasIssue is false`() {
        repository.saveAndFlush(
            log(
                resultEventId = "stage12-result-4",
                rawEventId = "stage12-raw-4",
                endpointKey = "fixture_single",
                apiId = "1208398",
                checkedAt = Instant.parse("2026-07-02T10:00:00Z"),
                issueCount = 0,
            ),
        )
        repository.saveAndFlush(
            log(
                resultEventId = "stage12-result-5",
                rawEventId = "stage12-raw-5",
                endpointKey = "fixture_single",
                apiId = "1208398",
                checkedAt = Instant.parse("2026-07-02T10:01:00Z"),
                issueCount = 1,
            ),
        )

        val result =
            service.findLogs(
                provider = null,
                endpointKey = "fixture_single",
                apiId = "1208398",
                checkedAtFrom = null,
                checkedAtTo = null,
                hasIssue = false,
                page = 0,
                size = 50,
            )

        assertThat(result.page).isZero()
        assertThat(result.size).isEqualTo(50)
        assertThat(result.totalElements).isEqualTo(1)
        assertThat(result.content.first().issueCount).isZero()
    }

    @Test
    fun `find logs rejects invalid web request`() {
        assertThatThrownBy {
            service.findLogs(
                provider = null,
                endpointKey = " ",
                apiId = null,
                checkedAtFrom = Instant.parse("2026-07-03T00:00:00Z"),
                checkedAtTo = Instant.parse("2026-07-02T00:00:00Z"),
                hasIssue = null,
                page = -1,
                size = 500,
            )
        }.isInstanceOf(ResponseStatusException::class.java)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `repository query honors pageable sort`() {
        val older =
            repository.saveAndFlush(
                log(
                    resultEventId = "stage12-result-sort-1",
                    rawEventId = "stage12-raw-sort-1",
                    endpointKey = "sort_probe",
                    apiId = "1208400",
                    checkedAt = Instant.parse("2026-07-02T12:00:00Z"),
                    issueCount = 0,
                ),
            )
        val newer =
            repository.saveAndFlush(
                log(
                    resultEventId = "stage12-result-sort-2",
                    rawEventId = "stage12-raw-sort-2",
                    endpointKey = "sort_probe",
                    apiId = "1208401",
                    checkedAt = Instant.parse("2026-07-02T13:00:00Z"),
                    issueCount = 0,
                ),
            )

        val result =
            repository.findLogs(
                provider = FootballDataProvider.API_SPORTS,
                endpointKey = "sort_probe",
                apiId = null,
                checkedAtFrom = null,
                checkedAtTo = null,
                hasIssue = null,
                pageable = PageRequest.of(0, 50, Sort.by(Sort.Order.asc("checkedAt"))),
            )

        assertThat(result.content.map { it.id }).containsExactly(older.id, newer.id)
    }

    @Test
    fun `detail returns parsed result json`() {
        val saved =
            repository.saveAndFlush(
                log(
                    resultEventId = "stage12-result-6",
                    rawEventId = "stage12-raw-6",
                    endpointKey = "fixture_single",
                    apiId = "1208399",
                    checkedAt = Instant.parse("2026-07-02T11:00:00Z"),
                    issueCount = 1,
                ),
            )

        val result = service.getLog(requireNotNull(saved.id))

        assertThat(result.id).isEqualTo(saved.id)
        assertThat(result.result["eventId"].asText()).isEqualTo("stage12-result-6")
        assertThat(result.result["summary"]["issueCount"].asInt()).isEqualTo(1)
    }

    @Test
    fun `missing detail throws 404`() {
        assertThatThrownBy {
            service.getLog(999_999_999L)
        }.isInstanceOf(ResponseStatusException::class.java)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.NOT_FOUND)
    }

    private fun log(
        resultEventId: String,
        rawEventId: String,
        endpointKey: String,
        apiId: String,
        checkedAt: Instant,
        issueCount: Int,
    ) = DataQualityResultLog(
        resultEventId = resultEventId,
        rawEventId = rawEventId,
        provider = FootballDataProvider.API_SPORTS,
        endpointKey = endpointKey,
        apiId = apiId,
        canonicalHash = "sha256-$resultEventId",
        rawJsonObjectKey = "data-quality/raw/api-sports/$endpointKey/$apiId/$resultEventId.json.gz",
        scannerVersion = "rule-2026-07-02",
        checkedAt = checkedAt,
        issueCount = issueCount,
        resultJson =
            """
            {
              "eventId": "$resultEventId",
              "sourceEventId": "$rawEventId",
              "summary": {
                "issueCount": $issueCount
              }
            }
            """.trimIndent(),
    )
}
