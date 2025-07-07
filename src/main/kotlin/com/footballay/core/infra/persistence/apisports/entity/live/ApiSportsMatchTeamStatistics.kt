package com.footballay.core.infra.persistence.apisports.entity.live

import jakarta.persistence.*

@Entity
@Table(name = "refac_apisports_match_team_stats")
data class ApiSportsMatchTeamStatistics(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "teamStatistics", optional = true)
    var matchTeam: ApiSportsMatchTeam?,

    @OneToMany(mappedBy = "matchTeamStatistics", fetch = FetchType.LAZY)
    var xgList: MutableList<ApiSportsMatchTeamXG> = mutableListOf(),

    // 슈팅 관련 통계
    @Column(name = "shots_on_goal")
    var shotsOnGoal: Int? = null,

    @Column(name = "shots_off_goal")
    var shotsOffGoal: Int? = null,

    @Column(name = "total_shots")
    var totalShots: Int? = null,

    @Column(name = "blocked_shots")
    var blockedShots: Int? = null,

    @Column(name = "shots_inside_box")
    var shotsInsideBox: Int? = null,

    @Column(name = "shots_outside_box")
    var shotsOutsideBox: Int? = null,

    // 기타 경기 통계
    @Column(name = "fouls")
    var fouls: Int? = null,

    @Column(name = "corner_kicks")
    var cornerKicks: Int? = null,

    @Column(name = "offsides")
    var offsides: Int? = null,

    @Column(name = "ball_possession")
    var ballPossession: String? = null, // "67%" 형태

    // 카드 관련 통계
    @Column(name = "yellow_cards")
    var yellowCards: Int? = null,

    @Column(name = "red_cards")
    var redCards: Int? = null,

    // 골키퍼 관련 통계
    @Column(name = "goalkeeper_saves")
    var goalkeeperSaves: Int? = null,

    // 패스 관련 통계
    @Column(name = "total_passes")
    var totalPasses: Int? = null,

    @Column(name = "passes_accurate")
    var passesAccurate: Int? = null,

    @Column(name = "passes_percentage")
    var passesPercentage: String? = null, // "88%" 형태

    // 기대득점 관련 통계
    @Column(name = "goals_prevented")
    var goalsPrevented: Int? = null,

)
