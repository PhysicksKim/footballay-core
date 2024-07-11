package com.gyechunsik.scoreboard.domain.football.external;

import com.gyechunsik.scoreboard.domain.football.entity.apicache.ApiCacheType;
import com.gyechunsik.scoreboard.domain.football.entity.live.LiveStatus;
import com.gyechunsik.scoreboard.domain.football.entity.relations.TeamPlayer;
import com.gyechunsik.scoreboard.domain.football.external.lastlog.LastCacheLogService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.*;
import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.entity.Player;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.entity.relations.LeagueTeam;
import com.gyechunsik.scoreboard.domain.football.repository.live.LiveStatusRepository;
import com.gyechunsik.scoreboard.domain.football.repository.relations.LeagueTeamRepository;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.relations.TeamPlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.gyechunsik.scoreboard.domain.football.external.fetch.response.LeagueInfoResponse.*;
import static com.gyechunsik.scoreboard.domain.football.external.fetch.response.PlayerSquadResponse.*;
import static com.gyechunsik.scoreboard.domain.football.external.fetch.response.TeamInfoResponse.*;

/**
 * <h3>개발 주의 사항</h3>
 * <pre>
 * 하나의 메서드는 1개의 External Api Call 을 만들어야합니다.
 * 하나의 메서드가 여러 Api Call 을 발생시키면 N+1 문제를 발생시킬 수 있습니다.
 * 따라서 메서드 하나에는 apiCallService 사용이 딱 한 번만 있어야 합니다.
 * 메서드 안에 여러 줄에서 apiCallService 가 호출된다면, 분기로 나뉘어서 독립적으로 호출되는 경우입니다.
 * 만약 하나의 메서드 안에 여러 apiCallService 가 등장한다면, 다시 별개의 private 메서드로 나누기를 권장합니다.
 * </pre>
 */
/*
 # 캐싱시 3가지 케이스를 나눠서 처리해야 합니다
 새로 캐싱, 이미 캐싱, 연관관계 끊어짐 처리 3가지 케이스가 있습니다.
 예를 들어 _Team 이 UEFA league 에 속했다가 다음 시즌에 UEFA 에 속하지 않는 경우 연관관계를 끊어줘야 한다.
 Cache _Teams Of _League 메서드에서는 리그에 속한 팀들을 캐싱한다. 이 때 3가지 경우로 나뉠 수 있다.
 1. api 있고 db 있음 : 해당 team 에 대한 leagueTeam 생성 스킵
 2. api 있고 db 없음 : 새롭게 leagueTeam 생성하여 db에 값을 넣음
 3. api 없고 db 있음 : 해당 leagueTeam 삭제
 따라서 _League 또는 _Team 을 캐싱하는 경우,
 LeagueTeam 에서 leagueId 일치하는 모든 항목을, TeamPlayer 에서 teamId 일치하는 모든 항목을 찾아서
 3가지 경우를 나눠서 처리해 줘야 한다.
*/
@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class FootballApiCacheService {

    private final ApiCallService apiCallService;
    private final LastCacheLogService lastCacheLogService;

    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final LeagueTeamRepository leagueTeamRepository;
    private final FixtureRepository fixtureRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final LiveStatusRepository liveStatusRepository;

    /**
     * 리그 아이디로 리그 정보를 캐싱합니다. 직접 리그 아이디를 외부 API 에서 찾아와야 합니다.
     *
     * @param leagueId
     * @return
     */
    public League cacheLeague(long leagueId) {
        LeagueInfoResponse leagueInfoResponse = apiCallService.leagueInfo(leagueId);
        _Response response = leagueInfoResponse.getResponse().get(0);

        League league = null;
        league = saveSingleLeague(leagueId, response);

        lastCacheLogService.saveApiCache(ApiCacheType.LEAGUE, Map.of("leagueId", leagueId), ZonedDateTime.now());
        log.info("leagueId: {} is cached", league.getLeagueId());
        log.info("cached league : {}", league);
        return league;
    }

    /**
     * 리그에 속한 팀들을 캐싱합니다.
     * 팀들을 캐싱 가능한 리그의 조건은 이미 캐싱되어 있고 해당 리그의 현재 시즌 값이 설정되어 있어야 합니다.
     * 현재 시즌 값은 this.cacheLeague(leagueId) 에 의해서 자동으로 캐싱되지만, 데이터 무결성을 위해서 검사를 거칩니다.
     * @param leagueId
     */
    public List<Team> cacheTeamsOfLeague(long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new RuntimeException("아직 캐싱되지 않은 league 입니다"));
        if (league.getCurrentSeason() == null) {
            throw new RuntimeException("아직 current _Season 이 캐싱되어있지 않습니다");
        }

        TeamInfoResponse teamInfoResponse = apiCallService.teamsInfo(leagueId, league.getCurrentSeason());

        Map<Long, _TeamResponse> apiTeamsSet = teamInfoResponse.getResponse().stream()
                .collect(Collectors.toMap(teamInfo -> teamInfo.getTeam().getId(), _TeamInfo::getTeam));
        List<Team> findTeams = teamRepository.findTeamsByLeague(league);

        List<Team> bothExistTeams = new ArrayList<>(); // CASE 1. db 존재 api 없음. 일부 필드 업데이트
        List<_TeamResponse> bothExistTeamRespons = new ArrayList<>(); // CASE 1. db 존재 api 없음. 일부 필드 업데이트
        List<Team> dbOnlyExistTeams = new ArrayList<>(); // CASE 2. db 존재 api 없음. leagueTeam 연관관계 끊어줌
        List<_TeamResponse> apiOnlyExistTeamRespons = new ArrayList<>(); // CASE 3. db 없음 api 존재. 새로운 팀 추가

        // CASE 1 & CASE 2
        for(Team team : findTeams) {
            if (apiTeamsSet.containsKey(team.getId())) {
                // CASE 1 : db 존재 api 존재
                bothExistTeams.add(team);
                bothExistTeamRespons.add(apiTeamsSet.get(team.getId()));
                apiTeamsSet.remove(team.getId());
            } else {
                // CASE 2 : db 존재 api 없음
                dbOnlyExistTeams.add(team);
            }
        }
        // CASE 3
        apiOnlyExistTeamRespons.addAll(apiTeamsSet.values());

        // CASE 1 : 일부 필드 업데이트
        for (int i = 0; i < bothExistTeams.size(); i++) {
            Team team = bothExistTeams.get(i);
            _TeamResponse teamResponse = bothExistTeamRespons.get(i);
            team.updateCompare(toTeamEntity(teamResponse));
            teamRepository.save(team);
        }

        // CASE 2 : leagueTeam 연관관계 끊어줌
        for (Team team : dbOnlyExistTeams) {
            leagueTeamRepository.deleteByLeagueAndTeam(league, team);
        }

        // CASE 3 : 새로운 팀 추가
        List<Team> newTeams = apiOnlyExistTeamRespons.stream()
                .map(FootballApiCacheService::toTeamEntity)
                .toList();
        List<Team> savedNewTeams = teamRepository.saveAll(newTeams);
        // 새로운 팀에 대해 리그팀 연관관계 추가
        for (Team newTeam : savedNewTeams) {
            LeagueTeam leagueTeam = LeagueTeam.builder()
                    .league(league)
                    .team(newTeam)
                    .build();
            leagueTeamRepository.save(leagueTeam);
        }

        log.info("CASE 1 ; Both exist teams : {}", bothExistTeams.stream().map(Team::getName).toList());
        log.info("CASE 2 ; DB only exist teams : {}", dbOnlyExistTeams.stream().map(Team::getName).toList());
        log.info("CASE 3 ; API only exist teams : {}", savedNewTeams.stream().map(Team::getName).toList());

        log.info("_Teams of [leagueId={},name={}] is cached", league.getLeagueId(), league.getName());

        lastCacheLogService.saveApiCache(ApiCacheType.LEAGUE_TEAMS, Map.of("leagueId", leagueId), ZonedDateTime.now());

        bothExistTeams.addAll(savedNewTeams);
        return bothExistTeams;
    }

    /**
     * LeagueTeam 연관관계를 채워넣어줍니다.
     * 팀 id 를 바탕으로 해당 팀이 현재 속한 리그들을 캐싱합니다.
     * 팀은 여러 리그에 속해있을 수 있습니다. (ex. epl, fa컵, uefa champions 등 여러 리그)
     * 따라서 팀 아이디를 기반으로 여러 리그에 속하도록 합니다.
     * 리그가 존재하지 않는 경우만 새롭게 엔티티를 추가하며,
     * 리그가 기존에 존재한다면 캐싱 데이터를 업데이트 하지 않고 연관관계만 추가해줍니다.
     * @param teamId
     */
    public void cacheTeamAndCurrentLeagues(long teamId) {
        Team team = null;
        League league = null;
        Optional<Team> findTeam = teamRepository.findById(teamId);
        if (findTeam.isEmpty()) {
            team = cacheSingleTeam(teamId);
            log.info("team is empty. cached new team : {}", team);
        } else {
            team = findTeam.get();
        }

        LeagueInfoResponse leagueInfoResponse = apiCallService.teamCurrentLeaguesInfo(teamId);
        for (_Response response : leagueInfoResponse.getResponse()) {
            long leagueId = response.getLeague().getId();
            Optional<League> findLeague = leagueRepository.findById(leagueId);

            if (findLeague.isEmpty()) {
                league = leagueRepository.save(toLeagueEntity(response));
            } else {
                league = findLeague.get();
            }

            LeagueTeam leagueTeam = LeagueTeam.builder()
                    .league(league)
                    .team(team)
                    .build();
            LeagueTeam saveLeagueTeam = leagueTeamRepository.save(leagueTeam);
        }

        lastCacheLogService.saveApiCache(ApiCacheType.CURRENT_LEAGUES_OF_TEAM, Map.of("teamId", teamId), ZonedDateTime.now());
    }

    /**
     * 팀 아이디로 하나의 팀 정보를 캐싱합니다.
     * 선수단 정보는 이 메서드에 의해서 캐싱되지 않습니다.
     * LeagueTeam 연관관계는 자동으로 저장되지 않습니다.
     * @param teamId
     * @return
     */
    public Team cacheSingleTeam(long teamId) {
        Optional<Team> findTeam = teamRepository.findById(teamId);
        TeamInfoResponse teamInfoResponse = apiCallService.teamInfo(teamId);
        _TeamResponse teamResponse = teamInfoResponse.getResponse().get(0).getTeam();

        Team build = Team.builder()
                .id(teamResponse.getId())
                .name(teamResponse.getName())
                .koreanName(null)
                .logo(teamResponse.getLogo())
                .build();

        log.info("teamId: {} is cached", build.getId());
        Team result;
        if (findTeam.isEmpty()) {
            result = teamRepository.save(build);
            log.info("new team saved :: {}", result);
        } else {
            findTeam.get().updateCompare(build);
            log.info("team updated :: {}", findTeam.get());
            result = findTeam.get();
        }

        lastCacheLogService.saveApiCache(ApiCacheType.TEAM, Map.of("teamId", teamId), ZonedDateTime.now());
        return result;
    }

    /**
     * <h1>Cases</h1>
     * <pre>
     * 1. api 있고 db 있음 : db에서 api 와 일치하지 않는 값을 업데이트
     * 2. api 있고 db 없음 : 새롭게 db에 값을 넣음
     * 3. api 없고 db 있음 : db 에서 teamId 값을 null 로 지움(연관관계 끊음)
     * </pre>
     */
    public List<Player> cacheTeamSquad(long teamId) {
        log.info("cache team squad : {}", teamId);
        Optional<Team> findTeam = teamRepository.findById(teamId);
        Team optionalTeam = null;
        if (findTeam.isEmpty()) {
            log.info("new team squad cache called! id : {}", teamId);
            optionalTeam = cacheSingleTeam(teamId);
        } else {
            optionalTeam = findTeam.get();
        }
        final Team team = optionalTeam;

        PlayerSquadResponse playerSquadResponse = apiCallService.playerSquad(teamId);

        List<_PlayerData> apiPlayers = playerSquadResponse.getResponse().get(0).getPlayers();
        Set<Long> apiPlayerIds = apiPlayers.stream()
                .map(_PlayerData::getId)
                .collect(Collectors.toSet());

        List<Player> dbPlayers = playerRepository.findAllByTeam(teamId);
        Set<Long> dbPlayerIds = dbPlayers.stream()
                .map(Player::getId)
                .collect(Collectors.toSet());

        List<Player> cachedPlayers = new ArrayList<>();

        // case 1 & 2 : API의 선수를 DB에 업데이트하거나 추가
        for (_PlayerData apiPlayer : apiPlayers) {
            Player cachedPlayer = playerRepository.findById(apiPlayer.getId())
                    .map(player -> {
                        // API 데이터로 업데이트
                        log.info("Updating player [{} - {}] with new API data.", player.getId(), player.getName());
                        updatePlayer(player, apiPlayer);
                        return playerRepository.save(player);
                    })
                    .orElseGet(() -> {
                        // DB에 없는 경우 새로 추가
                        log.info("Adding new player [{} - {}] to DB.", apiPlayer.getId(), apiPlayer.getName());
                        return playerRepository.save(toPlayer(apiPlayer));
                    });
            cachedPlayers.add(cachedPlayer);
        }

        // case 3 : DB에만 존재하는 선수의 teamId 연관관계 끊기
        dbPlayerIds.removeAll(apiPlayerIds);
        dbPlayers.stream()
                .filter(player -> dbPlayerIds.contains(player.getId()))
                .forEach(player -> {
                    log.info("_Player [{},{}] team relationship disconnected", player.getId(), player.getName());
                    // TeamId 연관관계 끊기
                    teamPlayerRepository.deleteByTeamAndPlayer(team, player);
                    playerRepository.save(player);
                });

        // TeamPlayer 연관관계 설정
        cachedPlayers.forEach(player -> {
            Optional<TeamPlayer> findTeamPlayer = teamPlayerRepository.findByTeamAndPlayer(team, player);
            if(findTeamPlayer.isEmpty()) {
                teamPlayerRepository.save(player.toTeamPlayer(team));
            }
        });
        cachedPlayers.addAll(dbPlayers);

        // 캐싱 날짜 저장
        ZonedDateTime now = ZonedDateTime.now();
        lastCacheLogService.saveApiCache(ApiCacheType.SQUAD, Map.of("teamId", teamId), now);

        return cachedPlayers;
    }

    /**
     * 모든 현재 진행중인 리그들을 캐싱합니다.
     */
    public void cacheAllCurrentLeagues() {
        log.info("cache all current leagues");
        LeagueInfoResponse response = apiCallService.allLeagueCurrent();
        List<League> leagues = new ArrayList<>();

        for (_Response leagueResponse : response.getResponse()) {
            League league = saveSingleLeague(leagueResponse.getLeague().getId(), leagueResponse);
            leagues.add(league);
        }

        log.info("cached leagues count :: {}", leagues.size());
        log.info("cached leagues :: {}", leagues.stream().map(League::getLeagueId).toList());
        lastCacheLogService.saveApiCache(ApiCacheType.CURRENT_LEAGUES, Map.of(), ZonedDateTime.now());
    }

    /**
     * 캐싱된 리그의 currentSeason 모든 경기 일정을 캐싱합니다.
     * @param leagueId
     */
    public List<Fixture> cacheFixturesOfLeague(long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new RuntimeException("아직 캐싱되지 않은 league 입니다"));
        final int leagueSeason = league.getCurrentSeason();

        FixtureResponse fixtureResponse = apiCallService.fixturesOfLeagueSeason(leagueId, leagueSeason);

        List<LiveStatus> liveStatusList = new ArrayList<>();
        List<Fixture> fixtures = new ArrayList<>();

        for (FixtureResponse._Response response : fixtureResponse.getResponse()) {
            Optional<Fixture> optionalFixture = fixtureRepository.findById(response.getFixture().getId());

            if(optionalFixture.isEmpty()) {
                LiveStatus liveStatus = toLiveStatusEntity(response);
                liveStatusList.add(liveStatusRepository.save(liveStatus));
                Fixture fixture = toFixtureEntity(response, liveStatus);
                fixtures.add(fixtureRepository.save(fixture));
            } else {
                Fixture fixture = optionalFixture.get();
                LiveStatus liveStatus = fixture.getLiveStatus();
                liveStatus.updateCompare(toLiveStatusEntity(response));
                fixture.updateCompare(toFixtureEntity(response, liveStatus));
                fixtures.add(fixture);
            }
        }

        lastCacheLogService.saveApiCache(
                ApiCacheType.FIXTURES_OF_LEAGUE,
                Map.of("leagueId", leagueId, "season", leagueSeason),
                ZonedDateTime.now());

        return fixtures;
    }

    private static League toLeagueEntity(_Response leagueResponse) {
        _LeagueResponse leagueInfo = leagueResponse.getLeague();
        int currentSeason = extractCurrentSeason(leagueResponse);

        return League.builder()
                .leagueId(leagueInfo.getId())
                .name(leagueInfo.getName())
                .koreanName(null)
                .logo(leagueInfo.getLogo())
                .currentSeason(currentSeason)
                .build();
    }

    private static Team toTeamEntity(_TeamResponse teamResponse) {
        return Team.builder()
                .id(teamResponse.getId())
                .name(teamResponse.getName())
                .koreanName(null)
                .logo(teamResponse.getLogo())
                .build();
    }

    private static int extractCurrentSeason(_Response info) {
        return info.getSeasons().stream()
                .filter(_Season::isCurrent)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Current season not found"))
                .getYear();
    }

    private LiveStatus toLiveStatusEntity(FixtureResponse._Response response) {
        return LiveStatus.builder()
                .longStatus(response.getFixture().getStatus().getLongStatus())
                .shortStatus(response.getFixture().getStatus().getShortStatus())
                .elapsed(response.getFixture().getStatus().getElapsed())
                .build();
    }

    private Player toPlayer(PlayerSquadResponse._PlayerData playerData) {
        Player player = new Player();
        player.setId(playerData.getId());
        player.setName(playerData.getName());
        player.setPhotoUrl(playerData.getPhoto());
        player.setPosition(playerData.getPosition());
        player.setNumber(playerData.getNumber());
        log.info("DEBUG :: number is null? = {}", playerData.getNumber());
        return player;
    }

    private void updatePlayer(Player player, _PlayerData apiData) {
        player.setName(apiData.getName());
        player.setPhotoUrl(apiData.getPhoto());
        player.setPosition(apiData.getPosition());
        player.setNumber(apiData.getNumber());
    }

    /*
    # timezone 이 없는 경우 기본 UTC
        fixture: {
        id: 1177042
        referee: null
        timezone: "UTC"
        date: "2024-06-27T00:00:00+00:00"
        timestamp: 1719446400
     */
    /*
    # Asia/Seoul 로 요청하는 경우
        fixture: {
        id: 1177042
        referee: null
        timezone: "Asia/Seoul"
        date: "2024-06-27T09:00:00+09:00"
        timestamp: 1719446400
     */

    private Fixture toFixtureEntity(FixtureResponse._Response response, LiveStatus status) {
        final ZoneId ZONE_ID_SEOUL = ZoneId.of("Asia/Seoul");

        ZonedDateTime zonedDateTime = ZonedDateTime.parse(response.getFixture().getDate(), DateTimeFormatter.ISO_DATE_TIME)
                .withZoneSameInstant(ZONE_ID_SEOUL);

        LocalDateTime dateTime = zonedDateTime.toLocalDateTime();
        String zoneId = ZONE_ID_SEOUL.toString();

        Optional<Team> findHome = teamRepository.findById(response.getTeams().getHome().getId());
        Optional<Team> findAway = teamRepository.findById(response.getTeams().getAway().getId());
        Optional<League> findLeague = leagueRepository.findById(response.getLeague().getId());

        Team home = findHome.orElseThrow(
                () -> new IllegalStateException("_Home team not found : " +
                        response.getTeams().getHome().getId() +
                        " , team name : " +
                        response.getTeams().getHome().getName())
        );
        Team away = findAway.orElseThrow(
                () -> new IllegalStateException("_Away team not found : " +
                        response.getTeams().getAway().getId() +
                        " , team name : " +
                        response.getTeams().getAway().getName())
        );
        League league = findLeague.orElseThrow(
                () -> new IllegalStateException("_League not found : " +
                        response.getLeague().getId() +
                        " , league name : " +
                        response.getLeague().getName())
        );

        return Fixture.builder()
                .fixtureId(response.getFixture().getId())
                .referee(response.getFixture().getReferee())
                .timezone(zoneId)
                .date(dateTime)
                .timestamp(response.getFixture().getTimestamp())
                .liveStatus(status)
                .league(league)
                .homeTeam(home)
                .awayTeam(away)
                .build();
    }

    private @NotNull League saveSingleLeague(long leagueId, _Response response) {
        League build = toLeagueEntity(response);
        League league;

        Optional<League> findLeague = leagueRepository.findById(leagueId);
        if (findLeague.isEmpty()) {
            league = leagueRepository.save(build);
        } else {
            // 이미 캐싱된경우 업데이트 수행
            league = findLeague.get();
            league.updateCompare(build);
        }
        return league;
    }

}
