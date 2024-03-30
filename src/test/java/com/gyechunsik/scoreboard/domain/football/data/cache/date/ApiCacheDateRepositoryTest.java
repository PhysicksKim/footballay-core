package com.gyechunsik.scoreboard.domain.football.data.cache.date;

import com.gyechunsik.scoreboard.domain.football.data.cache.date.entity.ApiCache;
import com.gyechunsik.scoreboard.domain.football.data.cache.date.entity.ApiCacheType;
import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.constant.TeamId;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@DataJpaTest
class ApiCacheDateRepositoryTest {

    @Autowired
    private ApiCacheDateRepository repository;

    @Autowired
    private EntityManager em;

    @DisplayName("ApiCache 엔티티를 저장하고 이를 다시 가져옵니다.")
    @Test
    void success_API_CACHE() {
        // given
        LocalDateTime cachedAt = LocalDateTime.now();
        ApiCache apiCache = ApiCache.builder()
                .apiCacheType(ApiCacheType.LEAGUE)
                .parametersJson(Map.of("leagueId", (int)LeagueId.EPL))
                .lastCachedAt(cachedAt)
                .build();

        // when
        ApiCache save = repository.save(apiCache);
        log.info("saved api cache :: {}", save);
        em.clear();
        List<ApiCache> all = repository.findAll();
        log.info("All Cached ApiCache List");
        for (ApiCache cache : all) {
            log.info("cache : {}", cache);
        }

        // then
        assertThat(apiCache).isEqualTo(all.get(0));
        assertThat(all.get(0)).isEqualTo(save).isEqualTo(apiCache);
    }

    @DisplayName("여러 ApiCache 엔티티를 저장하고, 저장한 엔티티와 각각 찾은 엔티티가 동일해야 합니다.")
    @Test
    void success_multipleApiCacheEntity_And_FindOne() {
        // given
        LocalDateTime leagueCacheLDT = LocalDateTime.now();
        ApiCache leagueCache = ApiCache.builder()
                .apiCacheType(ApiCacheType.LEAGUE)
                .parametersJson(Map.of("leagueId", (int) LeagueId.EPL))
                .lastCachedAt(leagueCacheLDT)
                .build();
        LocalDateTime teamCacheLDT = LocalDateTime.now();
        ApiCache teamCache = ApiCache.builder()
                .apiCacheType(ApiCacheType.TEAM)
                .parametersJson(Map.of("teamId", (int)TeamId.MANCITY))
                .lastCachedAt(teamCacheLDT)
                .build();

        List<ApiCache> leagueCacheList = List.of(leagueCache, teamCache);

        // when
        List<ApiCache> savedApiCache = repository.saveAll(leagueCacheList);
        em.clear();
        Optional<ApiCache> leagueFind
                = repository.findApiCacheByApiCacheTypeAndParametersJson(ApiCacheType.LEAGUE, Map.of("leagueId", (int) LeagueId.EPL));
        Optional<ApiCache> teamFind
                = repository.findApiCacheByApiCacheTypeAndParametersJson(ApiCacheType.TEAM, Map.of("teamId", (int) TeamId.MANCITY));
        List<ApiCache> findAll = repository.findAll();

        // then
        log.info("Cache League :: {}", leagueCache);
        log.info("Cache Team :: {}", teamCache);
        log.info("Find League :: {}", leagueFind);
        log.info("Find Team :: {}", teamFind);

        assertThat(savedApiCache).hasSize(2);
        assertThat(savedApiCache).containsAll(findAll);

        assertThat(leagueFind).isNotEmpty();
        assertThat(leagueFind.get()).isEqualTo(leagueCache);
        assertThat(teamFind).isNotEmpty();
        assertThat(teamFind.get()).isEqualTo(teamCache);

        assertThat(savedApiCache).contains(leagueFind.get(), teamFind.get());
        assertThat(findAll).contains(leagueCache, teamCache);
    }

}