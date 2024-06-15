package com.gyechunsik.scoreboard.domain.football.repository;

import com.gyechunsik.scoreboard.domain.football.entity.League;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {

    @NotNull
    Page<League> findAll(Pageable pageable);

    /**
     * 즐겨찾는 리그 목록을 조회합니다. available 로 조회 order by createdDate desc
     * @param available
     */
    List<League> findAllByAvailableOrderByCreatedDateDesc(Boolean available);

}
