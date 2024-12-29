package com.gyechunsik.scoreboard.domain.football.preference.util;

import org.springframework.web.multipart.MultipartFile;

public interface S3Uploader {

    void uploadFile(MultipartFile multipartFile, String s3Key);
    boolean existsFile(String s3Key);
    void deleteFile(String s3Key);
    void downloadFile(String s3Key, String localDownloadPath);
}
