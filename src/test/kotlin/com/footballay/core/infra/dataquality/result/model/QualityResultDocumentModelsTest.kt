package com.footballay.core.infra.dataquality.result.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.config.JacksonConfig
import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class QualityResultDocumentModelsTest {
    private val objectMapper: ObjectMapper = JacksonConfig().objectMapper()

    @Test
    fun `quality result document serializes data model spec fields`() {
        val document = qualityResultDocument()

        val json = objectMapper.readTree(objectMapper.writeValueAsString(document))

        assertThat(json["_id"].asText()).isEqualTo("01JZKT4NBQ9E2R4Y5A0K7J6F3M")
        assertThat(json.has("id")).isFalse()
        assertThat(json["rawEventId"].asText()).isEqualTo("01JZKT4M7QG3R6P2W8Y1V9N5XC")
        assertThat(json["provider"].asText()).isEqualTo("API_SPORTS")
        assertThat(json["endpointKey"].asText()).isEqualTo("fixtureSingle")
        assertThat(json["parameters"][0]["name"].asText()).isEqualTo("fixtureId")
        assertThat(json["parameters"][0]["value"].asText()).isEqualTo("123456")
        assertThat(json["canonicalHash"].asText()).isEqualTo("sha256-base64url")
        assertThat(json["rawJsonObjectKey"].asText()).contains(".json.gz")
        assertThat(json["checkedAt"].asText()).isEqualTo("2026-07-07T12:01:32Z")
        assertThat(json["scannerVersion"].asText()).isEqualTo("rule-v1.0.0")
        assertThat(json["hasIssue"].asBoolean()).isTrue()
        assertThat(json["issueCount"].asInt()).isEqualTo(1)
        assertThat(json["maxSeverity"].asText()).isEqualTo("WARN")
        assertThat(json["checkStatus"].asText()).isEqualTo("NEED_CHECK")
        assertThat(json["issues"][0]["issueInstanceId"].asText()).isEqualTo("01JZKT52A2S7KQZ3H8P4D6M1RB")
        assertThat(json["issues"][0]["checkStatus"].asText()).isEqualTo("NEED_CHECK")
        assertThat(json["issues"][0]["title"].asText()).isEqualTo("Substitution event has no player")
        assertThat(json["issues"][0]["responseLocation"]["section"].asText()).isEqualTo("events")
        assertThat(json["issues"][0]["evidence"]["rawFragment"]["type"].asText()).isEqualTo("subst")
        assertThat(json["issues"][0]["createdAt"].asText()).isEqualTo("2026-07-07T12:01:32Z")
        assertThat(json["issues"][0]["updatedAt"].asText()).isEqualTo("2026-07-07T12:01:32Z")
        assertThat(json["archive"]["status"].asText()).isEqualTo("NONE")
        assertThat(json["archive"]["objectKey"].isNull).isTrue()
    }

    @Test
    fun `quality result document does not serialize removed fields`() {
        val json = objectMapper.writeValueAsString(qualityResultDocument())

        assertThat(json).doesNotContain("parameterKey")
        assertThat(json).doesNotContain("parameterPathKey")
        assertThat(json).doesNotContain("issueTypeCodes")
        assertThat(json).doesNotContain("classificationStatus")
        assertThat(json).doesNotContain("description")
        assertThat(json).doesNotContain("message")
        assertThat(json).doesNotContain("adminNote")
        assertThat(json).doesNotContain("classifiedBy")
        assertThat(json).doesNotContain("classifiedAt")
        assertThat(json).doesNotContain("retentionProtected")
        assertThat(json).doesNotContain("archivedRawJsonObjectKey")
        assertThat(json).doesNotContain("reason")
        assertThat(json).doesNotContain("decidedBy")
        assertThat(json).doesNotContain("decidedAt")
    }

    @Test
    fun `mongo index definitions use current quality result document fields`() {
        assertThat(QualityResultMongoIndexDefinitions.qualityResults)
            .extracting<String> { it.name }
            .containsExactly(
                "quality_results_checked_at_desc",
                "quality_results_raw_event_id",
                "quality_results_source",
                "quality_results_canonical_hash",
                "quality_results_has_issue_checked_at_desc",
                "quality_results_max_severity_checked_at_desc",
                "quality_results_check_status_checked_at_desc",
                "quality_results_issue_suggested_type_code",
                "quality_results_issue_confirmed_type_code",
                "quality_results_archive_status_checked_at_desc",
                "quality_results_raw_json_object_key",
            )

        val rawEventIdIndex =
            QualityResultMongoIndexDefinitions.qualityResults
                .first { it.name == "quality_results_raw_event_id" }
        assertThat(rawEventIdIndex.unique).isTrue()
        assertThat(rawEventIdIndex.keys)
            .containsExactly(QualityResultMongoIndexKey("rawEventId", QualityResultMongoIndexDirection.ASC))

        val indexedFields =
            QualityResultMongoIndexDefinitions.qualityResults
                .flatMap { it.keys }
                .map { it.field }

        assertThat(indexedFields).doesNotContain("parameterKey")
        assertThat(indexedFields).doesNotContain("parameterPathKey")
        assertThat(indexedFields).doesNotContain("issueTypeCodes")
        assertThat(indexedFields).doesNotContain("classificationStatus")
        assertThat(indexedFields).doesNotContain("archive.retentionProtected")
    }

    @Test
    fun `closed quality result status values are fixed`() {
        assertThat(DataQualityMaxSeverity.entries.map { it.name })
            .containsExactly("NONE", "INFO", "WARN", "ERROR", "CRITICAL")
        assertThat(DataQualityIssueSeverity.entries.map { it.name })
            .containsExactly("INFO", "WARN", "ERROR", "CRITICAL")
        assertThat(DataQualityCheckStatus.entries.map { it.name })
            .containsExactly("NO_ISSUE", "NEED_CHECK", "PARTIALLY_CHECKED", "ALL_CHECKED", "CHECK_HOLD")
        assertThat(DataQualityIssueCheckStatus.entries.map { it.name })
            .containsExactly("NEED_CHECK", "ALL_CHECKED", "CHECK_HOLD")
        assertThat(DataQualityArchiveStatus.entries.map { it.name })
            .containsExactly("NONE", "ARCHIVED", "EXPIRED", "FAILED")
    }

    private fun qualityResultDocument(): QualityResultDocument =
        QualityResultDocument(
            id = "01JZKT4NBQ9E2R4Y5A0K7J6F3M",
            rawEventId = "01JZKT4M7QG3R6P2W8Y1V9N5XC",
            provider = FootballDataProvider.API_SPORTS,
            endpointKey = "fixtureSingle",
            parameters = listOf(QualityResultParameterDocument(name = "fixtureId", value = "123456")),
            canonicalHash = "sha256-base64url",
            rawJsonObjectKey =
                "football-data/raw/api-sports/fixture-single/2026/07/07/fixtureId-123456/" +
                    "20260707T120000Z_sha256.json.gz",
            checkedAt = CHECKED_AT,
            scannerVersion = "rule-v1.0.0",
            hasIssue = true,
            issueCount = 1,
            maxSeverity = DataQualityMaxSeverity.WARN,
            checkStatus = DataQualityCheckStatus.NEED_CHECK,
            issues =
                listOf(
                    QualityIssueDocument(
                        issueInstanceId = "01JZKT52A2S7KQZ3H8P4D6M1RB",
                        suggestedTypeCode = "EVENT_SUB_NO_PLAYER",
                        confirmedTypeCode = null,
                        checkStatus = DataQualityIssueCheckStatus.NEED_CHECK,
                        severity = DataQualityIssueSeverity.WARN,
                        title = "Substitution event has no player",
                        responseLocation =
                            QualityIssueResponseLocationDocument(
                                section = "events",
                                path = "$.response[0].events[32]",
                            ),
                        evidence =
                            mapOf(
                                "rawFragment" to
                                    mapOf(
                                        "type" to "subst",
                                        "player" to null,
                                        "assist" to null,
                                    ),
                            ),
                        createdAt = CHECKED_AT,
                        updatedAt = CHECKED_AT,
                    ),
                ),
            archive = QualityResultArchiveDocument(),
            createdAt = CHECKED_AT,
            updatedAt = CHECKED_AT,
        )

    private companion object {
        private val CHECKED_AT = Instant.parse("2026-07-07T12:01:32Z")
    }
}
