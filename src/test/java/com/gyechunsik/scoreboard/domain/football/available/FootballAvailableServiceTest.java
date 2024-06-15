package com.gyechunsik.scoreboard.domain.football.available;

import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.available.entity.AvailableLeague;
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
class FootballAvailableServiceTest {

    @Autowired
    private FootballAvailableService footballAvailableService;

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
        AvailableLeague availableLeague = footballAvailableService.addFavoriteLeague(save);

        // then
        assertThat(availableLeague).isNotNull();
        assertThat(availableLeague.getLeagueId()).isEqualTo(leagueId);
        assertThat(availableLeague.getName()).isEqualTo(name);
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
        AvailableLeague availableLeague1 = footballAvailableService.addFavoriteLeague(save1);
        AvailableLeague availableLeague2 = footballAvailableService.addFavoriteLeague(save2);

        // when
        List<AvailableLeague> oneAvailableLeague = footballAvailableService.getFavoriteLeagues(1);
        List<AvailableLeague> twoAvailableLeagues = footballAvailableService.getFavoriteLeagues(2);
        List<AvailableLeague> ThreeButExistTwoFavorite = footballAvailableService.getFavoriteLeagues(3);

        log.info("singleFavoriteLeague :: {}", oneAvailableLeague);
        log.info("twoAvailableLeagues :: {}", twoAvailableLeagues);
        log.info("ThreeButExistTwoFavorite :: {}", ThreeButExistTwoFavorite);

        // then
        assertThat(oneAvailableLeague).isNotNull();
        assertThat(twoAvailableLeagues).isNotNull();
        assertThat(ThreeButExistTwoFavorite).isNotNull();
        assertThat(oneAvailableLeague).hasSize(1);
        assertThat(twoAvailableLeagues).hasSize(2);
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
        AvailableLeague addAvailableLeague = footballAvailableService.addFavoriteLeague(save);

        em.flush();
        em.clear();

        // when
        AvailableLeague findAvailableLeague = footballAvailableService.findFavoriteLeague(leagueId);
        boolean removeFavoriteLeague = footballAvailableService.removeFavoriteLeague(leagueId);

        // then
        assertThat(addAvailableLeague).isNotNull();
        assertThat(findAvailableLeague).isNotNull();
        assertThat(removeFavoriteLeague).isTrue();
        assertThatThrownBy(() -> footballAvailableService.findFavoriteLeague(leagueId))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
