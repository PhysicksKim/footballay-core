package com.gyechunsik.scoreboard.websocket.service;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RemoteCodeService {

    private static final ConcurrentHashMap<RemoteCode, Set<RemotePublisher>> codeSessionMap = new ConcurrentHashMap<>();

    public RemoteCode generateCode() {
        RemoteCode remoteCode;
        do {
            remoteCode = RemoteCode.generate();
            log.info("remoteCode : [ {} ] , contains : [{}]", remoteCode, codeSessionMap.containsKey(remoteCode));
        }
        while (codeSessionMap.containsKey(remoteCode));
        codeSessionMap.put(remoteCode, new HashSet<>());
        return remoteCode;
    }

    /**
     * subscriber name 을 저장소에 등록합니다.
     * @param remoteCode 등록할 코드 값
     * @param name Principal.getName()으로 가져온 사용자 이름
     * @return true if the remoteCode is not already enrolled
     * @exception IllegalStateException 코드 저장소가 존재하지 않는 경우
     */
    public boolean enrollPubUser(@NotNull RemoteCode remoteCode, @NotNull String name) {
        Set<RemotePublisher> remotePubSet = codeSessionMap.get(remoteCode);
        if(remotePubSet == null) {
            throw new IllegalStateException("유효하지 않은 코드입니다. 코드를 다시 확인해주세요.");
        }

        RemotePublisher remotePublisher = new RemotePublisher(name);
        return remotePubSet.add(remotePublisher);
    }

    public boolean isValidCodeAndUser(@NotNull RemoteCode remoteCode, @NotNull String name) {
        Set<RemotePublisher> remotePubSet = codeSessionMap.get(remoteCode);
        if(remotePubSet == null) {
            log.info("존재하지 않은 코드에 대한 요청 발생. remoteCode = [{}], name = [{}]", remoteCode, name);
            return false;
        }

        RemotePublisher remotePublisher = new RemotePublisher(name);
        return remotePubSet.contains(remotePublisher);
    }

    public ConcurrentHashMap<RemoteCode, Set<RemotePublisher>> getCodeSessionMap() {
        return codeSessionMap;
    }

    /**
     * 목록에서 코드를 제거한다.
     * @param remoteCode 제거할 코드
     * @return 제거 성공 여부
     */
    public boolean expireCode(RemoteCode remoteCode) {
        log.info("expireCode : [{}]", remoteCode);
        return codeSessionMap.remove(remoteCode) != null;
    }

}
