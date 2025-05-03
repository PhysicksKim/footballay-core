package com.footballay.core.domain.football.preference.repository;

import com.footballay.core.domain.football.preference.persistence.PlayerCustomPhoto;
import com.footballay.core.domain.football.preference.persistence.PreferenceKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * `idx_preference_player_active` 인덱스가 동작하도록 하기 위해서 preferenceKey, playerId 순서대로 조건절을 작성하세요.
 */
@Repository
public interface PlayerCustomPhotoRepository extends JpaRepository<PlayerCustomPhoto, Long> {

    @Query("SELECT pho FROM PlayerCustomPhoto pho " +
            "JOIN FETCH pho.player pl " +
            "WHERE pho.preferenceKey.id = :preferenceKeyId " +
            "AND pho.player.id IN :playerIds AND pho.isActive = true")
    List<PlayerCustomPhoto> findAllActivesByPreferenceKeyAndPlayers(
            @Param("preferenceKeyId") Long preferenceKeyId,
            @Param("playerIds") Set<Long> playerIds
    );

    @Query("SELECT p FROM PlayerCustomPhoto p " +
            "WHERE p.preferenceKey = :preferenceKey " +
            "AND p.player.id = :playerId " +
            "AND p.isActive = true")
    Optional<PlayerCustomPhoto> findActivePhotoByPreferenceKeyAndPlayer(
            @Param("preferenceKey") PreferenceKey preferenceKey,
            @Param("playerId") Long playerId
    );

    /**
     * 활성 및 비활성 이미지들을 모두 가져 옵니다.
     * @param preferenceKey
     * @param playerId
     * @return
     */
    @Query("SELECT p FROM PlayerCustomPhoto p " +
            "WHERE p.preferenceKey = :preferenceKey " +
            "AND p.player.id = :playerId")
    List<PlayerCustomPhoto> findAllByPreferenceKeyAndPlayer(
            @Param("preferenceKey") PreferenceKey preferenceKey,
            @Param("playerId") Long playerId
    );

    void deleteByIdAndPreferenceKey(long photoId, PreferenceKey key);
}
