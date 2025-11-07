package com.footballay.core.infra.persistence.apisports.entity

import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import jakarta.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.time.Instant

@Entity
// @Table(
//    name = "fixture_apisports",
//    indexes = [
//        Index(name = "idx_fixture_apisports_core_id", columnList = "fixture_core_id"),
//    ],
// )
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class FixtureApiSports(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_core_id", referencedColumnName = "id")
    var core: FixtureCore? = null,
    @Column(name = "api_id", unique = true, nullable = false)
    var apiId: Long,
    var referee: String? = null,
    var timezone: String? = null,
    var date: Instant? = null,
    var round: String? = null,
    /**
     * ApiSports 측에서 제공하는 fixture 업데이트를 방지하기 위한 플래그입니다.
     *
     * 이 플래그가 true로 설정된 경우, ApiSports 데이터를 받아와 저장하는 과정을 무시하고
     * 기존에 저장된 fixture 정보를 그대로 유지합니다.
     */
    var preventUpdate: Boolean = false,
    @Column(nullable = false)
    var available: Boolean = false,
    @Embedded
    var status: ApiSportsStatus? = null,
    @Embedded
    var score: ApiSportsScore? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    var venue: VenueApiSports? = null,
    /**
     * 경우에 따라 간혹 ApiSports 측에서 orphan fixture 가 발생할 수 있으므로 이때 season 을 null 로 설정하여 리그 경기일정 조회에서 제외시키도록 합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    var season: LeagueApiSportsSeason?,
    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    var homeTeam: ApiSportsMatchTeam? = null,
    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    var awayTeam: ApiSportsMatchTeam? = null,
    @OneToMany(mappedBy = "fixtureApi", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var events: MutableList<ApiSportsMatchEvent> = mutableListOf(),
) {
    override fun toString(): String =
        "FixtureApiSports(id=$id, core.id=${core?.id}, apiId=$apiId, referee=$referee, timezone=$timezone, " +
            "date=$date, round=$round, venue.id=${venue?.id}, status=$status, " +
            "score=$score, homeTeam.id=${homeTeam?.id}, awayTeam.id=${awayTeam?.id})"
}
