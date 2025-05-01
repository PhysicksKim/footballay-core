package com.footballay.core.websocket.response;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 *
 code: number;
 message: string;
 metadata: {
     date: Date;
 };
 type: 'members';
 data: {
     members: string[];
 };
 */
@Getter
public class RemoteMembersResponse {

    private final int code = 200;
    private final String message = "remoteCode channel members";
    private final Map<String, Object> metadata = Map.of("date", LocalDateTime.now());
    private final String type = "members";
    private final Map<String, Object> data;

    public RemoteMembersResponse(List<String> members) {
        this.data = Map.of("members", members);
    }
}
