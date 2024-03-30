package com.gyechunsik.scoreboard.domain.football.data.cache.date;

import com.gyechunsik.scoreboard.domain.football.data.cache.date.entity.ApiCache;
import com.gyechunsik.scoreboard.domain.football.data.cache.date.entity.ApiCacheType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public interface ApiCacheDateRepository extends JpaRepository<ApiCache, Long> {

    Optional<ApiCache> findApiCacheByApiCacheTypeAndParametersJson(ApiCacheType type, Map<String, Object> parameters);
}
