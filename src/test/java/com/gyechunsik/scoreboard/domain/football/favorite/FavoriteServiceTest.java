package com.gyechunsik.scoreboard.domain.football.favorite;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.favorite.entity.FavoriteFixture;
import com.gyechunsik.scoreboard.domain.football.favorite.entity.FavoriteLeague;
import com.gyechunsik.scoreboard.domain.football.repository.FavoriteFixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.relations.LeagueTeamRepository;
import com.gyechunsik.scoreboard.domain.football.util.GenerateLeagueTeamFixture;
import com.gyechunsik.scoreboard.domain.football.util.GenerateLeagueTeamFixture.LeagueTeamFixture;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("mockapi")
class FavoriteServiceTest {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private LeagueRepository leagueRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private FixtureRepository fixtureRepository;
    @Autowired
    private LeagueTeamRepository leagueTeamRepository;
    @Autowired
    private FavoriteFixtureRepository favoriteFixtureRepository;

    @Autowired
    private EntityManager em;

    @Transactional
    @DisplayName("즐겨찾는 리그를 등록합니다")
    @Test
    void AddFavoriteLeague() {
        // given
        final Long leagueId = 39L;
        final String name = "Premier League";
        final String koreanName = "프리미어리그";
        final String logo = "https://media.api-sports.io/football/leagues/39.png";
        final Integer currentSeason = 2023;

        League league = League.builder()
                .leagueId(leagueId)
                .name(name)
                .koreanName(koreanName)
                .logo(logo)
                .currentSeason(currentSeason)
                .build();
        League save = leagueRepository.save(league);

        // when
        FavoriteLeague favoriteLeague = favoriteService.addFavoriteLeague(save);

        // then
        assertThat(favoriteLeague).isNotNull();
        assertThat(favoriteLeague.getLeagueId()).isEqualTo(leagueId);
        assertThat(favoriteLeague.getName()).isEqualTo(name);
        assertThat(favoriteLeague.getKoreanName()).isEqualTo(koreanName);
        assertThat(favoriteLeague.getSeason()).isEqualTo(currentSeason);
    }

    @Transactional
    @DisplayName("즐겨찾는 경기일정을 등록합니다")
    @Test
    void AddFavoriteMatch() {
        // given
        LeagueTeamFixture leagueTeamFixture = GenerateLeagueTeamFixture.generate();
        League saveLeague = leagueRepository.save(leagueTeamFixture.league);
        Team saveHome = teamRepository.save(leagueTeamFixture.home);
        Team saveAway = teamRepository.save(leagueTeamFixture.away);
        Fixture saveFixture = fixtureRepository.save(leagueTeamFixture.fixture);

        // when
        FavoriteFixture favoriteFixture = favoriteService.addFavoriteFixture(saveFixture);

        // then
        assertThat(favoriteFixture).isNotNull();
        assertThat(favoriteFixture.getFixtureId()).isEqualTo(saveFixture.getFixtureId());
        assertThat(favoriteFixture.getDate()).isEqualTo(saveFixture.getDate());
        assertThat(favoriteFixture.getShortStatus()).isEqualTo(saveFixture.getStatus().getShortStatus());
        assertThat(favoriteFixture.getLeagueId()).isEqualTo(saveFixture.getLeague().getLeagueId());
        assertThat(favoriteFixture.getLeagueName()).isEqualTo(saveFixture.getLeague().getName());
        assertThat(favoriteFixture.getLeagueKoreanName()).isEqualTo(saveFixture.getLeague().getKoreanName());
        assertThat(favoriteFixture.getHomeTeamId()).isEqualTo(saveFixture.getHomeTeam().getId());
        assertThat(favoriteFixture.getHomeTeamName()).isEqualTo(saveFixture.getHomeTeam().getName());
        assertThat(favoriteFixture.getHomeTeamKoreanName()).isEqualTo(saveFixture.getHomeTeam().getKoreanName());
        assertThat(favoriteFixture.getAwayTeamId()).isEqualTo(saveFixture.getAwayTeam().getId());
        assertThat(favoriteFixture.getAwayTeamName()).isEqualTo(saveFixture.getAwayTeam().getName());
        assertThat(favoriteFixture.getAwayTeamKoreanName()).isEqualTo(saveFixture.getAwayTeam().getKoreanName());
    }

    @Transactional
    @DisplayName("즐겨찾는 리그를 조회합니다")
    @Test
    void findFavoriteLeagues() {
        // given
        final Long leagueId1 = 39L;
        final String name1 = "Premier League";
        final String koreanName1 = "프리미어리그";
        final String logo1 = "https://media.api-sports.io/football/leagues/39.png";
        final Integer currentSeason1 = 2023;

        final Long leagueId2 = 4L;
        final String name2 = "Euro Championship";
        final String koreanName2 = "유로피언 챔피언십";
        final String logo2 =  "https://media.api-sports.io/football/leagues/4.png";
        final Integer currentSeason2 = 2024;

        League league1 = League.builder()
                .leagueId(leagueId1)
                .name(name1)
                .koreanName(koreanName1)
                .logo(logo1)
                .currentSeason(currentSeason1)
                .build();
        League league2 = League.builder()
                .leagueId(leagueId2)
                .name(name2)
                .koreanName(koreanName2)
                .logo(logo2)
                .currentSeason(currentSeason2)
                .build();

        League save1 = leagueRepository.save(league1);
        League save2 = leagueRepository.save(league2);
        FavoriteLeague favoriteLeague1 = favoriteService.addFavoriteLeague(save1);
        FavoriteLeague favoriteLeague2 = favoriteService.addFavoriteLeague(save2);

        // when
        List<FavoriteLeague> OneFavoriteLeague = favoriteService.getFavoriteLeagues(1);
        List<FavoriteLeague> TwoFavoriteLeagues = favoriteService.getFavoriteLeagues(2);
        List<FavoriteLeague> ThreeButExistTwoFavorite = favoriteService.getFavoriteLeagues(3);

        log.info("singleFavoriteLeague :: {}", OneFavoriteLeague);
        log.info("TwoFavoriteLeagues :: {}", TwoFavoriteLeagues);
        log.info("ThreeButExistTwoFavorite :: {}", ThreeButExistTwoFavorite);

        // then
        assertThat(OneFavoriteLeague).isNotNull();
        assertThat(TwoFavoriteLeagues).isNotNull();
        assertThat(ThreeButExistTwoFavorite).isNotNull();
        assertThat(OneFavoriteLeague).hasSize(1);
        assertThat(TwoFavoriteLeagues).hasSize(2);
        assertThat(ThreeButExistTwoFavorite).hasSize(2);
    }

    @Transactional
    @DisplayName("각각 서로 다른 리그 id 로 즐겨찾는 경기일정을 조회합니다.")
    @Test
    void findFavoriteFixtures() {
        // given
        List<LeagueTeamFixture> list = GenerateLeagueTeamFixture.generateTwoOtherLeague();

        LeagueTeamFixture leagueTeamFixture1 = list.get(0);
        LeagueTeamFixture leagueTeamFixture2 = list.get(1);

        League saveLeague1 = leagueRepository.save(leagueTeamFixture1.league);
        League saveLeague2 = leagueRepository.save(leagueTeamFixture2.league);
        Team saveHome1 = teamRepository.save(leagueTeamFixture1.home);
        Team saveAway1 = teamRepository.save(leagueTeamFixture1.away);
        Team saveHome2 = teamRepository.save(leagueTeamFixture2.home);
        Team saveAway2 = teamRepository.save(leagueTeamFixture2.away);
        Fixture saveFixture1 = fixtureRepository.save(leagueTeamFixture1.fixture);
        Fixture saveFixture2 = fixtureRepository.save(leagueTeamFixture2.fixture);

        FavoriteFixture favoriteFixture1 = favoriteService.addFavoriteFixture(saveFixture1);
        FavoriteFixture favoriteFixture2 = favoriteService.addFavoriteFixture(saveFixture2);

        // when
        List<FavoriteFixture> League1FavoriteFixture = favoriteService.getFavoriteFixtures(saveLeague1.getLeagueId(), 1);
        List<FavoriteFixture> League2FavoriteFixture = favoriteService.getFavoriteFixtures(saveLeague2.getLeagueId(), 2);

        // then
        assertThat(League1FavoriteFixture).isNotEmpty();
        assertThat(League2FavoriteFixture).isNotEmpty();
        assertThat(League1FavoriteFixture).hasSize(1);
        assertThat(League2FavoriteFixture).hasSize(1);
    }

    @Transactional
    @DisplayName("즐겨찾는 리그를 삭제합니다")
    @Test
    void RemoveFavoriteLeague() {
        // given
        final Long leagueId = 39L;
        final String name = "Premier League";
        final String koreanName = "프리미어리그";
        final String logo = "https://media.api-sports.io/football/leagues/39.png";
        final Integer currentSeason = 2023;

        League league = League.builder()
                .leagueId(leagueId)
                .name(name)
                .koreanName(koreanName)
                .logo(logo)
                .currentSeason(currentSeason)
                .build();
        League save = leagueRepository.save(league);
        FavoriteLeague addFavoriteLeague = favoriteService.addFavoriteLeague(save);

        em.flush();
        em.clear();

        // when
        FavoriteLeague findFavoriteLeague = favoriteService.findFavoriteLeague(leagueId);
        boolean removeFavoriteLeague = favoriteService.removeFavoriteLeague(leagueId);

        // then
        assertThat(addFavoriteLeague).isNotNull();
        assertThat(findFavoriteLeague).isNotNull();
        assertThat(removeFavoriteLeague).isTrue();
        assertThatThrownBy(() -> favoriteService.findFavoriteLeague(leagueId))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Transactional
    @DisplayName("즐겨찾는 경기일정을 삭제합니다")
    @Test
    void RemoveFavoriteMatch() {
        // given
        LeagueTeamFixture leagueTeamFixture = GenerateLeagueTeamFixture.generate();
        League saveLeague = leagueRepository.save(leagueTeamFixture.league);
        Team saveHome = teamRepository.save(leagueTeamFixture.home);
        Team saveAway = teamRepository.save(leagueTeamFixture.away);
        Fixture saveFixture = fixtureRepository.save(leagueTeamFixture.fixture);
        FavoriteFixture addFavoriteFixture = favoriteService.addFavoriteFixture(saveFixture);
        Long fixtureId = addFavoriteFixture.getFixtureId();
        log.info("fixtureId :: {}", fixtureId);
        em.flush();
        em.clear();

        // when
        log.info("before count :: {}", favoriteFixtureRepository.count());
        boolean removeFavoriteFixture = favoriteService.removeFavoriteFixture(fixtureId);
        long count = favoriteFixtureRepository.count();
        log.info("count :: {}", count);

        // then
        assertThat(removeFavoriteFixture).isTrue();
        assertThat(favoriteFixtureRepository.count()).isZero();
    }
}
