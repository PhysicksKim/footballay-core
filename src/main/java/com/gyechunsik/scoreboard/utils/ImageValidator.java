package com.gyechunsik.scoreboard.utils;

import org.apache.tika.Tika;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

@Component
public class ImageValidator {

    private final Tika tika;

    public ImageValidator() {
        this.tika = new Tika();
    }

    /**
     * 파일이 비어있는지 확인
     *
     * @param file 업로드할 파일
     */
    public void validateFileNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }
    }

    /**
     * 파일의 Content Type 이 일치하는지 확인
     *
     * @param file 업로드할 파일
     */
    public void validateContentType(MultipartFile file, String... allowContentTypes) {
        validateFileNotEmpty(file);
        if (allowContentTypes.length == 0) {
            return;
        }

        String contentType = file.getContentType();
        for (String memeType : allowContentTypes) {
            if (memeType.equalsIgnoreCase(contentType)) {
                return;
            }
        }
        throw new IllegalArgumentException("파일의 형식이 일치하지 않습니다. 주어진 파일형식=" + contentType
                + " 허용된 파일형식=" + Arrays.toString(allowContentTypes));
    }

    /**
     * Apache Tika 를 사용하여 파일의 실제 서명이 일치하는지 확인
     *
     * @param file 업로드할 파일
     */
    public void validateFileSignature(MultipartFile file, MediaType... allowMediaType) {
        try {
            validateFileNotEmpty(file);
            String detectedType = tika.detect(file.getInputStream());
            for (MediaType mediaType : allowMediaType) {
                if (mediaType.toString().equals(detectedType)) {
                    return;
                }
            }
            throw new IllegalArgumentException("파일의 서명이 일치하지 않습니다. 주어진 파일형식=" + detectedType
                    + " 허용된 파일형식=" + Arrays.toString(allowMediaType));
        } catch (IOException e) {
            throw new IllegalArgumentException("파일을 분석하는 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 이미지의 크기 확인
     *
     * @param file 업로드할 파일
     */
    public void validateImageDimensions(MultipartFile file, int width, int height) {
        try {
            validateFileNotEmpty(file);
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new IllegalArgumentException("유효한 이미지 파일이 아닙니다.");
            }
            int imgWidth = image.getWidth();
            int imgHeight = image.getHeight();
            if (imgWidth != width || imgHeight != height) {
                throw new IllegalArgumentException("이미지의 크기는 "+width+"px x "+height+"px이어야 합니다.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("이미지 파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }

}
