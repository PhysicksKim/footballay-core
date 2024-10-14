package com.gyechunsik.scoreboard.domain.football.repository.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.StartLineup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StartLineupRepository extends JpaRepository<StartLineup, Long> {

    @Query("SELECT sl FROM StartLineup sl " +
            "JOIN FETCH sl.startPlayers sp " +
            "JOIN FETCH sp.player p " +
            "WHERE sl.fixture = :fixture AND sl.team = :team")
    Optional<StartLineup> findByFixtureAndTeam(Fixture fixture, Team team);

    List<StartLineup> findAllByFixture(Fixture fixture);
}
