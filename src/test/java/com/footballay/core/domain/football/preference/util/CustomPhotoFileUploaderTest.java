package com.footballay.core.domain.football.preference.util;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Disabled("실제 AWS S3에 직접 업로드/다운로드를 수행하므로, 필요 시 활성화하여 테스트하세요.")
@ActiveProfiles("aws")
@SpringBootTest
class CustomPhotoFileUploaderTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomPhotoFileUploaderTest.class);
    @Autowired
    private CustomPhotoFileUploader customPhotoFileUploader;
    private static final String LOCAL_FILE_PATH = "src/main/resources/devdata/test1.png";
    private static final String S3_KEY = "test-upload/test2.png";
    private static final String DOWNLOAD_PATH = "src/main/resources/devdata/test-download/test2.png";

    @BeforeAll
    static void checkTestFileExists() {
        File file = new File(LOCAL_FILE_PATH);
        if (!file.exists()) {
            throw new IllegalArgumentException("[TEST] Local file does not exist: " + file.getAbsolutePath());
        }
    }

    @Test
    void testUploadAndDownloadFile() throws Exception {
        File file = new File(LOCAL_FILE_PATH);
        FileInputStream fis = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "image/png", fis);
        // 1) S3 업로드
        customPhotoFileUploader.uploadFile(multipartFile, S3_KEY);
        log.info("[TEST] File uploaded to S3");
        // 2) S3에서 로컬로 다운로드
        customPhotoFileUploader.downloadFile(S3_KEY, DOWNLOAD_PATH);
        Path downloadedPath = Paths.get(DOWNLOAD_PATH);
        // 3) 다운로드 성공 여부 검증 (파일 존재 확인)
        Assertions.assertTrue(Files.exists(downloadedPath), "[TEST] Downloaded file should exist at: " + DOWNLOAD_PATH);
        // 필요하다면, MD5 체크나 파일 크기 비교 등을 통해 검증할 수도 있습니다.
        // 예: Assertions.assertEquals(Files.size(Paths.get(LOCAL_FILE_PATH)), Files.size(downloadedPath));
        log.info("[TEST] File downloaded from S3 successfully. localPath={}", DOWNLOAD_PATH);
        // 4) 정리(테스트 후 파일 삭제 등)
        // (필요에 따라 다운로드 받은 파일을 삭제하고 싶다면 아래 코드 활성화)
        // Files.deleteIfExists(downloadedPath);
    }

    @Test
    void testDeleteFile() {
        customPhotoFileUploader.deleteFile(S3_KEY);
        log.info("[TEST] File deleted in S3. key={}", S3_KEY);
        // 삭제를 확인하는 확실한 방법은, 다시 getObject(...)를 시도하여
        // AmazonS3Exception(404) 발생 여부를 체크하는 것입니다.
        Assertions.assertThrows(AmazonS3Exception.class, () -> {
            customPhotoFileUploader.downloadFile(S3_KEY, DOWNLOAD_PATH);
        });
    }
}
