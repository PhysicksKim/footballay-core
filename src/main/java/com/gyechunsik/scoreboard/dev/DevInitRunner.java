package com.gyechunsik.scoreboard.dev;

import com.gyechunsik.scoreboard.domain.initval.Entity.DefaultMatch;
import com.gyechunsik.scoreboard.domain.initval.Entity.DefaultTeam;
import com.gyechunsik.scoreboard.domain.initval.Entity.Streamer;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.DefaultTeamCodes;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.DefaultUniform;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.LeagueCategory;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.TeamSide;
import com.gyechunsik.scoreboard.domain.initval.repository.DefaultMatchRepository;
import com.gyechunsik.scoreboard.domain.initval.repository.DefaultTeamRepository;
import com.gyechunsik.scoreboard.domain.initval.repository.StreamerRepository;
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

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Streamer gyechunhoe = saveGyechunhoe();
        DefaultMatch defaultMatch = saveEpl2324Round30(gyechunhoe);
        List<DefaultTeam> defaultTeams = saveTeamATeamB(gyechunhoe);

        log.info("Streamer :: {}", gyechunhoe);
        log.info("DefaultMatch :: {}", defaultMatch);
        log.info("DefaultTeams :: {}", defaultTeams);
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

}
