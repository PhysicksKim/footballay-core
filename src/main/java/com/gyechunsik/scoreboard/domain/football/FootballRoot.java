package com.gyechunsik.scoreboard.domain.football;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Player;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.favorite.FavoriteService;
import com.gyechunsik.scoreboard.domain.football.favorite.entity.FavoriteFixture;
import com.gyechunsik.scoreboard.domain.football.favorite.entity.FavoriteLeague;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.service.FootballDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class FootballRoot {

    private final FavoriteService favoriteService;

    private final LeagueRepository leagueRepository;
    private final FixtureRepository fixtureRepository;

    private final FootballApiCacheService footballApiCacheService;
    private final FootballDataService footballDataService;

    public FavoriteLeague addFavoriteLeague(long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리그입니다."));

        FavoriteLeague favoriteLeague = favoriteService.addFavoriteLeague(league);
        log.info("addFavoriteLeague :: {}", favoriteLeague);
        return favoriteLeague;
    }

    public FavoriteFixture addFavoriteFixture(long fixtureId) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경기입니다."));

        FavoriteFixture favoriteFixture = favoriteService.addFavoriteFixture(fixture);
        log.info("addFavoriteFixture :: {}", favoriteFixture);
        return favoriteFixture;
    }

    /**
     * 즐겨찾기 리그를 삭제합니다.
     * @param leagueId
     * @return 존재하지 않는 경우 false 를 반환합니다.
     */
    public boolean removeFavoriteLeague(long leagueId) {
        return favoriteService.removeFavoriteLeague(leagueId);
    }

    /**
     * 즐겨찾기 경기를 삭제합니다.
     * @param fixtureId
     * @return 존재하지 않는 경우 false 를 반환합니다.
     */
    public boolean removeFavoriteFixture(long fixtureId) {
        return favoriteService.removeFavoriteFixture(fixtureId);
    }

    /*
    1) cache league by id
    2) cache all teams of league by league id
    3) cache squad of a team by team id
    4) cache all fixtures of current season of a league by league id
     */

    public boolean cacheLeagueById(long leagueId) {
        try {
            League cachedLeague = footballApiCacheService.cacheLeague(leagueId);
            log.info("cachedLeague :: {}", cachedLeague);
        } catch (Exception e) {
            log.error("error while caching League :: {}", e.getMessage());
            return false;
        }
        return true;
    }

    public boolean cacheTeamsOfLeague(long leagueId) {
        try {
            List<Team> teams = footballApiCacheService.cacheTeamsOfLeague(leagueId);
            log.info("cached Teams LeagueId :: {}", leagueId);
            log.info("cached Teams :: {}", teams.stream().map(Team::getName).toList());
        } catch (Exception e) {
            log.error("error while caching Teams of League :: {}", e.getMessage());
            return false;
        }
        return true;
    }

    public boolean cacheSquadOfTeam(long teamId) {
        try {
            List<Player> players = footballApiCacheService.cacheTeamSquad(teamId);
            log.info("cachedSquadOfTeam :: {}", teamId);
            log.info("cached players :: {}",
                    players.stream().map(Player::getName).toList());
        } catch (Exception e) {
            log.error("error while caching Squad of Team :: {}", e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 해당 League 의 current season 값을 바탕으로 모든 경기 일정을 캐싱합니다.
     * @param leagueId
     * @return
     */
    public boolean cacheAllFixturesOfLeague(long leagueId) {
        try {
            League league = footballDataService.getLeagueById(leagueId);
            List<Fixture> fixtures = footballApiCacheService.cacheFixturesOfLeagueSeason(leagueId, league.getCurrentSeason());
            log.info("cachedAllFixturesOfLeague :: {}", leagueId);
            log.info("cached fixtures :: {}",
                    fixtures.stream().map(Fixture::getFixtureId).toList());
        } catch (Exception e) {
            log.error("error while caching All Fixtures of League :: {}", e.getMessage());
            return false;
        }
        return true;
    }
}
