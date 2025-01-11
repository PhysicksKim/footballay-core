package com.gyechunsik.scoreboard.domain.football.preference.service;

import com.gyechunsik.scoreboard.domain.football.preference.persistence.PreferenceKey;
import com.gyechunsik.scoreboard.domain.football.preference.repository.PreferenceKeyRepository;
import com.gyechunsik.scoreboard.entity.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreferenceKeyService {

    private final PreferenceKeyRepository preferenceKeyRepository;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int KEY_LENGTH = 32;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public PreferenceKey generatePreferenceKeyForUser(User user) {
        String key;
        do {
            key = generateRandomKey();
        } while (preferenceKeyRepository.existsByKeyhash(key));
        log.info("Generated preferenceKey={}", key);

        PreferenceKey preferKey = PreferenceKey.builder()
                .user(user)
                .keyhash(key)
                .build();
        return preferenceKeyRepository.save(preferKey);
    }

    @Transactional
    public boolean deletePreferenceKeyForUser(User user) {
        Optional<PreferenceKey> preferenceKey = preferenceKeyRepository.findByUser(user);
        if(preferenceKey.isEmpty()) {
            log.warn("PreferenceKey not found for user={}", user);
            return false;
        }
        preferenceKeyRepository.delete(preferenceKey.get());
        return true;
    }

    private static String generateRandomKey() {
        StringBuilder keyBuilder = new StringBuilder(KEY_LENGTH);
        for (int i = 0; i < KEY_LENGTH; i++) {
            keyBuilder.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return keyBuilder.toString();
    }

}
