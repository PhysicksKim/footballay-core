package com.gyechunsik.scoreboard.websocket.service;

import lombok.Getter;

import java.security.SecureRandom;

@Getter
public class Code {

    private String value;

    private Code() {
        this.value = "";
    }

    public static Code generateCode() {
        String code = CodeGenerator.generateCode();
        Code instance = new Code();
        instance.value = code;
        return instance;
    }

    private static class CodeGenerator {

        private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
        private static final int LENGTH = 6;
        private static final SecureRandom random = new SecureRandom();

        public static String generateCode() {
            StringBuilder stringBuilder = new StringBuilder(LENGTH);
            for (int i = 0; i < LENGTH; i++) {
                int index = random.nextInt(CHARACTERS.length());
                stringBuilder.append(CHARACTERS.charAt(index));
            }
            return stringBuilder.toString();
        }
    }
}
