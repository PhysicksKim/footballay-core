package com.gyechunsik.scoreboard.domain.football.external.fetch.response;

import lombok.*;

@Getter
@Setter
@ToString
public class LeagueResponse {
    protected long id;
    protected String name;
    protected String type;
    protected String logo;
}
