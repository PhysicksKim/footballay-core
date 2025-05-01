package com.footballay.core.domain.token;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * 앱 전체에서 동일한 JWT 비밀키를 사용하도록 하기 위해 Bean 으로 등록합니다.
 * HS256, RS256 알고리즘을 사용할 수 있도록 SecretKey, KeyPair 를 생성합니다.
 */
@Configuration
public class TokenKeyConfiguration {

    @Bean
    public SecretKey jwtHS256SecretKey(@Value("${jwt.secret.key:#{null}}") String secretKey) {
        if (secretKey != null && !secretKey.trim().isEmpty()) {
            return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        }
        return Jwts.SIG.HS256.key().build();
    }

    @Bean
    public KeyPair jwtRS256KeyPair() {
        return Jwts.SIG.RS256.keyPair().build();
    }

    @Bean
    public RSAPublicKey jwtRS256PublicKey(KeyPair jwtRS256KeyPair) {
        return (RSAPublicKey) jwtRS256KeyPair.getPublic();
    }

    @Bean
    public RSAPrivateKey jwtRS256PrivateKey(KeyPair jwtRS256KeyPair) {
        return (RSAPrivateKey) jwtRS256KeyPair.getPrivate();
    }
}

