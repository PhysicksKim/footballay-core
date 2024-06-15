package com.gyechunsik.scoreboard.domain.football.repository;

import com.gyechunsik.scoreboard.domain.football.available.entity.AvailableLeague;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteLeagueRepository extends JpaRepository<AvailableLeague, Long> {

    int deleteByLeagueId(Long leagueId);

    Optional<AvailableLeague> findByLeagueId(Long leagueId);

    Page<AvailableLeague> findByOrderByCreatedDateAsc(Pageable pageable);

}
