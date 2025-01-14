package com.gyechunsik.scoreboard.domain.football.preference.dto;

import com.gyechunsik.scoreboard.domain.football.preference.persistence.PlayerCustomPhoto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class PlayerCustomPhotoDto {
    private Long id;
    private Long playerId;
    private String photoUrl;
    private Boolean isActive;
    private String uploadedAt;
    private String updatedAt;

    public static PlayerCustomPhotoDto fromEntity(PlayerCustomPhoto photo) {
        return new PlayerCustomPhotoDto(
                photo.getId(),
                photo.getPlayer().getId(),
                photo.getPhotoUrl(),
                photo.isActive(),
                photo.getCreatedDate().toString(),
                photo.getModifiedDate().toString()
        );
    }
}