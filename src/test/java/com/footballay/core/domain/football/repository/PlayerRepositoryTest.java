package com.footballay.core.domain.football.repository;

import com.footballay.core.domain.football.persistence.Player;
import com.footballay.core.domain.football.persistence.Team;
import com.footballay.core.domain.football.repository.relations.TeamPlayerRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static com.footballay.core.domain.football.util.GenerateLeagueTeamFixture.LeagueTeamFixture;
import static com.footballay.core.domain.football.util.GenerateLeagueTeamFixture.generate;
import static com.footballay.core.domain.football.util.GeneratePlayersOfTeam.generatePlayersOfTeam;

@Transactional
@DataJpaTest
@ActiveProfiles("test")
class PlayerRepositoryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlayerRepositoryTest.class);
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private TeamPlayerRepository teamPlayerRepository;

    @DisplayName("팀 id 로 해당 팀에 속하는 모든 player 를 찾습니다")
    @Test
    void success_findAllPlayersByTeamId() {
        // given
        LeagueTeamFixture generate = generate();
        Team team = generate.home;
        List<Player> players = generatePlayersOfTeam(team);
        teamRepository.save(team);
        playerRepository.saveAll(players);
        teamPlayerRepository.saveAll(players.stream().map(player -> player.toTeamPlayer(team)).toList());
        // when
        List<Player> allPlayersByTeamId = playerRepository.findAllByTeam(team.getId());
        // then
        Assertions.assertThat(allPlayersByTeamId).containsAll(players);
    }
}
