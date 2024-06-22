package com.gyechunsik.scoreboard.domain.football.external.lastlog;

import com.gyechunsik.scoreboard.domain.football.entity.apicache.LastCacheLog;
import com.gyechunsik.scoreboard.domain.football.entity.apicache.ApiCacheType;
import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.constant.TeamId;
import com.gyechunsik.scoreboard.domain.football.repository.apicache.LastCacheLogRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@DataJpaTest
class LastCacheLogRepositoryTest {

    @Autowired
    private LastCacheLogRepository repository;

    @Autowired
    private EntityManager em;

    @DisplayName("LastCacheLog 엔티티를 저장하고 이를 다시 가져옵니다.")
    @Test
    void success_API_CACHE() {
        // given
        ZonedDateTime cachedAt = ZonedDateTime.now();
        LastCacheLog lastCacheLog = LastCacheLog.builder()
                .apiCacheType(ApiCacheType.LEAGUE)
                .parametersJson(Map.of("leagueId", (int)LeagueId.EPL))
                .lastCachedAt(cachedAt)
                .build();

        // when
        LastCacheLog save = repository.save(lastCacheLog);
        log.info("saved api cache :: {}", save);
        em.clear();
        List<LastCacheLog> all = repository.findAll();
        log.info("All Cached LastCacheLog List");
        for (LastCacheLog cache : all) {
            log.info("cache : {}", cache);
        }

        // then
        assertThat(lastCacheLog).isEqualTo(all.get(0));
        assertThat(all.get(0)).isEqualTo(save).isEqualTo(lastCacheLog);
    }

    @DisplayName("여러 LastCacheLog 엔티티를 저장하고, 저장한 엔티티와 각각 찾은 엔티티가 동일해야 합니다.")
    @Test
    void success_multipleApiCacheEntity_And_FindOne() {
        // given
        ZonedDateTime leagueCacheLDT = ZonedDateTime.now();
        LastCacheLog leagueCache = LastCacheLog.builder()
                .apiCacheType(ApiCacheType.LEAGUE)
                .parametersJson(Map.of("leagueId", (int) LeagueId.EPL))
                .lastCachedAt(leagueCacheLDT)
                .build();
        ZonedDateTime teamCacheLDT = ZonedDateTime.now();
        LastCacheLog teamCache = LastCacheLog.builder()
                .apiCacheType(ApiCacheType.TEAM)
                .parametersJson(Map.of("teamId", (int)TeamId.MANCITY))
                .lastCachedAt(teamCacheLDT)
                .build();

        List<LastCacheLog> leagueCacheList = List.of(leagueCache, teamCache);

        // when
        List<LastCacheLog> savedLastCacheLog = repository.saveAll(leagueCacheList);
        em.clear();
        Optional<LastCacheLog> teamFind
                = repository.findLastCacheLogByApiCacheTypeAndParametersJson(ApiCacheType.TEAM, Map.of("teamId", (int) TeamId.MANCITY));
        Optional<LastCacheLog> leagueFind
                = repository.findLastCacheLogByApiCacheTypeAndParametersJson(ApiCacheType.LEAGUE, Map.of("leagueId", (int) LeagueId.EPL));
        List<LastCacheLog> findAll = repository.findAll();

        // then
        log.info("Cache League :: {}", leagueCache);
        log.info("Cache Team :: {}", teamCache);
        log.info("Find League :: {}", leagueFind);
        log.info("Find Team :: {}", teamFind);

        assertThat(savedLastCacheLog).hasSize(2);
        assertThat(savedLastCacheLog).containsAll(findAll);

        assertThat(leagueFind).isNotEmpty();
        assertThat(leagueFind.get()).isEqualTo(leagueCache);
        assertThat(teamFind).isNotEmpty();
        assertThat(teamFind.get()).isEqualTo(teamCache);

        assertThat(savedLastCacheLog).contains(leagueFind.get(), teamFind.get());
        assertThat(findAll).contains(leagueCache, teamCache);
    }

}