package com.gyechunsik.scoreboard.domain.football;

import com.gyechunsik.scoreboard.domain.football.dto.*;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.League;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.*;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.service.FootballAvailableService;
import com.gyechunsik.scoreboard.domain.football.service.FootballDataService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Football 과 관련된 DomainRoot 에 해당합니다. <br>
 * Domain Root Class Level 에서는 @Transactional 이 시작되지 않도록 합니다. <br>
 * 여러 Service 간의 CRUD 가 이뤄질 때 의도적으로 Domain Root 계층에서 Transaction 이 종료되도록 하기 위함입니다. <br>
 * 예를 들어 팀-선수 간 연관관계 맵핑 엔티티들을 추가한 후 읽을 때, 쓰기와 읽기간 Transaction 이 분리되도록 합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FootballRoot {

    private final FootballApiCacheService footballApiCacheService;
    private final FootballDataService footballDataService;
    private final FootballAvailableService footballAvailableService;

    private final LeagueRepository leagueRepository;

    public List<LeagueDto> getLeagues() {
        try {
            List<League> leagueEntities = footballDataService.getLeagues(10);
            return FootballDomainDtoMapper.leagueDtosFromEntities(leagueEntities);
        } catch (Exception e) {
            log.error("error while getting Leagues :: {}", e.getMessage());
            return List.of();
        }
    }

    public List<LeagueDto> getAvailableLeagues() {
        List<League> leagueEntities = footballAvailableService.getAvailableLeagues();
        return FootballDomainDtoMapper.leagueDtosFromEntities(leagueEntities);
    }

    public LeagueDto addAvailableLeague(long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리그입니다."));

        footballAvailableService.updateAvailableLeague(leagueId, true);

        return FootballDomainDtoMapper.leagueDtoFromEntity(league);
    }

    public List<TeamDto> getTeamsOfLeague(long leagueId) {
        try {
            List<Team> teamEntities = footballDataService.getTeamsByLeagueId(leagueId);
            return FootballDomainDtoMapper.teamDtosFromEntities(teamEntities);
        } catch (Exception e) {
            log.error("error while getting _Teams by LeagueId :: {}", e.getMessage());
            return List.of();
        }
    }

    public List<PlayerDto> getSquadOfTeam(long teamId) {
        try {
            List<Player> squadEntities = footballDataService.getSquadOfTeam(teamId);
            return FootballDomainDtoMapper.playerDtosFromEntities(squadEntities);
        } catch (Exception e) {
            log.error("error while getting Squad by TeamId :: {}", e.getMessage());
            return List.of();
        }
    }

    public PlayerDto getPlayer(long playerId) {
        try {
            Player player = footballDataService.findPlayerById(playerId).orElseThrow();
            return FootballDomainDtoMapper.playerDtoFromEntity(player);
        } catch (Exception e) {
            log.error("error while getting Player by Id :: {}", e.getMessage());
            return null;
        }
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

    public FixtureInfoDto addAvailableFixture(long fixtureId) {
        Fixture fixture = footballDataService.getFixtureById(fixtureId);
        try {
            footballAvailableService.addAvailableFixture(fixtureId);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        log.info("Add Available fixture :: {}", fixture);
        fixture.getLiveStatus();
        return FootballDomainDtoMapper.fixtureInfoDtoFromEntity(fixture);
    }

    public Optional<FixtureInfoDto> getFixtureInfo(long fixtureId) {
        try {
            Fixture fixture = footballDataService.getFixtureById(fixtureId);
            return Optional.of(FootballDomainDtoMapper.fixtureInfoDtoFromEntity(fixture));
        } catch (Exception e) {
            log.error("error while getting _Fixture by Id :: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // TODO : getFixturesOnNearestDate() 와 중복되므로 이 메서드를 삭제해야함
    /**
     * 주어진 날짜를 기준으로 가장 가까운 날짜의 fixture 들을 모두 가져옵니다.
     * 주어진 날짜는 항상 00:00:00 으로 재설정 됩니다.
     * @return 주어진 날짜로 부터 가장 가까운 fixture 들
     */
    public List<FixtureInfoDto> getNextFixturesFromDate(long leagueId, ZonedDateTime zonedDateTime) {
        try {
            List<Fixture> fixtures = footballDataService.findFixturesOnNearestDate(leagueId, zonedDateTime);
            log.info("getNextFixturesFromDate :: {}", fixtures);
            return FootballDomainDtoMapper.fixtureInfoDtosFromEntities(fixtures);
        } catch (Exception e) {
            log.error("error while getting _Fixtures by LeagueId :: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 주어진 날짜를 기준으로 가장 가까운 fixture 를 찾아 해당 날짜의 fixture 들을 모두 가져옵니다. <br>
     * 주어진 날짜는 항상 00:00:00 으로 재설정 됩니다.
     * @param leagueId 리그 ID
     * @param zonedDateTime 탐색 시작 날짜
     * @return 주어진 날짜로 부터 가장 가까운 fixture 를 찾아서 해당 날짜의 fixture 들
     */
    public List<FixtureInfoDto> getFixturesOnNearestDate(long leagueId, ZonedDateTime zonedDateTime) {
        ZonedDateTime truncated = zonedDateTime.truncatedTo(ChronoUnit.DAYS);
        List<Fixture> fixturesOnNearestDate = footballDataService.findFixturesOnNearestDate(leagueId, truncated);
        return FootballDomainDtoMapper.fixtureInfoDtosFromEntities(fixturesOnNearestDate);
    }

    public List<FixtureInfoDto> getAvailableFixturesOnNearestDate(long leagueId, ZonedDateTime zonedDateTime) {
        ZonedDateTime truncated = zonedDateTime.truncatedTo(ChronoUnit.DAYS);
        List<Fixture> availableFixtures = footballAvailableService.findAvailableFixturesOnNearestDate(leagueId, truncated);
        return FootballDomainDtoMapper.fixtureInfoDtosFromEntities(availableFixtures);
    }

    /**
     * 주어진 날짜를 기준으로 해당 날짜의 fixture 들을 모두 가져옵니다.
     * @param leagueId 리그 ID
     * @param zonedDateTime 날짜
     * @return 해당 날짜의 fixture 들
     */
    public List<FixtureInfoDto> getFixturesOnDate(long leagueId, ZonedDateTime zonedDateTime) {
        ZonedDateTime truncated = zonedDateTime.truncatedTo(ChronoUnit.DAYS);
        List<Fixture> fixturesOnDate = footballDataService.findFixturesOnDate(leagueId, truncated);
        return FootballDomainDtoMapper.fixtureInfoDtosFromEntities(fixturesOnDate);
    }

    public List<FixtureInfoDto> getAvailableFixturesOnDate(long leagueId, ZonedDateTime zonedDateTime) {
        ZonedDateTime truncated = zonedDateTime.truncatedTo(ChronoUnit.DAYS);
        List<Fixture> availableFixturesOnDate = footballAvailableService.findAvailableFixturesOnDate(leagueId, truncated);
        return FootballDomainDtoMapper.fixtureInfoDtosFromEntities(availableFixturesOnDate);
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
            log.info("cached Teams of League id={}. teams.size={}", leagueId, teams.size());
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

    public Optional<LiveStatusDto> getFixtureLiveStatus(long fixtureId) {
        try {
            LiveStatus liveStatus = footballDataService.getFixtureLiveStatus(fixtureId);
            log.info("getFixtureLiveStatus :: {}", liveStatus);
            return Optional.of(FootballDomainDtoMapper.liveStatusDtoFromEntity(liveStatus));
        } catch (Exception e) {
            log.warn("error while getting _FixtureLiveStatus by Id :: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Fixture 기본 연관관계 정보와 더불어 라인업 데이터까지 재공합니다. <br>
     * @see Fixture
     * @see MatchLineup
     * @see MatchPlayer
     * @see LiveStatus
     * @see Player
     * @see Team
     * @see League
     * @param fixtureId 조회할 fixtureId
     * @return Optional fixtureWithLineupDto
     */
    public Optional<FixtureWithLineupDto> getFixtureWithLineup(long fixtureId) {
        try {
            log.info("try fixture lineup loading id={}", fixtureId);
            Fixture findFixture = footballDataService.getFixtureById(fixtureId);
            Team home = findFixture.getHomeTeam();
            Team away = findFixture.getAwayTeam();

            List<MatchLineup> lineups = new ArrayList<>();
            Optional<MatchLineup> homeLineup = footballDataService.getStartLineup(findFixture, home);
            Optional<MatchLineup> awayLineup = footballDataService.getStartLineup(findFixture, away);
            if(homeLineup.isEmpty() || awayLineup.isEmpty()) {
                log.info("lineup is not exist id={}", fixtureId);
                return Optional.of(FootballDomainDtoMapper.fixtureWithEmptyLineupDtoFromEntity(findFixture));
            }

            homeLineup.ifPresent(lineups::add);
            awayLineup.ifPresent(lineups::add);
            findFixture.setLineups(lineups);
            log.info("fixture eager loaded :: {}", findFixture.getFixtureId());
            return Optional.of(FootballDomainDtoMapper.fixtureWithLineupDtoFromEntity(findFixture));
        } catch (Exception e) {
            log.warn("error while getting Fixture with Lineup by Id :: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public List<FixtureEventWithPlayerDto> getFixtureEvents(long fixtureId) {
        try {
            Fixture fixture = footballDataService.getFixtureById(fixtureId);
            List<FixtureEvent> fixtureEvents = footballDataService.getFixtureEvents(fixture);
            return FootballDomainDtoMapper.fixtureEventDtosFromEntities(fixtureEvents);
        } catch (Exception e) {
            log.error("error while getting _FixtureEvents by Id :: {}", e.getMessage());
            return List.of();
        }
    }

    public List<TeamDto> getTeamsOfPlayer(long playerId) {
        try {
            List<Team> teamPlayer = footballDataService.getTeamsOfPlayer(playerId);
            log.info("getPlayerTeamRelations :: {}", teamPlayer);
            return FootballDomainDtoMapper.teamDtosFromEntities(teamPlayer);
        } catch (Exception e) {
            log.error("error while getting _PlayerTeamRelations by Id :: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * fixture, team, player 들이 transaction 내에서 eager 로 로딩되어서 반환되어야 합니다.
     * @param fixtureId
     * @return
     */
    @Transactional(readOnly = true)
    public MatchStatisticsDto getMatchStatistics(long fixtureId) {
        log.info("getMatchStatistics :: fixtureId={}", fixtureId);
        try {
            Fixture fixture = footballDataService.getFixtureById(fixtureId);
            LiveStatus liveStatus = fixture.getLiveStatus();
            Team home = fixture.getHomeTeam();
            Team away = fixture.getAwayTeam();

            @Nullable TeamStatistics homeStatistics = footballDataService.getTeamStatistics(fixture.getFixtureId(), home.getId()).orElse(null);
            @Nullable TeamStatistics awayStatistics = footballDataService.getTeamStatistics(fixture.getFixtureId(), away.getId()).orElse(null);
            List<MatchPlayer> homePlayerStatistics = footballDataService.getPlayerStatistics(fixture.getFixtureId(), home.getId());
            List<MatchPlayer> awayPlayerStatistics = footballDataService.getPlayerStatistics(fixture.getFixtureId(), away.getId());

            MatchStatisticsDto dto = FootballDomainDtoMapper.matchStatisticsDTOFromEntity(
                    fixture, liveStatus, home, away, homeStatistics, awayStatistics, homePlayerStatistics, awayPlayerStatistics
            );
            log.debug("return getMatchStatistics :: {}", dto);
            return dto;
        } catch (Exception e) {
            log.error("error while getting _MatchStatistics by Id", e);
            return null;
        }
    }
}
