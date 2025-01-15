package com.gyechunsik.scoreboard.web.admin.football.response.mapper;

import com.gyechunsik.scoreboard.domain.football.dto.PlayerDto;
import com.gyechunsik.scoreboard.domain.football.preference.dto.PlayerCustomPhotoDto;
import com.gyechunsik.scoreboard.web.admin.football.response.PlayerPhotosResponse;
import com.gyechunsik.scoreboard.web.admin.football.response.PlayerResponse;
import com.gyechunsik.scoreboard.web.admin.football.response.PreferenceKeyResponse;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FootballPreferenceMapper {

    public static PreferenceKeyResponse toPreferenceKeyResponse(String keyHash) {
        return new PreferenceKeyResponse(keyHash);
    }

    public static PlayerResponse toPlayerDtoWithCustomPhoto(PlayerDto player, @Nullable String customPhotoUrl) {
        return new PlayerResponse(
                player.id(),
                player.name(),
                player.koreanName(),
                customPhotoUrl != null ? customPhotoUrl : player.photoUrl(),
                player.position()
        );
    }

    public static PlayerPhotosResponse toPlayerPhotosResponse(PlayerDto player, List<PlayerCustomPhotoDto> customPhotos) {
        PlayerPhotosResponse._Player _player = new PlayerPhotosResponse._Player(
                player.id(),
                player.name(),
                player.koreanName(),
                player.photoUrl(),
                player.position()
        );

        List<PlayerPhotosResponse._Photo> photos = new ArrayList<>();
        for(PlayerCustomPhotoDto dto : customPhotos) {
            photos.add(new PlayerPhotosResponse._Photo(
                    dto.getId(),
                    dto.getPhotoUrl(),
                    dto.getIsActive()
            ));
        }

        return new PlayerPhotosResponse(_player, photos);
    }

}
