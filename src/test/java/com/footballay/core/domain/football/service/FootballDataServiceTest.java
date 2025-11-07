package com.footballay.core.domain.football.service;

import com.footballay.core.domain.football.persistence.League;
import com.footballay.core.domain.football.persistence.Player;
import com.footballay.core.domain.football.persistence.Team;
import com.footballay.core.domain.football.persistence.relations.LeagueTeam;
import com.footballay.core.domain.football.persistence.relations.TeamPlayer;
import com.footballay.core.domain.football.repository.LeagueRepository;
import com.footballay.core.domain.football.repository.PlayerRepository;
import com.footballay.core.domain.football.repository.TeamRepository;
import com.footballay.core.domain.football.repository.relations.LeagueTeamRepository;
import com.footballay.core.domain.football.repository.relations.TeamPlayerRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static com.footballay.core.domain.football.util.GenerateLeagueTeamFixture.*;
import static com.footballay.core.domain.football.util.GeneratePlayersOfTeam.generatePlayersOfTeam;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
class FootballDataServiceTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FootballDataServiceTest.class);
    @Autowired
    private FootballDataService footballDataService;
    @Autowired
    private LeagueRepository leagueRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private LeagueTeamRepository leagueTeamRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private TeamPlayerRepository teamPlayerRepository;
    @Autowired
    private EntityManager em;

    @DisplayName("사용 가능한 리그들을 조회합니다")
    @Test
    void success_getLeagues() {
        // given
        List<LeagueTeamFixture> ltfList = generateTwoOtherLeagues();
        League league1 = ltfList.get(0).league;
        League league2 = ltfList.get(1).league;
        leagueRepository.saveAll(List.of(league1, league2));
        // when
        List<League> leagues = footballDataService.getLeagues(2);
        List<League> leagues_OverSize = footballDataService.getLeagues(10);
        log.info("leagues :: {}", leagues);
        log.info("leagues_OverSize :: {}", leagues_OverSize);
        // then
        assertThat(leagues).hasSize(2);
        assertThat(leagues_OverSize).hasSize(2);
        assertThat(leagues).isEqualTo(leagues_OverSize);
    }

    @DisplayName("리그 아이디로 리그를 찾습니다")
    @Test
    void success_findLeagueById() {
        // given
        LeagueTeamFixture generate = generate();
        League league = generate.league;
        leagueRepository.save(league);
        // when
        League findLeague = footballDataService.findLeagueById(league.getLeagueId());
        // then
        assertThat(findLeague).isEqualTo(league);
    }

    @DisplayName("리그 아이디로, 리그에 속한 팀들을 모두 찾습니다.")
    @Test
    void success_getTeamsByLeagueId() {
        // given
        List<LeagueTeamFixture> ltfList = generateTwoSameLeague();
        League league = leagueRepository.save(ltfList.get(0).league);
        Team team1 = ltfList.get(0).home;
        Team team2 = ltfList.get(0).away;
        Team team3 = ltfList.get(1).home;
        Team team4 = ltfList.get(1).away;
        LeagueTeam leagueTeam1 = LeagueTeam.builder().league(league).team(team1).build();
        LeagueTeam leagueTeam2 = LeagueTeam.builder().league(league).team(team2).build();
        LeagueTeam leagueTeam3 = LeagueTeam.builder().league(league).team(team3).build();
        LeagueTeam leagueTeam4 = LeagueTeam.builder().league(league).team(team4).build();
        league.setLeagueTeams(List.of(leagueTeam1, leagueTeam2, leagueTeam3, leagueTeam4));
        team1.setLeagueTeams(List.of(leagueTeam1));
        team2.setLeagueTeams(List.of(leagueTeam2));
        team3.setLeagueTeams(List.of(leagueTeam3));
        team4.setLeagueTeams(List.of(leagueTeam4));
        List<Team> teams = teamRepository.saveAll(List.of(team1, team2, team3, team4));
        leagueTeamRepository.saveAll(List.of(leagueTeam1, leagueTeam2, leagueTeam3, leagueTeam4));
        // when
        List<Team> getTeams = footballDataService.getTeamsByLeagueId(league.getLeagueId());
        // then
        assertThat(getTeams).hasSize(4);
        List<LeagueTeam> all = leagueTeamRepository.findAll();
        log.info("all :: {}", all);
        League findLeague = leagueRepository.findById(ltfList.get(0).league.getLeagueId()).get();
        List<LeagueTeam> leagueTeams = findLeague.getLeagueTeams();
        log.info("leagueTeams :: {}", leagueTeams);
    }

    @Transactional
    @DisplayName("팀에 속한 선수들을 모두 찾습니다")
    @Test
    void success_getSquadOfTeam() {
        // given
        LeagueTeamFixture generate = generate();
        Team savedTeam = teamRepository.save(generate.home);
        List<Player> savedPlayers = playerRepository.saveAll(generatePlayersOfTeam(savedTeam));
        List<TeamPlayer> teamPlayerList = savedPlayers.stream().map(player -> player.toTeamPlayer(savedTeam)).toList();
        teamPlayerRepository.saveAll(teamPlayerList);
        em.flush();
        em.clear();
        Team team = teamRepository.findById(savedTeam.getId()).orElseThrow();
        List<TeamPlayer> teamPlayers = team.getTeamPlayers();
        log.info("teamPlayers :: {}", teamPlayers);
        // when
        List<Player> squad = footballDataService.getSquadOfTeam(savedTeam.getId());
        log.info("squad players : {}", squad.stream().map(Player::getName).toList());
        // then
        assertThat(squad).isNotEmpty();
        assertThat(squad.get(0).getId()).isNotNull();
        assertThat(squad.get(0).getName()).isNotNull();
    }
}
