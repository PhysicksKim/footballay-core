package com.gyechunsik.scoreboard.domain.football.repository;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    List<Fixture> findAllByLeague(League league);

    List<Fixture> findFixturesByLeague(League league, Pageable pageable);

    @Query("SELECT f FROM Fixture f WHERE f.date = " +
            "(SELECT MIN(f2.date) FROM Fixture f2 WHERE f2.date >= :date)")
    List<Fixture> findNextFixturesAfterDate(@Param("date") ZonedDateTime date);

    /**
     * 이용 가능한 fixture 를 조회합니다. leagueId , isAvailable , date 로 조회
     * 해당 리그의 date 이후의 이용 가능한 fixture 를 조회합니다.
     * 이를 위해 Index ( leagueId , isAvailable , date ) 로 생성합니다.
     */
    @Query("SELECT f FROM Fixture f WHERE f.league.leagueId = :leagueId AND f.available = true AND f.date >= :date")
    List<Fixture> findAvailableFixturesByLeagueIdAndDate(@Param("leagueId") Long leagueId, @Param("date") ZonedDateTime date);

    @EntityGraph(attributePaths = {"liveStatus", "homeTeam", "awayTeam", "league"})
    Optional<Fixture> findById(Long fixtureId);
}
