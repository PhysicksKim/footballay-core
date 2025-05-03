package com.footballay.core.domain.football.preference.repository;

import com.footballay.core.domain.football.preference.persistence.PreferenceKey;
import com.footballay.core.domain.user.entity.User;
import com.footballay.core.entity.IncludeDeleted;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PreferenceKeyRepository extends JpaRepository<PreferenceKey, Long> {

    boolean existsByKeyhash(String keyhash);

    Optional<PreferenceKey> findByUserId(@NotNull Long userId);

    Optional<PreferenceKey> findByKeyhash(String keyhash);

    Optional<PreferenceKey> findByUser(User user);

    /**
     * Soft Delete 된 PreferenceKey를 포함하여 조회
     * @param id 조회할 PreferenceKey의 ID
     * @return 조회된 PreferenceKey
     */
    @IncludeDeleted
    @Query("SELECT p FROM PreferenceKey p WHERE p.id = :id")
    Optional<PreferenceKey> findByIdIncludingDeleted(@Param("id") Long id);

    /**
     * Soft Delete 된 모든 PreferenceKey 조회
     * @return Soft Delete 된 모든 PreferenceKey
     */
    @IncludeDeleted
    @Query("SELECT p FROM PreferenceKey p WHERE p.enabled = false")
    List<PreferenceKey> findAllDeleted();
}
