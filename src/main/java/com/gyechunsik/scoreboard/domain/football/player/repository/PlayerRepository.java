package com.gyechunsik.scoreboard.domain.football.player.repository;

import com.gyechunsik.scoreboard.domain.football.player.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

}
