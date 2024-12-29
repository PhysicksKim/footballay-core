package com.gyechunsik.scoreboard.domain.football.preference.util;

public interface CustomPhotoS3Path {
    String pathFrom(long playerId, int version, String extension);
}
