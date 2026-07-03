package com.footballay.core.infra.persistence.dataquality.entity

import com.footballay.core.infra.dataquality.raw.model.FootballDataProvider
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "data_quality_result_log",
    indexes = [
        Index(name = "idx_data_quality_result_log_provider_endpoint_api", columnList = "provider,endpoint_key,api_id"),
        Index(name = "idx_data_quality_result_log_checked_at", columnList = "checked_at"),
        Index(name = "idx_data_quality_result_log_issue_count", columnList = "issue_count"),
        Index(name = "idx_data_quality_result_log_raw_event_id", columnList = "raw_event_id"),
        Index(name = "idx_data_quality_result_log_canonical_hash", columnList = "canonical_hash"),
    ],
)
data class DataQualityResultLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "result_event_id", nullable = false, unique = true, updatable = false)
    var resultEventId: String,
    @Column(name = "raw_event_id", nullable = false, updatable = false)
    var rawEventId: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    var provider: FootballDataProvider,
    @Column(name = "endpoint_key", nullable = false, updatable = false)
    var endpointKey: String,
    @Column(name = "api_id", nullable = false, updatable = false)
    var apiId: String,
    @Column(name = "canonical_hash", nullable = false, updatable = false)
    var canonicalHash: String,
    @Column(name = "raw_json_object_key", nullable = false, updatable = false)
    var rawJsonObjectKey: String,
    @Column(name = "scanner_version", nullable = false, updatable = false)
    var scannerVersion: String,
    @Column(name = "checked_at", nullable = false, updatable = false)
    var checkedAt: Instant,
    @Column(name = "issue_count", nullable = false)
    var issueCount: Int,
    @Column(name = "result_json", nullable = false, columnDefinition = "TEXT")
    var resultJson: String,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)

