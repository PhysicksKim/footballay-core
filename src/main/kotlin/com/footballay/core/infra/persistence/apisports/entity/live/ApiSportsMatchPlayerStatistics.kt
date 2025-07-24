package com.footballay.core.infra.persistence.apisports.entity.live

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "refac_apisports_match_player_stats")
data class ApiSportsMatchPlayerStatistics(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "match_player_id", nullable = true)
    var matchPlayer: ApiSportsMatchPlayer? = null,

    // Games statistics
    @Column(name = "minutes_played")
    var minutesPlayed: Int? = null,
    
    @Column(name = "shirt_number")
    var shirtNumber: Int? = null,
    
    @Column(name = "position")
    var position: String? = null,

    /**
     * 선수의 경기 중 평점
     * ex) 6.3, 최대 10.0
     *
     * ### BigDecimal 대신 Double 사용 이유
     * rating 값은 0.0~10.0 사이의 실수이므로 double 로 충분합니다.
     * BigDecimal은 정밀한 소수점 계산에 유리하지만 double 과 비교해서 성능상 약 10배 이상 차이가 납니다.
     * rating 값은 중요한 소수점 정밀도를 요구하지 않으며, double 은 대략 15자리까지 반올림 오차 없이 나타낼 수 있으므로 충분합니다.
     * 따라서 double 을 사용합니다.
     */
    @Column(name = "rating")
    var rating: Double? = null,
    
    @Column(name = "is_captain")
    var isCaptain: Boolean = false,
    
    @Column(name = "is_substitute")
    var isSubstitute: Boolean = false,
    
    // Offsides
    @Column(name = "offsides")
    var offsides: Int? = null,
    
    // Shots
    @Column(name = "shots_total")
    var shotsTotal: Int? = null,
    
    @Column(name = "shots_on_target")
    var shotsOnTarget: Int? = null,
    
    // Goals
    @Column(name = "goals_total")
    var goalsTotal: Int? = null,
    
    @Column(name = "goals_conceded")
    var goalsConceded: Int? = null,
    
    @Column(name = "assists")
    var assists: Int? = null,
    
    @Column(name = "saves")
    var saves: Int? = null,
    
    // Passes
    @Column(name = "passes_total")
    var passesTotal: Int? = null,
    
    @Column(name = "key_passes")
    var keyPasses: Int? = null,
    
    @Column(name = "passes_accuracy")
    var passesAccuracy: Int? = null,
    
    // Tackles
    @Column(name = "tackles_total")
    var tacklesTotal: Int? = null,
    
    @Column(name = "blocks")
    var blocks: Int? = null,
    
    @Column(name = "interceptions")
    var interceptions: Int? = null,
    
    // Duels
    @Column(name = "duels_total")
    var duelsTotal: Int? = null,
    
    @Column(name = "duels_won")
    var duelsWon: Int? = null,
    
    // Dribbles
    @Column(name = "dribbles_attempts")
    var dribblesAttempts: Int? = null,
    
    @Column(name = "dribbles_success")
    var dribblesSuccess: Int? = null,
    
    @Column(name = "dribbles_past")
    var dribblesPast: Int? = null,
    
    // Fouls
    @Column(name = "fouls_drawn")
    var foulsDrawn: Int? = null,
    
    @Column(name = "fouls_committed")
    var foulsCommitted: Int? = null,
    
    // Cards
    @Column(name = "yellow_cards")
    var yellowCards: Int = 0,
    
    @Column(name = "red_cards")
    var redCards: Int = 0,
    
    // Penalty
    @Column(name = "penalty_won")
    var penaltyWon: Int? = null, // uncertain - JSON에서 null로 제공됨
    
    @Column(name = "penalty_committed")
    var penaltyCommitted: Int? = null, // uncertain - JSON에서 null로 제공됨
    
    @Column(name = "penalty_scored")
    var penaltyScored: Int = 0,
    
    @Column(name = "penalty_missed")
    var penaltyMissed: Int = 0,
    
    @Column(name = "penalty_saved")
    var penaltySaved: Int = 0

)
