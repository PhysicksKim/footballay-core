package com.gyechunsik.scoreboard.domain.football.data.cache;

import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.league.League;
import com.gyechunsik.scoreboard.domain.football.league.LeagueRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Transactional
@SpringBootTest
@ActiveProfiles("api")
class ApiCacheServiceTest {

    @Autowired
    private ApiCacheService apiCacheService;

    @Autowired
    private LeagueRepository leagueRepository;

    @DisplayName("")
    @Test
    void success_leagueCaching() {
        // given
        long eplId = LeagueId.EPL;

        // when
        apiCacheService.cacheLeague(eplId);

        // then
        League findLeague = leagueRepository.findById(eplId)
                .orElseThrow(() ->
                        new RuntimeException("테스트 에러! repository 에 저장된 league 가 없습니다. 캐싱에 실패했습니다.")
                );
        assertThat(findLeague).isNotNull();
        assertThat(findLeague.getLeagueId()).isEqualTo(eplId);
    }
}