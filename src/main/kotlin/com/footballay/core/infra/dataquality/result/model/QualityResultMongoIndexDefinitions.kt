package com.footballay.core.infra.dataquality.result.model

// quality_results collection의 초기 index 계약을 정의한다.
object QualityResultMongoIndexDefinitions {
    val qualityResults: List<QualityResultMongoIndexDefinition> =
        listOf(
            QualityResultMongoIndexDefinition(
                name = "quality_results_checked_at_desc",
                keys = listOf(QualityResultMongoIndexKey("checkedAt", QualityResultMongoIndexDirection.DESC)),
            ),
            QualityResultMongoIndexDefinition(
                name = "quality_results_raw_event_id",
                keys = listOf(QualityResultMongoIndexKey("rawEventId", QualityResultMongoIndexDirection.ASC)),
                unique = true,
            ),
            QualityResultMongoIndexDefinition(
                name = "quality_results_source",
                keys =
                    listOf(
                        QualityResultMongoIndexKey("provider", QualityResultMongoIndexDirection.ASC),
                        QualityResultMongoIndexKey("endpointKey", QualityResultMongoIndexDirection.ASC),
                        QualityResultMongoIndexKey("parameters.name", QualityResultMongoIndexDirection.ASC),
                        QualityResultMongoIndexKey("parameters.value", QualityResultMongoIndexDirection.ASC),
                    ),
            ),
            QualityResultMongoIndexDefinition(
                name = "quality_results_canonical_hash",
                keys = listOf(QualityResultMongoIndexKey("canonicalHash", QualityResultMongoIndexDirection.ASC)),
            ),
            QualityResultMongoIndexDefinition(
                name = "quality_results_has_issue_checked_at_desc",
                keys =
                    listOf(
                        QualityResultMongoIndexKey("hasIssue", QualityResultMongoIndexDirection.ASC),
                        QualityResultMongoIndexKey("checkedAt", QualityResultMongoIndexDirection.DESC),
                    ),
            ),
            QualityResultMongoIndexDefinition(
                name = "quality_results_max_severity_checked_at_desc",
                keys =
                    listOf(
                        QualityResultMongoIndexKey("maxSeverity", QualityResultMongoIndexDirection.ASC),
                        QualityResultMongoIndexKey("checkedAt", QualityResultMongoIndexDirection.DESC),
                    ),
            ),
            QualityResultMongoIndexDefinition(
                name = "quality_results_check_status_checked_at_desc",
                keys =
                    listOf(
                        QualityResultMongoIndexKey("checkStatus", QualityResultMongoIndexDirection.ASC),
                        QualityResultMongoIndexKey("checkedAt", QualityResultMongoIndexDirection.DESC),
                    ),
            ),
            QualityResultMongoIndexDefinition(
                name = "quality_results_issue_suggested_type_code",
                keys = listOf(QualityResultMongoIndexKey("issues.suggestedTypeCode", QualityResultMongoIndexDirection.ASC)),
            ),
            QualityResultMongoIndexDefinition(
                name = "quality_results_issue_confirmed_type_code",
                keys = listOf(QualityResultMongoIndexKey("issues.confirmedTypeCode", QualityResultMongoIndexDirection.ASC)),
            ),
            QualityResultMongoIndexDefinition(
                name = "quality_results_archive_status_checked_at_desc",
                keys =
                    listOf(
                        QualityResultMongoIndexKey("archive.status", QualityResultMongoIndexDirection.ASC),
                        QualityResultMongoIndexKey("checkedAt", QualityResultMongoIndexDirection.DESC),
                    ),
            ),
            QualityResultMongoIndexDefinition(
                name = "quality_results_raw_json_object_key",
                keys = listOf(QualityResultMongoIndexKey("rawJsonObjectKey", QualityResultMongoIndexDirection.ASC)),
            ),
        )
}

data class QualityResultMongoIndexDefinition(
    val name: String,
    val collection: String = DataQualityMongoCollections.QUALITY_RESULTS,
    val keys: List<QualityResultMongoIndexKey>,
    val unique: Boolean = false,
)

data class QualityResultMongoIndexKey(
    val field: String,
    val direction: QualityResultMongoIndexDirection,
)

enum class QualityResultMongoIndexDirection {
    ASC,
    DESC,
}
