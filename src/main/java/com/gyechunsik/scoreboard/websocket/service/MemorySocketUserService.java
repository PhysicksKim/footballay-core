package com.gyechunsik.scoreboard.websocket.service;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MemorySocketUserService {

    Map<Code, WebSocketSession> codeMap = new ConcurrentHashMap<>();
    Set<String> usernameSet = ConcurrentHashMap.newKeySet();

    /**
     * 비회원의 웹 소켓 등록 전 유저이름을 등록합니다. 유저 이름 중복검사 후 등록하며, 기존 이름이 있는 경우 삭제합니다
     *
     * @param nickname     등록할 유저이름
     * @param prevNickname 이전 유저이름. httpSession 에 저장된 이름을 가져오도록 합니다.
     * @return 등록 성공 여부
     */
    public boolean registerNickname(@NotNull String nickname, @Nullable String prevNickname) {
        // log.info("register username : {}", nickname);
        // log.info("usernameSet : {}", usernameSet);
        if (usernameSet.contains(nickname))
            return false;

        // 이전 닉네임 제거
        if (prevNickname != null) {
            usernameSet.remove(prevNickname);
        }
        usernameSet.add(nickname);
        return true;
    }

    public Code registerWebsocketSession(WebSocketSession session) {
        Code code;
        do {
            code = Code.generateCode();
        } while (!codeMap.containsKey(code));

        codeMap.put(code, session);
        return code;
    }
}
