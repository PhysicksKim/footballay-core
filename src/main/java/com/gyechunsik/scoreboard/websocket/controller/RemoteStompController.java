package com.gyechunsik.scoreboard.websocket.controller;

import com.gyechunsik.scoreboard.websocket.request.RemoteConnectMessage;
import com.gyechunsik.scoreboard.websocket.response.ErrorResponse;
import com.gyechunsik.scoreboard.websocket.response.RemoteConnectResponse;
import com.gyechunsik.scoreboard.websocket.response.SuccessResponse;
import com.gyechunsik.scoreboard.websocket.service.RemoteCode;
import com.gyechunsik.scoreboard.websocket.service.RemoteCodeMapper;
import com.gyechunsik.scoreboard.websocket.service.RemoteCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@Controller
public class RemoteStompController {

    private final RemoteCodeService remoteCodeService;

    /**
     * 원격 client 가 전달받은 code 를 등록합니다.
     * client 는 응답으로 pubPath 를 받아서 해당 주소를 subscribe 합니다.
     * pubPath 는 "/topic/board.{remoteCode}" 입니다.
     * @param message remoteCode 가 담긴 STOMP 메시지
     * @param principal 원격 컨트롤 요청 client
     * @return pubPath 를 담은 응답 메세지
     * @since v1.0.0
     */
    @MessageMapping("/remote/code.connect")
    @SendToUser("/topic/remote/code.connect")
    public SuccessResponse remoteConnect(
            RemoteConnectMessage message,
            Principal principal
    ) {
        if (principal == null) {
            throw new IllegalArgumentException("코드 등록 에러. 유저이름 객체가 비어있습니다. 서버 관리자에게 문의해주세요");
        }

        RemoteCode remoteCode = RemoteCodeMapper.from(message);
        if (!remoteCodeService.enrollPubUser(remoteCode, principal.getName())) {
            throw new IllegalArgumentException("코드 등록 에러. 코드가 존재하지 않거나 이미 연결되어 있습니다.");
        }

        log.info("RemoteCode Publisher Enrolled :: remote connect = {} , Username = {}",
                remoteCode, principal.getName());

        // subPath = "/topic/board.{remoteCode}";
        return new RemoteConnectResponse("200", "코드 등록에 성공했습니다", remoteCode.getRemoteCode());
    }

    @MessageExceptionHandler(IllegalArgumentException.class)
    @SendToUser("/topic/error")
    public ResponseEntity<?> handleException(Exception e) {
        log.error("error : {}", e.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("400", e.getMessage()));
    }

}
