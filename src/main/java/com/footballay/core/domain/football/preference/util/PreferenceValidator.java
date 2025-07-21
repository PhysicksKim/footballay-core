package com.footballay.core.domain.football.preference.util;

import com.footballay.core.utils.ImageValidator;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class PreferenceValidator {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PreferenceValidator.class);
    private final ImageValidator imageValidator;
    private static final MediaType[] CUSTOM_PHOTO_MIME_TYPES = {MediaType.IMAGE_PNG};
    private static final String[] CUSTOM_PHOTO_CONTENT_TYPES = {MediaType.IMAGE_PNG_VALUE};
    private static final int CUSTOM_PHOTO_WIDTH = 150;
    private static final int CUSTOM_PHOTO_HEIGHT = 150;

    /**
     * 선수 커스텀 이미지가 유효한지 확인합니다.
     * @param image 선수 커스텀 이미지
     * @return 유효한 이미지인지 여부
     */
    public boolean isValidPlayerCustomPhotoImage(@NotNull MultipartFile image) {
        if (image == null) {
            log.error("Image is null");
            return false;
        }
        try {
            imageValidator.validateFileNotEmpty(image);
            imageValidator.validateContentType(image, CUSTOM_PHOTO_CONTENT_TYPES);
            imageValidator.validateFileSignature(image, CUSTOM_PHOTO_MIME_TYPES);
            imageValidator.validateImageDimensions(image, CUSTOM_PHOTO_WIDTH, CUSTOM_PHOTO_HEIGHT);
        } catch (IllegalArgumentException e) {
            log.error("Invalid image file", e);
            return false;
        }
        return true;
    }

    public PreferenceValidator(final ImageValidator imageValidator) {
        this.imageValidator = imageValidator;
    }
}
