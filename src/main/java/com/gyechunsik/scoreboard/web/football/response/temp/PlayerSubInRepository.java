package com.gyechunsik.scoreboard.web.football.response.temp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerSubInRepository extends JpaRepository<PlayerSubIn, Long> {
}
