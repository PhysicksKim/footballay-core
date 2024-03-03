package com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.service;

import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.entity.AutoRemoteGroup;
import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.repository.AutoRemoteGroupRepository;
import com.gyechunsik.scoreboard.websocket.domain.remote.code.RemoteCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AutoRemoteGroupService {

    private final AutoRemoteGroupRepository repository;
    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX_REMOTECODE_TO_AUTOREMOTEGROUP = "Remote:";
    private static final String SUFFIX_REMOTECODE_TO_AUTOREMOTEGROUP = "_AutoRemoteGroupId";

    public AutoRemoteGroup createAutoRemoteGroup() {
        AutoRemoteGroup created = new AutoRemoteGroup();
        created.setLastActiveAt(LocalDateTime.now());
        created.setExpiredAt(LocalDateTime.now().plusDays(30));
        repository.save(created);
        return created;
    }

    public void updateExpirationTime(AutoRemoteGroup group, LocalDateTime newExpirationTime) {
        if (isNotValidNewExpirationTime(newExpirationTime)) {
            throw new IllegalArgumentException("만료 시간은 현재 시간 이후로 설정해야 합니다.");
        }

        group.setExpiredAt(newExpirationTime);
    }

    public Optional<AutoRemoteGroup> getAutoRemoteGroup(Long id) {
        return repository.findById(id);
    }

    public void setRemoteCodeToAutoGroupId(RemoteCode remoteCode, long groupId) {
        String key = getRemoteCodeToAutoGroupIdKey(remoteCode);
        String value = Long.toString(groupId);
        redisTemplate.opsForValue().set(key, value);
    }

    private String getRemoteCodeToAutoGroupIdKey(RemoteCode remoteCode) {
        return PREFIX_REMOTECODE_TO_AUTOREMOTEGROUP + remoteCode.getRemoteCode() + SUFFIX_REMOTECODE_TO_AUTOREMOTEGROUP;
    }

    public Optional<String> getActiveGroupIdByRemoteCode(RemoteCode remoteCode) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(remoteCode.getRemoteCode()));
    }

    private boolean isNotValidNewExpirationTime(LocalDateTime newExpirationTime) {
        return newExpirationTime.isBefore(LocalDateTime.now());
    }

    public void reactivateGroup(AutoRemoteGroup group) {
        group.setLastActiveAt(LocalDateTime.now());

        // TODO : Redis에 활성화된 그룹 처리 필요
    }
}
