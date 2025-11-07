package com.footballay.core.domain.football.repository.live;

import com.footballay.core.domain.football.persistence.Fixture;
import com.footballay.core.domain.football.persistence.Player;
import com.footballay.core.domain.football.persistence.Team;
import com.footballay.core.domain.football.persistence.live.MatchLineup;
import com.footballay.core.domain.football.persistence.live.MatchPlayer;
import com.footballay.core.domain.football.persistence.relations.TeamPlayer;
import com.footballay.core.domain.football.repository.FixtureRepository;
import com.footballay.core.domain.football.repository.PlayerRepository;
import com.footballay.core.domain.football.repository.TeamRepository;
import com.footballay.core.util.TestFootballDataInitializer;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
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
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"test","mockapi"})
@SpringBootTest
@Transactional
public class MatchPlayerRepositoryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatchPlayerRepositoryTest.class);
    @Autowired
    private MatchPlayerRepository matchPlayerRepository;
    @Autowired
    private MatchLineupRepository matchLineupRepository;
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

    @DisplayName("MatchPlayer 추가에 성공")
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
        MatchLineup homeMatchLineup = MatchLineup.builder().fixture(fixture).team(home).formation("4-4-2").build();
        MatchLineup awayMatchLineup = MatchLineup.builder().fixture(fixture).team(away).formation("4-4-2").build();
        homeMatchLineup = matchLineupRepository.save(homeMatchLineup);
        awayMatchLineup = matchLineupRepository.save(awayMatchLineup);
        String[] girds = {"1:1", "2:1", "2:2", "2:3", "2:4", "3:1", "3:2", "3:3", "3:4", "4:1", "4:2"};
        log.info("home player size : {}", homePlayers.size());
        log.info("away player size : {}", awayPlayers.size());
        for (int i = 0; i < homePlayers.size(); i++) {
            Player homePlayer = homePlayers.get(i);
            MatchPlayer matchPlayer = MatchPlayer.builder().player(homePlayer).matchLineup(homeMatchLineup).position(homePlayer.getPosition()).grid(i < 11 ? girds[i] : null).substitute(false).build();
            matchPlayerRepository.save(matchPlayer);
        }
        for (int i = 0; i < awayPlayers.size(); i++) {
            Player awayPlayer = awayPlayers.get(i);
            MatchPlayer matchPlayer = MatchPlayer.builder().player(awayPlayer).matchLineup(awayMatchLineup).position(awayPlayer.getPosition()).grid(i < 11 ? girds[i] : null).substitute(false).build();
            matchPlayerRepository.save(matchPlayer);
        }
        em.flush();
        em.clear();
        // when
        List<MatchLineup> beforeDeleteMatchLineup = matchLineupRepository.findAllByFixture(fixture);
        log.info("BEFORE matchLineup size : {}", beforeDeleteMatchLineup.size());
        beforeDeleteMatchLineup.forEach(startLineup -> {
            log.info("BEFORE matchLineup : {}", startLineup);
        });
        int deleted = matchPlayerRepository.deleteByMatchLineupIn(beforeDeleteMatchLineup);
        log.info("deleted players : {}", deleted);
        List<MatchPlayer> all = matchPlayerRepository.findAll();
        // then
        assertThat(beforeDeleteMatchLineup).isNotEmpty();
        assertThat(deleted).isEqualTo(homePlayers.size() + awayPlayers.size());
        assertThat(all).isEmpty();
    }
}
