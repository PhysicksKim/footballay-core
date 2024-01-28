package com.gyechunsik.scoreboard.websocket.messages;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScoreBoardActionMessage {

    private String type; // "action" "error"
    private Action action;

    @Getter
    @Setter
    @ToString
    public static class Action {
        private String target;
        private String value;
    }

}
