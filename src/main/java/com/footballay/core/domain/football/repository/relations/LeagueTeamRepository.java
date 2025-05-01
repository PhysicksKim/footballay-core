package com.footballay.core.domain.football.repository.relations;

import com.footballay.core.domain.football.persistence.League;
import com.footballay.core.domain.football.persistence.Team;
import com.footballay.core.domain.football.persistence.relations.LeagueTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeagueTeamRepository extends JpaRepository<LeagueTeam, Long> {

    Optional<LeagueTeam> findByLeagueAndTeam(League league, Team team);

    void deleteByLeagueAndTeam(League league, Team team);

}
