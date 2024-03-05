package com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote;

import java.time.Duration;

public class RemoteExpireTimes {

    public static final Duration REMOTECODE_EXP = Duration.ofHours(6);
    public static final Duration ACTIVE_REMOTE_GROUP = Duration.ofHours(6);
    public static final Duration USER_PRE_CACHING = Duration.ofMinutes(5);

}
