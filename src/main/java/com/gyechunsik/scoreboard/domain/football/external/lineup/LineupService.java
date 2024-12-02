package com.gyechunsik.scoreboard.domain.football.external.lineup;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchLineup;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchPlayer;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchLineupRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchPlayerRepository;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse.*;

// TODO : lineup 에서 id = null 인 선수가 들어오면 제대로 null safe 하게 동작해서 라인업이 제대로 저장 되는지 테스트 작성 필요
@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class LineupService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final FixtureRepository fixtureRepository;
    private final MatchLineupRepository matchLineupRepository;
    private final MatchPlayerRepository matchPlayerRepository;

    /**
     * FixtureSingleResponse 에서 라인업 데이터의 존재 여부를 확인합니다.
     *
     * @param response 외부 API 호출의 응답 객체
     * @return 라인업 데이터가 존재하면 true, 그렇지 않으면 false
     */
    public boolean existLineupDataInResponse(FixtureSingleResponse response) {
        return !response.getResponse().get(0).getLineups().isEmpty();
    }

    /**
     * 라인업 데이터를 정리하고 다시 저장해야 하는지 여부를 결정합니다.
     * <p>
     * API 응답의 라인업 데이터와 데이터베이스에 저장된 기존 라인업 데이터를 비교합니다.
     * 새로운 라인업 데이터가 있거나 기존 데이터에 변경 사항(예: 미등록 선수가 등록 선수로 변경됨)이 있는 경우
     * true 를 반환하여 클린업 및 재저장이 필요함을 나타냅니다.
     * </p>
     *
     * @param response 외부 API 의 FixtureSingleResponse
     * @return 기존 저장 데이터와 API 응답을 비교해서 미등록/등록 선수 수가 불일치 하면 true, 그렇지 않으면 false
     * @throws IllegalArgumentException API 응답 또는 데이터베이스에 필요한 데이터가 없는 경우
     */
    public boolean isNeedToCleanUpAndReSaveLineup(FixtureSingleResponse response) {
        ResponseValues responseValues = ResponseValues.of(response);
        if(responseValues == null) {
            throw new IllegalArgumentException("FixtureSingleResponse 에서 필요한 데이터를 추출하는데 실패했습니다. " +
                    "API 응답 구조가 예상과 다르거나 FixtureId 및 home/away team 데이터가 API Response 에 존재하지 않습니다.");
        }

        // API 라인업 정보가 없다면 : 다시 저장할 필요가 없습니다
        //  혹시나 불완전한 이전 데이터가 잔존한다 하더라도 라인업 데이터가 없다면 라인업을 다시 저장할 필요가 없습니다.
        boolean notExistLineup = !this.existLineupDataInResponse(response);
        if(notExistLineup) {
            log.info("[Lineup Save Need : X] lineup is not exist in response. fixtureId={}", responseValues.fixtureId);
            return false;
        }

        Fixture fixture = fixtureRepository.findById(responseValues.fixtureId).orElseThrow(
                () -> new IllegalArgumentException("Fixture 데이터가 존재하지 않습니다. fixtureId=" + responseValues.fixtureId)
        );
        Team home = teamRepository.findById(responseValues.homeTeamId).orElseThrow();
        Team away = teamRepository.findById(responseValues.awayTeamId).orElseThrow();

        Optional<MatchLineup> optionalHomeLineup = matchLineupRepository.findTeamLineupByFixture(fixture, home);
        Optional<MatchLineup> optionalAwayLineup = matchLineupRepository.findTeamLineupByFixture(fixture, away);

        // API 라인업 정보가 있고, DB 라인업 데이터는 없다면 : 다시 저장해야 합니다
        if(optionalHomeLineup.isEmpty() || optionalAwayLineup.isEmpty()) {
            log.info("[Lineup Save Need : O] lineup data is not exist in database. fixtureId={}", responseValues.fixtureId);
            return true;
        }

        MatchLineup homeLineup = optionalHomeLineup.get();
        MatchLineup awayLineup = optionalAwayLineup.get();

        // API 라인업 정보가 있고, DB 라인업 데이터는 있는데, 등록|미등록 선수 숫자가 불일치 한다면 : 다시 저장해야 합니다
        return needToReSaveBecauseOfRegisterPlayerCountMismatch(responseValues, homeLineup, awayLineup);
    }

    /**
     * FixtureSingleResponse 를 받아 라인업 데이터를 데이터베이스에 저장합니다.
     * <p>
     * 이 메서드를 호출하기 전에 해당 경기의 기존 라인업 데이터가 클린업되었음을 전제합니다.
     * 클린업에는 Match Lineup 이외의 다른 엔티티 연관관계를 포함하므로 LineupService 외의 다른 계층에서 클린업 메서드를 구현하여 처리해야 합니다.
     * 라인업 데이터에서 새로운 선수를 캐싱하고, 기존 선수의 정보를 업데이트하며,
     * MatchLineup 및 MatchPlayer 엔티티를 저장합니다.
     * 모든 선수가 등록된 선수인 경우 true 를 반환합니다.
     * </p>
     *
     * @param response 라인업 데이터를 포함하는 FixtureSingleResponse
     * @return 라인업이 완전하고 모든 선수가 등록된 경우 true, 그렇지 않으면 false
     * @throws IllegalArgumentException 필요한 데이터가 없거나 경기 또는 팀이 데이터베이스에 없는 경우
     */
    // TODO : unregistered player 에 temporary ID 추가해야함
    public boolean saveLineup(FixtureSingleResponse response) {
        ResponseValues responseValues = ResponseValues.of(response);
        if(responseValues == null) {
            throw new IllegalArgumentException("FixtureSingleResponse 에서 필요한 데이터를 추출하는데 실패했습니다. " +
                    "API 응답 구조가 예상과 다르거나 FixtureId 및 home/away team 데이터가 API Response 에 존재하지 않습니다.");
        }
        if(responseValues.homeLineup == null || responseValues.awayLineup == null) {
            log.warn("라인업 정보가 없는데 saveLineup 을 요청했습니다. fixtureId={}", responseValues.fixtureId);
            return false;
        }
        Fixture fixture = fixtureRepository.findById(responseValues.fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("경기 정보가 아직 캐싱되지 않았습니다."));
        Team homeTeam = teamRepository.findById(responseValues.homeTeamId)
                .orElseThrow(() -> new IllegalArgumentException("홈팀이 아직 캐싱되지 않았습니다."));
        Team awayTeam = teamRepository.findById(responseValues.awayTeamId)
                .orElseThrow(() -> new IllegalArgumentException("어웨이팀이 아직 캐싱되지 않았습니다."));

        if(!matchLineupRepository.findAllByFixture(fixture).isEmpty()) {
            log.warn("fixtureId={} 에 이미 라인업 정보가 존재합니다. cleanup 이후 다시 저장해야 합니다.", responseValues.fixtureId);
            return false;
        }

        // 라인업 정보를 이용해 Player 를 저장 또는 업데이트 합니다.
        // (a) 존재하지 않는 player 를 저장하거나, (b) 존재한다면 number 정보를 업데이트 해줍니다.
        cacheAndUpdateFromLineupPlayers(responseValues.homeLineup);
        cacheAndUpdateFromLineupPlayers(responseValues.awayLineup);

        _Lineups homeLineupData = responseValues.homeLineup;
        _Lineups awayLineupData = responseValues.awayLineup;

        // 1. MatchLineup Entity 생성
        MatchLineup homeLineup = MatchLineup.builder()
                .fixture(fixture)
                .formation(homeLineupData.getFormation())
                .team(homeTeam)
                .build();
        MatchLineup awayLineup = MatchLineup.builder()
                .fixture(fixture)
                .formation(awayLineupData.getFormation())
                .team(awayTeam)
                .build();
        MatchLineup homeMatchLineup = matchLineupRepository.save(homeLineup);
        MatchLineup awayMatchLineup = matchLineupRepository.save(awayLineup);

        // 2. MatchPlayer Entity 생성
        List<MatchPlayer> homeMatchPlayerList = buildAndSaveMatchPlayerEntity(homeLineupData, homeMatchLineup, false);
        List<MatchPlayer> homeSubstitutePlayerList = buildAndSaveMatchPlayerEntity(homeLineupData, homeMatchLineup, true);
        List<MatchPlayer> awayMatchPlayerList = buildAndSaveMatchPlayerEntity(awayLineupData, awayMatchLineup, false);
        List<MatchPlayer> awaySubstitutePlayerList = buildAndSaveMatchPlayerEntity(awayLineupData, awayMatchLineup, true);
        log.info("fixtureId={} 라인업 정보 저장 완료", responseValues.fixtureId);
        log.info("홈팀 라인업 정보 저장 완료. fixtureId={}, teamId={}, startXI.size={}, subs.size={}",
                responseValues.fixtureId, homeTeam.getId(), homeMatchPlayerList.size(), homeSubstitutePlayerList.size());
        log.info("어웨이팀 라인업 정보 저장 완료. fixtureId={}, teamId={}, startXI.size={}, subs.size={}",
                responseValues.fixtureId, awayTeam.getId(), awayMatchPlayerList.size(), awaySubstitutePlayerList.size());

        return isAllRegisteredPlayers(new ResponseValues(response));
    }

    /**
     * 기존 라인업과 새로운 라인업 데이터를 비교하여 등록/미등록 선수의 수가 불일치 하다면 라인업 재저장을 진행해야 합니다.
     * <p>
     * 기존 라인업 데이터의 등록/미등록 선수 수를 API 응답의 등록/미등록 선수 수와 비교합니다.
     * 홈팀이나 원정팀 중 하나라도 선수 수에 불일치가 있으면 true 를 반환하여 라인업을 다시 저장해야 함을 나타냅니다.
     * </p>
     *
     * @param responseValues API 응답에서 추출한 값들
     * @param homeLineup 데이터베이스에 저장된 홈팀의 MatchLineup
     * @param awayLineup 데이터베이스에 저장된 원정팀의 MatchLineup
     * @return 선수 수 불일치로 재저장이 필요한 경우 true, 그렇지 않으면 false
     */
    private boolean needToReSaveBecauseOfRegisterPlayerCountMismatch(ResponseValues responseValues, MatchLineup homeLineup, MatchLineup awayLineup) {
        PlayerCount homePlayerCount = countPlayers(homeLineup);
        PlayerCount awayPlayerCount = countPlayers(awayLineup);

        boolean needToResaveHome = isPlayerCountMismatch(
                responseValues.homeTeamLineupPlayerIds.size(),
                responseValues.homeUnregisteredPlayers.size(),
                homePlayerCount
        );
        boolean needToResaveAway = isPlayerCountMismatch(
                responseValues.awayTeamLineupPlayerIds.size(),
                responseValues.awayUnregisteredPlayers.size(),
                awayPlayerCount
        );

        return needToResaveHome || needToResaveAway;
    }

    /**
     * 라인업의 모든 선수가 등록된 선수인지 확인합니다.
     * <p>
     * 홈팀과 원정팀의 라인업에 미등록 선수가 있는지 검사합니다.
     * 미등록 선수가 없으면 true 를 반환합니다.
     * </p>
     *
     * @param responseValues API 응답에서 추출한 값들
     * @return 모든 선수가 등록된 선수이면 true, 그렇지 않으면 false
     */
    protected boolean isAllRegisteredPlayers(ResponseValues responseValues) {
        log.info("DEV isAllRegistered? : unregis home={} away={}", responseValues.homeUnregisteredPlayers, responseValues.awayUnregisteredPlayers);
        return responseValues.homeUnregisteredPlayers.isEmpty() && responseValues.awayUnregisteredPlayers.isEmpty();
    }

    /**
     * 라인업 데이터에서 선수 정보를 캐싱하고 업데이트합니다.
     * <p>
     * 라인업에 포함된 선수를 처리하여 데이터베이스에 없는 새로운 선수를 캐싱하고,
     * 기존 선수의 번호 정보를 업데이트합니다.
     * ID가 null 인 미등록 선수는 이 과정에서 무시됩니다.
     * </p>
     *
     * @param lineupResponse 라인업 데이터
     */
    @Transactional(propagation = Propagation.REQUIRED)
    protected void cacheAndUpdateFromLineupPlayers(_Lineups lineupResponse) {
        List<_Lineups._StartPlayer> startXI = lineupResponse.getStartXI();
        List<_Lineups._StartPlayer> substitutes = lineupResponse.getSubstitutes();

        // id 가 null 인 선수는 무시하고 넘어갑니다. id 가 null 인 선수는 Player 로 저장되지 않고 MatchPlayer 에만 저장됩니다.
        Map<Long, _Lineups._StartPlayer> playerResponseMap = new HashMap<>();
        playerResponseMap.putAll(startXI.stream()
                .filter(player -> player.getPlayer().getId() != null)
                .collect(Collectors.toMap(player -> player.getPlayer().getId(), player -> player)));
        playerResponseMap.putAll(substitutes.stream()
                .filter(player -> player.getPlayer().getId() != null)
                .collect(Collectors.toMap(player -> player.getPlayer().getId(), player -> player)));

        Set<Long> playerIds = playerResponseMap.keySet();
        Set<Long> findPlayers = playerRepository.findAllById(playerIds).stream()
                .map(Player::getId)
                .collect(Collectors.toSet());

        Set<Long> existPlayerIds = playerIds.stream()
                .filter(findPlayers::contains)
                .collect(Collectors.toSet());
        updateExistPlayers(playerResponseMap, existPlayerIds);

        Set<Long> missingPlayerIds = playerIds.stream()
                .filter(playerId -> !findPlayers.contains(playerId))
                .collect(Collectors.toSet());
        if(missingPlayerIds.isEmpty()) {
            return;
        }
        List<_Lineups._StartPlayer> missingPlayers = playerResponseMap.entrySet().stream()
                .filter(entry -> missingPlayerIds.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
        convertAndCacheMissingPlayers(missingPlayers);
        log.info("라인업 선수 중 아직 캐싱되지 않은 선수를 저장했습니다. missingPlayers={}", missingPlayerIds);
    }

    /**
     * 기존 선수의 유니폼 번호를 라인업 데이터 기반으로 업데이트합니다.
     * <p>
     * 데이터베이스에 저장된 번호와 라인업에서 제공된 번호가 다른 경우 선수의 번호를 업데이트합니다.
     * </p>
     *
     * @param playerResponseMap 라인업에서의 선수 ID와 선수 데이터 매핑
     * @param existPlayerIds 데이터베이스에 존재하는 선수 ID 집합
     */
    private void updateExistPlayers(Map<Long, _Lineups._StartPlayer> playerResponseMap, Set<Long> existPlayerIds) {
        List<Player> existPlayers = playerRepository.findAllById(existPlayerIds);
        existPlayers.forEach(player -> {
            _Lineups._StartPlayer playerResponse = playerResponseMap.get(player.getId());
            if (playerResponse.getPlayer().getNumber() != null && !Objects.equals(player.getNumber(), playerResponse.getPlayer().getNumber())) {
                player.setNumber(playerResponse.getPlayer().getNumber());
                log.info("선수 번호 정보를 업데이트 했습니다. player={}", player);
            }
        });
        playerRepository.saveAll(existPlayers);
    }

    /**
     * 라인업 데이터에서 누락된 선수를 Player 엔티티로 변환하여 캐싱합니다.
     * <p>
     * 아직 데이터베이스에 저장되지 않은 라인업의 선수들을 Player 엔티티로 생성하고 저장합니다.
     * </p>
     *
     * @param missingPlayers 데이터베이스에 캐싱되지 않은 라인업 선수 목록
     */
    private void convertAndCacheMissingPlayers(List<_Lineups._StartPlayer> missingPlayers) {
        final String photoUrl_prefix = "https://media.api-sports.io/football/players/";
        final String photoUrl_suffix = ".png";
        List<Player> players = missingPlayers.stream()
                .map(playerResponse -> Player.builder()
                        .id(playerResponse.getPlayer().getId())
                        .name(playerResponse.getPlayer().getName())
                        .number(playerResponse.getPlayer().getNumber())
                        .photoUrl(photoUrl_prefix + playerResponse.getPlayer().getId() + photoUrl_suffix)
                        .build())
                .toList();
        playerRepository.saveAll(players);
        log.info("캐싱되지 않은 선수들을 라인업 데이터로 저장했습니다. players={}", players);
    }

    /**
     * 라인업 데이터를 기반으로 MatchPlayer 엔티티를 생성하고 저장합니다.
     * <p>
     * 선발 또는 교체 선수에 대한 MatchPlayer 엔티티를 생성합니다.
     * ID가 있는 선수는 Player 연관 관계를 설정하고, ID가 null 인 미등록 선수는 unregisteredPlayer 필드를 사용합니다.
     * </p>
     *
     * @param lineups 선수 정보를 포함하는 라인업 데이터
     * @param matchLineup MatchPlayer가 속한 MatchLineup 엔티티
     * @param isSubstitute 교체 선수를 처리하려면 true, 선발 선수를 처리하려면 false
     * @return 저장된 MatchPlayer 엔티티 목록
     */
    private List<MatchPlayer> buildAndSaveMatchPlayerEntity(_Lineups lineups, MatchLineup matchLineup, boolean isSubstitute) {
        List<MatchPlayer> matchPlayerList = new ArrayList<>();

        List<_Lineups._StartPlayer> startPlayers = (isSubstitute ? lineups.getSubstitutes() : lineups.getStartXI());

        Map<Long, _Lineups._Player> playerResponseMap = new HashMap<>();
        List<_Lineups._Player> idNullPlayerList = new ArrayList<>();
        startPlayers.forEach(startPlayer -> {
            _Lineups._Player player = startPlayer.getPlayer();
            if (player.getId() == null) {
                idNullPlayerList.add(player);
            } else {
                playerResponseMap.put(player.getId(), player);
            }
        });

        // id 존재하는 선수들은 playerRepository 에서 찾아서 저장
        List<Player> findPlayers = playerRepository.findAllById(playerResponseMap.keySet());
        findPlayers.forEach(playerEntity -> {
            _Lineups._Player playerResponse = playerResponseMap.get(playerEntity.getId());
            MatchPlayer matchPlayer = MatchPlayer.builder()
                    .matchLineup(matchLineup)
                    .player(playerEntity)
                    .position(playerResponse.getPos())
                    .grid(playerResponse.getGrid())
                    .substitute(isSubstitute)
                    .build();
            matchPlayerList.add(matchPlayer);
        });

        // id 가 null 인 선수들은 playerRepository 에 저장되지 않고 MatchPlayer 에만 저장
        idNullPlayerList.forEach(responsePlayer -> {
            MatchPlayer matchPlayer = MatchPlayer.builder()
                    .matchLineup(matchLineup)
                    .unregisteredPlayerName(responsePlayer.getName())
                    .unregisteredPlayerNumber(responsePlayer.getNumber())
                    .temporaryId(UUID.randomUUID())
                    .position(responsePlayer.getPos())
                    .grid(responsePlayer.getGrid())
                    .substitute(isSubstitute)
                    .build();
            matchPlayerList.add(matchPlayer);
        });

        return matchPlayerRepository.saveAll(matchPlayerList);
    }

    /**
     * 주어진 MatchLineup 에서 등록된 선수와 미등록 선수의 수를 계산합니다.
     *
     * @param lineup MatchLineup 엔티티
     * @return 등록 선수와 미등록 선수 수를 포함하는 PlayerCount 객체
     */
    private PlayerCount countPlayers(MatchLineup lineup) {
        int registered = 0;
        int unregistered = 0;
        if(lineup == null || lineup.getMatchPlayers() == null) {
            return new PlayerCount(registered, unregistered);
        }

        for (MatchPlayer matchPlayer : lineup.getMatchPlayers()) {
            if (matchPlayer.getPlayer() == null) {
                unregistered++;
            } else {
                registered++;
            }
        }
        return new PlayerCount(registered, unregistered);
    }

    /**
     * API 데이터와 DB 에 저장된 라인업 간의 등록/미등록 선수 수 불일치가 있는지 확인합니다.
     * <p>
     * 등록 선수와 미등록 선수의 예상 수와 실제 수를 비교하여 불일치가 있으면 true 를 반환합니다.
     * </p>
     *
     * @param expectedRegistered 예상되는 등록 선수 수
     * @param expectedUnregistered 예상되는 미등록 선수 수
     * @param actualCount 실제 등록 선수와 미등록 선수 수
     * @return 불일치가 있으면 true, 그렇지 않으면 false
     */
    private boolean isPlayerCountMismatch(int expectedRegistered, int expectedUnregistered, PlayerCount actualCount) {
        boolean isMismatch = actualCount.registered != expectedRegistered || actualCount.unregistered != expectedUnregistered;
        if(isMismatch) {
            log.info("[Lineup Save Need : O] 라인업 데이터의 등록/미등록 선수 수가 불일치합니다. expectedRegistered={}, expectedUnregistered={}, actualRegistered={}, actualUnregistered={}",
                    expectedRegistered, expectedUnregistered, actualCount.registered, actualCount.unregistered);
        } else {
            log.info("[Lineup Save Need : X] 라인업 데이터의 등록/미등록 선수 수가 일치합니다. expectedRegistered={}, expectedUnregistered={}, actualRegistered={}, actualUnregistered={}",
                    expectedRegistered, expectedUnregistered, actualCount.registered, actualCount.unregistered);
        }
        return isMismatch;
    }

    private static class PlayerCount {
        int registered;
        int unregistered;

        PlayerCount(int registered, int unregistered) {
            this.registered = registered;
            this.unregistered = unregistered;
        }
    }

    /**
     * FixtureSingleResponse 에서 필요한 데이터를 추출하여 간략하고 명료하게 값에 접근할 수 있도록 합니다.
     */
    protected static class ResponseValues {
        private final long fixtureId;
        private final long homeTeamId;
        private final long awayTeamId;

        private final Set<Long> homeTeamLineupPlayerIds;
        private final Set<Long> awayTeamLineupPlayerIds;
        private final List<_Lineups._StartPlayer> homeUnregisteredPlayers;
        private final List<_Lineups._StartPlayer> awayUnregisteredPlayers;

        @Nullable private final _Lineups homeLineup;
        @Nullable private final _Lineups awayLineup;

        private ResponseValues(FixtureSingleResponse response) {
            try{
                this.fixtureId = response.getResponse().get(0).getFixture().getId();
                this.homeTeamId = response.getResponse().get(0).getTeams().getHome().getId();
                this.awayTeamId = response.getResponse().get(0).getTeams().getAway().getId();
                this.homeTeamLineupPlayerIds = new HashSet<>();
                this.awayTeamLineupPlayerIds = new HashSet<>();
                this.homeUnregisteredPlayers = new ArrayList<>();
                this.awayUnregisteredPlayers = new ArrayList<>();

                _Lineups homeLineup = null;
                _Lineups awayLineup = null;
                for (_Lineups lineup : response.getResponse().get(0).getLineups()) {
                    if(lineup.getTeam().getId() == homeTeamId) {
                        homeLineup = lineup;
                    } else {
                        awayLineup = lineup;
                    }
                }
                if(homeLineup != null && awayLineup != null) {
                    this.homeLineup = homeLineup;
                    this.awayLineup = awayLineup;

                    List<_Lineups._StartPlayer> homeStartXI = homeLineup.getStartXI();
                    List<_Lineups._StartPlayer> homeSubstitutes = homeLineup.getSubstitutes();
                    List<_Lineups._StartPlayer> awayStartXI = awayLineup.getStartXI();
                    List<_Lineups._StartPlayer> awaySubstitutes = awayLineup.getSubstitutes();

                    addPlayerIds(homeStartXI, homeTeamLineupPlayerIds, homeUnregisteredPlayers);
                    addPlayerIds(homeSubstitutes, homeTeamLineupPlayerIds, homeUnregisteredPlayers);
                    addPlayerIds(awayStartXI, awayTeamLineupPlayerIds, awayUnregisteredPlayers);
                    addPlayerIds(awaySubstitutes, awayTeamLineupPlayerIds, awayUnregisteredPlayers);
                } else {
                    this.homeLineup = null;
                    this.awayLineup = null;
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("FixtureSingleResponse 에서 필요한 데이터를 추출하는데 실패했습니다. " +
                        "API 응답 구조가 예상과 다르거나 FixtureId 및 home/away team 데이터가 API Response 에 존재하지 않습니다.", e);
            }
        }

        private void addPlayerIds(List<_Lineups._StartPlayer> playerList, Set<Long> idSet, List<_Lineups._StartPlayer> unregisteredPlayers) {
            for (_Lineups._StartPlayer startPlayer : playerList) {
                if(startPlayer.getPlayer().getId() == null ) {
                    unregisteredPlayers.add(startPlayer);
                } else {
                    idSet.add(startPlayer.getPlayer().getId());
                }
            }
        }

        public static ResponseValues of(FixtureSingleResponse response) {
            if(response == null || response.getResponse() == null || response.getResponse().get(0) == null) {
                return null;
            }
            return new ResponseValues(response);
        }
    }
}
