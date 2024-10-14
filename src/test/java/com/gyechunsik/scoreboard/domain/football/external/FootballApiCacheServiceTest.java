package com.gyechunsik.scoreboard.domain.football.external;

import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.constant.PlayerId;
import com.gyechunsik.scoreboard.domain.football.constant.TeamId;
import com.gyechunsik.scoreboard.domain.football.persistence.League;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.relations.LeagueTeam;
import com.gyechunsik.scoreboard.domain.football.persistence.relations.TeamPlayer;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.relations.LeagueTeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.relations.TeamPlayerRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Transactional
@SpringBootTest
@ActiveProfiles("mockapi")
class FootballApiCacheServiceTest {

    @Autowired
    private FootballApiCacheService footballApiCacheService;
    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private EntityManager em;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private LeagueTeamRepository leagueTeamRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private TeamPlayerRepository teamPlayerRepository;

    @DisplayName("mockapi 를 사용해 league json 파일의 caching 에 성공합니다")
    @Test
    void success_leagueCaching() {
        // given
        long eplId = LeagueId.EPL;

        // when
        footballApiCacheService.cacheLeague(eplId);

        // then
        League findLeague = leagueRepository.findById(eplId)
                .orElseThrow(() ->
                        new RuntimeException("테스트 에러! repository 에 저장된 league 가 없습니다. 캐싱에 실패했습니다.")
                );
        assertThat(findLeague).isNotNull();
        assertThat(findLeague.getLeagueId()).isEqualTo(eplId);
    }

    @DisplayName("mockapi 를 사용해 league json 파일의 caching 에 성공합니다")
    @Test
    void success_teamCurrentLeagues() {
        // given
        long manutd = TeamId.MANUTD;

        // when
        footballApiCacheService.cacheTeamAndCurrentLeagues(manutd);

        // then
        List<League> findLeagues = leagueRepository.findAll();
        List<Team> findTeams = teamRepository.findAll();
        List<LeagueTeam> findLeagueTeams = leagueTeamRepository.findAll();

        assertThat(findLeagues).isNotEmpty();
        assertThat(findTeams).isNotEmpty();

        // log all finds
        log.info("getLeagues : {}", findLeagues);
        log.info("findTeams : {}", findTeams);
        log.info("findLeagueTeams : {}", findLeagueTeams);
    }

    @DisplayName("리그 캐싱 - 새롭게 캐싱")
    @Test
    void cacheLeagues() {
        // when
        League euro = footballApiCacheService.cacheLeague(LeagueId.EURO);

        // then
        List<League> leagues = leagueRepository.findAll();
        log.info("leagues: {}", leagues.stream().map(League::getName).toList());
        assertThat(leagues).isNotEmpty();
    }

    @DisplayName("리그 캐싱 - 업데이트")
    @Test
    void cacheLeagues_update() {
        // given
        saveEuroLeague();
        final String euroPrevName = "EURO-shouldUpdate";
        final String euroKoreanName = "유료";
        League euro = leagueRepository.findById(LeagueId.EURO).orElseThrow();
        euro.setName(euroPrevName);
        euro.setKoreanName(euroKoreanName);
        em.flush();
        em.clear();

        // when
        euro = footballApiCacheService.cacheLeague(LeagueId.EURO);

        // then
        assertThat(euro.getName()).isNotEqualTo(euroPrevName);
        assertThat(euro.getKoreanName()).isEqualTo(euroKoreanName);
    }

    @DisplayName("리그의 팀 캐싱 - 완전히 비어있고 새롭게 캐싱될 때")
    @Test
    void cacheTeamsOfLeague() {
        // given
        saveEuroLeague();
        final long leagueId = LeagueId.EURO;

        // when
        List<Team> teams = footballApiCacheService.cacheTeamsOfLeague(leagueId);

        // then
        log.info("teams: {}", teams.stream().map(Team::getName).toList());
        assertThat(teams).isNotEmpty();
    }

    @DisplayName("리그의 팀 캐싱 - 이미 캐싱된 팀이 있는 경우, 팀 엔티티의 일부 정보만 업데이트 합니다")
    @Test
    void cacheTeamsOfLeague_UpdateIfAlreadyExist() {
        // given
        saveEuroLeague();
        saveBelgium();
        final long leagueId = LeagueId.EURO;
        final long belgiumId = TeamId.BELGIUM;
        Team belgium = teamRepository.findById(belgiumId).orElseThrow();
        belgium.setName("Belgium-alreadyExist");
        em.flush();
        em.clear();
        belgium=null;

        // when
        belgium = teamRepository.findById(1L).orElseThrow();
        log.info("BEFORE CACHE :: belgium name = {}", belgium.getName());
        log.info("BEFORE CACHE :: belgium koreanName = {}", belgium.getKoreanName());

        List<Team> teams = footballApiCacheService.cacheTeamsOfLeague(leagueId);
        log.info("teams: {}", teams.stream().map(Team::getName).toList());

        // then
        belgium = teamRepository.findById(belgiumId).orElseThrow();
        log.info("AFTER CACHE :: belgium name = {}", belgium.getName());
        log.info("AFTER CACHE :: belgium koreanName = {}", belgium.getKoreanName());
        assertThat(belgium.getName()).isEqualTo("Belgium");
        assertThat(belgium.getKoreanName()).isEqualTo("벨기에")
                .withFailMessage("이미 저장되어 있던 팀은 api cache 에 의해 한글 이름 데이터가 변경되면 안됩니다");
    }

    @DisplayName("리그의 팀 캐싱 - 기존에 캐싱된 팀이, API 응답에는 없어진 경우")
    @Test
    void cacheTeamsOfLeague_RemovePrevExistTeam() {
        // given
        saveEuroLeague();
        saveKoreanInEuro();

        final long leagueId = LeagueId.EURO;
        final long koreaId = TeamId.SOUTH_KOREA;
        League euro = leagueRepository.findById(leagueId).orElseThrow();
        log.info("BEFORE CACHE :: EURO teams : {}" , teamRepository.findTeamsByLeague(euro).stream().map(Team::getName).toList());

        // when
        List<Team> teams = footballApiCacheService.cacheTeamsOfLeague(leagueId);

        // then
        log.info("AFTER CACHE :: EURO teams : {}" , teams.stream().map(Team::getName).toList());
        List<Team> euroTeams = teamRepository.findTeamsByLeague(euro);
        euroTeams.forEach(team -> {
            if(team.getId() == koreaId) {
                org.junit.jupiter.api.Assertions.fail("한국이 유로에서 제외되었는데, 한국이 팀 목록에 남아있습니다");
            }
        });
    }

    @DisplayName("리그의 팀 캐싱 - 연관관계 중복 저장 방지")
    @Test
    void cacheTeamsOfLeague_AvoidDuplicateLeagueTeam() {
        // given
        saveEuroLeague();
        saveBelgium();
        final long leagueId = LeagueId.EURO;
        final long belgiumId = TeamId.BELGIUM;

        // when
        List<Team> teams = footballApiCacheService.cacheTeamsOfLeague(leagueId);
        List<LeagueTeam> leagueTeams = leagueTeamRepository.findAll();

        // then
        long count = leagueTeams.stream()
                .filter(leagueTeam -> leagueTeam.getTeam().getId() == belgiumId)
                .count();
        assertThat(count).isEqualTo(1)
                .withFailMessage("리그팀 연관관계가 중복 저장되었습니다.");
    }

    @DisplayName("팀 스쿼드 캐싱 - 연관관계 중복 저장 방지 및 업데이트 테스트")
    @Test
    void cacheTeamSquad_AvoidDuplicateAndUpdate() {
        // given
        saveEuroLeague();
        saveBelgium();
        saveBelgiumPlayer();
        final long belgiumId = TeamId.BELGIUM;

        // when
        List<Player> players = footballApiCacheService.cacheTeamSquad(belgiumId);
        List<TeamPlayer> teamPlayers = teamPlayerRepository.findAll();

        // then
        long count = teamPlayers.stream()
                .filter(teamPlayer -> teamPlayer.getTeam().getId() == belgiumId && teamPlayer.getPlayer().getId() == PlayerId.De_Bruyne)
                .count();
        assertThat(count).isEqualTo(1)
                .withFailMessage("팀플레이어 연관관계가 중복 저장되었습니다.");

        Player deBruyne = playerRepository.findById(PlayerId.De_Bruyne).orElseThrow();
        assertThat(deBruyne.getKoreanName()).isEqualTo("케빈 데 브라위너")
                .withFailMessage("한글 이름 필드가 덮어써져서는 안됩니다.");
    }

    private void saveEuroLeague() {
        final long leagueId = LeagueId.EURO;
        League league = League.builder()
                .leagueId(leagueId)
                .name("EURO")
                .koreanName("유로")
                .logo("https://static.domain.com/league/" + leagueId + ".png")
                .available(true)
                .currentSeason(2024)
                .build();
        leagueRepository.save(league);

        em.flush();
        em.clear();
    }

    private void saveBelgium() {
        Team team = Team.builder()
                .id(1L)
                .name("Belgium")
                .koreanName("벨기에")
                .logo("https://media.api-sports.io/football/teams/1.png")
                .build();
        teamRepository.save(team);

        League euro = leagueRepository.findById(LeagueId.EURO).orElseThrow();
        leagueTeamRepository.save(LeagueTeam.builder()
                .league(euro)
                .team(team)
                .build());

        em.flush();
        em.clear();
    }

    private void saveKoreanInEuro() {
        Team korea = Team.builder()
                .id(TeamId.SOUTH_KOREA)
                .name("Korea")
                .koreanName("대한민국")
                .logo("https://media.api-sports.io/football/teams/2.png")
                .build();
        teamRepository.save(korea);

        League euro = leagueRepository.findById(LeagueId.EURO).orElseThrow();
        leagueTeamRepository.save(LeagueTeam.builder()
                .league(euro)
                .team(korea)
                .build());

        em.flush();
        em.clear();
    }

    private void saveBelgiumPlayer() {
        Team belgium = teamRepository.findById(TeamId.BELGIUM).orElseThrow();
        Player deBruyne = Player.builder()
                .id(PlayerId.De_Bruyne)
                .name("K. De Bruyne")
                .koreanName("케빈 데 브라위너")
                .photoUrl("https://media.api-sports.io/football/players/629.png")
                .position("Midfielder")
                .build();
        playerRepository.save(deBruyne);

        teamPlayerRepository.save(deBruyne.toTeamPlayer(belgium));
        em.flush();
        em.clear();
    }
}