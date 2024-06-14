package com.gyechunsik.scoreboard.domain.football.favorite;

import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.favorite.entity.FavoriteLeague;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
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

}
