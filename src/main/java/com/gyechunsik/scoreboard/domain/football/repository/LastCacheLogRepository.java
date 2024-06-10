package com.gyechunsik.scoreboard.domain.football.repository;

import com.gyechunsik.scoreboard.domain.football.entity.apicache.LastCacheLog;
import com.gyechunsik.scoreboard.domain.football.entity.apicache.ApiCacheType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public interface LastCacheLogRepository extends JpaRepository<LastCacheLog, Long> {

    Optional<LastCacheLog> findLastCacheLogByApiCacheTypeAndParametersJson(ApiCacheType type, Map<String, Object> parameters);
}
