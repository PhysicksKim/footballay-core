package com.footballay.core.domain.football.external.fetch.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class _LeagueResponse {
    protected long id;
    protected String name;
    protected String type;
    protected String logo;
}
