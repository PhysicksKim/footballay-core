package com.gyechunsik.scoreboard.domain.initval;

import com.gyechunsik.scoreboard.domain.initval.Entity.DefaultMatch;
import com.gyechunsik.scoreboard.domain.initval.Entity.DefaultTeam;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.DefaultUniform;
import com.gyechunsik.scoreboard.domain.initval.Entity.Streamer;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.DefaultTeamCodes;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.LeagueCategory;
import com.gyechunsik.scoreboard.domain.initval.Entity.enums.TeamSide;
import com.gyechunsik.scoreboard.domain.initval.repository.DefaultMatchRepository;
import com.gyechunsik.scoreboard.domain.initval.repository.DefaultTeamRepository;
import com.gyechunsik.scoreboard.domain.initval.repository.StreamerRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
class InitialValueServiceTest {

    @Autowired
    private StreamerRepository streamerRepository;

    @Autowired
    private DefaultMatchRepository defaultMatchRepository;

    @Autowired
    private DefaultTeamRepository defaultTeamRepository;

    @Autowired
    private InitialValueService initialValueService;

    @DisplayName("")
    @Test
    void Success() {
        // given
        String streamerName = "gyechunhoe";
        Streamer streamer = streamerRepository.save(new Streamer(streamerName));
        DefaultMatch defaultMatch = defaultMatchRepository.save(new DefaultMatch("2023-24 잉글랜드 프리미어리그 30R", streamer));

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
        defaultTeamRepository.saveAll(List.of(teamA, teamB));

        // when
        log.info("streamer :: {}", streamer);
        log.info("defaultMatch :: {}", defaultMatch);
        log.info("teamA :: {}", teamA);
        log.info("teamB :: {}", teamB);

        Map<String, Object> initialValueJson = initialValueService.getInitialValueJson(streamer.getHash());

        // then
        log.info("initialValueJson :: {}", initialValueJson);
    }
}
