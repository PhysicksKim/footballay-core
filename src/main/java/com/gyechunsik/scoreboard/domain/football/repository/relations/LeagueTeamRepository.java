package com.gyechunsik.scoreboard.domain.football.repository.relations;

import com.gyechunsik.scoreboard.domain.football.entity.relations.LeagueTeam;
import com.gyechunsik.scoreboard.domain.football.entity.relations.LeagueTeamId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeagueTeamRepository extends JpaRepository<LeagueTeam, Long> {
}
