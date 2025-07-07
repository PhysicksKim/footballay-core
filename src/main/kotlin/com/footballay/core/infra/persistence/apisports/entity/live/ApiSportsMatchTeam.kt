package com.footballay.core.infra.persistence.apisports.entity.live

import jakarta.persistence.*
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports

/**
 * 경기별 팀 정보 엔티티
 * 
 * ApiSports의 라이브 매치 데이터에서 팀 정보를 저장합니다.
 * TeamCore 대신 TeamApiSports를 직접 참조하여 API 데이터에 특화된 구조입니다.
 */
@Entity
@Table(
    name = "refac_apisports_match_team"
)
data class ApiSportsMatchTeam(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    /**
     * TeamApiSports 직접 참조
     * 
     * Match 엔티티들은 ApiSports API에 직접 의존하므로 
     * TeamCore를 거치지 않고 TeamApiSports를 직접 참조합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_apisports_id")
    var teamApiSports: TeamApiSports? = null,

    @Column(name = "formation", nullable = true)
    var formation: String? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "primary", column = Column(name = "player_color_primary")),
        AttributeOverride(name = "number", column = Column(name = "player_color_number")),
        AttributeOverride(name = "border", column = Column(name = "player_color_border"))
    )
    var playerColor: UniformColor? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "primary", column = Column(name = "goalkeeper_color_primary")),
        AttributeOverride(name = "number", column = Column(name = "goalkeeper_color_number")),
        AttributeOverride(name = "border", column = Column(name = "goalkeeper_color_border"))
    )
    var goalkeeperColor: UniformColor? = null,

    /**
     * null 인 경우 아직 승자가 결정되지 않은 상황입니다
     */
    var winner: Boolean? = null,

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    var teamStatistics: ApiSportsMatchTeamStatistics? = null,

    @OneToMany(mappedBy = "matchTeam", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var players: MutableList<ApiSportsMatchPlayer> = mutableListOf(),

) {
    /**
     * 팀 API ID 조회 헬퍼 메서드
     */
    fun getTeamApiId(): Long? {
        return teamApiSports?.apiId
    }
}
