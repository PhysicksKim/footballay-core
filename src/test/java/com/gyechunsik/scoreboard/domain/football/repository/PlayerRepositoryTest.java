package com.gyechunsik.scoreboard.domain.football.repository;

import com.gyechunsik.scoreboard.domain.football.entity.Player;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.repository.relations.TeamPlayerRepository;
import com.gyechunsik.scoreboard.domain.football.util.GenerateLeagueTeamFixture;
import com.gyechunsik.scoreboard.domain.football.util.GeneratePlayersOfTeam;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.gyechunsik.scoreboard.domain.football.util.GenerateLeagueTeamFixture.*;
import static com.gyechunsik.scoreboard.domain.football.util.GeneratePlayersOfTeam.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Transactional
@DataJpaTest
class PlayerRepositoryTest {

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
        teamPlayerRepository.saveAll(
                players.stream().map(player -> player.toTeamPlayer(team)).toList()
        );

        // when
        List<Player> allPlayersByTeamId = playerRepository.findAllByTeam(team.getId());

        // then
        Assertions.assertThat(allPlayersByTeamId).containsAll(players);
    }

}