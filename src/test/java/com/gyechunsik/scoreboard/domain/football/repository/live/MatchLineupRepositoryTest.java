package com.gyechunsik.scoreboard.domain.football.repository.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.League;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchLineup;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchPlayer;
import com.gyechunsik.scoreboard.domain.football.persistence.relations.LeagueTeam;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.relations.LeagueTeamRepository;
import com.gyechunsik.scoreboard.domain.football.util.GenerateLeagueTeamFixture;
import com.gyechunsik.scoreboard.domain.football.util.GeneratePlayersOfTeam;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static com.gyechunsik.scoreboard.domain.football.util.GenerateLeagueTeamFixture.generate;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataJpaTest
class MatchLineupRepositoryTest {

    @Autowired
    private MatchLineupRepository matchLineupRepository;
    @Autowired
    private LeagueRepository leagueRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private FixtureRepository fixtureRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private MatchPlayerRepository matchPlayerRepository;
    @Autowired
    private LeagueTeamRepository leagueTeamRepository;

    @Autowired
    private EntityManager em;

    @DisplayName("DISTINCT 사용시 하나의 Lineup 에 다수의 Player 가 fetch join으로 중복 없이 로딩됨")
    @Test
    void findAllByFixtureDistinctTest() {
        // given
        GenerateLeagueTeamFixture.LeagueTeamFixture generate = generate();
        League league = generate.league;
        Team home = generate.home;
        Team away = generate.away;
        Fixture fixture = generate.fixture;

        League saveLeague = leagueRepository.save(league);
        Team saveHome = teamRepository.save(home);
        Team saveAway = teamRepository.save(away);
        leagueTeamRepository.save(LeagueTeam.builder().league(saveLeague).team(saveHome).build());
        leagueTeamRepository.save(LeagueTeam.builder().league(saveLeague).team(saveAway).build());
        Fixture saveFixture = fixtureRepository.save(fixture);

        List<Player> players = GeneratePlayersOfTeam.generatePlayersOfTeam(saveHome);
        List<Player> savePlayers = playerRepository.saveAll(players);

        em.flush();
        em.clear();

        MatchLineup matchLineup = MatchLineup.builder()
                .fixture(saveFixture)
                .team(saveHome)
                .formation("4-4-2")
                .build();
        matchLineupRepository.save(matchLineup);

        em.flush();
        em.clear();

        List<MatchPlayer> matchPlayers = createMatchPlayersOfMatchLineup(matchLineup, savePlayers);
        matchPlayers = matchPlayerRepository.saveAll(matchPlayers);
        matchLineup.setMatchPlayers(matchPlayers);

        em.flush();
        em.clear();

        final int PLAYER_SIZE = savePlayers.size();

        // when
        List<MatchLineup> lineups = matchLineupRepository.findAllByFixture(fixture);

        // then
        // DISTINCT 를 사용했으므로 하나의 lineup 만 조회되어야 하며, 그 하나의 lineup 에 여러 matchPlayers 가 로딩되어 있어야 한다.
        assertThat(lineups).hasSize(1);

        MatchLineup foundLineup = lineups.get(0);
        // fetch join 으로 matchPlayers 가 즉시 로딩되었는지 확인
        assertThat(foundLineup.getMatchPlayers()).hasSize(PLAYER_SIZE);

        // matchPlayers 각각이 올바른 player 를 가지고 있는지 확인
        foundLineup.getMatchPlayers().forEach(mp -> {
            assertThat(mp.getPlayer()).isNotNull();
        });
    }

    private List<MatchPlayer> createMatchPlayersOfMatchLineup(MatchLineup matchLineup, List<Player> savePlayers) {
        List<MatchPlayer> matchPlayers = new ArrayList<>();
        for (int i = 0; i < savePlayers.size(); i++) {
            Player player = savePlayers.get(i);
            MatchPlayer matchPlayer = MatchPlayer.builder()
                    .matchLineup(matchLineup)
                    .player(player)
                    .position("DF")
                    .build();
            matchPlayers.add(matchPlayer);
        }
        return matchPlayers;
    }

}