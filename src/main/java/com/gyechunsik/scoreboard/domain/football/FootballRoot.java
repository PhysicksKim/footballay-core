package com.gyechunsik.scoreboard.domain.football;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Player;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.entity.live.FixtureEvent;
import com.gyechunsik.scoreboard.domain.football.entity.live.LiveStatus;
import com.gyechunsik.scoreboard.domain.football.entity.live.StartLineup;
import com.gyechunsik.scoreboard.domain.football.entity.live.StartPlayer;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.relations.TeamPlayerRepository;
import com.gyechunsik.scoreboard.domain.football.service.FootballAvailableService;
import com.gyechunsik.scoreboard.domain.football.service.FootballDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Football 과 관련된 DomainRoot 에 해당합니다. <br>
 * 여기서는 @Transactional 이 시작되면 안됩니다. <br>
 * 예를 들어 새롭게 저장된 entity 에서 연관관계 데이터를 가져오면, flush 되지 않아서
 * JPA 트랜잭션이 종료되고 flush 된 후에 동작해야 합니다.
 *
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FootballRoot {

    private final FootballApiCacheService footballApiCacheService;
    private final FootballDataService footballDataService;
    private final FootballAvailableService footballAvailableService;
    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TeamPlayerRepository teamPlayerRepository;

    public List<League> getLeagues() {
        List<League> leagues;
        try {
            leagues = footballDataService.getLeagues(10);
        } catch (Exception e) {
            log.error("error while getting Leagues :: {}", e.getMessage());
            return List.of();
        }
        return leagues;
    }

    public List<Team> getTeamsOfLeague(long leagueId) {
        List<Team> teams;
        try {
            teams = footballDataService.getTeamsByLeagueId(leagueId);
        } catch (Exception e) {
            log.error("error while getting _Teams by LeagueId :: {}", e.getMessage());
            return List.of();
        }
        return teams;
    }

    public List<Player> getSquadOfTeam(long teamId) {
        List<Player> squad;
        try {
            squad = footballDataService.getSquadOfTeam(teamId);
        } catch (Exception e) {
            log.error("error while getting Squad by TeamId :: {}", e.getMessage());
            return List.of();
        }
        return squad;
    }

    public Player getPlayer(long playerId) {
        Player player;
        try {
            player = footballDataService.findPlayerById(playerId).orElseThrow();
        } catch (Exception e) {
            log.error("error while getting _Player by Id :: {}", e.getMessage());
            return null;
        }
        return player;
    }

    /**
     * 주어진 날짜를 기준으로 가장 가까운 날짜의 fixture 들을 모두 가져옵니다.
     * 주어진 날짜는 항상 00:00:00 으로 재설정 됩니다.
     * @return
     */
    public List<Fixture> getNextFixturesFromDate(long leagueId, ZonedDateTime zonedDateTime) {
        List<Fixture> fixtures;
        try {
            fixtures = footballDataService.findFixturesOnClosestDate(leagueId, zonedDateTime);
            log.info("getNextFixturesFromDate :: {}", fixtures);
        } catch (Exception e) {
            log.error("error while getting _Fixtures by LeagueId :: {}", e.getMessage());
            return List.of();
        }
        return fixtures;
    }

    public League addAvailableLeague(long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리그입니다."));

        footballAvailableService.updateAvailableLeague(leagueId, true);

        return league;
    }

    public List<League> getAvailableLeagues() {
        return footballAvailableService.getAvailableLeagues();
    }

    /**
     * 즐겨찾기 리그를 삭제합니다.
     * @param leagueId
     * @return 존재하지 않는 경우 false 를 반환합니다.
     */
    public boolean removeAvailableLeague(long leagueId) {
        try {
            footballAvailableService.updateAvailableLeague(leagueId, false);
        } catch (Exception e) {
            log.error("error while removing Available _League :: {}", e.getMessage());
            return false;
        }
        return true;
    }

    public Fixture addAvailableFixture(long fixtureId) {
        Fixture fixture = footballDataService.getFixtureById(fixtureId);
        try {
            footballAvailableService.addAvailableFixture(fixtureId);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        log.info("Add Available fixture :: {}", fixture);
        return fixture;
    }

    public List<Fixture> getFixturesOnClosestDate(long leagueId, ZonedDateTime zonedDateTime) {
        ZonedDateTime truncated = zonedDateTime.truncatedTo(ChronoUnit.DAYS);
        return footballDataService.findFixturesOnClosestDate(leagueId, truncated);
    }

    public List<Fixture> getAvailableFixturesOnClosestDate(long leagueId, ZonedDateTime zonedDateTime) {
        ZonedDateTime truncated = zonedDateTime.truncatedTo(ChronoUnit.DAYS);
        return footballAvailableService.findAvailableFixturesOnClosestDate(leagueId, truncated);
    }

    public List<Fixture> getFixturesOnDate(long leagueId, ZonedDateTime zonedDateTime) {
        ZonedDateTime truncated = zonedDateTime.truncatedTo(ChronoUnit.DAYS);
        return footballDataService.findFixturesOnDate(leagueId, truncated);
    }

    public List<Fixture> getAvailableFixturesOnDate(long leagueId, ZonedDateTime zonedDateTime) {
        ZonedDateTime truncated = zonedDateTime.truncatedTo(ChronoUnit.DAYS);
        return footballAvailableService.findAvailableFixturesOnDate(leagueId, truncated);
    }

    public boolean removeAvailableFixture(long fixtureId) {
        try {
            footballAvailableService.removeAvailableFixture(fixtureId);
        } catch (Exception e) {
            log.error("error while removing Available Fixture :: {}", e.getMessage());
            return false;
        }
        return true;
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
            log.info("cached _Teams LeagueId :: {}", leagueId);
            log.info("cached _Teams :: {}", teams.stream().map(Team::getName).toList());
        } catch (Exception e) {
            log.error("error while caching Teams of League :: {}", e.getMessage());
            return false;
        }
        return true;
    }

    public boolean cacheTeamAndCurrentLeagues(long teamId) {
        try {
            footballApiCacheService.cacheTeamAndCurrentLeagues(teamId);
            log.info("cachedTeamAndCurrentLeagues :: {}", teamId);
        } catch (Exception e) {
            log.error("error while caching Team and Current Leagues :: {}", e.getMessage());
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
            log.error("error while caching All Fixtures of League :: {}", e.getMessage(), e);
            return false;
        }
        return true;
    }

    // TODO : 웹레이어 구현필요 - 선수 단일 캐싱 추가
    /**
     * 해당 playerId 의 선수 정보를 캐싱합니다.
     * @param playerId
     * @param leagueId
     * @param season
     * @return 캐싱이 이루어 졌는지 여부. 만약 이미 존재하는 선수라면 캐싱 요청을 보내지 않고 false 를 반환합니다.
     */
    public boolean cachePlayerSingle(long playerId, long leagueId, int season) {
        Optional<Player> findPlayer = footballDataService.findPlayerById(playerId);
        if(findPlayer.isPresent()) {
            log.info("player already exists :: {}", findPlayer);
            return false;
        }
        return cacheNewPlayer(playerId, leagueId, season);
    }

    private boolean cacheNewPlayer(long playerId, long leagueId, int season) {
        try {
            Player player = footballApiCacheService.cachePlayerSingle(playerId, leagueId, season);
            log.info("cachedPlayerSingle :: {}", player);
            return true;
        } catch (Exception e) {
            log.error("error while caching new Player :: {}", e.getMessage());
            return false;
        }
    }

    // TODO : 웹레이어 구현필요 - 팀-선수 연관관계 추가
    /**
     * 팀-선수 연관관계를 추가합니다. <br>
     * 수동으로 relation 을 지정해 주고 해당 relation 이 보존되도록 하기 위해서 해당 player 의 preventUnlink 를 true 로 같이 지정해줍니다. <br>
     * @param teamId
     * @param playerId
     * @return
     */
    public boolean addTeamPlayerRelation(long teamId, long playerId) {
        try{
            Player player = footballDataService.addTeamPlayerRelationManually(teamId, playerId);
            log.info("addTeamPlayerRelation :: teamId={}, player={}", teamId, player);
        } catch (Exception e) {
            log.error("error while adding Team-Player relation :: {}", e.getMessage());
            return false;
        }
        return true;
    }

    // TODO : 웹레이어 구현필요 - 팀-선수 연관관계 삭제
    /**
     * 팀-선수 연관관계를 삭제합니다. <br>
     * 수동으로 relation 을 지정해 주고 해당 relation 이 보존되도록 하기 위해서 해당 player 의 preventUnlink 를 true 로 같이 지정해줍니다. <br>
     * @param teamId
     * @param playerId
     * @return 성공 여부
     */
    public boolean removeTeamPlayerRelation(long teamId, long playerId) {
        try {
            Player player = footballDataService.removeTeamPlayerRelationManually(teamId, playerId);
            log.info("removeTeamPlayerRelation :: teamId={}, player={}", teamId, player);
        } catch (Exception e) {
            log.error("error while removing Team-Player relation :: {}", e.getMessage());
            return false;
        }
        return true;
    }

    // TODO : 웹레이어 구현필요 - preventUnlink 설정
    /**
     * 해당 playerId 의 preventUnlink 값을 설정합니다. <br>
     * @see Player#getPreventUnlink()
     * @param playerId
     * @param preventUnlink
     * @return 성공 여부
     */
    public boolean setPlayerPreventUnlink(long playerId, boolean preventUnlink) {
        try {
            footballDataService.setPreventUnlink(playerId, preventUnlink);
        } catch (Exception e) {
            log.error("error while setting Player PreventUnlink :: {}", e.getMessage());
            return false;
        }
        return true;
    }

    public Optional<Fixture> getFixture(long fixtureId) {
        try {
            Fixture findFixture = footballDataService.getFixtureById(fixtureId);
            log.info("getFixture :: {}", findFixture);
            return Optional.of(findFixture);
        } catch (Exception e) {
            log.warn("error while getting _Fixture by Id :: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<LiveStatus> getFixtureLiveStatus(long fixtureId) {
        try {
            LiveStatus liveStatus = footballDataService.getFixtureLiveStatus(fixtureId);
            log.info("getFixtureLiveStatus :: {}", liveStatus);
            return Optional.of(liveStatus);
        } catch (Exception e) {
            log.warn("error while getting _FixtureLiveStatus by Id :: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Fixture Entity 를 연관관계들을 모두 채워서 재공합니다. <br>
     * 트랜잭션 범위 밖에서 Lazy Loading 에러가 발생하지 않도록, Fixture 관련 연관관계 엔티티 필드들을 채워서 제공해줍니다.
     * @see Fixture
     * @see StartLineup
     * @see StartPlayer
     * @see FixtureEvent
     * @see LiveStatus
     * @see Player
     * @see Team
     * @see League
     * @param fixtureId 조회할 fixtureId
     * @return 연관관계들이 모두 채워져 있는 fixture entity
     */
    public Optional<Fixture> getFixtureWithEager(long fixtureId) {
        try {
            log.info("try fixture eager loading :: {}", fixtureId);
            Fixture findFixture = footballDataService.getFixtureWithEager(fixtureId);
            Team home = findFixture.getHomeTeam();
            Team away = findFixture.getAwayTeam();

            List<StartLineup> lineups = new ArrayList<>();
            Optional<StartLineup> homeLineup = footballDataService.getStartLineup(findFixture, home);
            Optional<StartLineup> awayLineup = footballDataService.getStartLineup(findFixture, away);
            homeLineup.ifPresent(lineups::add);
            awayLineup.ifPresent(lineups::add);
            findFixture.setLineups(lineups);

            List<FixtureEvent> events = footballDataService.getFixtureEvents(findFixture);
            findFixture.setEvents(events);
            log.info("fixture eager loaded :: {}", findFixture.getFixtureId());
            return Optional.of(findFixture);
        } catch (Exception e) {
            log.warn("error while getting _Fixture with Eager by Id :: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public List<FixtureEvent> getFixtureEvents(long fixtureId) {
        try {
            Fixture fixture = footballDataService.getFixtureById(fixtureId);
            return footballDataService.getFixtureEvents(fixture);
        } catch (Exception e) {
            log.error("error while getting _FixtureEvents by Id :: {}", e.getMessage());
            return List.of();
        }
    }
}
