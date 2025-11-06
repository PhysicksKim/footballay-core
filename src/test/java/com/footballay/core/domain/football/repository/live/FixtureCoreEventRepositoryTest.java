package com.footballay.core.domain.football.repository.live;

import com.footballay.core.domain.football.persistence.Fixture;
import com.footballay.core.domain.football.persistence.League;
import com.footballay.core.domain.football.persistence.Player;
import com.footballay.core.domain.football.persistence.Team;
import com.footballay.core.domain.football.persistence.live.EventType;
import com.footballay.core.domain.football.persistence.live.FixtureEvent;
import com.footballay.core.domain.football.persistence.live.MatchLineup;
import com.footballay.core.domain.football.persistence.live.MatchPlayer;
import com.footballay.core.domain.football.persistence.relations.LeagueTeam;
import com.footballay.core.domain.football.repository.FixtureRepository;
import com.footballay.core.domain.football.repository.LeagueRepository;
import com.footballay.core.domain.football.repository.PlayerRepository;
import com.footballay.core.domain.football.repository.TeamRepository;
import com.footballay.core.domain.football.repository.relations.LeagueTeamRepository;
import com.footballay.core.domain.football.util.GeneratePlayersOfTeam;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.ArrayList;
import java.util.List;
import static com.footballay.core.domain.football.util.GenerateLeagueTeamFixture.LeagueTeamFixture;
import static com.footballay.core.domain.football.util.GenerateLeagueTeamFixture.generate;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FixtureEventRepositoryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FixtureEventRepositoryTest.class);
    @Autowired
    private LeagueRepository leagueRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private FixtureRepository fixtureRepository;
    @Autowired
    private LeagueTeamRepository leagueTeamRepository;
    @Autowired
    private FixtureEventRepository fixtureEventRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private EntityManager em;
    @Autowired
    private MatchLineupRepository matchLineupRepository;
    @Autowired
    private MatchPlayerRepository matchPlayerRepository;

    @DisplayName("Fixture 의 Event 를 sequence 순서대로 조회한다")
    @Test
    void FixtureEventSequenceDESC() {
        // given
        LeagueTeamFixture generate = generate();
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
        MatchLineup matchLineup = MatchLineup.builder().fixture(saveFixture).team(saveHome).formation("4-4-2").build();
        matchLineupRepository.save(matchLineup);
        em.flush();
        em.clear();
        List<MatchPlayer> matchPlayers = createMatchPlayersOfMatchLineup(matchLineup, savePlayers);
        matchPlayers = matchPlayerRepository.saveAll(matchPlayers);
        matchLineup.setMatchPlayers(matchPlayers);
        em.flush();
        em.clear();
        // when
        FixtureEvent seq0_normalGoal = FixtureEvent.builder().fixture(saveFixture).team(home).player(matchPlayers.get(0)).sequence(0).timeElapsed(10).extraTime(0).type(EventType.GOAL).detail("Normal Goal").build();
        FixtureEvent seq1_substitution = FixtureEvent.builder().fixture(saveFixture).team(home).player(matchPlayers.get(1)).assist(matchPlayers.get(2)).sequence(1).timeElapsed(15).extraTime(0).type(EventType.SUBST).detail("Substitution 1").build();
        fixtureEventRepository.saveAll(List.of(seq0_normalGoal, seq1_substitution));
        em.flush();
        em.clear();
        List<FixtureEvent> findFixtureEvents = fixtureEventRepository.findByFixtureOrderBySequenceDesc(saveFixture);
        for (FixtureEvent findFixtureEvent : findFixtureEvents) {
            log.info("findFixtureEvent Elapsed={},Type={},sequence={}", findFixtureEvent.getTimeElapsed(), findFixtureEvent.getType(), findFixtureEvent.getSequence());
        }
        // then
        assertThat(findFixtureEvents.get(0).getSequence()).isEqualTo(0);
        assertThat(findFixtureEvents.get(1).getSequence()).isEqualTo(1);
        assertThat(findFixtureEvents.get(0).getTeam().getId()).isEqualTo(home.getId());
    }

    private List<MatchPlayer> createMatchPlayersOfMatchLineup(MatchLineup matchLineup, List<Player> savePlayers) {
        List<MatchPlayer> matchPlayers = new ArrayList<>();
        for (int i = 0; i < savePlayers.size(); i++) {
            Player player = savePlayers.get(i);
            MatchPlayer matchPlayer = MatchPlayer.builder().matchLineup(matchLineup).player(player).position("DF").build();
            matchPlayers.add(matchPlayer);
        }
        return matchPlayers;
    }
}
