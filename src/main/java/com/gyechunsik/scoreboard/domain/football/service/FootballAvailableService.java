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
public class FootballAvailableService {

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

    public void updateAvailableFixture(long fixtureId, boolean isAvailable) {
        log.info("updateAvailableFixture :: fixtureId={}, isAvailable={}", fixtureId, isAvailable);
        Fixture fixture = fixtureRepository.findById(fixtureId).
                orElseThrow(() -> new IllegalArgumentException("fixture not found"));

        // TODO : 여기서 Available Quartz 2가지 세트를 등록해줘야함

        fixture.setAvailable(isAvailable);
        fixtureRepository.save(fixture);
    }

    /**
     * 주어진 date 를 포함하여, 가장 가까운 fixture 가 있는 날짜의 모든 fixture 를 반환합니다.  <br>
     * 예를 들어 2024/06/13 에 최초로 fixture 가 2개 있고, 2024/06/14 에 fixture 가 3개 있다면  <br>
     * 1) 2024/06/01 로 요청하는 경우 2024/06/13 에 있는 2개의 fixture 를 제공합니다.  <br>
     * 2) 2024/06/13 의 아무 시간 으로 요청하는 경우 2024/06/13 에 있는 2개의 fixture 를 제공합니다 <br>
     * @param leagueId 리그 아이디
     * @param matchDateFrom 포함하여 가장 가까운 날짜에 있는 모든 경기일정 조회
     * @return 해당 날짜에 있는 모든 경기일정
     */
    public List<Fixture> getAvailableFixturesFromDate(long leagueId, ZonedDateTime matchDateFrom) {
        log.info("getAvailableFixturesFromDate :: leagueId={}, matchDateFrom={}", leagueId, matchDateFrom);
        ZonedDateTime truncated = matchDateFrom.truncatedTo(ChronoUnit.DAYS);
        return fixtureRepository.findAvailableFixturesByLeagueIdAndDate(leagueId, truncated);
    }

}
