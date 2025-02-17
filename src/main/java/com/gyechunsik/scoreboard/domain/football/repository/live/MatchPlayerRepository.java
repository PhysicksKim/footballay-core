package com.gyechunsik.scoreboard.domain.football.repository.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchLineup;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchPlayerRepository extends JpaRepository<MatchPlayer, Long> {

    List<MatchPlayer> findByMatchLineup(MatchLineup matchLineup);

    int deleteByMatchLineupIn(List<MatchLineup> lineups);

    /**
     * 경기에 출전한 특정 선수를 찾습니다. <br>
     * unregisteredPlayer 는 무시합니다. <br>
     * @param fixtureId 경기 ID
     * @param teamId 팀 ID
     * @param playerId 선수 ID
     * @return {@link Optional}<{@link MatchPlayer}> 경기에 출전한 특정 선수
     */
    @Query("SELECT mp FROM MatchPlayer mp " +
            "WHERE mp.matchLineup.fixture.fixtureId = :fixtureId " +
            "AND mp.matchLineup.team.id = :teamId " +
            "AND mp.player.id = :playerId " +
            "AND mp.player IS NOT NULL")
    Optional<MatchPlayer> findMatchPlayerByFixtureTeamAndPlayer(
            @Param("fixtureId") Long fixtureId,
            @Param("teamId") Long teamId,
            @Param("playerId") Long playerId);

    @Query("SELECT mp FROM MatchPlayer mp " +
            "JOIN FETCH mp.matchLineup ml " +
            "LEFT JOIN FETCH mp.playerStatistics ps " +
            "WHERE ml.fixture = :fixture AND ml.team = :team")
    List<MatchPlayer> findMatchPlayerByFixtureAndTeam(Fixture fixture, Team team);

    @Query("SELECT mp FROM MatchPlayer mp " +
            "WHERE mp.matchLineup.fixture.fixtureId = :fixtureId " +
            "AND mp.matchLineup.team.id = :teamId " +
            "AND mp.unregisteredPlayerName = :playerName")
    Optional<MatchPlayer> findUnregisteredPlayerByName(long fixtureId, long teamId, String playerName);
}
