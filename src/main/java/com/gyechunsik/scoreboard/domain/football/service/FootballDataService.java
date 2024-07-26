package com.gyechunsik.scoreboard.domain.football.service;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Player;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.entity.live.FixtureEvent;
import com.gyechunsik.scoreboard.domain.football.entity.live.StartLineup;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.FixtureEventRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.StartLineupRepository;
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

    private static final Supplier<IllegalArgumentException> LEAGUE_NOT_EXIST_THROW_SUPPLIER
            = () -> new IllegalArgumentException("존재하지 않는 리그입니다.");
    private final PlayerRepository playerRepository;
    private final FixtureRepository fixtureRepository;
    private final FixtureEventRepository fixtureEventRepository;
    private final StartLineupRepository startLineupRepository;

    /**
     * 캐싱된 리그를 오름차순으로 조회합니다.
     * @param numOfLeagues 조회할 리그 수 (page size)
     * @return 조회된 리그 리스트
     */
    public List<League> getLeagues(int numOfLeagues) {
        Page<League> leagues = leagueRepository.findAll(PageRequest.of(0, numOfLeagues, Sort.by(Sort.Order.asc("createdDate"))));
        return leagues.getContent();
    }

    public League findLeagueById(long leagueId) {
        return leagueRepository.findById(leagueId)
                .orElseThrow(LEAGUE_NOT_EXIST_THROW_SUPPLIER);
    }

    public List<Team> getTeamsByLeagueId(long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(LEAGUE_NOT_EXIST_THROW_SUPPLIER);
        return teamRepository.findTeamsByLeague(league);
    }

    public List<Player> getSquadOfTeam(long teamId) {
        return playerRepository.findAllByTeam(teamId);
    }

    public Fixture getFixtureById(long fixtureId) {
        return fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경기입니다."));
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

    public Player getPlayerById(long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 선수입니다."));
    }

    public Fixture getFixtureWithEager(long fixtureId) {
        return fixtureRepository.findFixtureByIdWithDetails(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경기입니다."));
    }

    public List<FixtureEvent> getFixtureEvents(Fixture fixture) {
        return fixtureEventRepository.findByFixtureOrderBySequenceDesc(fixture);
    }

    public Optional<StartLineup> getStartLineup(Fixture fixture, Team team) {
        return startLineupRepository.findByFixtureAndTeam(fixture, team);
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

}
