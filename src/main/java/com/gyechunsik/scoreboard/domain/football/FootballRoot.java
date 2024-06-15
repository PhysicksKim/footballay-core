package com.gyechunsik.scoreboard.domain.football;

import com.gyechunsik.scoreboard.domain.football.available.entity.AvailableLeague;
import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Player;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.available.FootballAvailableService;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.service.FootballDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class FootballRoot {

    private final FootballAvailableService footballAvailableService;

    private final LeagueRepository leagueRepository;

    private final FootballApiCacheService footballApiCacheService;
    private final FootballDataService footballDataService;

    public List<League> getLeagues() {
        List<League> leagues;
        try {
            leagues = footballDataService.getLeagues(10);
            log.info("getLeagues :: {}", leagues);
        } catch (Exception e) {
            log.error("error while getting Leagues :: {}", e.getMessage());
            return List.of();
        }
        return leagues;
    }

    public List<Team> getTeamsByLeagueId(long leagueId) {
        List<Team> teams;
        try {
            teams = footballDataService.getTeamsByLeagueId(leagueId);
            log.info("getTeamsByLeagueId :: {}", teams);
        } catch (Exception e) {
            log.error("error while getting Teams by LeagueId :: {}", e.getMessage());
            return List.of();
        }
        return teams;
    }

    public List<Player> getSquadByTeamId(long teamId) {
        List<Player> squad;
        try {
            squad = footballDataService.getSquadOfTeam(teamId);
            log.info("squad players :: {}", squad.stream().map(Player::getName).toList());
        } catch (Exception e) {
            log.error("error while getting Squad by TeamId :: {}", e.getMessage());
            return List.of();
        }
        return squad;
    }

    /**
     * 해당 리그의 가장 가까운 날짜의 fixture 들을 모두 가져옵니다.
     * @param leagueId
     * @return
     */
    public List<Fixture> getNextFixturesFromToday(long leagueId) {
        List<Fixture> fixtures;
        try {
            fixtures = footballDataService.getFixturesOfLeague(leagueId);
            log.info("getFixturesByLeagueId :: {}", fixtures);
        } catch (Exception e) {
            log.error("error while getting Fixtures by LeagueId :: {}", e.getMessage());
            return List.of();
        }
        return fixtures;
    }

    /**
     * 주어진 날짜를 기준으로 가장 가까운 날짜의 fixture 들을 모두 가져옵니다.
     * 주어진 날짜는 항상 00:00:00 으로 재설정 됩니다.
     * @return
     */
    public List<Fixture> getNextFixturesFromDate(long leagueId, ZonedDateTime zonedDateTime) {
        List<Fixture> fixtures;
        try {
            fixtures = footballDataService.getFixturesOfLeagueAfterDate(leagueId, zonedDateTime);
            log.info("getNextFixturesFromDate :: {}", fixtures);
        } catch (Exception e) {
            log.error("error while getting Fixtures by LeagueId :: {}", e.getMessage());
            return List.of();
        }
        return fixtures;
    }

    public AvailableLeague addFavoriteLeague(long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리그입니다."));

        AvailableLeague availableLeague = footballAvailableService.addFavoriteLeague(league);
        log.info("addFavoriteLeague :: {}", availableLeague);
        return availableLeague;
    }

    public List<AvailableLeague> getFavoriteLeagues() {
        return footballAvailableService.getFavoriteLeagues();
    }

    public List<AvailableLeague> getFavoriteLeagues(int count) {
        return footballAvailableService.getFavoriteLeagues(count);
    }

    /**
     * 즐겨찾기 리그를 삭제합니다.
     * @param leagueId
     * @return 존재하지 않는 경우 false 를 반환합니다.
     */
    public boolean removeFavoriteLeague(long leagueId) {
        return footballAvailableService.removeFavoriteLeague(leagueId);
    }

    public boolean cacheAllCurrentLeagues() {
        try {
            footballApiCacheService.cacheAllCurrentLeagues();
            log.info("cachedAllCurrentLeagues");
        } catch (Exception e) {
            log.error("error while caching All Current Leagues :: {}", e.getMessage());
            return false;
        }
        return true;
    }

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
            List<Fixture> fixtures = footballApiCacheService.cacheFixturesOfLeague(leagueId);
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
