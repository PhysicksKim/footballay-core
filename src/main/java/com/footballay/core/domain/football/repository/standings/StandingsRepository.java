package com.footballay.core.domain.football.repository.standings;

import com.footballay.core.domain.football.persistence.standings.Standing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StandingsRepository extends JpaRepository<Standing, Long> {
}
