package com.footballay.core.infra.persistence.apisports.entity.live

import jakarta.persistence.*
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports

@Entity
@Table(
    name = "refac_apisports_match_event"
)
data class ApiSportsMatchEvent(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_api_id")
    var fixtureApi: FixtureApiSports,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_team_id")
    var matchTeam: ApiSportsMatchTeam? = null,

    /**
     * subst 인 경우 sub-in (교체 투입) 선수로 저장합니다.
     * api provider response 를 곧장 저장하지 말고, **sub-in/out 을 정규화 검사** 하여서 저장해야 합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    var player: ApiSportsMatchPlayer? = null,

    /**
     * subst 인 경우 sub-out (교체 아웃) 선수로 저장합니다.
     * api provider response 를 곧장 저장하지 말고, **sub-in/out 을 정규화 검사** 하여서 저장해야 합니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assist_id")
    var assist: ApiSportsMatchPlayer? = null,

    @Column(name = "sequence", nullable = false)
    var sequence: Int,

    // --- event info fields ---

    @Column(name = "elapsed_time")
    var elapsedTime: Int,                        // 경기 시간

    @Column(name = "extra_time")
    var extraTime: Int? = null,                  // 추가 시간

    /**
     * goal, card, subst, var 이 있습니다.
     *
     * ### subst 유의사항
     * ApiSports 의 Response 는 경기에 따라 player/assist 필드가 sub in/out 이 일관되지 않게 마구잡이로 제공되는 경우가 잦습니다.
     * 심지어 같은 경기 내에서도 어떤 이벤트는 player 필드가 in 이고 어떤 이벤트는 assist 필드가 in 이 되는 경우가 있습니다.
     * 따라서 sub in/out 정규화를 실시한 뒤에 DB에 저장시에 player는 sub-in (교체 투입) 선수, assist는 sub-out (교체 아웃) 으로 저장해야 합니다.
     */
    @Column(name = "event_type")
    var eventType: String,                       // 이벤트 타입

    @Column(name = "detail")
    var detail: String? = null,                  // 이벤트 상세

    @Column(name = "comments")
    var comments: String? = null,                // 코멘트

)
