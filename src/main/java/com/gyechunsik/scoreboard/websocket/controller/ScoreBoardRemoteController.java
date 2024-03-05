package com.gyechunsik.scoreboard.websocket.controller;

import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.RemoteCode;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.ScoreBoardRemote;
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

    private final ScoreBoardRemote scoreBoardRemote;

    /**
     * STOMP 를 통해 자동 연결 요청/생성이 성공한 이후에, 유저 식별 Cookie 를 받아오기 위해서 사용합니다.
     * Host, Member 여부와 상관없이 최초 자동 원격 생성 요청자가 Group 을 생성시킵니다.
     * Group 이 생성되면 RDB 에는 유저 정보와 그룹이 생성되어 저장됩니다.
     * Redis 에는 현재 RemoteCode 의 다른 사용자가 자동 연결 시, 같은 그룹으로 맵핑시켜줄 수 있도록 {RemoteCode, AutoRemoteGroupId} 가 저장됩니다.
     * @param remoteCode
     * @return
     */
    @GetMapping("/{remoteCode}/userCookie")
    public ResponseEntity<Void> giveUserUUIDCookie(
            @PathVariable String remoteCode
    ) {
        UUID uuid = scoreBoardRemote.issueUUIDForAnonymousUserCookie(RemoteCode.of(remoteCode));
        ResponseCookie cookie = createAnonymousUserUUIDCookie(uuid.toString());

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    /**
     * STOMP 기존 자동 연결 그룹 참여 이전에 유저 식별자를 Redis 에 캐싱해두기 위해서 사용합니다.
     * STOMP 에서는 Cookie 를 사용할 수 없으므로, HTTP 요청을 통해서 Redis 에 캐싱해두고 STOMP 에서는 Cache 된 값을 가져오도록 합니다.
     * @param userUUID
     * @param principal
     * @return
     */
    @PostMapping("/preRemoteCache")
    public ResponseEntity<Void> cacheUserBeforeAutoRemote(
            @CookieValue(value = "AnonymousUserUUID", required = true) String userUUID,
            Principal principal
    ) {
        scoreBoardRemote.cacheUserBeforeAutoRemote(principal, userUUID);
        ResponseCookie cookie = createAnonymousUserUUIDCookie(userUUID);

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

