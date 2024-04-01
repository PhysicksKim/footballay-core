package com.gyechunsik.scoreboard.domain.initval.repository;

import com.gyechunsik.scoreboard.domain.initval.Entity.DefaultMatch;
import com.gyechunsik.scoreboard.domain.initval.Entity.Streamer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DefaultMatchRepository extends JpaRepository<DefaultMatch, Long> {

    Optional<DefaultMatch> findDefaultMatchByStreamer(Streamer streamer);

}
