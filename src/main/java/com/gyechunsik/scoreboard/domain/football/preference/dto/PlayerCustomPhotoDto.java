package com.gyechunsik.scoreboard.domain.football.preference.dto;

import com.gyechunsik.scoreboard.domain.football.preference.persistence.PlayerCustomPhoto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerCustomPhotoDto {
    private Long id;
    private Long userId;
    private Long playerId;
    private String photoUrl;
    private Integer version;
    private Boolean isActive;
    private String uploadedAt;
    private String updatedAt;

    /**
     * 엔티티를 DTO로 변환합니다.
     *
     * @param photo PlayerCustomPhoto 엔티티
     * @return PlayerCustomPhotoDto
     */
    public static PlayerCustomPhotoDto fromEntity(PlayerCustomPhoto photo) {
        return new PlayerCustomPhotoDto(
                photo.getId(),
                photo.getPreferenceKey().getUser().getId(),
                photo.getPlayer().getId(),
                photo.getPhotoUrl(),
                photo.getVersion(),
                photo.isActive(),
                photo.getCreatedDate().toString(),
                photo.getModifiedDate().toString()
        );
    }
}