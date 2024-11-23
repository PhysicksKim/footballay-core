package com.gyechunsik.scoreboard.domain.football.repository.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchLineup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchLineupRepository extends JpaRepository<MatchLineup, Long> {

    @Query("SELECT ml FROM MatchLineup ml " +
            "JOIN FETCH ml.matchPlayers mp " +
            "LEFT JOIN FETCH mp.player p " +
            "WHERE ml.fixture = :fixture AND ml.team = :team")
    Optional<MatchLineup> findByFixtureAndTeam(Fixture fixture, Team team);

    List<MatchLineup> findAllByFixture(Fixture fixture);

    /**
     * 라인업 선수들이 포함된 MatchLineup 을 조회합니다. <br>
     * 미등록 선수도 조회되도록 하기 위하여 LEFT JOIN 을 사용합니다.
     * @param fixture 경기 정보
     * @param team 팀 정보
     * @return 등록/미등록 라인업 선수들이 포함된 MatchLineup
     */
    @Query("SELECT ml FROM MatchLineup ml " +
            "JOIN FETCH ml.matchPlayers mp " +
            "LEFT JOIN FETCH mp.player p " +
            "WHERE ml.fixture = :fixture AND ml.team = :team")
    Optional<MatchLineup> findTeamLineupByFixture(@Param("fixture") Fixture fixture, @Param("team") Team team);

}
