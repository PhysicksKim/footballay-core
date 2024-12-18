package com.gyechunsik.scoreboard.domain.defaultmatch;

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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Transactional
@ActiveProfiles({"mockapi"})
@SpringBootTest
class DefaultMatchServiceTest {

    @Autowired
    private StreamerRepository streamerRepository;

    @Autowired
    private DefaultMatchRepository defaultMatchRepository;

    @Autowired
    private DefaultTeamRepository defaultTeamRepository;

    @Autowired
    private DefaultMatchService defaultMatchService;

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

        Map<String, Object> initialValueJson = defaultMatchService.getDefaultMatchJson(streamer.getHash());

        // then
        log.info("initialValueJson :: {}", initialValueJson);
    }

    @DisplayName("streamer 의 Map<이름,해시> 목록을 가져옵니다.")
    @Test
    void success_getStreamers() {
        // given
        streamerRepository.save(new Streamer("gyechunhoe"));
        streamerRepository.save(new Streamer("LookSam"));

        // when
        Map<String, String> streamers = defaultMatchService.getStreamers();

        // then
        log.info("streamers :: {}", streamers);
    }
}
