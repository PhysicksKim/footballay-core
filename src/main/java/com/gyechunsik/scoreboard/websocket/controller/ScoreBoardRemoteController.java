package com.gyechunsik.scoreboard.websocket.controller;

import com.gyechunsik.scoreboard.websocket.domain.remote.code.RemoteCode;
import com.gyechunsik.scoreboard.websocket.domain.remote.RemoteService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

// TODO : 로직 다시 재점검 해야함. Cookie 가져올 때 유저 식별자 어디에 저장되는거지? 코드 신규발급/연결 과정에서 Redis 에 캐싱해야 할 것 같은데
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/scoreboard")
public class ScoreBoardRemoteController {

    private final RemoteService remoteService;

    @GetMapping("/{remoteCode}/userCookie")
    public ResponseEntity<Void> getUserUUIDCookie(
            @PathVariable String remoteCode,
            HttpServletResponse response
    ) {
        UUID uuid = remoteService.getUUIDForAnonymousUserCookie(RemoteCode.of(remoteCode));
        response.addCookie(new Cookie("AnonymousUserUUID", uuid.toString()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/preRemoteCache")
    public ResponseEntity<Void> cacheUserBeforeAutoRemote(
            @CookieValue(value = "AnonymousUserUUID") String userUUID,
            Principal principal
    ) {
        remoteService.cacheUserBeforeAutoRemote(principal, userUUID);
        return ResponseEntity.ok().build();
    }
}

