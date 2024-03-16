package com.gyechunsik.scoreboard.domain.football.data.cache;

import com.gyechunsik.scoreboard.domain.football.data.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.LeagueInfoResponse;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.LeagueInfoResponse.LeagueResponse;
import com.gyechunsik.scoreboard.domain.football.league.League;
import com.gyechunsik.scoreboard.domain.football.league.LeagueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiCacheService {

    private final ApiCallService apiCallService;
    private final LeagueRepository leagueRepository;

    public void cacheLeague(long leagueId) {
        LeagueInfoResponse leagueInfoResponse = apiCallService.leagueInfo(leagueId);
        LeagueResponse leagueResponse = leagueInfoResponse.getResponse().get(0).getLeague();

        League build = League.builder()
                .leagueId(leagueResponse.getId())
                .name(leagueResponse.getName())
                .korean_name(null)
                .logo(leagueResponse.getLogo())
                .build();
        League save = leagueRepository.save(build);

        log.info("leagueId: {} is cached", save.getLeagueId());
        log.info("cached league : {}", save);
    }

    public boolean cacheTeam(long teamId) {
        // teamId 로 info 찾음
        // team 의 leagueId 확인
        // 해당 leagueId 가 캐싱되어 있지 않으면 캐싱하도록 명령
        return false;
    }

}
