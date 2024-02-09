package com.gyechunsik.scoreboard.websocket.service;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RemoteCodeService {

    private static final ConcurrentHashMap<RemoteCode, RemoteSubscriber> codeSessionMap = new ConcurrentHashMap<>();

    public RemoteCode generateCode(Principal principal) {
        RemoteCode remoteCode;
        do {
            remoteCode = RemoteCode.generate();
            log.info("remoteCode : [{}] , contains : [{}]", remoteCode, codeSessionMap.containsKey(remoteCode));
        }
        while (codeSessionMap.containsKey(remoteCode));

        RemoteSubscriber remoteSubscriber = new RemoteSubscriber(principal.getName(), remoteCode, LocalDateTime.now());

        codeSessionMap.put(remoteCode, remoteSubscriber);
        return remoteCode;
    }

    public boolean isValidCode(@NotNull RemoteCode remoteCode) {
        if (!codeSessionMap.containsKey(remoteCode)) {
            throw new IllegalStateException("유효하지 않은 코드입니다. 코드를 다시 확인해주세요.");
        }

        return true;
    }

    public ConcurrentHashMap<RemoteCode, RemoteSubscriber> getCodeSessionMap() {
        return codeSessionMap;
    }

    /**
     * 목록에서 코드를 제거한다.
     *
     * @param remoteCode 제거할 코드
     * @return 제거 성공 여부
     */
    public boolean expireCode(RemoteCode remoteCode) {
        log.info("expireCode : [{}]", remoteCode);
        return codeSessionMap.remove(remoteCode) != null;
    }

}
