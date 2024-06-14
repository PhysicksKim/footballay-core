package com.gyechunsik.scoreboard.domain.football.repository;

import com.gyechunsik.scoreboard.domain.football.favorite.entity.FavoriteLeague;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteLeagueRepository extends JpaRepository<FavoriteLeague, Long> {

    int deleteByLeagueId(Long leagueId);

    Optional<FavoriteLeague> findByLeagueId(Long leagueId);

    Page<FavoriteLeague> findByOrderByCreatedDateAsc(Pageable pageable);

}
