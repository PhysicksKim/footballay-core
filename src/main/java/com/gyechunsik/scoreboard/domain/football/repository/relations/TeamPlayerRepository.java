package com.gyechunsik.scoreboard.domain.football.repository.relations;

import com.gyechunsik.scoreboard.domain.football.entity.Player;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.entity.relations.TeamPlayer;
import com.gyechunsik.scoreboard.domain.football.entity.relations.TeamPlayersId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamPlayerRepository extends JpaRepository<TeamPlayer, TeamPlayersId>{

    void deleteByTeamAndPlayer(Team team, Player player);

    // void deleteByPlayer(Player player);
}
