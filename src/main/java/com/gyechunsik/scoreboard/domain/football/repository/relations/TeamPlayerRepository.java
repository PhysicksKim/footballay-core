package com.gyechunsik.scoreboard.domain.football.repository.relations;

import com.gyechunsik.scoreboard.domain.football.entity.Player;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.entity.relations.LeagueTeam;
import com.gyechunsik.scoreboard.domain.football.entity.relations.TeamPlayer;
import com.gyechunsik.scoreboard.domain.football.entity.relations.TeamPlayersId;
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
