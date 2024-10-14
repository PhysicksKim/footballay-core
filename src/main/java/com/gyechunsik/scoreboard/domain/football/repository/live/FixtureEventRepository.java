package com.gyechunsik.scoreboard.domain.football.repository.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.live.FixtureEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FixtureEventRepository extends JpaRepository<FixtureEvent, Long> {

    @Query("SELECT fe FROM FixtureEvent fe " +
            "JOIN FETCH fe.team t " +
            "JOIN FETCH fe.player p " +
            "LEFT JOIN FETCH fe.assist a " +
            "WHERE fe.fixture = :fixture " +
            "ORDER BY fe.sequence ASC")
    List<FixtureEvent> findByFixtureOrderBySequenceDesc(Fixture fixture);

}
