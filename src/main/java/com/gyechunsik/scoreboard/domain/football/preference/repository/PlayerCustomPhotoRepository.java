package com.gyechunsik.scoreboard.domain.football.preference.repository;

import com.gyechunsik.scoreboard.domain.football.preference.persistence.PlayerCustomPhoto;
import com.gyechunsik.scoreboard.domain.football.preference.persistence.UserFilePath;
import com.gyechunsik.scoreboard.domain.football.preference.persistence.UserPathCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PlayerCustomPhotoRepository extends JpaRepository<PlayerCustomPhoto, Long> {

    @Query("SELECT pho FROM PlayerCustomPhoto pho " +
            "JOIN FETCH pho.player pl " +
            "WHERE pho.preferenceKey.id = :preferenceKeyId " +
            "AND pl.id IN :playerIds AND pho.isActive = true")
    List<PlayerCustomPhoto> findActivePhotosByPreferenceKeyAndPlayers(
            @Param("preferenceKeyId") Long preferenceKeyId,
            @Param("playerIds") Set<Long> playerIds
    );

    @Query("SELECT p FROM PlayerCustomPhoto p " +
            "WHERE p.preferenceKey.id = :preferenceKeyId " +
            "AND p.player.id = :playerId " +
            "ORDER BY p.version DESC " +
            "LIMIT 1")
    Optional<PlayerCustomPhoto> findLatestPhotoByPreferenceKeyAndPlayer(
            @Param("preferenceKeyId") Long preferenceKeyId,
            @Param("playerId") Long playerId
    );

    @Query("SELECT p FROM PlayerCustomPhoto p " +
            "WHERE p.preferenceKey.id = :preferenceKeyId " +
            "AND p.player.id = :playerId " +
            "AND p.isActive = true")
    List<PlayerCustomPhoto> findActivePhotosByPreferenceKeyAndPlayer(
            @Param("preferenceKeyId") Long preferenceKeyId,
            @Param("playerId") Long playerId
    );

    @Query("SELECT p FROM PlayerCustomPhoto p " +
            "WHERE p.preferenceKey.id = (SELECT pk.id FROM PreferenceKey pk WHERE pk.keyhash = :keyhash) " +
            "AND p.player.id = :playerId " +
            "AND p.isActive = true")
    List<PlayerCustomPhoto> findPlayerCustomPhotosByPreferenceKeyAndPlayer(
            @Param("keyhash") String keyhash,
            @Param("playerId") Long playerId
    );

}
