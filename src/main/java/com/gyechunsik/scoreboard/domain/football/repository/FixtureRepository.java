package com.gyechunsik.scoreboard.domain.football.repository;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    List<Fixture> findAllByLeague(League league);

    List<Fixture> findFixturesByLeague(League league, Pageable pageable);

    @Query("SELECT f FROM Fixture f WHERE f.date = " +
            "(SELECT MIN(f2.date) FROM Fixture f2 WHERE f2.date >= :date)")
    List<Fixture> findNextFixturesAfterDate(@Param("date") ZonedDateTime date);
}
