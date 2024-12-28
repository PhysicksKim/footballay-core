package com.gyechunsik.scoreboard.domain.football.preference.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Profile("aws")
@Slf4j
@RequiredArgsConstructor
@Service
public class S3UploaderImpl implements S3Uploader {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.public-domain}")
    private String s3domain;

    private static final String FILE_PATH_PREFIX = "/test-upload/";

    /**
     * 로컬 파일을 S3에 업로드
     *
     * @param localFilePath 로컬 파일 경로 (예: "src/main/resources/devdata/test1.png")
     * @param s3Key         S3 상의 업로드될 경로/파일명 (예: "test2.png")
     * @return 업로드된 파일의 전체 URL (예: https://.../test2.png)
     */
    public String uploadFile(String localFilePath, String s3Key) {
        File file = new File(localFilePath);
        if(!file.exists()) {
            throw new IllegalArgumentException("Local file does not exist: " + localFilePath);
        }

        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, s3Key, file);
            amazonS3.putObject(putObjectRequest);

            String fileUrl = s3domain + "/" + s3Key;
            log.info("Uploaded file to S3. URL={}", fileUrl);
            return fileUrl;
        } catch (AmazonS3Exception e) {
            log.error("[S3UploaderImpl] Failed to upload file to S3. error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * S3에 업로드된 파일을 로컬 경로로 다운로드
     *
     * @param s3Key            S3 상의 파일명 (예: "test2.png")
     * @param localDownloadPath 다운로드 받을 로컬 경로 (예: "src/main/resources/devdata/test_downloaded.png")
     */
    public void downloadFile(String s3Key, String localDownloadPath) {
        try (S3Object s3Object = amazonS3.getObject(bucketName, s3Key);
             S3ObjectInputStream inputStream = s3Object.getObjectContent()) {

            // 로컬에 파일 생성
            Path path = Paths.get(localDownloadPath);
            Files.createDirectories(path.getParent()); // 디렉토리 없으면 생성
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

            log.info("Downloaded file from S3. localPath={}", localDownloadPath);

        } catch (AmazonS3Exception e) {
            log.error("[S3UploaderImpl] Failed to download file from S3. error={}", e.getMessage(), e);
            throw e;
        } catch (IOException e) {
            log.error("[S3UploaderImpl] I/O error occurred while downloading file from S3. error={}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // 필요하다면, S3 파일 삭제 등 유틸 메서드도 만들 수 있습니다.
    public void deleteFile(String s3Key) {
        try {
            amazonS3.deleteObject(bucketName, s3Key);
            log.info("Deleted file in S3. s3Key={}", s3Key);
        } catch (AmazonS3Exception e) {
            log.error("[S3UploaderImpl] Failed to delete file in S3. error={}", e.getMessage(), e);
            throw e;
        }
    }

    // -----------------------------------

    /**
     * 파일명 생성: {version}_{timestamp}.png
     *
     * @param version          이미지 버전
     * @param originalFilename 원본 파일명
     * @return 생성된 파일명
     */
    public String generateFileName(int version, String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return version + "_" + timestamp + "." + extension;
    }


    /**
     * 파일 업로드 및 경로 반환
     *
     * @param file     업로드할 파일
     * @param userId   사용자 ID
     * @param playerId 선수 ID
     * @param version  이미지 버전
     * @return 업로드된 파일의 S3 URL
     */
    public String upload(MultipartFile file, Long userId, Long playerId, int version) {
        String fileName = generateFileName(version, file.getOriginalFilename());
        String filePath = FILE_PATH_PREFIX + userId + "/" + playerId + "/" + fileName;

        try {
            amazonS3.putObject(new PutObjectRequest(bucketName, filePath, file.getInputStream(), null));
            return s3domain + "/" + filePath;
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
        }
    }

    /**
     * 파일 확장자 추출
     *
     * @param filename 파일명
     * @return 파일 확장자
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("파일 이름이 유효하지 않습니다.");
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
