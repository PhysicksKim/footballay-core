package com.footballay.core.domain.football.preference.service;

import com.footballay.core.domain.football.preference.persistence.PreferenceKey;
import com.footballay.core.domain.football.preference.repository.PreferenceKeyRepository;
import com.footballay.core.domain.user.entity.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.util.Optional;

@Service
public class PreferenceKeyService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PreferenceKeyService.class);
    private final PreferenceKeyRepository preferenceKeyRepository;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int KEY_LENGTH = 32;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public Optional<PreferenceKey> findPreferenceKey(User user) {
        return preferenceKeyRepository.findByUser(user);
    }

    @Transactional
    public PreferenceKey generatePreferenceKeyForUser(User user) {
        String keyHash = generateKeyHash();
        log.info("Generated preferenceKey={}", keyHash);
        PreferenceKey preferKey = PreferenceKey.builder().user(user).keyhash(keyHash).build();
        return preferenceKeyRepository.save(preferKey);
    }

    @Transactional
    public PreferenceKey reissuePreferenceKeyForUser(User user) {
        PreferenceKey preferenceKey = preferenceKeyRepository.findByUser(user).orElseThrow(() -> new IllegalStateException("PreferenceKey not found for user=" + user));
        String newKeyHash = generateKeyHash();
        preferenceKey.setKeyhash(newKeyHash);
        return preferenceKeyRepository.save(preferenceKey);
    }

    /**
     * Soft Delete 됩니다.
     *
     * @see PreferenceKey
     * @param user 삭제할 Key 를 소유한 User
     * @return 삭제 성공하면 true, PreferenceKey 가 없으면 false
     */
    @Transactional
    public boolean deletePreferenceKeyForUser(User user) {
        try {
            Optional<PreferenceKey> preferenceKey = preferenceKeyRepository.findByUser(user);
            if (preferenceKey.isEmpty()) {
                log.warn("PreferenceKey not found for user={}", user);
                return false;
            }
            preferenceKeyRepository.delete(preferenceKey.get());
            return true;
        } catch (Exception e) {
            log.error("Unexpected fail while deleting preferenceKey for user={}", user, e);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public boolean validateKeyHash(@NotNull String keyHash) {
        return preferenceKeyRepository.existsByKeyhash(keyHash);
    }

    private static String generateRandomKey() {
        StringBuilder keyBuilder = new StringBuilder(KEY_LENGTH);
        for (int i = 0; i < KEY_LENGTH; i++) {
            keyBuilder.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return keyBuilder.toString();
    }

    private String generateKeyHash() {
        String key;
        do {
            key = generateRandomKey();
        } while (preferenceKeyRepository.existsByKeyhash(key));
        return key;
    }

    public PreferenceKeyService(final PreferenceKeyRepository preferenceKeyRepository) {
        this.preferenceKeyRepository = preferenceKeyRepository;
    }
}
