package com.gyechunsik.scoreboard.runner;

import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.constant.TeamId;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
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
import com.gyechunsik.scoreboard.domain.quartz.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("dev")
public class DevInitRunner implements ApplicationRunner {

    private final StreamerRepository streamerRepository;
    private final DefaultMatchRepository defaultMatchRepository;
    private final DefaultTeamRepository defaultTeamRepository;
    private final FootballApiCacheService footballApiCacheService;

    private final SchedulerService schedulerService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Streamer gyechunhoe = saveGyechunhoe();
        DefaultMatch defaultMatch = saveEpl2324Round30(gyechunhoe);
        List<DefaultTeam> defaultTeams = saveTeamATeamB(gyechunhoe);

        log.info("Streamer :: {}", gyechunhoe);
        log.info("DefaultMatch :: {}", defaultMatch);
        log.info("DefaultTeams :: {}", defaultTeams);

        // schedulerService.start();
        // log.info("스케쥴러 시작 , 10분 후 SimpleJob 실행 , {} ", LocalDateTime.now().plusMinutes(10));
    }

    private Streamer saveGyechunhoe() {
        return streamerRepository.save(new Streamer("gyechunhoe"));
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

    private void cacheEuro2024() {
        footballApiCacheService.cacheLeague(LeagueId.EURO);
        footballApiCacheService.cacheTeamsOfLeague(LeagueId.EURO);
        for (Long teamId : TeamId.EURO2024TEAMS) {
            footballApiCacheService.cacheTeamSquad(teamId);
        }
        footballApiCacheService.cacheFixturesOfLeague(LeagueId.EURO);
        cacheEuro2024FavoriteFixtures();
    }

    private void cacheFavoriteLeague(long leagueId) {

    }

    private void cacheEuro2024FavoriteFixtures() {

    }

}
