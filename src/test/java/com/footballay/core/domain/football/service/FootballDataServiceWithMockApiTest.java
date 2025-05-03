package com.footballay.core.domain.football.service;

import com.footballay.core.domain.football.constant.FixtureId;
import com.footballay.core.domain.football.constant.LeagueId;
import com.footballay.core.domain.football.constant.TeamId;
import com.footballay.core.domain.football.external.FootballApiCacheService;
import com.footballay.core.domain.football.persistence.Fixture;
import com.footballay.core.domain.football.scheduler.lineup.PreviousMatchTask;
import com.footballay.core.domain.football.scheduler.live.LiveMatchProcessor;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"dev", "mockapi"})
@SpringBootTest
public class FootballDataServiceWithMockApiTest {

    @Autowired
    private FootballDataService footballDataService;

    @Autowired
    private FootballApiCacheService footballApiCacheService;
    @Autowired
    private PreviousMatchTask previousMatchTask;
    @Autowired
    private LiveMatchProcessor liveMatchProcessor;

    @Autowired
    private EntityManager em;

    @Transactional
    @DisplayName("Fixture Info 요청에 대해 Eager Fetch Join 메서드로 조회합니다")
    @Test
    void FixtureInfoFetchJoinTest() {
        // given
        footballApiCacheService.cacheLeague(LeagueId.EURO);
        footballApiCacheService.cacheTeamsOfLeague(LeagueId.EURO);
        footballApiCacheService.cacheTeamSquad(TeamId.SPAIN);
        footballApiCacheService.cacheTeamSquad(TeamId.CROATIA);
        footballApiCacheService.cacheFixturesOfLeague(LeagueId.EURO);
        previousMatchTask.requestAndSaveLineup(FixtureId.FIXTURE_EURO2024_SPAIN_CROATIA);
        liveMatchProcessor.requestAndSaveLiveMatchData(FixtureId.FIXTURE_EURO2024_SPAIN_CROATIA);

        em.flush();
        em.clear();

        // when
        Fixture fixtureWithEager = footballDataService.getFixtureWithEager(FixtureId.FIXTURE_EURO2024_SPAIN_CROATIA);
        em.detach(fixtureWithEager);

        // then
        assertThat(fixtureWithEager.getLeague()).isNotNull();
        assertThat(fixtureWithEager.getHomeTeam()).isNotNull();
        assertThat(fixtureWithEager.getAwayTeam()).isNotNull();
        assertThat(fixtureWithEager.getLiveStatus()).isNotNull();
        // OneToMany 관계들은 Eager Load 되지 않고, 별도 메서드를 이용해 채워넣어야 합니다.
        // 이는 OneToMany 를 Fetch Join 한다면 너무 많은 데이터를 가져오기 때문입니다.
    }

}
