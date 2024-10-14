package com.gyechunsik.scoreboard.domain.football.repository;

import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.League;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT t FROM Team t JOIN FETCH t.leagueTeams lt WHERE lt.league = :league")
    List<Team> findTeamsByLeague(@Param("league") League league);

    List<Team> findByIdIn(List<Long> teamIds);
}
