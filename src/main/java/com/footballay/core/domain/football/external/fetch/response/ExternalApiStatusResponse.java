package com.footballay.core.domain.football.external.fetch.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalApiStatusResponse {

    private String get;
    private List<String> parameters;
    private List<String> errors;
    private int results;
    private Map<String, Integer> paging;

    private Response response;
    private _Headers headers;

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Headers {
        private int xRatelimitLimit;
        private int xRatelimitRemaining;
        private int xRatelimitRequestsLimit;
        private int xRatelimitRequestsRemaining;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Account account;
        private Subscription subscription;
        private Requests requests;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Account {
        private String firstname;
        private String lastname;
        private String email;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Subscription {
        private String plan;
        private String end;
        private boolean active;
    }

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Requests {
        private int current;
        private int limit_day;
    }
}

/*
"response": {
    "account": {
        "firstname": "HongChan",
        "lastname": "Kim",
        "email": "physickskim@gmail.com"
    },
    "subscription": {
        "plan": "Pro",
        "end": "2025-06-29T13:23:33+00:00",
        "active": true
    },
    "requests": {
        "current": 2,
        "limit_day": 7500
    }
}
 */