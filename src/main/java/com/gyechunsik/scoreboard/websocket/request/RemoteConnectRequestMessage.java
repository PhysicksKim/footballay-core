package com.gyechunsik.scoreboard.websocket.request;

import com.gyechunsik.scoreboard.websocket.response.AbstractSubPubPathResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

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
@NoArgsConstructor
public class RemoteConnectRequestMessage {

    protected String remoteCode;
    protected String nickname;
    protected boolean isAutoRemote;

}
