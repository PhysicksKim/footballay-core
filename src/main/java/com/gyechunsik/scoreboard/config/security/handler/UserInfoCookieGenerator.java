package com.gyechunsik.scoreboard.config.security.handler;

import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserInfoCookieGenerator {

    @Value("${cookies.user-info.name}")
    private String userInfoName;
    @Value("${cookies.user-info.max-age}")
    private int userInfoMaxAge;

    public Cookie generateCookie(Authentication authentication) {
        String userInfoString = getUserInfoString(authentication);
        Cookie userInfoCookie = new Cookie(userInfoName, userInfoString);
        userInfoCookie.setMaxAge(userInfoMaxAge);
        userInfoCookie.setPath("/");
        userInfoCookie.setSecure(true);
        return userInfoCookie;
    }

    private String getUserInfoString(Authentication authentication) {
        // Authentication 객체에서 사용자 정보를 추출하여 문자열로 변환하는 로직 구현
        // ...

        Object details = authentication.getDetails();
        log.info("UserInfoCookieGenerator Authentication :: {}", authentication);
        return "this-is-test-authentication-details";
    }

}
