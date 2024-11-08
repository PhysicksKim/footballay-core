package com.gyechunsik.scoreboard.domain.football.repository.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchLineup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchLineupRepository extends JpaRepository<MatchLineup, Long> {

    @Query("SELECT ml FROM MatchLineup ml " +
            "JOIN FETCH ml.matchPlayers sp " +
            "JOIN FETCH sp.player p " +
            "WHERE ml.fixture = :fixture AND ml.team = :team")
    Optional<MatchLineup> findByFixtureAndTeam(Fixture fixture, Team team);

    List<MatchLineup> findAllByFixture(Fixture fixture);

    // @Query("SELECT ml FROM MatchLineup ml " +
    //         "JOIN FETCH ml.matchPlayers sp " +
    //         "JOIN FETCH sp.player p " +
    //         "WHERE ml.fixture = :fixture AND ml.team = :fixture.homeTeam")
    // Optional<MatchLineup> findHomeLineupByFixture(Fixture fixture);

    @Query("SELECT ml FROM MatchLineup ml " +
            "JOIN FETCH ml.matchPlayers mp " +
            "JOIN FETCH mp.player p " +
            "WHERE ml.fixture = :fixture AND ml.team = :team")
    Optional<MatchLineup> findTeamLineupByFixture(@Param("fixture") Fixture fixture, @Param("team") Team team);

}
