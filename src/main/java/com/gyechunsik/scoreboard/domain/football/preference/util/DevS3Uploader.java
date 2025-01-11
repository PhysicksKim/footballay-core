package com.gyechunsik.scoreboard.domain.football.preference.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Profile("!aws")
@Slf4j
@Component
public class DevS3Uploader implements S3Uploader {

    static final List<String> mockFileList = new ArrayList<>();

    @Override
    public void uploadFile(MultipartFile multipartFile, String s3Key) {
        log.info("[DevS3Uploader] Uploading file to local storage. s3Key={}", s3Key);
        mockFileList.add(s3Key);
    }

    @Override
    public boolean existsFile(String s3Key) {
        log.info("[DevS3Uploader] Checking file existence in local storage. s3Key={}", s3Key);
        return mockFileList.contains(s3Key);
    }

    @Override
    public void deleteFile(String s3Key) {
        log.info("[DevS3Uploader] Deleting file in local storage. s3Key={}", s3Key);
        mockFileList.remove(s3Key);
    }

    @Override
    public void downloadFile(String s3Key, String localDownloadPath) {
        log.info("[DevS3Uploader] Downloading file from local storage. s3Key={}, localDownloadPath={}", s3Key, localDownloadPath);
    }
}
