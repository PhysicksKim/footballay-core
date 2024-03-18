package com.gyechunsik.scoreboard.domain.football.data.cache;

import com.gyechunsik.scoreboard.domain.football.data.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.*;
import com.gyechunsik.scoreboard.domain.football.league.League;
import com.gyechunsik.scoreboard.domain.football.league.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.player.entity.Player;
import com.gyechunsik.scoreboard.domain.football.player.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.relations.LeagueTeam;
import com.gyechunsik.scoreboard.domain.football.relations.LeagueTeamRepository;
import com.gyechunsik.scoreboard.domain.football.team.Team;
import com.gyechunsik.scoreboard.domain.football.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gyechunsik.scoreboard.domain.football.data.fetch.response.LeagueInfoResponse.*;
import static com.gyechunsik.scoreboard.domain.football.data.fetch.response.PlayerSquadResponse.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiCacheService {

    private final ApiCallService apiCallService;

    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final LeagueTeamRepository leagueTeamRepository;

    public League cacheLeague(long leagueId) {
        LeagueInfoResponse leagueInfoResponse = apiCallService.leagueInfo(leagueId);
        Response response = leagueInfoResponse.getResponse().get(0);

        League league = null;
        Optional<League> findLeague = leagueRepository.findById(leagueId);
        if (findLeague.isEmpty()) {
            League build = toLeagueEntity(response);
            league = leagueRepository.save(build);
        } else {
            // 이미 캐싱된경우 업데이트 수행
            league = findLeague.get();
            league.setLogo(response.getLeague().getLogo());
            league.setName(response.getLeague().getName());
            league.setCurrentSeason(extractCurrentSeason(response));
        }

        log.info("leagueId: {} is cached", league.getLeagueId());
        log.info("cached league : {}", league);
        return league;
    }

    public void cacheTeamAndCurrentLeagues(long teamId) {
        Team team = null;
        League league = null;
        Optional<Team> findTeam = teamRepository.findById(teamId);
        if (findTeam.isEmpty()) {
            team = cacheSingleTeam(teamId);
            log.info("team is empty. cached new team : {}", team);
        } else {
            team = findTeam.get();
        }

        LeagueInfoResponse leagueInfoResponse = apiCallService.teamCurrentLeaguesInfo(teamId);
        for (Response response : leagueInfoResponse.getResponse()) {
            long leagueId = response.getLeague().getId();
            Optional<League> findLeague = leagueRepository.findById(leagueId);

            if (findLeague.isEmpty()) {
                league = leagueRepository.save(toLeagueEntity(response));
            } else {
                league = findLeague.get();
            }

            LeagueTeam leagueTeam = LeagueTeam.builder()
                    .league(league)
                    .team(team)
                    .build();
            LeagueTeam saveLeagueTeam = leagueTeamRepository.save(leagueTeam);
        }
    }

    public Team cacheSingleTeam(long teamId) {
        Optional<Team> findTeam = teamRepository.findById(teamId);
        TeamInfoResponse teamInfoResponse = apiCallService.teamInfo(teamId);
        TeamInfoResponse.TeamResponse teamResponse = teamInfoResponse.getResponse().get(0).getTeam();

        Team build = Team.builder()
                .id(teamResponse.getId())
                .name(teamResponse.getName())
                .korean_name(null)
                .logo(teamResponse.getLogo())
                .build();

        log.info("teamId: {} is cached", build.getId());
        if (findTeam.isEmpty()) {
            Team save = teamRepository.save(build);
            log.info("new team saved :: {}", save);
            return save;
        } else {
            findTeam.get().updateCompare(build);
            log.info("team updated :: {}", findTeam.get());
            return findTeam.get();
        }
    }

    /**
     * <h1>Cases</h1>
     * <pre>
     * 1. api 있고 db 있음 : db에서 api 와 일치하지 않는 값을 업데이트
     * 2. api 있고 db 없음 : 새롭게 db에 값을 넣음
     * 3. api 없고 db 있음 : db 에서 teamId 값을 null 로 지움(연관관계 끊음)
     * </pre>
     */
    public void cacheTeamSquad(long teamId) {
        log.info("cache team squad : {}", teamId);
        Optional<Team> findTeam = teamRepository.findById(teamId);
        Team optionalTeam = null;
        if (findTeam.isEmpty()) {
            log.info("new team squad cache called! id : {}", teamId);
            optionalTeam = cacheSingleTeam(teamId);
        } else {
            optionalTeam = findTeam.get();
        }
        final Team team = optionalTeam;

        PlayerSquadResponse playerSquadResponse = apiCallService.playerSquad(teamId);

        List<PlayerData> apiPlayers = playerSquadResponse.getResponse().get(0).getPlayers();
        Set<Long> apiPlayerIds = apiPlayers.stream()
                .map(PlayerData::getId)
                .collect(Collectors.toSet());

        List<Player> dbPlayers = playerRepository.findAllByTeamId(teamId);
        Set<Long> dbPlayerIds = dbPlayers.stream()
                .map(Player::getId)
                .collect(Collectors.toSet());

        // case 1 & 2 : API의 선수를 DB에 업데이트하거나 추가
        for (PlayerData apiPlayer : apiPlayers) {
            playerRepository.findById(apiPlayer.getId())
                    .map(player -> {
                        // API 데이터로 업데이트
                        log.info("Updating player [{} - {}] with new API data.", player.getId(), player.getName());
                        player.updateFromApiData(apiPlayer);
                        return playerRepository.save(player);
                    })
                    .orElseGet(() -> {
                        // DB에 없는 경우 새로 추가
                        log.info("Adding new player [{} - {}] to DB.", apiPlayer.getId(), apiPlayer.getName());
                        Player newPlayer = new Player(apiPlayer);
                        newPlayer.setTeam(team); // TeamId 설정
                        return playerRepository.save(newPlayer);
                    });
        }

        // case 3 : DB에만 존재하는 선수의 teamId 연관관계 끊기
        dbPlayerIds.removeAll(apiPlayerIds);
        dbPlayers.stream()
                .filter(player -> dbPlayerIds.contains(player.getId()))
                .forEach(player -> {
                    log.info("Player [{},{}] team relationship disconnected", player.getId(), player.getName());
                    player.setTeam(null); // TeamId 연관관계 끊기
                    playerRepository.save(player);
                });
    }

    public void cacheAllCurrentLeagues() {
        LeagueInfoResponse response = apiCallService.allLeagueCurrent();
        List<League> leagues = new ArrayList<>();

        for (LeagueInfoResponse.Response leagueResponse : response.getResponse()) {
            League league = toLeagueEntity(leagueResponse);
            leagues.add(league);
        }

        if (!leagues.isEmpty()) {
            leagueRepository.saveAll(leagues);
        }
    }

    private static League toLeagueEntity(LeagueInfoResponse.Response leagueResponse) {
        LeagueResponse leagueInfo = leagueResponse.getLeague();
        int currentSeason = extractCurrentSeason(leagueResponse);

        return League.builder()
                .leagueId(leagueInfo.getId())
                .name(leagueInfo.getName())
                .korean_name(null)
                .logo(leagueInfo.getLogo())
                .currentSeason(currentSeason)
                .build();
    }

    private static int extractCurrentSeason(Response info) {
        return info.getSeasons().stream()
                .filter(Season::isCurrent)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Current season not found"))
                .getYear();
    }
}
