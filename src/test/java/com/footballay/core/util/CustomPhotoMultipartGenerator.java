package com.footballay.core.util;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CustomPhotoMultipartGenerator {

    private static final int CUSTOM_PHOTO_WIDTH = 150;
    private static final int CUSTOM_PHOTO_HEIGHT = 150;
    private static final String CUSTOM_PHOTO_MIME_TYPE = "image/png";
    private static final String CUSTOM_PHOTO_FILE_FORMAT = "png";
    private static final String CUSTOM_PHOTO_FILE_NAME = "custom_photo";

    public static MultipartFile generate() {
        try {
            BufferedImage bufferedImage = createCustomPhotoBufferedImage();
            fillImageBlue(bufferedImage);
            byte[] imageBytes = parseToByteArray(bufferedImage);
            return createMockMultipartPngFile(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("MultipartFile 생성 중 오류 발생");
        }
    }

    private static BufferedImage createCustomPhotoBufferedImage() {
        return new BufferedImage(CUSTOM_PHOTO_WIDTH, CUSTOM_PHOTO_HEIGHT, BufferedImage.TYPE_INT_RGB);
    }

    private static MockMultipartFile createMockMultipartPngFile(byte[] imageBytes) {
        return new MockMultipartFile(
                "file", // 필드 이름
                CUSTOM_PHOTO_FILE_NAME+"."+CUSTOM_PHOTO_FILE_FORMAT, // 원본 파일 이름
                CUSTOM_PHOTO_MIME_TYPE, // MIME 타입
                imageBytes // 파일 내용
        );
    }

    private static byte[] parseToByteArray(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, CUSTOM_PHOTO_FILE_FORMAT, baos);
        return baos.toByteArray();
    }

    private static void fillImageBlue(BufferedImage bufferedImage) {
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setPaint(Color.BLUE); // 배경색 설정
        graphics.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
        graphics.dispose();
    }
}
