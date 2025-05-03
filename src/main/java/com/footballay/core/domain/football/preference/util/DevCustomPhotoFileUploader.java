package com.footballay.core.domain.football.preference.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Profile("!aws")
@Slf4j
@Component
public class DevCustomPhotoFileUploader implements CustomPhotoFileUploader {

    static final List<String> mockFileList = new ArrayList<>();

    @Override
    public void uploadFile(MultipartFile multipartFile, String s3Key) {
        log.info("[DevCustomPhotoFileUploader] Uploading file to local storage. s3Key={}", s3Key);
        mockFileList.add(s3Key);
    }

    @Override
    public boolean existsFile(String s3Key) {
        log.info("[DevCustomPhotoFileUploader] Checking file existence in local storage. s3Key={}", s3Key);
        return mockFileList.contains(s3Key);
    }

    @Override
    public void deleteFile(String s3Key) {
        log.info("[DevCustomPhotoFileUploader] Deleting file in local storage. s3Key={}", s3Key);
        mockFileList.remove(s3Key);
    }

    @Override
    public void downloadFile(String s3Key, String localDownloadPath) {
        log.info("[DevCustomPhotoFileUploader] Downloading file from local storage. s3Key={}, localDownloadPath={}", s3Key, localDownloadPath);
    }
}
