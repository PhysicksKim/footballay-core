package com.gyechunsik.scoreboard.websocket.controller;

import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.AutoRemote;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.RemoteCode;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.ScoreBoardRemote;
import com.gyechunsik.scoreboard.websocket.user.StompPrincipal;
import io.jsonwebtoken.lang.Strings;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/scoreboard")
public class ScoreBoardRemoteController {

    private final AutoRemote autoRemote;
    private final ScoreBoardRemote scoreBoardRemote;

    /**
     * UUID -> 쿠키 발급
     * @param request
     * @return
     */
    @GetMapping("/user/cookie")
    public ResponseEntity<Void> giveUserUUIDCookie(
            HttpServletRequest request
    ) {
        HttpSession session = request.getSession();
        UUID userUuid = autoRemote.getUuidForCookieFromPrincipalName(session.getId());
        ResponseCookie cookie = createAnonymousUserUUIDCookie(userUuid.toString());
        log.info("giveUserUUIDCookie : {}", userUuid);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    /**
     * 쿠키 -> cache UUID To Redis
     * @param userUuid
     * @return
     */
    @PostMapping("/user/cookie")
    public ResponseEntity<String> refreshUserUuidCookie(
            @CookieValue(value = "anonymousUserUUID", required = false) String userUuid,
            HttpServletRequest request,
            Principal principal
    ) {
        Cookie[] cookies = request.getCookies();
        log.info("cookies iteration");
        for (Cookie cookie : cookies) {
            log.info("cookie name : {}", cookie.getName());
        }
        log.info("userUuid cookie : {}", userUuid);
        if (!Strings.hasText(userUuid)) {
            log.info("user Uuid cookie 가 없으므로 400 badRequest 를 반환.");
            return ResponseEntity.badRequest().body("userUuid 쿠키가 없습니다");
        }

        if (principal == null) {
            log.info("principal == null 이므로 sessionId 로 principal 을 생성");
            HttpSession session = request.getSession();
            log.info("Http session id : {}", session.getId());
            principal = new StompPrincipal(session.getId());
        }
        scoreBoardRemote.cacheUserBeforeAutoRemote(principal, userUuid);
        ResponseCookie cookie = createAnonymousUserUUIDCookie(userUuid);
        log.info("cache 완료 및 200 OK 반환");
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    private ResponseCookie createAnonymousUserUUIDCookie(String uuid) {
        return ResponseCookie.from("anonymousUserUUID", uuid)
                .httpOnly(true)
                .secure(true)
                .sameSite("none")
                .path("/")
                .maxAge(Duration.ofDays(60))
                .build();
    }
}

