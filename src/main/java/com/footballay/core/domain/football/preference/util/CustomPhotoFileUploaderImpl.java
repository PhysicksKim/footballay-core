package com.footballay.core.domain.football.preference.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Profile("aws")
@Slf4j
@RequiredArgsConstructor
@Component
public class CustomPhotoFileUploaderImpl implements CustomPhotoFileUploader {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.public-domain}")
    private String s3domain;

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
            log.error("[CustomPhotoFileUploaderImpl] Failed to download file from S3. error={}", e.getMessage(), e);
            throw e;
        } catch (IOException e) {
            log.error("[CustomPhotoFileUploaderImpl] I/O error occurred while downloading file from S3. error={}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * MultipartFile을 S3에 업로드
     * @param multipartFile 업로드할 파일
     * @param s3Key S3 상의 업로드될 경로/파일명 (예: "test2.png")
     */
    public void uploadFile(MultipartFile multipartFile, String s3Key) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(multipartFile.getSize());
            metadata.setContentType(multipartFile.getContentType());

            log.info("Uploading file to S3. s3Key={}", s3Key);
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    s3Key,
                    multipartFile.getInputStream(),
                    metadata
            );
            amazonS3.putObject(putObjectRequest);
        } catch (IOException e) {
            throw new RuntimeException("fail S3 file upload", e);
        }
    }

    /**
     * 해당 key에 해당하는 파일이 S3 버킷에 존재하는지 여부를 확인합니다.
     *
     * @param s3Key S3 상의 전체 경로(예: "test-upload/test2.png")
     * @return true: 존재함 / false: 존재하지 않음
     */
    public boolean existsFile(String s3Key) {
        try {
            return amazonS3.doesObjectExist(bucketName, s3Key);
        } catch (AmazonS3Exception e) {
            log.error("[CustomPhotoFileUploader] Failed to check object existence in S3. error={}", e.getMessage(), e);
            throw e;
        }
    }

    public void deleteFile(String s3Key) {
        try {
            amazonS3.deleteObject(bucketName, s3Key);
            log.info("Deleted file in S3. s3Key={}", s3Key);
        } catch (AmazonS3Exception e) {
            log.error("[CustomPhotoFileUploaderImpl] Failed to delete file in S3. error={}", e.getMessage(), e);
            throw e;
        }
    }

}
