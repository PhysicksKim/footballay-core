package com.gyechunsik.scoreboard.websocket.domain.autoremote;

import com.gyechunsik.scoreboard.websocket.domain.autoremote.entity.AutoRemoteGroup;
import com.gyechunsik.scoreboard.websocket.domain.autoremote.service.AnonymousUserService;
import com.gyechunsik.scoreboard.websocket.domain.autoremote.service.AutoRemoteGroupService;
import com.gyechunsik.scoreboard.websocket.domain.autoremote.entity.AnonymousUser;
import com.gyechunsik.scoreboard.websocket.domain.autoremote.service.AutoRemoteRedisService;
import com.gyechunsik.scoreboard.websocket.response.RemoteConnectResponse;
import com.gyechunsik.scoreboard.websocket.service.RemoteCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class AutoRemote {

    private final AnonymousUserService anonymousUserService;
    private final AutoRemoteGroupService autoRemoteGroupService;

    private final AutoRemoteRedisService autoRemoteRedisService;

    public AutoRemoteGroup createAutoRemoteGroup() {
        return autoRemoteGroupService.createAutoRemoteGroup();
    }

    public AnonymousUser createAnonymousUser(AutoRemoteGroup autoRemoteGroup) {
        return anonymousUserService.createAndSaveAnonymousUser(autoRemoteGroup);
    }

    public void preRemoteUserCaching(Principal principal, UUID userId) {
        // TODO : Redis 에 사용자 정보 캐싱
        // ## 유효한 사용자(존재하는 AnonymousUser)인지 확인
        // 유효하지 않은 사용자라면 예외 처리
        // 유효한 사용자라면 레디스에 캐싱후 종료
    }

    public RemoteConnectResponse connect(Principal principal, String nickname) {


        // principal.getName 으로 redis 에 캐싱된 UUID 가져옴 - 없으면 예외 발생
        // UUID 로 RDB 에서 AutoRemoteGroup 가져옴 - 없으면 예외 발생
        // AutoRemoteGroup 이 redis 에 활성화 되어있는지 확인
        //
        // ## 이미 Group 이 활성화 된 경우
        // Redis 에서 Key:AutoRemoteGroup 으로 Hash<GroupId, remoteCode> 로 remoteCode 가져와서
        // 기존 RemoteCode 에서 Member Connect (AddSub) 과정과 동일하게 진행
        //
        // ## Group 이 활성화 되어있지 않은 경우
        // RemoteCode 생성하고 Hash<GroupId, remoteCode> 로 새 그룹 저장
        // 기존 RemoteCode 에서 Host Issue 과정과 동일하게 진행

        return null;
    }

    // 원격 연결 그룹의 만료 시간 업데이트
    public void updateGroupExpiration(AutoRemoteGroup group, LocalDateTime newExpirationTime) {
        autoRemoteGroupService.updateExpirationTime(group, newExpirationTime);
    }

    // 원격 연결 그룹 재활성화
    public void reactivateGroup(AutoRemoteGroup group) {
        autoRemoteGroupService.reactivateGroup(group);
    }

}
