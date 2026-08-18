package com.footballay.core.infra.persistence.apisports.entity.live

import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import jakarta.persistence.*

@Entity
@Table(
    name = "apisports_match_event",
)
class ApiSportsMatchEvent(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_api_id")
    var fixtureApi: FixtureApiSports,
    /**
     * 이벤트가 화면상 귀속되는 팀입니다.
     *
     * 일반 이벤트는 provider가 전달한 이벤트 팀입니다. 다만 `Goal`의 `Own Goal`은
     * 자책골로 득점이 인정된 상대 팀으로 정규화되어 저장됩니다. 이 경우 [player]는
     * [matchTeam]과 다른 팀의 자책골 선수일 수 있습니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_team_id")
    var matchTeam: ApiSportsMatchTeam? = null,
    /**
     * 이벤트와 관련된 선수입니다.
     * - Goal: 골 넣은 선수
     * - Subst: sub-in 선수 (이미 정규화됨)
     * - Card: 카드 받은 선수
     * - VAR: VAR 관련 선수
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    var player: ApiSportsMatchPlayer? = null,
    /**
     * 이벤트와 관련된 어시스트 선수입니다.
     * - Goal: 도움 선수
     * - Subst: sub-out 선수 (이미 정규화됨)
     * - Card: null
     * - VAR: null
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assist_id")
    var assist: ApiSportsMatchPlayer? = null,
    @Column(name = "sequence", nullable = false)
    /**
     * Core가 재정렬 후 부여한 이벤트 순서입니다.
     * provider 배열 index가 아니며, 같은 시간의 이벤트를 안정적으로 정렬하는 자연 키입니다.
     */
    var sequence: Int,
    // --- event info fields ---
    @Column(name = "elapsed_time")
    var elapsedTime: Int, // 경기 시간
    @Column(name = "extra_time")
    var extraTime: Int? = null, // 추가 시간
    @Column(name = "event_type")
    /**
     * 정규화된 이벤트 타입입니다. 예. `Goal`, `Card`, `Subst`, `VAR`, `ETC`.
     *
     * provider의 `Goal + Missed Penalty`는 득점이 아니므로 `ETC + Missed Penalty`로 저장됩니다.
     */
    var eventType: String,
    @Column(name = "detail")
    var detail: String? = null, // 이벤트 상세 정보
    @Column(name = "comments")
    var comments: String? = null, // 이벤트 코멘트
) {
    /**
     * JPA 엔티티 동등성: sequence 기반 비교
     * - sequence를 자연 키로 사용
     * - 영속 상태에서만 동등성 확인 (id != null)
     * - 연관관계는 제외하여 성능 및 무한 재귀 방지
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false

        // 간단한 타입 체크
        if (other !is ApiSportsMatchEvent) return false

        // 자연 키 기반 비교
        return sequence == other.sequence
    }

    /**
     * 일관된 해시코드: sequence 기반
     * - 자연 키 사용으로 영속 상태 전환에 영향받지 않음
     * - 안정적인 해시코드 보장
     */
    override fun hashCode(): Int = sequence.hashCode()

    /**
     * 안전한 toString: 연관관계 제외
     * - 지연 로딩 방지
     * - 디버깅에 필요한 정보만 포함
     */
    override fun toString(): String =
        "ApiSportsMatchEvent(" +
            "id=$id, " +
            "sequence=$sequence, " +
            "eventType='$eventType', " +
            "elapsedTime=$elapsedTime, " +
            "detail='$detail'" +
            ")"
}
