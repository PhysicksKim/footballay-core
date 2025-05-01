package com.footballay.core.domain.football.repository.live;

import com.footballay.core.domain.football.persistence.live.PlayerStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerStatisticsRepository extends JpaRepository<PlayerStatistics, Long> {
    //
    // /**
    //  * fixture, team, players 를 fetch join 합니다.
    //  * @param fixture
    //  * @param team
    //  * @param players
    //  * @return
    //  */
    // @Query("SELECT ps FROM PlayerStatistics ps " +
    //         "JOIN FETCH ps.team " + // Fetch join team
    //         "JOIN FETCH ps.player " + // Fetch join player
    //         "WHERE ps.fixture = :fixture AND ps.team = :team AND ps.player IN :players")
    // List<PlayerStatistics> findByFixtureAndTeamAndPlayerIn(
    //         @Param("fixture") Fixture fixture,
    //         @Param("team") Team team,
    //         @Param("players") List<Player> players
    // );
    //
    // /**
    //  * team 과 player 를 fetch join 합니다.
    //  * @param fixture
    //  * @param team
    //  * @return
    //  */
    // @Query("SELECT ps FROM PlayerStatistics ps " +
    //         "JOIN FETCH ps.team " + // Fetch join team
    //         "JOIN FETCH ps.player " + // Fetch join player
    //         "WHERE ps.fixture = :fixture AND ps.team = :team")
    // List<PlayerStatistics> findByFixtureAndTeam(
    //         @Param("fixture") Fixture fixture,
    //         @Param("team") Team team
    // );

}
