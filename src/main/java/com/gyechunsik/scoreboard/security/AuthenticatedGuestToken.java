package com.gyechunsik.scoreboard.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class AuthenticatedGuestToken extends AnonymousAuthenticationToken {
    public AuthenticatedGuestToken(String key, Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(key, principal, authorities);
    }

    @Override
    public boolean isAuthenticated() {
        return true;  // 게스트 사용자도 인증된 것으로 처리해서 컨트롤러 argument resolver 에서 null 이 아닌 객체를 제공하도록 함
    }
}
