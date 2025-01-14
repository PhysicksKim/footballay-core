package com.gyechunsik.scoreboard.web.admin.football.response;

import java.util.List;

public record PlayerPhotosResponse(
        _Player player,
        List<_Photo> photos
) {
        public record _Player(
                long playerId,
                String name,
                String koreanName,
                String photoUrl,
                String position
        ){}

        public record _Photo(
                long photoId,
                String photoUrl,
                boolean active
        ){}
}
