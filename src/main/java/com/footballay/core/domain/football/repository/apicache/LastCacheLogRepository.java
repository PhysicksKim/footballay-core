package com.footballay.core.domain.football.repository.apicache;

import com.footballay.core.domain.football.persistence.apicache.ApiCacheType;
import com.footballay.core.domain.football.persistence.apicache.LastCacheLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public interface LastCacheLogRepository extends JpaRepository<LastCacheLog, Long> {

    Optional<LastCacheLog> findLastCacheLogByApiCacheTypeAndParametersJson(ApiCacheType type, Map<String, Object> parameters);
}
