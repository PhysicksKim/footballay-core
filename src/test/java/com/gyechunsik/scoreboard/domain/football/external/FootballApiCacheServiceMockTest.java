package com.gyechunsik.scoreboard.domain.football.external;

import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.PlayerSquadResponse;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.relations.TeamPlayer;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.relations.TeamPlayerRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.gyechunsik.scoreboard.domain.football.external.fetch.response.PlayerSquadResponse._PlayerData;
import static com.gyechunsik.scoreboard.domain.football.external.fetch.response.PlayerSquadResponse._TeamSquad;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@Slf4j
@Transactional
@SpringBootTest
class FootballApiCacheServiceMockTest {

    @Autowired
    private FootballApiCacheService footballApiCacheService;

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private EntityManager em;

    @MockBean
    private ApiCallService apiCallService;

    private Team team;
    @Autowired
    private TeamPlayerRepository teamPlayerRepository;

    @BeforeEach
    public void setup() {
        team = teamRepository.save(Team.builder()
                .id(50L)
                .name("Manchester City")
                .koreanName(null)
                .logo("logoUrl")
                .build()
        );
    }

    @DisplayName("1. Api 응답에 존재 | DB 에 존재 | 정보 불일치로 업데이트")
    @Test
    public void whenPlayerExistsInApiAndDb_thenUpdatePlayer() {
        // Mock API 데이터 생성
        _PlayerData playerData1 = new _PlayerData(1L, "_Player One", 25, 10, "Defender", "url1");
        PlayerSquadResponse mockPlayerSquadResponse = createMockPlayerSquadResponse(playerData1);
        when(apiCallService.playerSquad(anyLong())).thenReturn(mockPlayerSquadResponse);

        // DB에 선수 데이터 사전 등록
        // _Player existingPlayer = new _Player(1L, "_Player One", null, "photoUrl", "Midfielder",);
        Player existingPlayer = Player.builder()
                .id(1L)
                .name("_Player One")
                .photoUrl("photoUrl")
                .position("Midfielder")
                .build();

        playerRepository.save(existingPlayer);
        log.info("saved team : {}", team);
        log.info("saved player : {}", existingPlayer);

        footballApiCacheService.cacheTeamSquad(team.getId());

        // 검증
        Player updatedPlayer = playerRepository.findById(1L).get();
        assertEquals("Defender", updatedPlayer.getPosition());
        assertEquals("_Player One", updatedPlayer.getName());
    }


    @DisplayName("2. Api 응답에 존재 | DB 에 없음 | 새로운 선수 업데이트")
    @Test
    public void whenPlayerExistsInApiAndNotInDb_thenAddPlayerToDb() {
        // Mock API 데이터 생성
        _PlayerData newPlayerData = new _PlayerData(2L, "_Player Two", 22, 9, "Midfielder", "url2");
        PlayerSquadResponse mockPlayerSquadResponse = createMockPlayerSquadResponse(newPlayerData);
        when(apiCallService.playerSquad(anyLong())).thenReturn(mockPlayerSquadResponse);

        // cacheTeamSquad() 메서드 실행
        footballApiCacheService.cacheTeamSquad(team.getId());

        // 검증
        Optional<Player> newPlayerOptional = playerRepository.findById(2L);
        assertTrue(newPlayerOptional.isPresent());
        assertEquals("_Player Two", newPlayerOptional.get().getName());
        assertEquals("Midfielder", newPlayerOptional.get().getPosition());
    }

    @DisplayName("3. Api 응답에 없음 | DB 응답에 존재 | 다른 팀으로 옮긴 선수로 판단하고 연관관계 끊음")
    @Test
    public void whenPlayerNotInApiAndExistsInDb_thenDisconnectTeamRelation() {
        // DB에 선수 데이터 사전 등록
        Player existingPlayer = Player.builder()
                .id(3L)
                .name("_Player Three")
                .koreanName(null)
                .photoUrl("photoUrl")
                .position("Defender")
                .build();
        playerRepository.save(existingPlayer);
        teamPlayerRepository.save(TeamPlayer.builder().team(team).player(existingPlayer).build());

        // Mock API 데이터 생성 (빈 목록)
        PlayerSquadResponse mockPlayerSquadResponse = createMockPlayerSquadResponse();
        when(apiCallService.playerSquad(anyLong())).thenReturn(mockPlayerSquadResponse);

        // cacheTeamSquad() 메서드 실행
        footballApiCacheService.cacheTeamSquad(team.getId());

        // 검증
        Optional<Player> disconnectedPlayerOptional = playerRepository.findById(3L);
        assertTrue(disconnectedPlayerOptional.isPresent());
    }

    @DisplayName("4. Api 응답에 없음 | DB 에 존재 | preventUnlink = true 인 경우 연관관계가 끊어지지 않아야 함")
    @Test
    public void whenPlayerNotInApiAndExistsInDbWithPreventUnlinkTrue_thenDoNotDisconnectTeamRelation() {
        // DB에 선수 데이터 사전 등록 (preventUnlink = true)
        Player existingPlayer = Player.builder()
                .id(4L)
                .name("_Player Four")
                .position("Forward")
                .preventUnlink(true)
                .build();
        playerRepository.save(existingPlayer);
        teamPlayerRepository.save(TeamPlayer.builder().team(team).player(existingPlayer).build());

        // Mock API 데이터 생성 (빈 목록)
        PlayerSquadResponse mockPlayerSquadResponse = createMockPlayerSquadResponse();
        when(apiCallService.playerSquad(anyLong())).thenReturn(mockPlayerSquadResponse);

        // cacheTeamSquad() 메서드 실행
        footballApiCacheService.cacheTeamSquad(team.getId());

        em.flush(); em.clear();

        // 검증: 연관관계가 끊어지지 않았는지 확인
        Optional<Player> foundPlayerOptional = playerRepository.findById(4L);
        assertTrue(foundPlayerOptional.isPresent());

        Player foundPlayer = foundPlayerOptional.get();
        assertFalse(foundPlayer.getTeamPlayers().isEmpty());
        assertEquals(team.getId(), foundPlayer.getTeamPlayers().iterator().next().getTeam().getId());
    }

    @DisplayName("5. Api 응답에 존재 | DB 에 존재 | preventUnlink = true 인 경우 연관관계가 새로 추가되어야 함")
    @Test
    public void whenPlayerExistsInApiAndExistsInDbWithPreventUnlinkTrue_thenAddTeamRelationIfMissing() {
        // DB에 선수 데이터 사전 등록 (preventUnlink = true, 연관관계 없음)
        Player existingPlayer = Player.builder()
                .id(5L)
                .name("_Player Five")
                .position("Midfielder")
                .preventUnlink(true)
                .build();
        playerRepository.save(existingPlayer);

        // Mock API 데이터 생성 (해당 선수가 응답에 포함됨)
        _PlayerData playerData = new _PlayerData(5L, "_Player Five", 28, 11, "Midfielder", "url5");
        PlayerSquadResponse mockPlayerSquadResponse = createMockPlayerSquadResponse(playerData);
        when(apiCallService.playerSquad(anyLong())).thenReturn(mockPlayerSquadResponse);

        // cacheTeamSquad() 메서드 실행
        footballApiCacheService.cacheTeamSquad(team.getId());

        em.flush(); em.clear();

        // 검증: 연관관계가 추가되었는지 확인
        Optional<Player> foundPlayerOptional = playerRepository.findById(5L);
        assertTrue(foundPlayerOptional.isPresent());

        Player foundPlayer = foundPlayerOptional.get();
        assertFalse(foundPlayer.getTeamPlayers().isEmpty());
        assertEquals(team.getId(), foundPlayer.getTeamPlayers().iterator().next().getTeam().getId());
    }

    private PlayerSquadResponse createMockPlayerSquadResponse(_PlayerData... playerData) {
        // _PlayerData 객체들을 리스트로 변환
        List<_PlayerData> playerDataList = Arrays.asList(playerData);

        // _TeamSquad 객체 생성 및 _PlayerData 리스트 설정
        _TeamSquad teamSquad = new _TeamSquad();
        teamSquad.setPlayers(playerDataList);

        // _TeamSquad 리스트 생성 및 _TeamSquad 객체 추가
        List<_TeamSquad> teamSquads = Collections.singletonList(teamSquad);

        // PlayerSquadResponse 객체 생성 및 _TeamSquad 리스트 설정
        PlayerSquadResponse mockResponse = new PlayerSquadResponse();
        mockResponse.setResponse(teamSquads);

        return mockResponse;
    }

}