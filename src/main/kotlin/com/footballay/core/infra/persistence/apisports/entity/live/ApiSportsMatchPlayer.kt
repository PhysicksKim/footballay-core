package com.footballay.core.infra.persistence.apisports.entity.live

import com.footballay.core.infra.persistence.apisports.entity.PlayerApiSports
import jakarta.persistence.*
import org.hibernate.proxy.HibernateProxy

/**
 * 경기별 선수 정보 엔티티
 * 
 * ApiSports의 불완전한 데이터 특성으로 인해 다양한 소스에서 등장하는 선수들을 통합 관리합니다.
 */
@Entity
@Table(
    name = "refac_apisports_match_player",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["match_player_uid"])
    ]
)
class ApiSportsMatchPlayer(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "match_player_uid", nullable = false, unique = true)
    var matchPlayerUid: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_apisports_id", nullable = true)
    var playerApiSports : PlayerApiSports? = null,

//    /**
//     * ApiSports 데이터의 불완전성으로 인한 MatchPlayer 역추적을 위한 연관관계.
//     *
//     * 의도적으로 **"3NF(Third Normal Form) 역정규화"** 를 적용한 연관관계입니다.
//     * 해당 경기에 존재하는 모든 [ApiSportsMatchPlayer] 를 빠르고 단순하게 조회하기 위해서
//     *
//     * ### 필요한 이유:
//     * - **Event 선수 매칭:** Events에서 등장하는 선수를 기존 lineup/statistics의 MatchPlayer와 연결
//     * - **ID=null 선수 처리:** 이름 기반 매칭으로 동일 선수 식별 및 재사용
//     * - **팀 불명확 선수:** Own goal, VAR, 벤치 경고 등에서 matchTeam 연결이 어려운 경우
//     * - **실시간 데이터 변경:** 경기 중 ApiSports에서 선수 정보가 수정되는 경우 대응
//     *
//     * 다만 이 연관관계는 역정규화로 인해 오히려 복잡도를 증가시킬 수 있으므로 향후 필요하다면 활성화 하도록 합니다.
//     *
//     * @see MatchPlayerResolver 선수 매칭 로직
//     * @see MatchPlayerContext fixture별 선수 캐싱
//     */
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "fixture_apisports_id", nullable = false)
//    var fixtureApiSports: FixtureApiSports,

    @Column(name = "name", nullable = false)
    var name: String,

    /**
     * 선수의 등번호
     * - ApiSports에서 제공하는 등번호 사용
     * - 예시: 10, 7, null (등번호가 없는 경우)
     */
    @Column(name = "number", nullable = true)
    var number: Int? = null,

    /**
     * 선수의 포지션
     * - ApiSports에서 제공하는 포지션 코드 사용
     * - 예시: "G", "D", "M", "F", "Unknown"
     */
    @Column(name = "position", nullable = false)
    var position: String,

    @Column(name = "grid", nullable = true)
    var grid: String? = null,

    @Column(name = "substitute", nullable = false)
    var substitute: Boolean,

    /**
     * 정상 라인업 선수는 필수, 팀 불명확 선수는 nullable
     *
     * 라인업에 등장하지 않고 이벤트에만 등장한 경우 MatchPlayer 의 team 이 null 일 수 있습니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_team_id", nullable = true)
    var matchTeam: ApiSportsMatchTeam?,

    @OneToOne(mappedBy = "matchPlayer", cascade = [CascadeType.ALL], optional = true)
    var statistics: ApiSportsMatchPlayerStatistics? = null,

) {
    /**
     * JPA 엔티티 동등성: 자연 키 기반 비교
     * - matchPlayerUid를 자연 키로 사용
     * - 영속 상태에서만 동등성 확인 (id != null)
     * - 연관관계는 제외하여 성능 및 무한 재귀 방지
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        
        // 간단한 타입 체크
        if (other !is ApiSportsMatchPlayer) return false
        
        // 자연 키 기반 비교
        return matchPlayerUid == other.matchPlayerUid
    }

    /**
     * 일관된 해시코드: matchPlayerUid 기반
     * - 자연 키 사용으로 영속 상태 전환에 영향받지 않음
     * - 안정적인 해시코드 보장
     */
    override fun hashCode(): Int {
        return matchPlayerUid.hashCode()
    }

    /**
     * 안전한 toString: 연관관계 제외
     * - 지연 로딩 방지
     * - 디버깅에 필요한 정보만 포함
     */
    override fun toString(): String {
        return "ApiSportsMatchPlayer(" +
               "id=$id, " +
               "matchPlayerUid='$matchPlayerUid', " +
               "name='$name', " +
               "position='$position', " +
               "number=$number, " +
               "substitute=$substitute" +
               ")"
    }
}