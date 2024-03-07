package com.gyechunsik.scoreboard.websocket.controller;

import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.AutoRemote;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.RemoteCode;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.ScoreBoardRemote;
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
    public ResponseEntity<Void> refreshUserUuidCookie(
            @CookieValue(value = "AnonymousUserUUID", required = true) String userUuid,
            Principal principal
    ) {
        scoreBoardRemote.cacheUserBeforeAutoRemote(principal, userUuid);
        ResponseCookie cookie = createAnonymousUserUUIDCookie(userUuid);
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

