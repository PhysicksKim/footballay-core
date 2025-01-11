package com.gyechunsik.scoreboard.util;

import org.apache.tika.Tika;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomPhotoMultipartGeneratorTest {

    private static final Tika tika = new Tika();

    @Test
    void createMockMultipartFile_With150x150PngImage() throws IOException {
        MultipartFile multipartFile = CustomPhotoMultipartGenerator.generate();
        String tikaDetectMimeType = tika.detect(multipartFile.getInputStream());

        assertThat(multipartFile).isNotNull();
        assertThat(multipartFile.getContentType()).isEqualTo("image/png");
        assertThat(tikaDetectMimeType).isEqualTo("image/png");
    }
}
