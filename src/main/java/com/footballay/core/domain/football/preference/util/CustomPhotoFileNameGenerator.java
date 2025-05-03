package com.footballay.core.domain.football.preference.util;

import java.security.SecureRandom;

public class CustomPhotoFileNameGenerator {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int KEY_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(long playerId, String extension) {
        if(extension.startsWith(".")) {
            extension = extension.substring(1);
        }

        String hash = generateRandomHash();
        return String.format("%d_%s.%s", playerId, hash, extension);
    }

    private static String generateRandomHash() {
        StringBuilder keyBuilder = new StringBuilder(KEY_LENGTH);
        for (int i = 0; i < KEY_LENGTH; i++) {
            keyBuilder.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return keyBuilder.toString();
    }

}
