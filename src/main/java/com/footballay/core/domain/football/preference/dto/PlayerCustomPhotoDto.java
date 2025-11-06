package com.footballay.core.domain.football.preference.dto;

import com.footballay.core.domain.football.preference.persistence.PlayerCustomPhoto;

public class PlayerCustomPhotoDto {
    private Long id;
    private Long playerId;
    private String photoUrl;
    private Boolean isActive;
    private String uploadedAt;
    private String updatedAt;

    public static PlayerCustomPhotoDto fromEntity(PlayerCustomPhoto photo) {
        return new PlayerCustomPhotoDto(photo.getId(), photo.getPlayer().getId(), photo.getPhotoUrl(), photo.isActive(), photo.getCreatedDate().toString(), photo.getModifiedDate().toString());
    }

    public Long getId() {
        return this.id;
    }

    public Long getPlayerId() {
        return this.playerId;
    }

    public String getPhotoUrl() {
        return this.photoUrl;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public String getUploadedAt() {
        return this.uploadedAt;
    }

    public String getUpdatedAt() {
        return this.updatedAt;
    }

    public PlayerCustomPhotoDto(final Long id, final Long playerId, final String photoUrl, final Boolean isActive, final String uploadedAt, final String updatedAt) {
        this.id = id;
        this.playerId = playerId;
        this.photoUrl = photoUrl;
        this.isActive = isActive;
        this.uploadedAt = uploadedAt;
        this.updatedAt = updatedAt;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "PlayerCustomPhotoDto(id=" + this.getId() + ", playerId=" + this.getPlayerId() + ", photoUrl=" + this.getPhotoUrl() + ", isActive=" + this.getIsActive() + ", uploadedAt=" + this.getUploadedAt() + ", updatedAt=" + this.getUpdatedAt() + ")";
    }
}
