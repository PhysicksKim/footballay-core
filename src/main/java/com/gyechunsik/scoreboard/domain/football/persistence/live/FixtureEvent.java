package com.gyechunsik.scoreboard.domain.football.persistence.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.lang.Nullable;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class FixtureEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;

    /**
     * 이벤트 발생 순서
     * timeElapsed 는 분 단위로 체크되므로 timeElapsed 만으로는 이벤트 발생 순서를 보장할 수 없기 때문에
     * sequence 를 추가로 사용합니다.
     * sequence 는 api 응답에서 events: [] 배열의 index 값을 담게되며 0 부터 시작합니다.
     */
    @Column(nullable = false)
    private Integer sequence;

    /**
     * 이벤트 발생 시간. 분 단위
     */
    private Integer timeElapsed;

    /**
     * 추가시간 정보를 포함합니다. <br>
     * 예를 들어 전반 추가 3분에 발생한 이벤트인 경우 timeElapsed 는 45 이고 extraTime 는 3 입니다. <br>
     * 추가시간이 없는 경우 0 입니다.
     */
    private Integer extraTime;

    /**
     * 이벤트 타입. 대소문자는 정확히 아래와 같이, subst 만 첫 글자가 소문자로 되어있습니다.  <br>
     * <pre>
     * "subst" : 교체
     * "Goal" : 골
     * "Card" : 카드
     * "Var" : VAR
     * </pre>
     */
    @Enumerated(EnumType.STRING)
    private EventType type;

    /**
     * 각 EventType 에서 상세 정보를 담습니다. (ex. Yellow Card, Red Card, Substitution 1 2 3 ... 등) <br>
     * substitution 은 팀 별로 "Substitution 1", "Substitution 2" ... 로 팀 별로 몇 번째 교체카드인지 구분합니다 <br>
     * Var 은 "Goal Disallowed - offside", "Goal Disallowed - handball" 같은 식으로 표기되는데, 명확히 어떤 종류가 있는지는 문서에 나와있지 않음 <br>
     * <a href="https://www.api-football.com/documentation-v3#tag/Fixtures/operation/get-fixtures-events">공식문서</a>
     */
    @Column(nullable = false)
    private String detail;

    /**
     * 이벤트에 대한 추가 설명.
     * 카드에 대한 추가 설명을 담습니다.
     * ex. comments: "Tripping" , "Holding" , "Roughing"
     */
    @Column(nullable = true)
    private String comments;

    // TODO : 팀도 혹시 null인 경우가 있을 수 있는거아님? 이거도 로직 필요해보이네
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // TODO : 감독이나 코치의 경우 coach id 로 들어오니까 이벤트의 Player 필드는 구분 필요할듯.
    //  감독이나 코치는 player 필드로 들어오는데,
    //  player id 와 coach id 는 따로라서 이거 구별 필요해보임
    //  아마 id 로 찾은 다음 name 비교 해서 일치 여부 비교부터 하는 게 맞을듯?
    //  아니면 MatchPlayer(구 MatchPlayer) 가 MatchLineup 기준으로 나오는 거니까 MatchPlayer 에 있는지로 선수인지 코치감독인지 구분해야할듯
    // TODO : 선수 id가 null인 경우 있음. 감독이나 코치 또는 이외의 이벤트의 경우 player id null 일 수 있음. 이에 대응 가능하도록 로직 필요
    /**
     * 1) id != null 인 경우 : registered Player 인 경우 Player 연관관계를 맺은 MatchPlayer 를 저장합니다. <br>
     * 2) id == null && name != null 인 경우 : unregistered player name 을 채운 MatchPlayer 를 저장합니다. <br>
     * 2) id == null && name == null 인 경우 : null 로 남겨둡니다. <br>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_player_id", nullable = true)
    @Nullable
    private MatchPlayer player;

    /**
     * 이벤트 타입이 교체("subst") 인 경우, "player" 는 교체 들어가는 선수고, "assist" 는 교체되어 나오는 선수 입니다. <br>
     * 1) id != null 인 경우 : registered Player 인 경우 Player 연관관계를 맺은 MatchPlayer 를 저장합니다. <br>
     * 2) id == null && name != null 인 경우 : unregistered player name 을 채운 MatchPlayer 를 저장합니다. <br>
     * 2) id == null && name == null 인 경우 : null 로 남겨둡니다. <br>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_player_assist_id", nullable = true)
    @Nullable
    private MatchPlayer assist;

    @Override
    public String toString() {
        return "FixtureEvent{" +
                "sequence=" + sequence +
                ", timeElapsed=" + timeElapsed +
                ", extraTime=" + extraTime +
                ", type=" + type +
                ", detail='" + detail + '\'' +
                ", comments='" + comments + '\'' +
                ", team=" + team.getId() +
                ", matchPlayerId=" + player.getId() +
                ", assistId=" + (assist == null ? "null" : assist.getId()) +
                '}';
    }
}
