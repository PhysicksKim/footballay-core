package com.gyechunsik.scoreboard.websocket.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AutoRemoteReconnectRequestMessage {

    private String nickname;

}
