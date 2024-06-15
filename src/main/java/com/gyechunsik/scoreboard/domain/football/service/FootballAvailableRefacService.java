package com.gyechunsik.scoreboard.domain.football.service;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class FootballAvailableRefacService {

    private final LeagueRepository leagueRepository;
    private final FixtureRepository fixtureRepository;

    // league available CRD
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

    // fixture available CRD
    public void updateAvailableFixture(long fixtureId, boolean isAvailable) {
        log.info("updateAvailableFixture :: fixtureId={}, isAvailable={}", fixtureId, isAvailable);
        Fixture fixture = fixtureRepository.findById(fixtureId).
                orElseThrow(() -> new IllegalArgumentException("fixture not found"));
        fixture.setAvailable(isAvailable);
        fixtureRepository.save(fixture);
    }

    public List<Fixture> getAvailableFixturesFromDate(long leagueId, ZonedDateTime matchDateFrom) {
        log.info("getAvailableFixturesFromDate :: leagueId={}, matchDateFrom={}", leagueId, matchDateFrom);
        ZonedDateTime truncated = matchDateFrom.truncatedTo(ChronoUnit.DAYS);
        return fixtureRepository.findAvailableFixturesByLeagueIdAndDate(leagueId, truncated);
    }

}
