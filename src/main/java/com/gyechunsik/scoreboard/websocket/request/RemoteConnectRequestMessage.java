package com.gyechunsik.scoreboard.websocket.request;

import com.gyechunsik.scoreboard.websocket.response.AbstractSubPubPathResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * <pre>
 *     body
 *     { remoteCode: "a2s3kw3" }
 * </pre>
 */
@Getter
@ToString
@AllArgsConstructor
public class RemoteConnectRequestMessage {

    protected final String remoteCode;

}
