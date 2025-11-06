package com.footballay.core.domain.football.persistence.live;

import com.footballay.core.domain.football.persistence.Fixture;
import com.footballay.core.domain.football.persistence.Player;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import java.util.UUID;

/**
 * {@link Fixture} 에 등장한 선수 및 사람 정보를 담습니다. <br>
 * {@link Fixture} 의 event, lineups, players(선수별 통계) 응답에 선수 정보가 player field 로 등장합니다.
 * 대부분의 경우 선수 {@link Player} 를 담고 있지만, 감독이나 코치도 포함되며 예상치 못한 다른 사람이 등장할 수 있습니다.
 * 특히 {@link FixtureEvent} 에서는 선수가 아닌 경우가 자주 있습니다.
 * 따라서 선수가 아닌 사람이 등장하여 <code>id:null</code> 인 경우를 대비해, 경기 데이터에 나타나는 모든 사람 정보들은 MatchPlayer 를 거치도록 합니다. <br><br>
 * id 가 존재하는 경우 선수 {@link Player} 를 참조하며, <br>
 * id 가 null 인 경우 unregisteredPlayerName 을 사용해서 이름을 저장합니다. <br>
 * <h2>올바른 접근 경로</h2>
 * <pre>
 *     Fixture - MatchLineup - MatchPlayer (연관관계 Player 또는 필드 unregisteredPlayer 사용)
 *     Fixture - FixtureEvent - MatchPlayer
 *     Fixture - PlayerStatistics - MatchPlayer
 * </pre>
 * 아주 드물게 무명 선수인 경우 PlayerId 가 null 이므로 Player 참조를 null 로 두고 MatchPlayer 를 거쳐서 unregisteredPlayerName 을 사용합니다. <br>
 * @see Fixture
 * @see MatchLineup
 * @see Player
 */
@Entity
public class MatchPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 해당 {@link MatchPlayer} 가 경기에 뛸 수 있는 선수(선발+후보)인 경우 {@link MatchLineup} 과 연관관계를 맺습니다. <br>
     * {@link FixtureEvent} 로 인해 추가된 선수 이외의 사람의 경우 null 이 됩니다. <br>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_lineup_id", nullable = true)
    @Nullable
    private MatchLineup matchLineup;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "player_statistics_id", nullable = true)
    @Nullable
    private PlayerStatistics playerStatistics;
    /**
     * null 일 수 있습니다. <br>
     * 드물게 무명 선수인 경우 External API 측에서 관리되지 않아서 player id=null 로 제공되는 경우가 있습니다. <br>
     * 이 경우 unregisteredPlayer 접두어를 가진 필드를 사용해서 데이터를 저장합니다. <br>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = true)
    @Nullable
    private Player player;
    /**
     * 선수 id 가 null 인 경우 임시로 생성한 UUID id 를 사용해서 선수 데이터를 저장합니다. <br>
     * 이 id는 해당 경기 동안만 사용됩니다. <br>
     * uuid 는 MatchLineup - MatchPlayer 생성 시에만 부여되며, FixtureEvent 에서 등장했으나 lineup 상에서 생성되지 않은 미등록 선수에 대해서는 uuid 를 부여하지 않습니다. <br>
     * FixtureEvent 에서는 미등록 선수 등장 시 matchLineup 에서 일치하는 선수가 있는지 찾고, 없다면 event 에서 name 값만 사용한 미등록 선수를 생성합니다. <br>
     */
    @Column(nullable = true)
    @Nullable
    private UUID temporaryId;
    /**
     * Player { id=null } 인 경우 대신 unregisteredPlayerName 을 사용해서 이름을 저장합니다. <br>
     */
    @Column(nullable = true)
    @Nullable
    private String unregisteredPlayerName;
    /**
     * Player { id=null } 인 경우 대신 unregisteredPlayerNumber 를 사용해서 등번호를 저장합니다. <br>
     */
    @Column(nullable = true)
    @Nullable
    private Integer unregisteredPlayerNumber;
    /**
     * G, D, M, F
     */
    @Nullable
    private String position;
    /**
     * 경기장을 가로, 좌측 팀 기준으로 생각하고, x:y 형태로 표현합니다. <br>
     * y 는 Left 부터 시작입니다. (ex. 2:1 = 레프트백)
     * <pre>
     * 1  ------- X ------- x
     * |           2:1 (Left)
     * Y  GK 1:1   2:2
     * |           2:3
     * y           2:4
     * </pre>
     * 골키퍼는 1:1 수비수는 2:n 이후 3~x:n <br>
     * 교체선수인 경우 null 입니다.
     */
    @Column(nullable = true)
    private String grid;
    /**
     * Event 에서 사용하는 UnregisteredPlayer 의 경우 true 로 설정합니다. <br>
     */
    @Column(nullable = false)
    private Boolean substitute;
    public static final String UNREGISTERED_PLAYER_PHOTO_URL = "https://media.api-sports.io/football/players/0.png";

    @Override
    public String toString() {
        return "MatchPlayer{" + "id=" + id + ", matchLineup=" + (matchLineup != null ? matchLineup.getId() : "null") + ", player=" + player + ", position=\'" + position + '\'' + ", grid=\'" + grid + '\'' + ", substitute=" + substitute + (unregisteredPlayerName != null ? ", unregisteredPlayerName=\'" + unregisteredPlayerName + '\'' : "") + (unregisteredPlayerNumber != null ? ", unregisteredPlayerNumber=" + unregisteredPlayerNumber : "") + (temporaryId != null ? ", temporaryId=" + temporaryId : "") + '}';
    }

    private static Boolean $default$substitute() {
        return true;
    }


    public static class MatchPlayerBuilder {
        private Long id;
        private MatchLineup matchLineup;
        private PlayerStatistics playerStatistics;
        private Player player;
        private UUID temporaryId;
        private String unregisteredPlayerName;
        private Integer unregisteredPlayerNumber;
        private String position;
        private String grid;
        private boolean substitute$set;
        private Boolean substitute$value;

        MatchPlayerBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public MatchPlayer.MatchPlayerBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * 해당 {@link MatchPlayer} 가 경기에 뛸 수 있는 선수(선발+후보)인 경우 {@link MatchLineup} 과 연관관계를 맺습니다. <br>
         * {@link FixtureEvent} 로 인해 추가된 선수 이외의 사람의 경우 null 이 됩니다. <br>
         * @return {@code this}.
         */
        public MatchPlayer.MatchPlayerBuilder matchLineup(@Nullable final MatchLineup matchLineup) {
            this.matchLineup = matchLineup;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public MatchPlayer.MatchPlayerBuilder playerStatistics(@Nullable final PlayerStatistics playerStatistics) {
            this.playerStatistics = playerStatistics;
            return this;
        }

        /**
         * null 일 수 있습니다. <br>
         * 드물게 무명 선수인 경우 External API 측에서 관리되지 않아서 player id=null 로 제공되는 경우가 있습니다. <br>
         * 이 경우 unregisteredPlayer 접두어를 가진 필드를 사용해서 데이터를 저장합니다. <br>
         * @return {@code this}.
         */
        public MatchPlayer.MatchPlayerBuilder player(@Nullable final Player player) {
            this.player = player;
            return this;
        }

        /**
         * 선수 id 가 null 인 경우 임시로 생성한 UUID id 를 사용해서 선수 데이터를 저장합니다. <br>
         * 이 id는 해당 경기 동안만 사용됩니다. <br>
         * uuid 는 MatchLineup - MatchPlayer 생성 시에만 부여되며, FixtureEvent 에서 등장했으나 lineup 상에서 생성되지 않은 미등록 선수에 대해서는 uuid 를 부여하지 않습니다. <br>
         * FixtureEvent 에서는 미등록 선수 등장 시 matchLineup 에서 일치하는 선수가 있는지 찾고, 없다면 event 에서 name 값만 사용한 미등록 선수를 생성합니다. <br>
         * @return {@code this}.
         */
        public MatchPlayer.MatchPlayerBuilder temporaryId(@Nullable final UUID temporaryId) {
            this.temporaryId = temporaryId;
            return this;
        }

        /**
         * Player { id=null } 인 경우 대신 unregisteredPlayerName 을 사용해서 이름을 저장합니다. <br>
         * @return {@code this}.
         */
        public MatchPlayer.MatchPlayerBuilder unregisteredPlayerName(@Nullable final String unregisteredPlayerName) {
            this.unregisteredPlayerName = unregisteredPlayerName;
            return this;
        }

        /**
         * Player { id=null } 인 경우 대신 unregisteredPlayerNumber 를 사용해서 등번호를 저장합니다. <br>
         * @return {@code this}.
         */
        public MatchPlayer.MatchPlayerBuilder unregisteredPlayerNumber(@Nullable final Integer unregisteredPlayerNumber) {
            this.unregisteredPlayerNumber = unregisteredPlayerNumber;
            return this;
        }

        /**
         * G, D, M, F
         * @return {@code this}.
         */
        public MatchPlayer.MatchPlayerBuilder position(@Nullable final String position) {
            this.position = position;
            return this;
        }

        /**
         * 경기장을 가로, 좌측 팀 기준으로 생각하고, x:y 형태로 표현합니다. <br>
         * y 는 Left 부터 시작입니다. (ex. 2:1 = 레프트백)
         * <pre>
         * 1  ------- X ------- x
         * |           2:1 (Left)
         * Y  GK 1:1   2:2
         * |           2:3
         * y           2:4
         * </pre>
         * 골키퍼는 1:1 수비수는 2:n 이후 3~x:n <br>
         * 교체선수인 경우 null 입니다.
         * @return {@code this}.
         */
        public MatchPlayer.MatchPlayerBuilder grid(final String grid) {
            this.grid = grid;
            return this;
        }

        /**
         * Event 에서 사용하는 UnregisteredPlayer 의 경우 true 로 설정합니다. <br>
         * @return {@code this}.
         */
        public MatchPlayer.MatchPlayerBuilder substitute(final Boolean substitute) {
            this.substitute$value = substitute;
            substitute$set = true;
            return this;
        }

        public MatchPlayer build() {
            Boolean substitute$value = this.substitute$value;
            if (!this.substitute$set) substitute$value = MatchPlayer.$default$substitute();
            return new MatchPlayer(this.id, this.matchLineup, this.playerStatistics, this.player, this.temporaryId, this.unregisteredPlayerName, this.unregisteredPlayerNumber, this.position, this.grid, substitute$value);
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "MatchPlayer.MatchPlayerBuilder(id=" + this.id + ", matchLineup=" + this.matchLineup + ", playerStatistics=" + this.playerStatistics + ", player=" + this.player + ", temporaryId=" + this.temporaryId + ", unregisteredPlayerName=" + this.unregisteredPlayerName + ", unregisteredPlayerNumber=" + this.unregisteredPlayerNumber + ", position=" + this.position + ", grid=" + this.grid + ", substitute$value=" + this.substitute$value + ")";
        }
    }

    public static MatchPlayer.MatchPlayerBuilder builder() {
        return new MatchPlayer.MatchPlayerBuilder();
    }

    public MatchPlayer() {
        this.substitute = MatchPlayer.$default$substitute();
    }

    /**
     * Creates a new {@code MatchPlayer} instance.
     *
     * @param id
     * @param matchLineup 해당 {@link MatchPlayer} 가 경기에 뛸 수 있는 선수(선발+후보)인 경우 {@link MatchLineup} 과 연관관계를 맺습니다. <br>
     * {@link FixtureEvent} 로 인해 추가된 선수 이외의 사람의 경우 null 이 됩니다. <br>
     * @param playerStatistics
     * @param player null 일 수 있습니다. <br>
     * 드물게 무명 선수인 경우 External API 측에서 관리되지 않아서 player id=null 로 제공되는 경우가 있습니다. <br>
     * 이 경우 unregisteredPlayer 접두어를 가진 필드를 사용해서 데이터를 저장합니다. <br>
     * @param temporaryId 선수 id 가 null 인 경우 임시로 생성한 UUID id 를 사용해서 선수 데이터를 저장합니다. <br>
     * 이 id는 해당 경기 동안만 사용됩니다. <br>
     * uuid 는 MatchLineup - MatchPlayer 생성 시에만 부여되며, FixtureEvent 에서 등장했으나 lineup 상에서 생성되지 않은 미등록 선수에 대해서는 uuid 를 부여하지 않습니다. <br>
     * FixtureEvent 에서는 미등록 선수 등장 시 matchLineup 에서 일치하는 선수가 있는지 찾고, 없다면 event 에서 name 값만 사용한 미등록 선수를 생성합니다. <br>
     * @param unregisteredPlayerName Player { id=null } 인 경우 대신 unregisteredPlayerName 을 사용해서 이름을 저장합니다. <br>
     * @param unregisteredPlayerNumber Player { id=null } 인 경우 대신 unregisteredPlayerNumber 를 사용해서 등번호를 저장합니다. <br>
     * @param position G, D, M, F
     * @param grid 경기장을 가로, 좌측 팀 기준으로 생각하고, x:y 형태로 표현합니다. <br>
     * y 는 Left 부터 시작입니다. (ex. 2:1 = 레프트백)
     * <pre>
     * 1  ------- X ------- x
     * |           2:1 (Left)
     * Y  GK 1:1   2:2
     * |           2:3
     * y           2:4
     * </pre>
     * 골키퍼는 1:1 수비수는 2:n 이후 3~x:n <br>
     * 교체선수인 경우 null 입니다.
     * @param substitute Event 에서 사용하는 UnregisteredPlayer 의 경우 true 로 설정합니다. <br>
     */
    public MatchPlayer(final Long id, @Nullable final MatchLineup matchLineup, @Nullable final PlayerStatistics playerStatistics, @Nullable final Player player, @Nullable final UUID temporaryId, @Nullable final String unregisteredPlayerName, @Nullable final Integer unregisteredPlayerNumber, @Nullable final String position, final String grid, final Boolean substitute) {
        this.id = id;
        this.matchLineup = matchLineup;
        this.playerStatistics = playerStatistics;
        this.player = player;
        this.temporaryId = temporaryId;
        this.unregisteredPlayerName = unregisteredPlayerName;
        this.unregisteredPlayerNumber = unregisteredPlayerNumber;
        this.position = position;
        this.grid = grid;
        this.substitute = substitute;
    }

    public Long getId() {
        return this.id;
    }

    /**
     * 해당 {@link MatchPlayer} 가 경기에 뛸 수 있는 선수(선발+후보)인 경우 {@link MatchLineup} 과 연관관계를 맺습니다. <br>
     * {@link FixtureEvent} 로 인해 추가된 선수 이외의 사람의 경우 null 이 됩니다. <br>
     */
    @Nullable
    public MatchLineup getMatchLineup() {
        return this.matchLineup;
    }

    @Nullable
    public PlayerStatistics getPlayerStatistics() {
        return this.playerStatistics;
    }

    /**
     * null 일 수 있습니다. <br>
     * 드물게 무명 선수인 경우 External API 측에서 관리되지 않아서 player id=null 로 제공되는 경우가 있습니다. <br>
     * 이 경우 unregisteredPlayer 접두어를 가진 필드를 사용해서 데이터를 저장합니다. <br>
     */
    @Nullable
    public Player getPlayer() {
        return this.player;
    }

    /**
     * 선수 id 가 null 인 경우 임시로 생성한 UUID id 를 사용해서 선수 데이터를 저장합니다. <br>
     * 이 id는 해당 경기 동안만 사용됩니다. <br>
     * uuid 는 MatchLineup - MatchPlayer 생성 시에만 부여되며, FixtureEvent 에서 등장했으나 lineup 상에서 생성되지 않은 미등록 선수에 대해서는 uuid 를 부여하지 않습니다. <br>
     * FixtureEvent 에서는 미등록 선수 등장 시 matchLineup 에서 일치하는 선수가 있는지 찾고, 없다면 event 에서 name 값만 사용한 미등록 선수를 생성합니다. <br>
     */
    @Nullable
    public UUID getTemporaryId() {
        return this.temporaryId;
    }

    /**
     * Player { id=null } 인 경우 대신 unregisteredPlayerName 을 사용해서 이름을 저장합니다. <br>
     */
    @Nullable
    public String getUnregisteredPlayerName() {
        return this.unregisteredPlayerName;
    }

    /**
     * Player { id=null } 인 경우 대신 unregisteredPlayerNumber 를 사용해서 등번호를 저장합니다. <br>
     */
    @Nullable
    public Integer getUnregisteredPlayerNumber() {
        return this.unregisteredPlayerNumber;
    }

    /**
     * G, D, M, F
     */
    @Nullable
    public String getPosition() {
        return this.position;
    }

    /**
     * 경기장을 가로, 좌측 팀 기준으로 생각하고, x:y 형태로 표현합니다. <br>
     * y 는 Left 부터 시작입니다. (ex. 2:1 = 레프트백)
     * <pre>
     * 1  ------- X ------- x
     * |           2:1 (Left)
     * Y  GK 1:1   2:2
     * |           2:3
     * y           2:4
     * </pre>
     * 골키퍼는 1:1 수비수는 2:n 이후 3~x:n <br>
     * 교체선수인 경우 null 입니다.
     */
    public String getGrid() {
        return this.grid;
    }

    /**
     * Event 에서 사용하는 UnregisteredPlayer 의 경우 true 로 설정합니다. <br>
     */
    public Boolean getSubstitute() {
        return this.substitute;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    /**
     * 해당 {@link MatchPlayer} 가 경기에 뛸 수 있는 선수(선발+후보)인 경우 {@link MatchLineup} 과 연관관계를 맺습니다. <br>
     * {@link FixtureEvent} 로 인해 추가된 선수 이외의 사람의 경우 null 이 됩니다. <br>
     */
    public void setMatchLineup(@Nullable final MatchLineup matchLineup) {
        this.matchLineup = matchLineup;
    }

    public void setPlayerStatistics(@Nullable final PlayerStatistics playerStatistics) {
        this.playerStatistics = playerStatistics;
    }

    /**
     * null 일 수 있습니다. <br>
     * 드물게 무명 선수인 경우 External API 측에서 관리되지 않아서 player id=null 로 제공되는 경우가 있습니다. <br>
     * 이 경우 unregisteredPlayer 접두어를 가진 필드를 사용해서 데이터를 저장합니다. <br>
     */
    public void setPlayer(@Nullable final Player player) {
        this.player = player;
    }

    /**
     * 선수 id 가 null 인 경우 임시로 생성한 UUID id 를 사용해서 선수 데이터를 저장합니다. <br>
     * 이 id는 해당 경기 동안만 사용됩니다. <br>
     * uuid 는 MatchLineup - MatchPlayer 생성 시에만 부여되며, FixtureEvent 에서 등장했으나 lineup 상에서 생성되지 않은 미등록 선수에 대해서는 uuid 를 부여하지 않습니다. <br>
     * FixtureEvent 에서는 미등록 선수 등장 시 matchLineup 에서 일치하는 선수가 있는지 찾고, 없다면 event 에서 name 값만 사용한 미등록 선수를 생성합니다. <br>
     */
    public void setTemporaryId(@Nullable final UUID temporaryId) {
        this.temporaryId = temporaryId;
    }

    /**
     * Player { id=null } 인 경우 대신 unregisteredPlayerName 을 사용해서 이름을 저장합니다. <br>
     */
    public void setUnregisteredPlayerName(@Nullable final String unregisteredPlayerName) {
        this.unregisteredPlayerName = unregisteredPlayerName;
    }

    /**
     * Player { id=null } 인 경우 대신 unregisteredPlayerNumber 를 사용해서 등번호를 저장합니다. <br>
     */
    public void setUnregisteredPlayerNumber(@Nullable final Integer unregisteredPlayerNumber) {
        this.unregisteredPlayerNumber = unregisteredPlayerNumber;
    }

    /**
     * G, D, M, F
     */
    public void setPosition(@Nullable final String position) {
        this.position = position;
    }

    /**
     * 경기장을 가로, 좌측 팀 기준으로 생각하고, x:y 형태로 표현합니다. <br>
     * y 는 Left 부터 시작입니다. (ex. 2:1 = 레프트백)
     * <pre>
     * 1  ------- X ------- x
     * |           2:1 (Left)
     * Y  GK 1:1   2:2
     * |           2:3
     * y           2:4
     * </pre>
     * 골키퍼는 1:1 수비수는 2:n 이후 3~x:n <br>
     * 교체선수인 경우 null 입니다.
     */
    public void setGrid(final String grid) {
        this.grid = grid;
    }

    /**
     * Event 에서 사용하는 UnregisteredPlayer 의 경우 true 로 설정합니다. <br>
     */
    public void setSubstitute(final Boolean substitute) {
        this.substitute = substitute;
    }
}
