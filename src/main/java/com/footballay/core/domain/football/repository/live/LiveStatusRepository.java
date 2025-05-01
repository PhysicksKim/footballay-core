package com.footballay.core.domain.football.repository.live;

import com.footballay.core.domain.football.persistence.Fixture;
import com.footballay.core.domain.football.persistence.live.LiveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LiveStatusRepository extends JpaRepository<LiveStatus, Long> {

    Optional<LiveStatus> findLiveStatusByFixture(Fixture fixture);
}
