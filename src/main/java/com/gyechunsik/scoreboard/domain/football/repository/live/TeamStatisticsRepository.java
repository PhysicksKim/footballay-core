package com.gyechunsik.scoreboard.domain.football.repository.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.TeamStatistics;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface TeamStatisticsRepository extends JpaRepository<TeamStatistics, Long> {

    /**
     * fixture, team, expectedGoalsList 를 fetch join 합니다.
     * @param fixture
     * @param homeTeam
     * @return
     */
    @EntityGraph(attributePaths = {"fixture", "team", "expectedGoalsList"})
    Optional<TeamStatistics> findByFixtureAndTeam(Fixture fixture, Team homeTeam);
}
