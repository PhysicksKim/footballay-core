package com.gyechunsik.scoreboard.domain.football.repository.relations;

import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.entity.relations.LeagueTeam;
import com.gyechunsik.scoreboard.domain.football.entity.relations.LeagueTeamId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeagueTeamRepository extends JpaRepository<LeagueTeam, Long> {

    Optional<LeagueTeam> findByLeagueAndTeam(League league, Team team);

    void deleteByLeagueAndTeam(League league, Team team);

}
