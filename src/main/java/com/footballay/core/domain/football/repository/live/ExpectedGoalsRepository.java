package com.footballay.core.domain.football.repository.live;

import com.footballay.core.domain.football.persistence.live.ExpectedGoals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpectedGoalsRepository extends JpaRepository<ExpectedGoals, Long> {
}
