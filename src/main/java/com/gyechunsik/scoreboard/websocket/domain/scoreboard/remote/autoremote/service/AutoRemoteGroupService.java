package com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.service;

import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.entity.AutoRemoteGroup;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.repository.AutoRemoteGroupRepository;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.repository.AutoRemoteRedisRepository;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.RemoteCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AutoRemoteGroupService {

    private final AutoRemoteGroupRepository groupRepository;
    private final AutoRemoteRedisRepository redisRepository;

    public AutoRemoteGroup createAutoRemoteGroup() {
        AutoRemoteGroup created = new AutoRemoteGroup();
        created.setLastActiveAt(LocalDateTime.now());
        created.setExpiredAt(LocalDateTime.now().plusDays(30));
        groupRepository.save(created);
        return created;
    }

    public Optional<AutoRemoteGroup> getAutoRemoteGroup(Long id) {
        return groupRepository.findById(id);
    }

    /**
     * key : Remote:{remoteCode}_AutoRemoteGroupId
     * value : {AutoRemoteGroupId}
     * @param remoteCode
     * @param groupId
     */
    public void activateAutoRemoteGroup(RemoteCode remoteCode, long groupId) {
        redisRepository.setActiveAutoRemoteKeyPair(Long.toString(groupId), remoteCode.getRemoteCode());
    }

    public Optional<Long> getActiveGroupIdBy(RemoteCode remoteCode) {
        Optional<String> activeAutoGroupId = redisRepository.findAutoGroupIdFromRemoteCode(remoteCode.getRemoteCode());
        return activeAutoGroupId.map(Long::parseLong);
    }

    private boolean isNotValidNewExpirationTime(LocalDateTime newExpirationTime) {
        return newExpirationTime.isBefore(LocalDateTime.now());
    }
}
