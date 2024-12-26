package com.gyechunsik.scoreboard.domain.football.preference.util;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Uploader {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.public-domain}")
    private String domain;

    private static final String FILE_PATH_PREFIX = "/chuncity/preference/customphoto/";

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
            return domain + "/" + filePath;
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
