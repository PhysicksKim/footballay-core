package com.gyechunsik.scoreboard.domain.football.persistence.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
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
    @Builder.Default
    @Column(nullable = false)
    private Boolean substitute = true;

    public static final String UNREGISTERED_PLAYER_PHOTO_URL = "https://media.api-sports.io/football/players/0.png";

    @Override
    public String toString() {
        return "MatchPlayer{" +
                "id=" + id +
                ", matchLineup=" + (matchLineup != null ? matchLineup.getId() : "null") +
                ", player=" + player +
                ", position='" + position + '\'' +
                ", grid='" + grid + '\'' +
                ", substitute=" + substitute +
                (unregisteredPlayerName != null ? ", unregisteredPlayerName='" + unregisteredPlayerName + '\'' : "") +
                (unregisteredPlayerNumber != null ? ", unregisteredPlayerNumber=" + unregisteredPlayerNumber : "") +
                (temporaryId != null ? ", temporaryId=" + temporaryId : "") +
                '}';
    }
}
