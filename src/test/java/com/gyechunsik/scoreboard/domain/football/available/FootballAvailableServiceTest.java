package com.gyechunsik.scoreboard.domain.football.available;

import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.service.FootballAvailableRefacService;
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
    private FootballAvailableRefacService footballAvailableRefacService;

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

        em.flush();
        em.clear();

        // when
        footballAvailableRefacService.updateAvailableLeague(leagueId, true);
        List<League> availableLeague = footballAvailableRefacService.getAvailableLeagues();

        // then
        assertThat(availableLeague).isNotNull();
        assertThat(availableLeague).hasSize(1);
        League findLeague = availableLeague.get(0);
        assertThat(findLeague.getLeagueId()).isEqualTo(leagueId);
        assertThat(findLeague.getName()).isEqualTo(name);
        assertThat(findLeague.getKoreanName()).isEqualTo(koreanName);
        assertThat(findLeague.getLogo()).isEqualTo(logo);
        assertThat(findLeague.getCurrentSeason()).isEqualTo(currentSeason);
    }

    @Transactional
    @DisplayName("여러 개의 즐겨찾는 리그를 조회합니다")
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
        footballAvailableRefacService.updateAvailableLeague(leagueId1, true);
        footballAvailableRefacService.updateAvailableLeague(leagueId2, true);

        em.flush();
        em.clear();

        // when
        List<League> availableLeagues = footballAvailableRefacService.getAvailableLeagues();
        log.info("availableLeagues :: {}", availableLeagues);

        // then
        assertThat(availableLeagues).isNotNull();
        assertThat(availableLeagues).hasSize(2);
        availableLeagues.forEach(league -> {
            if (league.getLeagueId().equals(leagueId1)) {
                assertThat(league.getName()).isEqualTo(name1);
                assertThat(league.getKoreanName()).isEqualTo(koreanName1);
                assertThat(league.getLogo()).isEqualTo(logo1);
                assertThat(league.getCurrentSeason()).isEqualTo(currentSeason1);
            } else if (league.getLeagueId().equals(leagueId2)) {
                assertThat(league.getName()).isEqualTo(name2);
                assertThat(league.getKoreanName()).isEqualTo(koreanName2);
                assertThat(league.getLogo()).isEqualTo(logo2);
                assertThat(league.getCurrentSeason()).isEqualTo(currentSeason2);
            }
        });
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
        footballAvailableRefacService.updateAvailableLeague(leagueId, true);

        em.flush();
        em.clear();

        // when
        footballAvailableRefacService.updateAvailableLeague(leagueId, false);
        List<League> availableLeagues = footballAvailableRefacService.getAvailableLeagues();

        // then
        assertThat(availableLeagues).isNotNull();
        assertThat(availableLeagues).hasSize(0);
    }

}
