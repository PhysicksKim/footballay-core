package com.gyechunsik.scoreboard.domain.football.preference.repository;

import com.gyechunsik.scoreboard.domain.football.preference.persistence.PreferenceKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreferenceKeyRepository extends JpaRepository<PreferenceKey, Long> {

    boolean existsByKey(String key);

    Optional<PreferenceKey> findByUserIdAndKey(Long userId, String aDefault);

    Optional<PreferenceKey> findByKey(String preferenceKey);
}
