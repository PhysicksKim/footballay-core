package com.gyechunsik.scoreboard.domain.football.util;

import com.gyechunsik.scoreboard.domain.defaultmatch.entity.DefaultMatch;
import com.gyechunsik.scoreboard.domain.defaultmatch.entity.DefaultTeam;
import com.gyechunsik.scoreboard.domain.defaultmatch.entity.Streamer;
import com.gyechunsik.scoreboard.domain.defaultmatch.entity.enums.DefaultTeamCodes;
import com.gyechunsik.scoreboard.domain.defaultmatch.entity.enums.DefaultUniform;
import com.gyechunsik.scoreboard.domain.defaultmatch.entity.enums.LeagueCategory;
import com.gyechunsik.scoreboard.domain.defaultmatch.entity.enums.TeamSide;
import com.gyechunsik.scoreboard.domain.defaultmatch.repository.DefaultMatchRepository;
import com.gyechunsik.scoreboard.domain.defaultmatch.repository.DefaultTeamRepository;
import com.gyechunsik.scoreboard.domain.defaultmatch.repository.StreamerRepository;
import com.gyechunsik.scoreboard.domain.football.FootballRoot;
import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.constant.TeamId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DevInitData {

    private final FootballRoot footballRoot;
    private final StreamerRepository streamerRepository;
    private final DefaultMatchRepository defaultMatchRepository;
    private final DefaultTeamRepository defaultTeamRepository;

    public void addData() {
        // Save Streamer "gyechunhoe"
        Streamer gyechunhoe = saveStreamer();

        // Save [ DefaultMatch, DefaultTeams] of EPL 2023-24 Round 30
        saveDefaultMatchAndTeams(gyechunhoe);

        // Save [ _League, _Team, _Player, _Fixture ] of Euro 2024
        cacheLeagueTeamPlayerFixtureOfEuro2024();

        // Save [ AvailableLeague, AvailableFixture ] of Euro 2024
        cacheAvailableLeagueAndAvailableFixtureOfEuro2024();
    }

    private void cacheLeagueTeamPlayerFixtureOfEuro2024() {
        footballRoot.cacheLeagueById(LeagueId.EURO);
        footballRoot.cacheTeamsOfLeague(LeagueId.EURO);
        for (Long teamId : TeamId.EURO2024TEAMS) {
            footballRoot.cacheSquadOfTeam(teamId);
        }
        footballRoot.cacheAllFixturesOfLeague(LeagueId.EURO);
    }

    private void cacheAvailableLeagueAndAvailableFixtureOfEuro2024() {
        cacheAvailableLeague(LeagueId.EURO);
        cacheAvailableFixture(LeagueId.EURO);
    }

    private void cacheAvailableLeague(long leagueId) {
        footballRoot.addAvailableLeague(leagueId);
    }

    private void cacheAvailableFixture(long leagueId) {
        if(leagueId == LeagueId.EURO)
            footballRoot.addAvailableFixture(FixtureId.FIXTURE_EURO2024_1);
    }

    private Streamer saveStreamer() {
        Streamer streamer = streamerRepository.save(new Streamer("gyechunhoe"));
        log.info("Streamer :: {}", streamer);
        return streamer;
    }

    private void saveDefaultMatchAndTeams(Streamer streamer) {
        DefaultMatch defaultMatch = saveEpl2324Round30(streamer);
        List<DefaultTeam> defaultTeams = saveTeamATeamB(streamer);
        log.info("DefaultMatch :: {}", defaultMatch);
        log.info("DefaultTeams :: {}", defaultTeams);
    }

    private DefaultMatch saveEpl2324Round30(Streamer streamer) {
        return defaultMatchRepository.save(new DefaultMatch("2023-24 잉글랜드 프리미어리그 30R", streamer));
    }

    private List<DefaultTeam> saveTeamATeamB(Streamer streamer) {
        LeagueCategory category = LeagueCategory.epl2324;
        DefaultTeam teamA = new DefaultTeam(TeamSide.A,
                category,
                DefaultTeamCodes.mci,
                DefaultUniform.home,
                streamer);
        DefaultTeam teamB = new DefaultTeam(TeamSide.B,
                category,
                DefaultTeamCodes.ars,
                DefaultUniform.away,
                streamer);
        return defaultTeamRepository.saveAll(List.of(teamA, teamB));
    }

}
