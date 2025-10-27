package com.footballay.core.infra.persistence.apisports.entity

import jakarta.persistence.*
import java.time.OffsetDateTime

enum class DataProvider {
    API_SPORTS,
}

enum class DiscrepancyState {
    OPEN,
    RESOLVED,
}

enum class DiscrepancyReason {
    MISSING_IN_SNAPSHOT,
    SUSPECT_ERROR,
    CANCELLED_SUSPECT,
}

@Entity
@Table(
    name = "refac_fixture_provider_discrepancy",
    indexes = [
        Index(name = "idx_discrepancy_provider_fixture", columnList = "provider, fixture_api_id"),
        Index(name = "idx_discrepancy_scope_state", columnList = "provider, league_api_id, season_year, state"),
        Index(name = "idx_discrepancy_state_checked", columnList = "state, last_checked_at")
    ]
)
data class FixtureProviderDiscrepancy(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var provider: DataProvider = DataProvider.API_SPORTS,

    @Column(name = "fixture_api_id", nullable = false)
    var fixtureApiId: Long,

    @Column(name = "league_api_id", nullable = false)
    var leagueApiId: Long,

    @Column(name = "season_year", nullable = false)
    var seasonYear: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var state: DiscrepancyState = DiscrepancyState.OPEN,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var reason: DiscrepancyReason = DiscrepancyReason.MISSING_IN_SNAPSHOT,

    @Column(name = "first_detected_at", nullable = false)
    var firstDetectedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "last_checked_at")
    var lastCheckedAt: OffsetDateTime? = null,

    @Column(name = "last_seen_at")
    var lastSeenAt: OffsetDateTime? = null,

    @Column(name = "resolved_at")
    var resolvedAt: OffsetDateTime? = null,

    @Column(name = "snapshot_id_last_seen")
    var snapshotIdLastSeen: String? = null,

    @Column(name = "snapshot_id_checked")
    var snapshotIdChecked: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_fixture_id")
    var linkedFixture: FixtureApiSports? = null,
)


