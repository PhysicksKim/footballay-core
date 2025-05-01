package com.footballay.core.domain.football.repository;

import com.footballay.core.domain.football.persistence.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    @Query("SELECT p FROM Player p JOIN p.teamPlayers tp WHERE tp.team.id = :teamId")
    List<Player> findAllByTeam(Long teamId);
}
