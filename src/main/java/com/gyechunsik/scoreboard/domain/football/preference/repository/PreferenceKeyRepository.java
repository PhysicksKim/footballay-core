package com.gyechunsik.scoreboard.domain.football.preference.repository;

import com.gyechunsik.scoreboard.domain.football.preference.persistence.PreferenceKey;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreferenceKeyRepository extends JpaRepository<PreferenceKey, Long> {

    boolean existsByKeyhash(String keyhash);

    Optional<PreferenceKey> findByUserId(@NotNull Long userId);

    Optional<PreferenceKey> findByKeyhash(String keyhash);
}
