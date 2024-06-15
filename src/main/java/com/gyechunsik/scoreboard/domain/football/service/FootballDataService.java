package com.gyechunsik.scoreboard.domain.football.service;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Player;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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

    /*
    1) get leagues
    2) get teams by league id
    3) get squads by team id
    4) get fixtures by league id order by date asc limit 10 offset n
    5) add favorite league by league id
     */

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

    public List<Fixture> getFixturesOfLeague(long leagueId) {
        return this.getFixturesOfLeagueAfterDate(leagueId, ZonedDateTime.now());
    }

    public Fixture getFixtureById(long fixtureId) {
        return fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경기입니다."));
    }

    public List<Fixture> getFixturesOfLeagueAfterDate(long leagueId, ZonedDateTime zonedDateTime) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(LEAGUE_NOT_EXIST_THROW_SUPPLIER);
        ZonedDateTime truncatedZonedDateTime = zonedDateTime.truncatedTo(ChronoUnit.DAYS);
        return fixtureRepository.findNextFixturesAfterDate(truncatedZonedDateTime);
    }

}
