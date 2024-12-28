package com.gyechunsik.scoreboard.domain.football.preference.util;

public interface S3Uploader {

    String uploadFile(String localFilePath, String s3Key);
    void downloadFile(String s3Key, String localDownloadPath);
    void deleteFile(String s3Key);

}
