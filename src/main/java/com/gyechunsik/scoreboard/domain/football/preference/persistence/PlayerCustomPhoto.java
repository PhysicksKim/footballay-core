package com.gyechunsik.scoreboard.domain.football.preference.persistence;

import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.entity.BaseDateAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "custom_player_photos", indexes = {
        @Index(name = "idx_preference_player_active", columnList = "preference_key_id, player_id, is_active")
})
public class PlayerCustomPhoto extends BaseDateAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preference_key_id", nullable = false)
    private PreferenceKey preferenceKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Player player;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private String domain;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false)
    private boolean isActive;

    public String getPhotoUrl() {
        return this.domain + this.path;
    }
}
