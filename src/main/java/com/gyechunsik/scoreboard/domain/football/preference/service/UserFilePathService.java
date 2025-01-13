package com.gyechunsik.scoreboard.domain.football.preference.service;

import com.gyechunsik.scoreboard.domain.football.preference.persistence.UserFilePath;
import com.gyechunsik.scoreboard.domain.football.preference.persistence.UserPathCategory;
import com.gyechunsik.scoreboard.domain.football.preference.repository.UserFilePathRepository;
import com.gyechunsik.scoreboard.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserFilePathService {

    private final UserFilePathRepository userFilePathRepository;

    @Value("${aws.cloudfront.domain}")
    private String CLOUDFRONT_DOMAIN;

    @Value("${path.football.players.photo.prefix}")
    private String PLAYER_CUSTOM_PHOTO_PREFIX;

    @Value("${path.football.players.photo.suffix}")
    private String PLAYER_CUSTOM_PHOTO_SUFFIX;

    /**
     * 사용자별 커스텀 선수 이미지 경로를 제공합니다.
     * 이미 경로가 존재하는 경우 해당 경로를 반환하고, 경로가 존재하지 않는 경우 새로운 경로를 생성하여 반환합니다.
     * @param user 사용자 엔티티
     * @return 사용자별 커스텀 선수 이미지 경로
     */
    @Transactional
    public UserFilePath getPlayerCustomPhotoPath(User user) {
        UserPathCategory category = UserPathCategory.PLAYER_CUSTOM_PHOTO;

        Optional<UserFilePath> optionalUserFilePath = userFilePathRepository.findByUserIdAndUserPathCategory(user.getId(), category);
        UserFilePath userFilePath;
        if(optionalUserFilePath.isEmpty()) {
            String userPathHash;
            do {
                userPathHash = HashGenerator.generateUserPathHash();
            } while (userFilePathRepository.existsByUserPathHash(userPathHash));

            UserFilePath build = UserFilePath.builder()
                    .user(user)
                    .domain(CLOUDFRONT_DOMAIN)
                    .prefixPath(PLAYER_CUSTOM_PHOTO_PREFIX)
                    .suffixPath(PLAYER_CUSTOM_PHOTO_SUFFIX)
                    .userPathCategory(category)
                    .userPathHash(userPathHash)
                    .build();
            userFilePath = userFilePathRepository.save(build);
        } else {
            userFilePath = optionalUserFilePath.get();
        }

        return userFilePath;
    }

    private static final class HashGenerator {
        private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
        private static final int KEY_LENGTH = 16;

        private static final SecureRandom RANDOM = new SecureRandom();

        public static String generateUserPathHash() {
            StringBuilder keyBuilder = new StringBuilder(KEY_LENGTH);
            for (int i = 0; i < KEY_LENGTH; i++) {
                keyBuilder.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
            }
            return keyBuilder.toString();
        }
    }

}
