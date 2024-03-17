package com.gyechunsik.scoreboard.domain.football.data.cache;

import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.constant.TeamId;
import com.gyechunsik.scoreboard.domain.football.league.League;
import com.gyechunsik.scoreboard.domain.football.league.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.relations.LeagueTeam;
import com.gyechunsik.scoreboard.domain.football.relations.LeagueTeamRepository;
import com.gyechunsik.scoreboard.domain.football.team.Team;
import com.gyechunsik.scoreboard.domain.football.team.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

//TODO : 혹시 이미 캐싱된 항목에 다시 캐싱 요청하는 경우에 대한 테스트 필요.
@Slf4j
@Transactional
@SpringBootTest
// @ActiveProfiles("api")
@ActiveProfiles("mockapi")
class ApiCacheServiceTest {

    @Autowired
    private ApiCacheService apiCacheService;

    @Autowired
    private LeagueRepository leagueRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private LeagueTeamRepository leagueTeamRepository;

    @DisplayName("")
    @Test
    void success_leagueCaching() {
        // given
        long eplId = LeagueId.EPL;

        // when
        apiCacheService.cacheLeague(eplId);

        // then
        League findLeague = leagueRepository.findById(eplId)
                .orElseThrow(() ->
                        new RuntimeException("테스트 에러! repository 에 저장된 league 가 없습니다. 캐싱에 실패했습니다.")
                );
        assertThat(findLeague).isNotNull();
        assertThat(findLeague.getLeagueId()).isEqualTo(eplId);
    }

    @DisplayName("")
    @Test
    void success_teamCurrentLeagues() {
        // given
        long manutd = TeamId.MANUTD;

        // when
        apiCacheService.cacheTeamAndCurrentLeagues(manutd);

        // then
        List<League> findLeagues = leagueRepository.findAll();
        List<Team> findTeams = teamRepository.findAll();
        List<LeagueTeam> findLeagueTeams = leagueTeamRepository.findAll();

        // log all finds
        log.info("findLeagues : {}", findLeagues);
        log.info("findTeams : {}", findTeams);
        log.info("findLeagueTeams : {}", findLeagueTeams);
    }
}