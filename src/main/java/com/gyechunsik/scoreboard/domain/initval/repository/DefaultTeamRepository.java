package com.gyechunsik.scoreboard.domain.initval.repository;

import com.gyechunsik.scoreboard.domain.initval.Entity.DefaultTeam;
import com.gyechunsik.scoreboard.domain.initval.Entity.Streamer;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.TeamSide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DefaultTeamRepository extends JpaRepository<DefaultTeam, Long> {

    List<DefaultTeam> findDefaultTeamsByStreamer(Streamer streamer);

    List<DefaultTeam> findByStreamer(Streamer streamer);
    Optional<DefaultTeam> findByStreamerAndSide(Streamer streamer, TeamSide side);
}
