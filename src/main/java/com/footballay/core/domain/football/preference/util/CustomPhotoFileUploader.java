package com.footballay.core.domain.football.preference.util;

import org.springframework.web.multipart.MultipartFile;

public interface CustomPhotoFileUploader {

    /**
     * Custom Image MultipartFile 을 파일 저장소에 업로드합니다. <br>
     * 업로드 전에 파일을 검증해야 합니다.
     *
     * @param multipartFile
     * @param s3Key
     */
    void uploadFile(MultipartFile multipartFile, String s3Key);
    boolean existsFile(String s3Key);
    void deleteFile(String s3Key);
    void downloadFile(String s3Key, String localDownloadPath);
}
