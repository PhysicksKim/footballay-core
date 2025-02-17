package com.gyechunsik.scoreboard.domain.football.repository.standings;

import com.gyechunsik.scoreboard.domain.football.persistence.standings.StandingTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StandingTeamRepository extends JpaRepository<StandingTeam, Long> {
}
