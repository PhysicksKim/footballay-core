package com.gyechunsik.scoreboard.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import java.util.List;
import java.util.UUID;

public class CustomAnonymousAuthenticationFilter extends AnonymousAuthenticationFilter {

    private final String key;

    public CustomAnonymousAuthenticationFilter(String key) {
        super(key);
        this.key = key;
    }

    @Override
    protected Authentication createAuthentication(HttpServletRequest request) {
        // 익명 사용자에게 부여할 권한
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_GUEST");
        // 익명 사용자의 식별 정보 (예: UUID 생성)
        String principal = UUID.randomUUID().toString();
        // 사용자 정의 익명 인증 토큰 생성
        return new AuthenticatedGuestToken(key, principal, authorities);
    }
}
