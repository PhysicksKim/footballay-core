package com.gyechunsik.scoreboard.domain.football.repository;

import com.gyechunsik.scoreboard.domain.football.favorite.entity.FavoriteFixture;
import com.gyechunsik.scoreboard.domain.football.favorite.entity.FavoriteLeague;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteFixtureRepository extends JpaRepository<FavoriteFixture, Long> {

    int deleteByFixtureId(Long fixtureId);

    Optional<FavoriteFixture> findByFixtureId(Long fixtureId);

    Page<FavoriteFixture> findByLeagueIdOrderByDateAsc(Long leagueId, Pageable pageable);

}
