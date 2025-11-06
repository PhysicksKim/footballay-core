package com.footballay.core.domain.football.preference.persistence;

import com.footballay.core.domain.football.persistence.Player;
import com.footballay.core.entity.BaseDateAuditEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "custom_player_photos", indexes = {@Index(name = "idx_preference_player_active", columnList = "preference_key_id, player_id")})
public class PlayerCustomPhoto extends BaseDateAuditEntity {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlayerCustomPhoto.class);
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
    private boolean isActive;

    @PrePersist
    @PreUpdate
    private void removeFilenameSlash() {
        if (fileName != null) {
            fileName = fileName.replaceAll("/", "");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerCustomPhoto)) return false;
        PlayerCustomPhoto that = (PlayerCustomPhoto) o;
        return id != null && id.equals(that.id);
    }

    public String getPhotoUrl() {
        String fullPath = this.userFilePath.getFullPath();
        String noStartSlashFilename = this.fileName.startsWith("/") ? this.fileName.substring(1) : this.fileName;
        if (fullPath.endsWith("/")) {
            return fullPath + noStartSlashFilename;
        } else {
            return fullPath + "/" + noStartSlashFilename;
        }
    }

    @Override
    public String toString() {
        return "PlayerCustomPhoto{" + "id=" + id + ", preferenceKey.id=" + preferenceKey.getId() + ", player.id=" + player.getId() + ", userFilePath.id=" + userFilePath.getId() + ", fileName=\'" + fileName + '\'' + ", isActive=" + isActive + '}';
    }


    public static class PlayerCustomPhotoBuilder {
        private Long id;
        private PreferenceKey preferenceKey;
        private Player player;
        private UserFilePath userFilePath;
        private String fileName;
        private boolean isActive;

        PlayerCustomPhotoBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public PlayerCustomPhoto.PlayerCustomPhotoBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PlayerCustomPhoto.PlayerCustomPhotoBuilder preferenceKey(final PreferenceKey preferenceKey) {
            this.preferenceKey = preferenceKey;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PlayerCustomPhoto.PlayerCustomPhotoBuilder player(final Player player) {
            this.player = player;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PlayerCustomPhoto.PlayerCustomPhotoBuilder userFilePath(final UserFilePath userFilePath) {
            this.userFilePath = userFilePath;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PlayerCustomPhoto.PlayerCustomPhotoBuilder fileName(final String fileName) {
            this.fileName = fileName;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public PlayerCustomPhoto.PlayerCustomPhotoBuilder isActive(final boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public PlayerCustomPhoto build() {
            return new PlayerCustomPhoto(this.id, this.preferenceKey, this.player, this.userFilePath, this.fileName, this.isActive);
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "PlayerCustomPhoto.PlayerCustomPhotoBuilder(id=" + this.id + ", preferenceKey=" + this.preferenceKey + ", player=" + this.player + ", userFilePath=" + this.userFilePath + ", fileName=" + this.fileName + ", isActive=" + this.isActive + ")";
        }
    }

    public static PlayerCustomPhoto.PlayerCustomPhotoBuilder builder() {
        return new PlayerCustomPhoto.PlayerCustomPhotoBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public PreferenceKey getPreferenceKey() {
        return this.preferenceKey;
    }

    public Player getPlayer() {
        return this.player;
    }

    public UserFilePath getUserFilePath() {
        return this.userFilePath;
    }

    public String getFileName() {
        return this.fileName;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setPreferenceKey(final PreferenceKey preferenceKey) {
        this.preferenceKey = preferenceKey;
    }

    public void setPlayer(final Player player) {
        this.player = player;
    }

    public void setUserFilePath(final UserFilePath userFilePath) {
        this.userFilePath = userFilePath;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public void setActive(final boolean isActive) {
        this.isActive = isActive;
    }

    public PlayerCustomPhoto() {
    }

    protected PlayerCustomPhoto(final Long id, final PreferenceKey preferenceKey, final Player player, final UserFilePath userFilePath, final String fileName, final boolean isActive) {
        this.id = id;
        this.preferenceKey = preferenceKey;
        this.player = player;
        this.userFilePath = userFilePath;
        this.fileName = fileName;
        this.isActive = isActive;
    }
}
