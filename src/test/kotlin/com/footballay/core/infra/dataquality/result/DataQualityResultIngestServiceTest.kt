package com.footballay.core.infra.dataquality.result

import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import com.footballay.core.infra.dataquality.result.model.DataQualityIssueItem
import com.footballay.core.infra.dataquality.result.model.DataQualityResultMessage
import com.footballay.core.infra.dataquality.result.model.DataQualityResultSummary
import com.footballay.core.infra.persistence.dataquality.repository.DataQualityResultLogRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataQualityResultIngestServiceTest {
    @Autowired
    private lateinit var ingestService: DataQualityResultIngestService

    @Autowired
    private lateinit var repository: DataQualityResultLogRepository

    @Test
    fun `result ingest saves log`() {
        val message = validMessage()

        val saved = ingestService.ingest(message)

        assertThat(saved).isNotNull
        assertThat(saved!!.id).isNotNull
        assertThat(saved.resultEventId).isEqualTo(message.eventId)
        assertThat(saved.rawEventId).isEqualTo(message.sourceEventId)
        assertThat(saved.issueCount).isEqualTo(2)
        assertThat(saved.resultJson).contains(message.eventId)
        assertThat(repository.findAll()).hasSize(1)
    }

    @Test
    fun `duplicate result_event_id handling`() {
        val message = validMessage()

        val first = ingestService.ingest(message)
        val second = ingestService.ingest(message)

        assertThat(first).isNotNull
        assertThat(second).isNull()
        assertThat(repository.count()).isEqualTo(1)
    }

    @Test
    fun `malformed result validation`() {
        val message = validMessage().copy(eventId = " ")

        org.assertj.core.api.Assertions.assertThatThrownBy {
            ingestService.ingest(message)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    private fun validMessage(): DataQualityResultMessage =
        DataQualityResultMessage(
            eventId = "result-event-1",
            sourceEventId = "raw-event-1",
            provider = FootballDataProvider.API_SPORTS,
            endpointKey = "fixture_single",
            apiId = "1208397",
            canonicalHash = "sha256-base64url",
            rawJsonObjectKey = "data-quality/raw/api-sports/fixture_single/2026/07/02/1208397/sample.json.gz",
            checkedAt = Instant.parse("2026-07-02T08:00:10Z"),
            scannerVersion = "rule-2026-07-02",
            summary = DataQualityResultSummary(issueCount = 2),
            items = listOf(
                DataQualityIssueItem(
                    code = "EVENT_NEGATIVE_ELAPSED_CARD",
                    severity = "ERROR",
                    classification = "CONFIRMED_ISSUE",
                    path = "$.response[0].events[12].time.elapsed",
                    message = "Card event elapsed minute is negative.",
                ),
            ),
        )
}
