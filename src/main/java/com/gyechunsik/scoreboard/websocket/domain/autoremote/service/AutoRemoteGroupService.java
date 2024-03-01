package com.gyechunsik.scoreboard.websocket.domain.autoremote.service;

import com.gyechunsik.scoreboard.websocket.domain.autoremote.entity.AutoRemoteGroup;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AutoRemoteGroupService {

    public AutoRemoteGroup createAutoRemoteGroup() {
        AutoRemoteGroup created = new AutoRemoteGroup();
        created.setLastActiveAt(LocalDateTime.now());
        created.setExpiredAt(LocalDateTime.now().plusDays(30));
        return created;
    }

    public void updateExpirationTime(AutoRemoteGroup group, LocalDateTime newExpirationTime) {
        if (isNotValidNewExpirationTime(newExpirationTime)) {
            throw new IllegalArgumentException("만료 시간은 현재 시간 이후로 설정해야 합니다.");
        }

        group.setExpiredAt(newExpirationTime);
    }

    private boolean isNotValidNewExpirationTime(LocalDateTime newExpirationTime) {
        return newExpirationTime.isBefore(LocalDateTime.now());
    }

    public void reactivateGroup(AutoRemoteGroup group) {
        group.setLastActiveAt(LocalDateTime.now());

        // TODO : Redis에 활성화된 그룹 처리 필요
    }
}
