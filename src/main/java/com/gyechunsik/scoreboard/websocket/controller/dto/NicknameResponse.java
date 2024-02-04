package com.gyechunsik.scoreboard.websocket.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NicknameResponse {
    private String nickname;
    private String message;

    public NicknameResponse(String nickname, String message) {
        this.nickname = nickname;
        this.message = message;
    }
}
