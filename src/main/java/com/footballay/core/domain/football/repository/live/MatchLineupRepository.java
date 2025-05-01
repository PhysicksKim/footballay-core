package com.footballay.core.domain.football.repository.live;

import com.footballay.core.domain.football.persistence.Fixture;
import com.footballay.core.domain.football.persistence.Team;
import com.footballay.core.domain.football.persistence.live.MatchLineup;
import com.footballay.core.domain.football.persistence.live.MatchPlayer;
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

    /*
    SELECT DISTINCT 를 하지 않으면 List<MatchLineup> 에 lineup 하나에 matchPlayer 하나씩 들어가게 됩니다.
    따라서 라인업 하나에 연관관계 있는 모든 선수들이 들어가도록 하기 위하여 DISTINCT 를 사용합니다.
     */
    /**
     * 경기에 등록된 모든 라인업을 조회합니다. <br>
     * {@link MatchPlayer} 도 함께 조회되도록 하기 위하여 JOIN FETCH 를 사용합니다. <br>
     * 미등록 선수도 조회되도록 하기 위하여 LEFT JOIN 을 사용합니다. <br>
     *
     * @param fixture
     * @return
     */
    @Query("SELECT DISTINCT ml FROM MatchLineup ml " +
            "JOIN FETCH ml.matchPlayers mp " +
            "LEFT JOIN FETCH mp.player p " +
            "WHERE ml.fixture = :fixture")
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
