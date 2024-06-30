package com.gyechunsik.scoreboard.domain.football.repository.live;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.live.FixtureEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FixtureEventRepository extends JpaRepository<FixtureEvent, Long> {

    @Query("SELECT fe FROM FixtureEvent fe WHERE fe.fixture = :fixture ORDER BY fe.sequence ASC")
    List<FixtureEvent> findByFixtureOrderBySequenceDesc(Fixture fixture);

}
