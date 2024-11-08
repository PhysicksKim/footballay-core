package com.gyechunsik.scoreboard.domain.football.repository;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.League;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    List<Fixture> findAllByLeague(League league);

    List<Fixture> findFixturesByLeague(League league, Pageable pageable);

    @Query("SELECT f FROM Fixture f " +
            "JOIN FETCH f.liveStatus ls " +
            "JOIN FETCH f.league l " +
            "JOIN FETCH f.homeTeam ht " +
            "JOIN FETCH f.awayTeam at " +
            "WHERE f.league = :league " +
            "AND CAST( f.date AS DATE ) = (SELECT CAST( MIN(f2.date) AS DATE) FROM Fixture f2 WHERE f2.date >= :date)")
    List<Fixture> findNextFixturesAfterDate(@Param("league") League league, @Param("date") LocalDateTime date);

    @EntityGraph(attributePaths = {"liveStatus", "homeTeam", "awayTeam", "league"})
    Optional<Fixture> findById(Long fixtureId);

    @Query("SELECT f FROM Fixture f " +
            "JOIN FETCH f.league l " +
            "JOIN FETCH f.homeTeam ht " +
            "JOIN FETCH f.awayTeam at " +
            "JOIN FETCH f.liveStatus ls " +
            "WHERE f.fixtureId = :fixtureId")
    Optional<Fixture> findFixtureByIdWithDetails(long fixtureId);

    /**
     * 이용 가능한 fixture 를 조회합니다. league , isAvailable , date 로 조회
     * 해당 리그의 date 이후의 이용 가능한 fixture 를 조회합니다.
     * 이를 위해 Index ( league , isAvailable , date ) 로 생성합니다.
     */
    @Query("SELECT f FROM Fixture f " +
            "LEFT JOIN FETCH f.liveStatus ls " +
            "JOIN FETCH f.league l " +
            "JOIN FETCH f.homeTeam ht " +
            "JOIN FETCH f.awayTeam at " +
            "WHERE f.league = :league " +
            "AND f.available = true " +
            "AND f.date >= :date"
    )
    List<Fixture> findAvailableFixturesByLeagueAndDate(@Param("league") League league,
                                                       @Param("date") LocalDateTime date);

    /**
     * 해당 리그의 date 이후의 fixture 들을 조회합니다.
     * parameter pageable 을 이용하여 fixture 를 조회합니다.
     *
     * @param league   리그
     * @param date     fixture 찾기 기준 날짜. 해당 dateTime 을 포함합니다.
     * @param pageable 날짜 이후 첫 번째 fixture 만 얻으려는 경우 PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "date"));
     * @return
     */
    @Query("SELECT f FROM Fixture f " +
            "LEFT JOIN FETCH f.liveStatus ls " +
            "JOIN FETCH f.league l " +
            "JOIN FETCH f.homeTeam ht " +
            "JOIN FETCH f.awayTeam at " +
            "WHERE f.league = :league " +
            "AND f.date >= :date " +
            "ORDER BY f.date ASC"
    )
    List<Fixture> findFixturesByLeagueAndDateAfter(League league, LocalDateTime date, Pageable pageable);

    @Query("SELECT f FROM Fixture f " +
            "LEFT JOIN FETCH f.liveStatus ls " +
            "JOIN FETCH f.league l " +
            "JOIN FETCH f.homeTeam ht " +
            "JOIN FETCH f.awayTeam at " +
            "WHERE f.league = :league " +
            "AND f.date >= :startOfDay " +
            "AND f.date < :endOfDay " +
            "ORDER BY f.date ASC"
    )
    List<Fixture> findFixturesByLeagueAndDateRange(League league, LocalDateTime startOfDay, LocalDateTime endOfDay);

    /**
     * 해당 리그의 date 이후의 Available fixture 들을 조회합니다.
     * parameter pageable 을 이용하여 fixture 를 조회합니다.
     *
     * @param league   리그
     * @param date     fixture 찾기 기준 날짜. 해당 dateTime 을 포함합니다.
     * @param pageable 날짜 이후 첫 번째 available fixture 만 얻으려는 경우 PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "date"));
     * @return
     */
    @Query("SELECT f FROM Fixture f " +
            "LEFT JOIN FETCH f.liveStatus ls " +
            "JOIN FETCH f.league l " +
            "JOIN FETCH f.homeTeam ht " +
            "JOIN FETCH f.awayTeam at " +
            "WHERE f.league = :league " +
            "AND f.available = true " +
            "AND f.date >= :date " +
            "ORDER BY f.date ASC"
    )
    List<Fixture> findAvailableFixturesByLeagueAndDateAfter(League league, LocalDateTime date, Pageable pageable);

    @Query("SELECT f FROM Fixture f " +
            "LEFT JOIN FETCH f.liveStatus ls " +
            "JOIN FETCH f.league l " +
            "JOIN FETCH f.homeTeam ht " +
            "JOIN FETCH f.awayTeam at " +
            "WHERE f.league = :league " +
            "AND f.available = true " +
            "AND f.date >= :startOfDay " +
            "AND f.date < :endOfDay " +
            "ORDER BY f.date ASC"
    )
    List<Fixture> findAvailableFixturesByLeagueAndDateRange(League league, LocalDateTime startOfDay, LocalDateTime endOfDay);

    // TODO : 테스트 및 쿼리 문제 해결 필요
    // @Query("SELECT f FROM Fixture f " +
    //         "LEFT JOIN FETCH f.liveStatus ls " +
    //         "JOIN FETCH f.league l " +
    //         "JOIN FETCH f.homeTeam ht " +
    //         "JOIN FETCH f.awayTeam at " +
    //         "JOIN FETCH f.lineups lu JOIN FETCH lu.matchPlayers mp " +
    //         "JOIN FETCH f.events e " +
    //         "JOIN FETCH e.player ep JOIN FETCH ep.matchLineup epml " +
    //         "JOIN FETCH e.assist ea JOIN FETCH ea.matchLineup eaml " +
    //         "JOIN FETCH f.teamStatistics ts JOIN FETCH ts.expectedGoalsList xg " +
    //         "WHERE f.fixtureId = :fixtureId"
    // )
    // Optional<Fixture> findByIdWithAllAssociations(long fixtureId);

    @Query("SELECT f FROM Fixture f " +
            "LEFT JOIN FETCH f.liveStatus ls " +
            "JOIN FETCH f.league l " +
            "JOIN FETCH f.homeTeam ht " +
            "JOIN FETCH f.awayTeam at " +
            "LEFT JOIN FETCH f.lineups lu " +
            "WHERE f.fixtureId = :fixtureId"
    )
    Optional<Fixture> findByIdWithAllAssociations(long fixtureId);
}
