package com.footballay.core.domain.football.service;

import com.footballay.core.domain.football.persistence.Fixture;
import com.footballay.core.domain.football.persistence.League;
import com.footballay.core.domain.football.repository.FixtureRepository;
import com.footballay.core.domain.football.repository.LeagueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.zone.ZoneRulesException;
import java.util.List;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class FootballAvailableService {

    private final FixtureJobManageService fixtureJobManageService;

    private final LeagueRepository leagueRepository;
    private final FixtureRepository fixtureRepository;

    public void updateAvailableLeague(long leagueId, boolean isAvailable) {
        log.info("updateAvailableLeague :: leagueId={}, isAvailable={}", leagueId, isAvailable);
        League league = leagueRepository.findById(leagueId).orElseThrow();
        league.setAvailable(isAvailable);
        leagueRepository.save(league);
    }

    public List<League> getAvailableLeagues() {
        log.info("getAvailableLeagues");
        List<League> leagues = leagueRepository.findAllByAvailableOrderByCreatedDateDesc(true);
        log.info("leagues={}", leagues);
        return leagues;
    }

    public void addAvailableFixture(long fixtureId) throws SchedulerException {
        log.info("addAvailableFixture :: fixtureId={}", fixtureId);
        Fixture fixture =
                fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("fixture not found"));
        if(fixture.isAvailable()) {
            throw new IllegalArgumentException("fixture is already available");
        }

        fixtureJobManageService.addFixtureJobs(fixture);

        fixture.setAvailable(true);
        fixtureRepository.save(fixture);
    }

    public void removeAvailableFixture(long fixtureId) throws SchedulerException {
        log.info("removeAvailableFixture :: fixtureId={}", fixtureId);
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("fixture not found"));

        fixtureJobManageService.removeFixtureJobs(fixture);

        fixture.setAvailable(false);
        fixtureRepository.save(fixture);
    }

    /**
     * 주어진 date 를 포함하여, 가장 가까운 fixture 가 있는 날짜의 모든 fixture 를 반환합니다.  <br>
     * 예를 들어 2024/06/13 에 최초로 fixture 가 2개 있고, 2024/06/14 에 fixture 가 3개 있다면  <br>
     * 1) 2024/06/01 로 요청하는 경우 2024/06/13 에 있는 2개의 fixture 를 제공합니다.  <br>
     * 2) 2024/06/13 의 아무 시간 으로 요청하는 경우 2024/06/13 에 있는 2개의 fixture 를 제공합니다 <br>
     *
     * @param leagueId      리그 아이디
     * @param matchDateFrom 포함하여 가장 가까운 날짜에 있는 모든 경기일정 조회
     * @return 해당 날짜에 있는 모든 경기일정
     */
    public List<Fixture> findAvailableFixturesOnNearestDate(long leagueId, ZonedDateTime matchDateFrom) {
        LocalDateTime localDateTime = toLocalDateTimeTruncated(matchDateFrom);
        League league = getLeagueById(leagueId);
        log.info("findAvailableFixturesOnNearestDate :: leagueId={}, matchDateFrom={}", leagueId, matchDateFrom);

        // Find the first fixture after the given date
        List<Fixture> fixturesByLeagueAndDate = fixtureRepository.findAvailableFixturesByLeagueAndDateAfter(
                league,
                localDateTime,
                PageRequestForOnlyOneNearest()
        );
        if (fixturesByLeagueAndDate.isEmpty()) {
            return List.of();
        }

        // Get the date of the nearest fixture
        Fixture nearestFixture = fixturesByLeagueAndDate.get(0);
        LocalDateTime nearestDate = nearestFixture.getDate().truncatedTo(ChronoUnit.DAYS);
        List<Fixture> availableFixturesOfNearestDate = fixtureRepository.findAvailableFixturesByLeagueAndDateRange(
                league,
                nearestDate,
                nearestDate.plusDays(1).minusSeconds(1)
        );
        log.info("date of nearest fixture={}, size of availableFixturesOfNearestDate={}", nearestDate, availableFixturesOfNearestDate.size());
        return availableFixturesOfNearestDate;
    }

    public List<Fixture> findAvailableFixturesOnDate(long leagueId, ZonedDateTime matchDate) {
        LocalDateTime localDateTime = toLocalDateTimeTruncated(matchDate);
        League league = getLeagueById(leagueId);
        log.info("findAvailableFixturesOnDate :: leagueId={}, matchDate={}", leagueId, matchDate);

        List<Fixture> availableFixturesByLeagueAndDate = fixtureRepository.findAvailableFixturesByLeagueAndDateRange(
                league,
                localDateTime,
                localDateTime.plusDays(1).minusSeconds(1)
        );
        log.info("size of availableFixturesOfTheDay={}", availableFixturesByLeagueAndDate.size());
        return availableFixturesByLeagueAndDate;
    }

    private static Pageable PageRequestForOnlyOneNearest() {
        return PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "date"));
    }

    private static LocalDateTime toLocalDateTimeTruncated(ZonedDateTime dateTime) {
        return dateTime.truncatedTo(ChronoUnit.DAYS).toLocalDateTime();
    }

    private ZonedDateTime toSeoulZonedDateTime(LocalDateTime kickoffTime, String timeZone, long timestamp) {
        try {
            ZoneId zoneId = ZoneId.of(timeZone);
            return ZonedDateTime.of(kickoffTime, zoneId)
                    .withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        } catch (ZoneRulesException e) {
            Instant instant = Instant.ofEpochSecond(timestamp);
            return ZonedDateTime.ofInstant(instant, ZoneId.of("Asia/Seoul"));
        }
    }

    private League getLeagueById(long leagueId) {
        return leagueRepository.findById(leagueId).orElseThrow();
    }

}
