package com.gyechunsik.scoreboard.web.defaultmatch.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DefaultTeamResponse {
    private String category;
    private String code;
    private String name;
    private String side;
    private String uniform;
}
