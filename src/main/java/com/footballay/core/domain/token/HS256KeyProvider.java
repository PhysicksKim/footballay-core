package com.footballay.core.domain.token;


import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * HS256 Key 를 제공합니다.
 * {@link TokenKeyConfiguration} 에서 생성된 SecretKey 를 주입받아 제공합니다.
 */
@Component
public class HS256KeyProvider {

    private final SecretKey SECRET_KEY;

    public HS256KeyProvider(SecretKey secretKey) {
        this.SECRET_KEY = secretKey;
    }

    public SecretKey getSECRET_KEY() {
        return SECRET_KEY;
    }
}
