package com.gyechunsik.scoreboard.domain.initval.repository;

import com.gyechunsik.scoreboard.domain.initval.Entity.Streamer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StreamerRepository extends JpaRepository<Streamer, Long> {

    Optional<Streamer> findByHash(String hash);

    Optional<Streamer> findByName(String name);

}
