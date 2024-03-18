package com.gyechunsik.scoreboard.domain.football.data.cache;

import com.gyechunsik.scoreboard.domain.football.data.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.PlayerSquadResponse;
import com.gyechunsik.scoreboard.domain.football.league.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.player.entity.Player;
import com.gyechunsik.scoreboard.domain.football.player.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.relations.LeagueTeamRepository;
import com.gyechunsik.scoreboard.domain.football.team.Team;
import com.gyechunsik.scoreboard.domain.football.team.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.gyechunsik.scoreboard.domain.football.data.fetch.response.PlayerSquadResponse.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

//TODO : 혹시 이미 캐싱된 항목에 다시 캐싱 요청하는 경우에 대한 테스트 필요.
@Slf4j
@Transactional
@SpringBootTest
class ApiCacheServiceMockBeanTest {

    @Autowired
    private ApiCacheService apiCacheService;

    @Autowired
    private LeagueRepository leagueRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private LeagueTeamRepository leagueTeamRepository;
    @Autowired
    private PlayerRepository playerRepository;

    @MockBean
    private ApiCallService apiCallService;

    private Team team;

    @BeforeEach
    public void setup() {
        team = teamRepository.save(Team.builder()
                .id(50L)
                .name("Manchester City")
                .korean_name(null)
                .logo("logoUrl")
                .build()
        );
    }

    @DisplayName("1. Api 존재 | DB 존재 | 정보 불일치로 업데이트")
    @Test
    public void whenPlayerExistsInApiAndDb_thenUpdatePlayer() {
        // Mock API 데이터 생성
        PlayerData playerData1 = new PlayerData(1L, "Player One", 25, 10, "Defender", "url1");
        PlayerSquadResponse mockPlayerSquadResponse = createMockPlayerSquadResponse(playerData1);
        when(apiCallService.playerSquad(anyLong())).thenReturn(mockPlayerSquadResponse);

        // DB에 선수 데이터 사전 등록
        Player existingPlayer = new Player(1L, "Player One", null, "photoUrl", "Midfielder", team);
        playerRepository.save(existingPlayer);
        log.info("saved team : {}", team);
        log.info("saved player : {}", existingPlayer);

        // 여기서는 ApiCallService가 실제로 작동한다고 가정하고 메서드를 직접 호출합니다.
        apiCacheService.cacheTeamSquad(team.getId());

        // 검증
        Player updatedPlayer = playerRepository.findById(1L).get();
        assertEquals("Defender", updatedPlayer.getPosition());
        assertEquals("Player One", updatedPlayer.getName());
    }


    @DisplayName("2. Api 존재 | DB 없음 | 새로운 선수 업데이트")
    @Test
    public void whenPlayerExistsInApiAndNotInDb_thenAddPlayerToDb() {
        // Mock API 데이터 생성
        PlayerData newPlayerData = new PlayerData(2L, "Player Two", 22, 9, "Midfielder", "url2");
        PlayerSquadResponse mockPlayerSquadResponse = createMockPlayerSquadResponse(newPlayerData);
        when(apiCallService.playerSquad(anyLong())).thenReturn(mockPlayerSquadResponse);

        // cacheTeamSquad() 메서드 실행
        apiCacheService.cacheTeamSquad(team.getId());

        // 검증
        Optional<Player> newPlayerOptional = playerRepository.findById(2L);
        assertTrue(newPlayerOptional.isPresent());
        assertEquals("Player Two", newPlayerOptional.get().getName());
        assertEquals("Midfielder", newPlayerOptional.get().getPosition());
    }

    @DisplayName("3. Api 없음 | DB 존재 | 다른 팀으로 옮긴 선수로 판단하고 연관관계 끊음")
    @Test
    public void whenPlayerNotInApiAndExistsInDb_thenDisconnectTeamRelation() {
        // DB에 선수 데이터 사전 등록
        Player existingPlayer = new Player(3L, "Player Three", null, "photoUrl", "Defender", team);
        playerRepository.save(existingPlayer);

        // Mock API 데이터 생성 (빈 목록)
        PlayerSquadResponse mockPlayerSquadResponse = createMockPlayerSquadResponse();
        when(apiCallService.playerSquad(anyLong())).thenReturn(mockPlayerSquadResponse);

        // cacheTeamSquad() 메서드 실행
        apiCacheService.cacheTeamSquad(team.getId());

        // 검증
        Optional<Player> disconnectedPlayerOptional = playerRepository.findById(3L);
        assertTrue(disconnectedPlayerOptional.isPresent());
        assertNull(disconnectedPlayerOptional.get().getTeam());
    }

    private PlayerSquadResponse createMockPlayerSquadResponse(PlayerData... playerDatas) {
        // PlayerData 객체들을 리스트로 변환
        List<PlayerData> playerDataList = Arrays.asList(playerDatas);

        // TeamSquad 객체 생성 및 PlayerData 리스트 설정
        TeamSquad teamSquad = new TeamSquad();
        teamSquad.setPlayers(playerDataList);

        // TeamSquad 리스트 생성 및 TeamSquad 객체 추가
        List<TeamSquad> teamSquads = Collections.singletonList(teamSquad);

        // PlayerSquadResponse 객체 생성 및 TeamSquad 리스트 설정
        PlayerSquadResponse mockResponse = new PlayerSquadResponse();
        mockResponse.setResponse(teamSquads);

        return mockResponse;
    }

}