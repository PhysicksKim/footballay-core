package com.footballay.core.domain.football.preference.util;

import org.springframework.web.multipart.MultipartFile;

@Deprecated(since = "2026-07-07")
public class CustomPhotoFileUploaderImpl implements CustomPhotoFileUploader {
    private static final String DISABLED_MESSAGE = "Custom photo file upload is disabled.";

    @Override
    public void downloadFile(String s3Key, String localDownloadPath) {
        throw new UnsupportedOperationException(DISABLED_MESSAGE);
    }

    @Override
    public void uploadFile(MultipartFile multipartFile, String s3Key) {
        throw new UnsupportedOperationException(DISABLED_MESSAGE);
    }

    @Override
    public boolean existsFile(String s3Key) {
        throw new UnsupportedOperationException(DISABLED_MESSAGE);
    }

    @Override
    public void deleteFile(String s3Key) {
        throw new UnsupportedOperationException(DISABLED_MESSAGE);
    }
}
