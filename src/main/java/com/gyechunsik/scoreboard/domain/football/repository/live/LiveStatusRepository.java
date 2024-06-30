package com.gyechunsik.scoreboard.domain.football.repository.live;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.live.LiveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LiveStatusRepository extends JpaRepository<LiveStatus, Long> {

    Optional<LiveStatus> findLiveStatusByFixture(Fixture fixture);
}
