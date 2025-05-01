package com.footballay.core.domain.football.repository.relations;

import com.footballay.core.domain.football.persistence.Player;
import com.footballay.core.domain.football.persistence.Team;
import com.footballay.core.domain.football.persistence.relations.TeamPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamPlayerRepository extends JpaRepository<TeamPlayer, Long>{

    void deleteByTeamAndPlayer(Team team, Player player);

    // Optional<LeagueTeam> findByLeagueAndTeam(_League league, _Team team);

    Optional<TeamPlayer> findByTeamAndPlayer(Team team, Player player);

    List<TeamPlayer> findTeamsByPlayer(Player player);
    // void deleteByPlayer(_Player player);
}
