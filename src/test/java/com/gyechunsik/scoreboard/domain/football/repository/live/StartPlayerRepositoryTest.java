package com.gyechunsik.scoreboard.domain.football.repository.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.StartLineup;
import com.gyechunsik.scoreboard.domain.football.persistence.live.StartPlayer;
import com.gyechunsik.scoreboard.domain.football.persistence.relations.TeamPlayer;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.util.TestFootballDataInitializer;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@ActiveProfiles({"dev", "mockapi"})
@SpringBootTest
@Transactional
public class StartPlayerRepositoryTest {

    @Autowired
    private StartPlayerRepository startPlayerRepository;
    @Autowired
    private StartLineupRepository startLineupRepository;
    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TestFootballDataInitializer dataInitializer;

    @Autowired
    private FixtureRepository fixtureRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private EntityManager em;

    private Map<String, String> dataIdMap = new HashMap<>();

    @BeforeEach
    void setup() {
        dataIdMap = dataInitializer.generateSingleSet();
    }

    @DisplayName("StartPlayer 추가에 성공")
    @Test
    void AddStartPlayer() {
        // given
        List<String> playerIds = dataInitializer.getPlayerIds(dataIdMap);
        log.info("playerIds : {}", playerIds);
        String fixtureId = dataInitializer.getFixtureId(dataIdMap).get(0);
        String homeId = dataInitializer.getHomeTeamId(dataIdMap).get(0);
        String awayId = dataInitializer.getAwayTeamId(dataIdMap).get(0);
        Fixture fixture = fixtureRepository.findById(Long.parseLong(fixtureId)).orElseThrow();
        Team home = teamRepository.findById(Long.parseLong(homeId)).orElseThrow();
        Team away = teamRepository.findById(Long.parseLong(awayId)).orElseThrow();
        List<Player> players = new ArrayList<>();
        for (String playerId : playerIds) {
            players.add(playerRepository.findById(Long.parseLong(playerId)).orElseThrow());
        }

        List<Player> homePlayers = home.getTeamPlayers().stream().map(TeamPlayer::getPlayer).toList();
        List<Player> awayPlayers = away.getTeamPlayers().stream().map(TeamPlayer::getPlayer).toList();
        log.info("homePlayers : {}", homePlayers);
        log.info("awayPlayers : {}", awayPlayers);

        StartLineup homeStartLineup = StartLineup.builder()
                .fixture(fixture)
                .team(home)
                .formation("4-4-2")
                .build();
        StartLineup awayStartLineup = StartLineup.builder()
                .fixture(fixture)
                .team(away)
                .formation("4-4-2")
                .build();
        homeStartLineup = startLineupRepository.save(homeStartLineup);
        awayStartLineup = startLineupRepository.save(awayStartLineup);

        String[] girds = {
                "1:1",
                "2:1", "2:2", "2:3", "2:4",
                "3:1", "3:2", "3:3", "3:4",
                "4:1", "4:2"
        };

        log.info("home player size : {}", homePlayers.size());
        log.info("away player size : {}", awayPlayers.size());

        for (int i = 0; i < homePlayers.size(); i++) {
            Player homePlayer = homePlayers.get(i);
            StartPlayer startPlayer = StartPlayer.builder()
                    .player(homePlayer)
                    .startLineup(homeStartLineup)
                    .position(homePlayer.getPosition())
                    .grid(i<11 ? girds[i] : null)
                    .substitute(false)
                    .build();
            startPlayerRepository.save(startPlayer);
        }
        for (int i = 0; i < awayPlayers.size(); i++) {
            Player awayPlayer = awayPlayers.get(i);
            StartPlayer startPlayer = StartPlayer.builder()
                    .player(awayPlayer)
                    .startLineup(awayStartLineup)
                    .position(awayPlayer.getPosition())
                    .grid(i<11 ? girds[i] : null)
                    .substitute(false)
                    .build();
            startPlayerRepository.save(startPlayer);
        }

        em.flush();
        em.clear();

        // when
        List<StartLineup> beforeDeleteStartLineup = startLineupRepository.findAllByFixture(fixture);
        log.info("BEFORE startLineup size : {}", beforeDeleteStartLineup.size());
        beforeDeleteStartLineup.forEach(startLineup -> {
            log.info("BEFORE startLineup : {}", startLineup);
        });

        int deleted = startPlayerRepository.deleteByStartLineupIn(beforeDeleteStartLineup);
        log.info("deleted players : {}", deleted);

        List<StartPlayer> all = startPlayerRepository.findAll();

        // then
        assertThat(beforeDeleteStartLineup).isNotEmpty();
        assertThat(deleted).isEqualTo(homePlayers.size() + awayPlayers.size());
        assertThat(all).isEmpty();
    }
}