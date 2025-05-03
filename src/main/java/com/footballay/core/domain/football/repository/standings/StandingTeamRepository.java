package com.footballay.core.domain.football.repository.standings;

import com.footballay.core.domain.football.persistence.standings.StandingTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StandingTeamRepository extends JpaRepository<StandingTeam, Long> {
}
