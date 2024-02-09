package com.gyechunsik.scoreboard.websocket.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;

@Getter
public class RemoteCode {

    private final String remoteCode;

    protected RemoteCode(@NotBlank String remoteCode) {
        this.remoteCode = remoteCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteCode that = (RemoteCode) o;

        return getRemoteCode() != null ? getRemoteCode().equals(that.getRemoteCode()) : that.getRemoteCode() == null;
    }

    @Override
    public int hashCode() {
        return getRemoteCode() != null ? getRemoteCode().hashCode() : 0;
    }

    @Override
    public String toString() {
        return '{' + remoteCode + '}';
    }

    protected static RemoteCode generate() {
        String RemoteCode = CodeGenerator.generateCode();
        return new RemoteCode(RemoteCode);
    }

    public static RemoteCode of(@NotBlank String remoteCode) {
        if(!StringUtils.hasText(remoteCode)) {
            throw new IllegalArgumentException("remoteCode should not null or empty.");
        }
        if(remoteCode.length() != CodeGenerator.LENGTH) {
            throw new IllegalArgumentException("remoteCode length should be " + CodeGenerator.LENGTH);
        }
        if(remoteCode.chars().anyMatch(c -> CodeGenerator.CHARACTERS.indexOf(c) == -1)) {
            throw new IllegalArgumentException("remoteCode should be composed of {" + CodeGenerator.CHARACTERS + '}');
        }

        return new RemoteCode(remoteCode);
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
