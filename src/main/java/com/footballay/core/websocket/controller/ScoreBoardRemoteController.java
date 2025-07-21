package com.footballay.core.websocket.controller;

import com.footballay.core.websocket.domain.scoreboard.remote.ScoreBoardRemoteServiceImpl;
import com.footballay.core.websocket.user.StompPrincipal;
import io.jsonwebtoken.lang.Strings;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/scoreboard")
public class ScoreBoardRemoteController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScoreBoardRemoteController.class);
    private final ScoreBoardRemoteServiceImpl scoreBoardRemoteService;

    /**
     * UUID -> 쿠키 발급
     * @param request
     * @return
     */
    @GetMapping("/user/cookie")
    public ResponseEntity<Void> giveUserUUIDCookie(HttpServletRequest request) {
        HttpSession session = request.getSession();
        UUID userUuid = scoreBoardRemoteService.getUuidForCookieFromPrincipalName(session.getId());
        ResponseCookie cookie = createAnonymousUserUUIDCookie(userUuid.toString());
        log.info("giveUserUUIDCookie : {}", userUuid);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    /**
     * 쿠키 -> cache UUID To Redis
     * @param userUuid
     * @return
     */
    @PostMapping("/user/cookie")
    public ResponseEntity<String> refreshUserUuidCookie(@CookieValue(value = "anonymousUserUUID", required = false) String userUuid, HttpServletRequest request, Principal principal) {
        Cookie[] cookies = request.getCookies();
        log.info("cookies iteration");
        for (Cookie cookie : cookies) {
            log.info("cookie name : {}", cookie.getName());
        }
        log.info("userUuid cookie : {}", userUuid);
        if (!Strings.hasText(userUuid)) {
            return ResponseEntity.badRequest().body("userUuid 쿠키가 없습니다");
        }
        if (principal == null) {
            log.info("principal == null 이므로 sessionId 로 principal 을 생성");
            HttpSession session = request.getSession();
            log.info("Http session id : {}", session.getId());
            principal = new StompPrincipal(session.getId());
        }
        scoreBoardRemoteService.cacheUserBeforeAutoRemote(principal, userUuid);
        ResponseCookie cookie = createAnonymousUserUUIDCookie(userUuid);
        log.info("cache 완료 및 200 OK 반환");
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    // POST /user/cookie/clear
    // 사용자 인증 관련 쿠키를 모두 즉시 expire 함
    @PostMapping("/user/cookie/clear")
    public ResponseEntity<Void> clearUserUuidCookie() {
        ResponseCookie cookie = ResponseCookie.from("anonymousUserUUID", "").httpOnly(true).secure(true).sameSite("none").path("/").maxAge(Duration.ofSeconds(0)).build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    private ResponseCookie createAnonymousUserUUIDCookie(String uuid) {
        return ResponseCookie.from("anonymousUserUUID", uuid).httpOnly(true).secure(true).sameSite("none").path("/").maxAge(Duration.ofDays(60)).build();
    }

    public ScoreBoardRemoteController(final ScoreBoardRemoteServiceImpl scoreBoardRemoteService) {
        this.scoreBoardRemoteService = scoreBoardRemoteService;
    }
}
