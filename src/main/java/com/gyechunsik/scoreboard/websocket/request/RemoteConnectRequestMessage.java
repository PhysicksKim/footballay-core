package com.gyechunsik.scoreboard.websocket.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * <pre>
 * {
 *   remoteCode: "a2s3kw3",
 *   nickname: "gyechunhoe"
 *   isAutoRemote: true,
 *   afterSetCookieUrl: "/api/scoreboard/usercookie"
 * }
 * </pre>
 */
@Getter
@ToString
@AllArgsConstructor
public class RemoteConnectRequestMessage {

    protected String remoteCode;
    protected String nickname;
    protected boolean autoRemote;

}
