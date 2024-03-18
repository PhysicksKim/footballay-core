package com.gyechunsik.scoreboard.domain.football.player.repository;

import com.gyechunsik.scoreboard.domain.football.player.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findAllByTeamId(Long teamId);
}
