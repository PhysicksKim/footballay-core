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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_file_path_id", nullable = true)
    private UserFilePath userFilePath;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false)
    private boolean isActive;

    @PrePersist
    @PreUpdate
    private void removeFilenameSlash() {
        if (fileName != null) {
            fileName = fileName.replaceAll("/", "");
        }
    }

    public String getPhotoUrl() {
        String fullPath = this.userFilePath.getFullPath();
        String noStartSlashFilename = this.fileName.startsWith("/") ? this.fileName.substring(1) : this.fileName;
        if(fullPath.endsWith("/")) {
            return fullPath + noStartSlashFilename;
        } else {
            return fullPath + "/" + noStartSlashFilename;
        }
    }

}
