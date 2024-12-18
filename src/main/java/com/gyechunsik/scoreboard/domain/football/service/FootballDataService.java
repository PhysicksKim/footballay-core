package com.gyechunsik.scoreboard.domain.football.service;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.League;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.*;
import com.gyechunsik.scoreboard.domain.football.persistence.relations.TeamPlayer;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.FixtureEventRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchLineupRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchPlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.TeamStatisticsRepository;
import com.gyechunsik.scoreboard.domain.football.repository.relations.TeamPlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class FootballDataService {

    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final FixtureRepository fixtureRepository;
    private final FixtureEventRepository fixtureEventRepository;
    private final MatchLineupRepository matchLineupRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final TeamStatisticsRepository teamStatisticsRepository;
    private final MatchPlayerRepository matchPlayerRepository;

    private static final Supplier<IllegalArgumentException> LEAGUE_NOT_EXIST_THROW_SUPPLIER
            = () -> new IllegalArgumentException("존재하지 않는 리그입니다.");
    private static final Supplier<IllegalArgumentException> TEAM_NOT_EXIST_THROW_SUPPLIER
            = () -> new IllegalArgumentException("존재하지 않는 팀입니다.");
    private static final Supplier<IllegalArgumentException> PLAYER_NOT_EXIST_THROW_SUPPLIER
            = () -> new IllegalArgumentException("존재하지 않는 선수입니다.");
    private static final Supplier<IllegalArgumentException> FIXTURE_NOT_EXIST_THROW_SUPPLIER
            = () -> new IllegalArgumentException("존재하지 않는 경기입니다.");

    /**
     * 캐싱된 리그를 오름차순으로 조회합니다.
     *
     * @param numOfLeagues 조회할 리그 수 (page size)
     * @return 조회된 리그 리스트
     */
    public List<League> getLeagues(int numOfLeagues) {
        Page<League> leagues = leagueRepository.findAll(PageRequest.of(0, numOfLeagues, Sort.by(Sort.Order.asc("createdDate"))));
        return leagues.getContent();
    }

    public League findLeagueById(long leagueId) {
        return leagueRepository.findById(leagueId).orElseThrow(LEAGUE_NOT_EXIST_THROW_SUPPLIER);
    }

    public List<Team> getTeamsByLeagueId(long leagueId) {
        League league = leagueRepository.findById(leagueId).orElseThrow(LEAGUE_NOT_EXIST_THROW_SUPPLIER);
        return teamRepository.findTeamsByLeague(league);
    }

    public List<Player> getSquadOfTeam(long teamId) {
        return playerRepository.findAllByTeam(teamId);
    }

    public Fixture getFixtureById(long fixtureId) {
        return findFixtureOrThrow(fixtureId);
    }

    public LiveStatus getFixtureLiveStatus(long fixtureId) {
        return findFixtureOrThrow(fixtureId).getLiveStatus();
    }

    public List<Fixture> findFixturesOnClosestDate(long leagueId, ZonedDateTime matchDateFrom) {
        LocalDateTime localDateTime = toLocalDateTimeTruncated(matchDateFrom);
        League league = getLeagueById(leagueId);
        log.info("findFixturesOnClosestDate :: leagueId={}, matchDateFrom={}", leagueId, matchDateFrom);

        // Find the first fixture after the given date
        List<Fixture> fixturesByLeagueAndDate = fixtureRepository.findFixturesByLeagueAndDateAfter(
                league,
                localDateTime,
                PageRequestForOnlyOneClosest()
        );
        if (fixturesByLeagueAndDate.isEmpty()) {
            return List.of();
        }

        // Get the date of the closest fixture
        Fixture closestFixture = fixturesByLeagueAndDate.get(0);
        LocalDateTime closestDate = closestFixture.getDate().truncatedTo(ChronoUnit.DAYS);
        List<Fixture> fixturesOfClosestDate = fixtureRepository.findFixturesByLeagueAndDateRange(
                league,
                closestDate,
                closestDate.plusDays(1).minusSeconds(1)
        );
        log.info("date of closest fixture={}, size of closestDateFixtures={}", closestDate, fixturesOfClosestDate.size());
        return fixturesOfClosestDate;
    }

    public List<Fixture> findFixturesOnDate(long leagueId, ZonedDateTime matchDate) {
        LocalDateTime localDateTime = toLocalDateTimeTruncated(matchDate);
        League league = getLeagueById(leagueId);
        log.info("findFixturesOnDate :: leagueId={}, matchDate={}", leagueId, matchDate);

        List<Fixture> fixturesByLeagueAndDate = fixtureRepository.findFixturesByLeagueAndDateRange(
                league,
                localDateTime,
                localDateTime.plusDays(1).minusSeconds(1)
        );
        log.info("size of fixturesOfTheDay={}", fixturesByLeagueAndDate.size());
        return fixturesByLeagueAndDate;
    }

    public Optional<Player> findPlayerById(long playerId) {
        return playerRepository.findById(playerId);
    }

    public Fixture getFixtureWithEager(long fixtureId) {
        return fixtureRepository.findFixtureByIdWithDetails(fixtureId)
                .orElseThrow(FIXTURE_NOT_EXIST_THROW_SUPPLIER);
    }

    public List<FixtureEvent> getFixtureEvents(Fixture fixture) {
        return fixtureEventRepository.findByFixtureOrderBySequenceDesc(fixture);
    }

    public Optional<MatchLineup> getStartLineup(Fixture fixture, Team team) {
        return matchLineupRepository.findByFixtureAndTeam(fixture, team);
    }

    public Player addTeamPlayerRelationManually(long teamId, long playerId) {
        Team team = findTeamOrThrow(teamId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(TEAM_NOT_EXIST_THROW_SUPPLIER);
        Optional<TeamPlayer> findTeamPlayer = teamPlayerRepository.findByTeamAndPlayer(team, player);
        if (findTeamPlayer.isPresent()) {
            log.info("TeamPlayer relation already exists :: team=[{},{}], playerId=[{},{}]", teamId, team.getName(), playerId, player.getName());
            return player;
        }

        TeamPlayer teamPlayer = TeamPlayer.builder()
                .player(player)
                .team(team)
                .build();
        teamPlayerRepository.save(teamPlayer);
        log.info("TeamPlayer relation added manually :: team=[{},{}], playerId=[{},{}]", teamId, team.getName(), playerId, player.getName());
        setPreventUnlink(playerId, true);
        return player;
    }

    public Player removeTeamPlayerRelationManually(long teamId, long playerId) {
        Team team = findTeamOrThrow(teamId);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(PLAYER_NOT_EXIST_THROW_SUPPLIER);
        teamPlayerRepository.deleteByTeamAndPlayer(team, player);
        log.info("TeamPlayer relation removed manually :: team=[{},{}], playerId=[{},{}]", teamId, team.getName(), playerId, player.getName());
        setPreventUnlink(playerId, true);
        return player;
    }

    public void setPreventUnlink(long playerId, boolean preventUnlink) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(PLAYER_NOT_EXIST_THROW_SUPPLIER);
        player.setPreventUnlink(preventUnlink);
        playerRepository.save(player);
        log.info("PreventUnlink set to {} for player=[{},{}]", preventUnlink, playerId, player.getName());
    }

    private static Pageable PageRequestForOnlyOneClosest() {
        return PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "date"));
    }

    private static LocalDateTime toLocalDateTimeTruncated(ZonedDateTime dateTime) {
        return dateTime.truncatedTo(ChronoUnit.DAYS).toLocalDateTime();
    }

    private League getLeagueById(long leagueId) {
        return leagueRepository.findById(leagueId)
                .orElseThrow(LEAGUE_NOT_EXIST_THROW_SUPPLIER);
    }

    public List<Team> getTeamsOfPlayer(long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(PLAYER_NOT_EXIST_THROW_SUPPLIER);
        List<Team> teamsOfPlayer = teamPlayerRepository.findTeamsByPlayer(player).stream()
                .map(TeamPlayer::getTeam)
                .toList();
        log.info("teams of player=[{},{}]={}", playerId, player.getName(), teamsOfPlayer);
        return teamsOfPlayer;
    }

    public Optional<TeamStatistics> getTeamStatistics(long fixtureId, long teamId) {
        Fixture fixture = findFixtureOrThrow(fixtureId);
        Team team = findTeamOrThrow(teamId);
        return teamStatisticsRepository.findByFixtureAndTeam(fixture, team);
    }

    public List<MatchPlayer> getPlayerStatistics(long fixtureId, long teamId) {
        Fixture fixture = findFixtureOrThrow(fixtureId);
        Team team = findTeamOrThrow(teamId);
        return matchPlayerRepository.findMatchPlayerByFixtureAndTeam(fixture, team);
    }

    private Fixture findFixtureOrThrow(long fixtureId) {
        return fixtureRepository.findById(fixtureId)
                .orElseThrow(FIXTURE_NOT_EXIST_THROW_SUPPLIER);
    }

    private Team findTeamOrThrow(long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(TEAM_NOT_EXIST_THROW_SUPPLIER);
    }

}
