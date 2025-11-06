package com.footballay.core.domain.football.persistence.live;

import com.footballay.core.domain.football.persistence.Fixture;
import com.footballay.core.domain.football.persistence.Team;
import jakarta.persistence.*;
import org.springframework.lang.Nullable;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;
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
        return "FixtureEvent{" + "sequence=" + sequence + ", timeElapsed=" + timeElapsed + ", extraTime=" + extraTime + ", type=" + type + ", detail=\'" + detail + '\'' + ", comments=\'" + comments + '\'' + ", team=" + team.getId() + ", matchPlayerId=" + player.getId() + ", assistId=" + (assist == null ? "null" : assist.getId()) + '}';
    }


    public static class FixtureEventBuilder {
        private Long id;
        private Fixture fixture;
        private Integer sequence;
        private Integer timeElapsed;
        private Integer extraTime;
        private EventType type;
        private String detail;
        private String comments;
        private Team team;
        private MatchPlayer player;
        private MatchPlayer assist;

        FixtureEventBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public FixtureEvent.FixtureEventBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public FixtureEvent.FixtureEventBuilder fixture(final Fixture fixture) {
            this.fixture = fixture;
            return this;
        }

        /**
         * 이벤트 발생 순서
         * timeElapsed 는 분 단위로 체크되므로 timeElapsed 만으로는 이벤트 발생 순서를 보장할 수 없기 때문에
         * sequence 를 추가로 사용합니다.
         * sequence 는 api 응답에서 events: [] 배열의 index 값을 담게되며 0 부터 시작합니다.
         * @return {@code this}.
         */
        public FixtureEvent.FixtureEventBuilder sequence(final Integer sequence) {
            this.sequence = sequence;
            return this;
        }

        /**
         * 이벤트 발생 시간. 분 단위
         * @return {@code this}.
         */
        public FixtureEvent.FixtureEventBuilder timeElapsed(final Integer timeElapsed) {
            this.timeElapsed = timeElapsed;
            return this;
        }

        /**
         * 추가시간 정보를 포함합니다. <br>
         * 예를 들어 전반 추가 3분에 발생한 이벤트인 경우 timeElapsed 는 45 이고 extraTime 는 3 입니다. <br>
         * 추가시간이 없는 경우 0 입니다.
         * @return {@code this}.
         */
        public FixtureEvent.FixtureEventBuilder extraTime(final Integer extraTime) {
            this.extraTime = extraTime;
            return this;
        }

        /**
         * 이벤트 타입. 대소문자는 정확히 아래와 같이, subst 만 첫 글자가 소문자로 되어있습니다.  <br>
         * <pre>
         * "subst" : 교체
         * "Goal" : 골
         * "Card" : 카드
         * "Var" : VAR
         * </pre>
         * @return {@code this}.
         */
        public FixtureEvent.FixtureEventBuilder type(final EventType type) {
            this.type = type;
            return this;
        }

        /**
         * 각 EventType 에서 상세 정보를 담습니다. (ex. Yellow Card, Red Card, Substitution 1 2 3 ... 등) <br>
         * substitution 은 팀 별로 "Substitution 1", "Substitution 2" ... 로 팀 별로 몇 번째 교체카드인지 구분합니다 <br>
         * Var 은 "Goal Disallowed - offside", "Goal Disallowed - handball" 같은 식으로 표기되는데, 명확히 어떤 종류가 있는지는 문서에 나와있지 않음 <br>
         * <a href="https://www.api-football.com/documentation-v3#tag/Fixtures/operation/get-fixtures-events">공식문서</a>
         * @return {@code this}.
         */
        public FixtureEvent.FixtureEventBuilder detail(final String detail) {
            this.detail = detail;
            return this;
        }

        /**
         * 이벤트에 대한 추가 설명.
         * 카드에 대한 추가 설명을 담습니다.
         * ex. comments: "Tripping" , "Holding" , "Roughing"
         * @return {@code this}.
         */
        public FixtureEvent.FixtureEventBuilder comments(final String comments) {
            this.comments = comments;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public FixtureEvent.FixtureEventBuilder team(final Team team) {
            this.team = team;
            return this;
        }

        /**
         * 1) id != null 인 경우 : registered Player 인 경우 Player 연관관계를 맺은 MatchPlayer 를 저장합니다. <br>
         * 2) id == null && name != null 인 경우 : unregistered player name 을 채운 MatchPlayer 를 저장합니다. <br>
         * 2) id == null && name == null 인 경우 : null 로 남겨둡니다. <br>
         * @return {@code this}.
         */
        public FixtureEvent.FixtureEventBuilder player(@Nullable final MatchPlayer player) {
            this.player = player;
            return this;
        }

        /**
         * 이벤트 타입이 교체("subst") 인 경우, "player" 는 교체 들어가는 선수고, "assist" 는 교체되어 나오는 선수 입니다. <br>
         * 1) id != null 인 경우 : registered Player 인 경우 Player 연관관계를 맺은 MatchPlayer 를 저장합니다. <br>
         * 2) id == null && name != null 인 경우 : unregistered player name 을 채운 MatchPlayer 를 저장합니다. <br>
         * 2) id == null && name == null 인 경우 : null 로 남겨둡니다. <br>
         * @return {@code this}.
         */
        public FixtureEvent.FixtureEventBuilder assist(@Nullable final MatchPlayer assist) {
            this.assist = assist;
            return this;
        }

        public FixtureEvent build() {
            return new FixtureEvent(this.id, this.fixture, this.sequence, this.timeElapsed, this.extraTime, this.type, this.detail, this.comments, this.team, this.player, this.assist);
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "FixtureEvent.FixtureEventBuilder(id=" + this.id + ", fixture=" + this.fixture + ", sequence=" + this.sequence + ", timeElapsed=" + this.timeElapsed + ", extraTime=" + this.extraTime + ", type=" + this.type + ", detail=" + this.detail + ", comments=" + this.comments + ", team=" + this.team + ", player=" + this.player + ", assist=" + this.assist + ")";
        }
    }

    public static FixtureEvent.FixtureEventBuilder builder() {
        return new FixtureEvent.FixtureEventBuilder();
    }

    public FixtureEvent() {
    }

    /**
     * Creates a new {@code FixtureEvent} instance.
     *
     * @param id
     * @param fixture
     * @param sequence 이벤트 발생 순서
     * timeElapsed 는 분 단위로 체크되므로 timeElapsed 만으로는 이벤트 발생 순서를 보장할 수 없기 때문에
     * sequence 를 추가로 사용합니다.
     * sequence 는 api 응답에서 events: [] 배열의 index 값을 담게되며 0 부터 시작합니다.
     * @param timeElapsed 이벤트 발생 시간. 분 단위
     * @param extraTime 추가시간 정보를 포함합니다. <br>
     * 예를 들어 전반 추가 3분에 발생한 이벤트인 경우 timeElapsed 는 45 이고 extraTime 는 3 입니다. <br>
     * 추가시간이 없는 경우 0 입니다.
     * @param type 이벤트 타입. 대소문자는 정확히 아래와 같이, subst 만 첫 글자가 소문자로 되어있습니다.  <br>
     * <pre>
     * "subst" : 교체
     * "Goal" : 골
     * "Card" : 카드
     * "Var" : VAR
     * </pre>
     * @param detail 각 EventType 에서 상세 정보를 담습니다. (ex. Yellow Card, Red Card, Substitution 1 2 3 ... 등) <br>
     * substitution 은 팀 별로 "Substitution 1", "Substitution 2" ... 로 팀 별로 몇 번째 교체카드인지 구분합니다 <br>
     * Var 은 "Goal Disallowed - offside", "Goal Disallowed - handball" 같은 식으로 표기되는데, 명확히 어떤 종류가 있는지는 문서에 나와있지 않음 <br>
     * <a href="https://www.api-football.com/documentation-v3#tag/Fixtures/operation/get-fixtures-events">공식문서</a>
     * @param comments 이벤트에 대한 추가 설명.
     * 카드에 대한 추가 설명을 담습니다.
     * ex. comments: "Tripping" , "Holding" , "Roughing"
     * @param team
     * @param player 1) id != null 인 경우 : registered Player 인 경우 Player 연관관계를 맺은 MatchPlayer 를 저장합니다. <br>
     * 2) id == null && name != null 인 경우 : unregistered player name 을 채운 MatchPlayer 를 저장합니다. <br>
     * 2) id == null && name == null 인 경우 : null 로 남겨둡니다. <br>
     * @param assist 이벤트 타입이 교체("subst") 인 경우, "player" 는 교체 들어가는 선수고, "assist" 는 교체되어 나오는 선수 입니다. <br>
     * 1) id != null 인 경우 : registered Player 인 경우 Player 연관관계를 맺은 MatchPlayer 를 저장합니다. <br>
     * 2) id == null && name != null 인 경우 : unregistered player name 을 채운 MatchPlayer 를 저장합니다. <br>
     * 2) id == null && name == null 인 경우 : null 로 남겨둡니다. <br>
     */
    public FixtureEvent(final Long id, final Fixture fixture, final Integer sequence, final Integer timeElapsed, final Integer extraTime, final EventType type, final String detail, final String comments, final Team team, @Nullable final MatchPlayer player, @Nullable final MatchPlayer assist) {
        this.id = id;
        this.fixture = fixture;
        this.sequence = sequence;
        this.timeElapsed = timeElapsed;
        this.extraTime = extraTime;
        this.type = type;
        this.detail = detail;
        this.comments = comments;
        this.team = team;
        this.player = player;
        this.assist = assist;
    }

    public Long getId() {
        return this.id;
    }

    public Fixture getFixture() {
        return this.fixture;
    }

    /**
     * 이벤트 발생 순서
     * timeElapsed 는 분 단위로 체크되므로 timeElapsed 만으로는 이벤트 발생 순서를 보장할 수 없기 때문에
     * sequence 를 추가로 사용합니다.
     * sequence 는 api 응답에서 events: [] 배열의 index 값을 담게되며 0 부터 시작합니다.
     */
    public Integer getSequence() {
        return this.sequence;
    }

    /**
     * 이벤트 발생 시간. 분 단위
     */
    public Integer getTimeElapsed() {
        return this.timeElapsed;
    }

    /**
     * 추가시간 정보를 포함합니다. <br>
     * 예를 들어 전반 추가 3분에 발생한 이벤트인 경우 timeElapsed 는 45 이고 extraTime 는 3 입니다. <br>
     * 추가시간이 없는 경우 0 입니다.
     */
    public Integer getExtraTime() {
        return this.extraTime;
    }

    /**
     * 이벤트 타입. 대소문자는 정확히 아래와 같이, subst 만 첫 글자가 소문자로 되어있습니다.  <br>
     * <pre>
     * "subst" : 교체
     * "Goal" : 골
     * "Card" : 카드
     * "Var" : VAR
     * </pre>
     */
    public EventType getType() {
        return this.type;
    }

    /**
     * 각 EventType 에서 상세 정보를 담습니다. (ex. Yellow Card, Red Card, Substitution 1 2 3 ... 등) <br>
     * substitution 은 팀 별로 "Substitution 1", "Substitution 2" ... 로 팀 별로 몇 번째 교체카드인지 구분합니다 <br>
     * Var 은 "Goal Disallowed - offside", "Goal Disallowed - handball" 같은 식으로 표기되는데, 명확히 어떤 종류가 있는지는 문서에 나와있지 않음 <br>
     * <a href="https://www.api-football.com/documentation-v3#tag/Fixtures/operation/get-fixtures-events">공식문서</a>
     */
    public String getDetail() {
        return this.detail;
    }

    /**
     * 이벤트에 대한 추가 설명.
     * 카드에 대한 추가 설명을 담습니다.
     * ex. comments: "Tripping" , "Holding" , "Roughing"
     */
    public String getComments() {
        return this.comments;
    }

    public Team getTeam() {
        return this.team;
    }

    /**
     * 1) id != null 인 경우 : registered Player 인 경우 Player 연관관계를 맺은 MatchPlayer 를 저장합니다. <br>
     * 2) id == null && name != null 인 경우 : unregistered player name 을 채운 MatchPlayer 를 저장합니다. <br>
     * 2) id == null && name == null 인 경우 : null 로 남겨둡니다. <br>
     */
    @Nullable
    public MatchPlayer getPlayer() {
        return this.player;
    }

    /**
     * 이벤트 타입이 교체("subst") 인 경우, "player" 는 교체 들어가는 선수고, "assist" 는 교체되어 나오는 선수 입니다. <br>
     * 1) id != null 인 경우 : registered Player 인 경우 Player 연관관계를 맺은 MatchPlayer 를 저장합니다. <br>
     * 2) id == null && name != null 인 경우 : unregistered player name 을 채운 MatchPlayer 를 저장합니다. <br>
     * 2) id == null && name == null 인 경우 : null 로 남겨둡니다. <br>
     */
    @Nullable
    public MatchPlayer getAssist() {
        return this.assist;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setFixture(final Fixture fixture) {
        this.fixture = fixture;
    }

    /**
     * 이벤트 발생 순서
     * timeElapsed 는 분 단위로 체크되므로 timeElapsed 만으로는 이벤트 발생 순서를 보장할 수 없기 때문에
     * sequence 를 추가로 사용합니다.
     * sequence 는 api 응답에서 events: [] 배열의 index 값을 담게되며 0 부터 시작합니다.
     */
    public void setSequence(final Integer sequence) {
        this.sequence = sequence;
    }

    /**
     * 이벤트 발생 시간. 분 단위
     */
    public void setTimeElapsed(final Integer timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    /**
     * 추가시간 정보를 포함합니다. <br>
     * 예를 들어 전반 추가 3분에 발생한 이벤트인 경우 timeElapsed 는 45 이고 extraTime 는 3 입니다. <br>
     * 추가시간이 없는 경우 0 입니다.
     */
    public void setExtraTime(final Integer extraTime) {
        this.extraTime = extraTime;
    }

    /**
     * 이벤트 타입. 대소문자는 정확히 아래와 같이, subst 만 첫 글자가 소문자로 되어있습니다.  <br>
     * <pre>
     * "subst" : 교체
     * "Goal" : 골
     * "Card" : 카드
     * "Var" : VAR
     * </pre>
     */
    public void setType(final EventType type) {
        this.type = type;
    }

    /**
     * 각 EventType 에서 상세 정보를 담습니다. (ex. Yellow Card, Red Card, Substitution 1 2 3 ... 등) <br>
     * substitution 은 팀 별로 "Substitution 1", "Substitution 2" ... 로 팀 별로 몇 번째 교체카드인지 구분합니다 <br>
     * Var 은 "Goal Disallowed - offside", "Goal Disallowed - handball" 같은 식으로 표기되는데, 명확히 어떤 종류가 있는지는 문서에 나와있지 않음 <br>
     * <a href="https://www.api-football.com/documentation-v3#tag/Fixtures/operation/get-fixtures-events">공식문서</a>
     */
    public void setDetail(final String detail) {
        this.detail = detail;
    }

    /**
     * 이벤트에 대한 추가 설명.
     * 카드에 대한 추가 설명을 담습니다.
     * ex. comments: "Tripping" , "Holding" , "Roughing"
     */
    public void setComments(final String comments) {
        this.comments = comments;
    }

    public void setTeam(final Team team) {
        this.team = team;
    }

    /**
     * 1) id != null 인 경우 : registered Player 인 경우 Player 연관관계를 맺은 MatchPlayer 를 저장합니다. <br>
     * 2) id == null && name != null 인 경우 : unregistered player name 을 채운 MatchPlayer 를 저장합니다. <br>
     * 2) id == null && name == null 인 경우 : null 로 남겨둡니다. <br>
     */
    public void setPlayer(@Nullable final MatchPlayer player) {
        this.player = player;
    }

    /**
     * 이벤트 타입이 교체("subst") 인 경우, "player" 는 교체 들어가는 선수고, "assist" 는 교체되어 나오는 선수 입니다. <br>
     * 1) id != null 인 경우 : registered Player 인 경우 Player 연관관계를 맺은 MatchPlayer 를 저장합니다. <br>
     * 2) id == null && name != null 인 경우 : unregistered player name 을 채운 MatchPlayer 를 저장합니다. <br>
     * 2) id == null && name == null 인 경우 : null 로 남겨둡니다. <br>
     */
    public void setAssist(@Nullable final MatchPlayer assist) {
        this.assist = assist;
    }
}
