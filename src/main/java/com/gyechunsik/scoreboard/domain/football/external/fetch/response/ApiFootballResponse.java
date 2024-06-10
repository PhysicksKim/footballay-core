package com.gyechunsik.scoreboard.domain.football.external.fetch.response;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class ApiFootballResponse {
    private String get;
    private Map<String, String> parameters;
    private List<String> errors;
    private int results;
    private Map<String, Integer> paging;
}
