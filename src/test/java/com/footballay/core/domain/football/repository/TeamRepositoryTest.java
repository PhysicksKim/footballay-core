package com.footballay.core.domain.football.repository;

import com.footballay.core.domain.football.persistence.League;
import com.footballay.core.domain.football.persistence.Team;
import com.footballay.core.domain.football.persistence.relations.LeagueTeam;
import com.footballay.core.domain.football.repository.relations.LeagueTeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.footballay.core.domain.football.util.GenerateLeagueTeamFixture.LeagueTeamFixture;
import static com.footballay.core.domain.football.util.GenerateLeagueTeamFixture.generateTwoSameLeague;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Named Query, @Query 등 Repository 의 커스텀 메서드들을 테스트 합니다.
 */
@Slf4j
@Transactional
@DataJpaTest
class TeamRepositoryTest {

    @Autowired
    private LeagueRepository leagueRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private LeagueTeamRepository leagueTeamRepository;

    @DisplayName("리그로 팀을 찾습니다")
    @Test
    void Success_findTeamByLeague() {
        // given
        List<LeagueTeamFixture> list = generateTwoSameLeague();
        LeagueTeamFixture ltf1 = list.get(0);
        LeagueTeamFixture ltf2 = list.get(1);

        League league = leagueRepository.save(ltf1.league);
        List<Team> teams = teamRepository.saveAll(
                List.of(ltf1.home, ltf1.away, ltf2.home, ltf2.away)
        );
        List<LeagueTeam> lts = new ArrayList<>();
        teams.forEach(team -> {
            lts.add(LeagueTeam.builder().league(league).team(team).build());
        });
        leagueTeamRepository.saveAll(lts);
        log.info("finished given inserting data");

        // when
        List<Team> teamsByLeague = teamRepository.findTeamsByLeague(league);

        // then
        assertThat(teamsByLeague).hasSize(4);
    }

}