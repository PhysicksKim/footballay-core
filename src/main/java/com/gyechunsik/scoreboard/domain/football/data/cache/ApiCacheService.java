package com.gyechunsik.scoreboard.domain.football.data.cache;

import com.gyechunsik.scoreboard.domain.football.constant.TeamId;
import com.gyechunsik.scoreboard.domain.football.data.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.LeagueInfoResponse;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.LeagueResponse;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.PlayerSquadResponse;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.TeamInfoResponse;
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
        LeagueResponse leagueResponse = leagueInfoResponse.getResponse().get(0).getLeague();

        League league = null;
        Optional<League> findLeague = leagueRepository.findById(leagueId);
        if(findLeague.isEmpty()) {
            League build = toLeagueEntity(leagueResponse);
            league = leagueRepository.save(build);
        } else { // 이미 캐싱된경우 업데이트 수행
            league = findLeague.get();
            league.setLogo(leagueResponse.getLogo());
            league.setName(leagueResponse.getName());
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
            log.info("team is empty. cached team : {}", team);
        }

        LeagueInfoResponse leagueInfoResponse = apiCallService.teamCurrentLeaguesInfo(teamId);
        for (LeagueInfoResponse.Response response : leagueInfoResponse.getResponse()) {
            long leagueId = response.getLeague().getId();
            Optional<League> findLeague = leagueRepository.findById(leagueId);

            if(findLeague.isEmpty()) {
                league = leagueRepository.save(toLeagueEntity(response.getLeague()));
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

    // TODO : 이미 캐싱된 경우에 대해서 처리 필요
    public Team cacheSingleTeam(long teamId) {
        TeamInfoResponse teamInfoResponse = apiCallService.teamInfo(TeamId.MANCITY);
        TeamInfoResponse.TeamResponse team = teamInfoResponse.getResponse().get(0).getTeam();

        Team build = Team.builder()
                .id(team.getId())
                .name(team.getName())
                .korean_name(null)
                .logo(team.getLogo())
                .build();

        Team save = teamRepository.save(build);

        log.info("teamId: {} is cached", save.getId());
        log.info("cached team : {}", save);
        return save;
    }

    // TODO : 이미 캐싱된 경우에 대해서 처리 필요
    public List<Player> cacheSquad(long teamId) {
        PlayerSquadResponse playerSquadResponse = apiCallService.playerSquad(TeamId.MANCITY);
        List<PlayerSquadResponse.PlayerData> players = playerSquadResponse.getResponse().get(0).getPlayers();

        List<Player> savePlayers = new ArrayList<>();
        for (PlayerSquadResponse.PlayerData player : players) {
            Player build = Player.builder()
                    .id(player.getId())
                    .name(player.getName())
                    .koreanName(null)
                    .position(player.getPosition())
                    .photoUrl(player.getPhoto())
                    .build();
            Player save = playerRepository.save(build);
            savePlayers.add(save);
        }

        log.info("team squad cached", teamId);
        log.info("cached players : {}", savePlayers);
        return savePlayers;
    }

    private static League toLeagueEntity(LeagueResponse leagueResponse) {
        League build = League.builder()
                .leagueId(leagueResponse.getId())
                .name(leagueResponse.getName())
                .korean_name(null)
                .logo(leagueResponse.getLogo())
                .build();
        return build;
    }
}
