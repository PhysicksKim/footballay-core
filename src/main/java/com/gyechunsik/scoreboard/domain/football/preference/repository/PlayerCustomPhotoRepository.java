package com.gyechunsik.scoreboard.domain.football.preference.repository;

import com.gyechunsik.scoreboard.domain.football.preference.persistence.PlayerCustomPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PlayerCustomPhotoRepository extends JpaRepository<PlayerCustomPhoto, Long> {

    @Query("SELECT p FROM PlayerCustomPhoto p WHERE p.preferenceKey.id = :preferenceKeyId AND p.player.id IN :playerIds AND p.isActive = true")
    List<PlayerCustomPhoto> findActivePhotosByPreferenceKeyAndPlayers(
            @Param("preferenceKeyId") Long preferenceKeyId,
            @Param("playerIds") Set<Long> playerIds
    );
}
