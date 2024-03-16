package com.gyechunsik.scoreboard.domain.football.relations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeagueTeamRepository extends JpaRepository<LeagueTeam, LeagueTeamId> {
}
