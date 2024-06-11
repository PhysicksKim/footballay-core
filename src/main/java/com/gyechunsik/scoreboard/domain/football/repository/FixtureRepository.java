package com.gyechunsik.scoreboard.domain.football.repository;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FixtureRepository extends JpaRepository<Fixture, Long> {

    List<Fixture> findAllByLeague(League league);

}
