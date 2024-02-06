package com.gyechunsik.scoreboard.websocket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CodeService {

    private final ConcurrentHashMap<Code, ArrayList<ControlPublisher>> codeSessionMap = new ConcurrentHashMap<>();

    public Code generateCode() {
        Code code;
        do {
            code = Code.generateCode();
            log.info("code : {}", code);
            log.info("contains : {}", codeSessionMap.containsKey(code));
        }
        while (codeSessionMap.containsKey(code));
        codeSessionMap.put(code, new ArrayList<>());
        return code;
    }

    public ConcurrentHashMap<Code, ArrayList<ControlPublisher>> getCodeSessionMap() {
        return codeSessionMap;
    }

    /**
     * 코드를 맵에서 제거한다.
     *
     * @param code 제거할 코드
     * @return 제거 성공 여부
     */
    public boolean removeCode(Code code) {
        return codeSessionMap.remove(code) != null;
    }

}
