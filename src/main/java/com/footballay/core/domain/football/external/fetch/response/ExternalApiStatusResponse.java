package com.footballay.core.domain.football.external.fetch.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalApiStatusResponse {
    private String get;
    private List<String> parameters;
    private List<String> errors;
    private int results;
    private Map<String, Integer> paging;
    private Response response;
    private _Headers headers;


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class _Headers {
        private int xRatelimitLimit;
        private int xRatelimitRemaining;
        private int xRatelimitRequestsLimit;
        private int xRatelimitRequestsRemaining;

        public int getXRatelimitLimit() {
            return this.xRatelimitLimit;
        }

        public int getXRatelimitRemaining() {
            return this.xRatelimitRemaining;
        }

        public int getXRatelimitRequestsLimit() {
            return this.xRatelimitRequestsLimit;
        }

        public int getXRatelimitRequestsRemaining() {
            return this.xRatelimitRequestsRemaining;
        }

        public void setXRatelimitLimit(final int xRatelimitLimit) {
            this.xRatelimitLimit = xRatelimitLimit;
        }

        public void setXRatelimitRemaining(final int xRatelimitRemaining) {
            this.xRatelimitRemaining = xRatelimitRemaining;
        }

        public void setXRatelimitRequestsLimit(final int xRatelimitRequestsLimit) {
            this.xRatelimitRequestsLimit = xRatelimitRequestsLimit;
        }

        public void setXRatelimitRequestsRemaining(final int xRatelimitRequestsRemaining) {
            this.xRatelimitRequestsRemaining = xRatelimitRequestsRemaining;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "ExternalApiStatusResponse._Headers(xRatelimitLimit=" + this.getXRatelimitLimit() + ", xRatelimitRemaining=" + this.getXRatelimitRemaining() + ", xRatelimitRequestsLimit=" + this.getXRatelimitRequestsLimit() + ", xRatelimitRequestsRemaining=" + this.getXRatelimitRequestsRemaining() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Account account;
        private Subscription subscription;
        private Requests requests;

        public Account getAccount() {
            return this.account;
        }

        public Subscription getSubscription() {
            return this.subscription;
        }

        public Requests getRequests() {
            return this.requests;
        }

        public void setAccount(final Account account) {
            this.account = account;
        }

        public void setSubscription(final Subscription subscription) {
            this.subscription = subscription;
        }

        public void setRequests(final Requests requests) {
            this.requests = requests;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "ExternalApiStatusResponse.Response(account=" + this.getAccount() + ", subscription=" + this.getSubscription() + ", requests=" + this.getRequests() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Account {
        private String firstname;
        private String lastname;
        private String email;

        public String getFirstname() {
            return this.firstname;
        }

        public String getLastname() {
            return this.lastname;
        }

        public String getEmail() {
            return this.email;
        }

        public void setFirstname(final String firstname) {
            this.firstname = firstname;
        }

        public void setLastname(final String lastname) {
            this.lastname = lastname;
        }

        public void setEmail(final String email) {
            this.email = email;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "ExternalApiStatusResponse.Account(firstname=" + this.getFirstname() + ", lastname=" + this.getLastname() + ", email=" + this.getEmail() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Subscription {
        private String plan;
        private String end;
        private boolean active;

        public String getPlan() {
            return this.plan;
        }

        public String getEnd() {
            return this.end;
        }

        public boolean isActive() {
            return this.active;
        }

        public void setPlan(final String plan) {
            this.plan = plan;
        }

        public void setEnd(final String end) {
            this.end = end;
        }

        public void setActive(final boolean active) {
            this.active = active;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "ExternalApiStatusResponse.Subscription(plan=" + this.getPlan() + ", end=" + this.getEnd() + ", active=" + this.isActive() + ")";
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Requests {
        private int current;
        private int limit_day;

        public int getCurrent() {
            return this.current;
        }

        public int getLimit_day() {
            return this.limit_day;
        }

        public void setCurrent(final int current) {
            this.current = current;
        }

        public void setLimit_day(final int limit_day) {
            this.limit_day = limit_day;
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "ExternalApiStatusResponse.Requests(current=" + this.getCurrent() + ", limit_day=" + this.getLimit_day() + ")";
        }
    }

    public String getGet() {
        return this.get;
    }

    public List<String> getParameters() {
        return this.parameters;
    }

    public List<String> getErrors() {
        return this.errors;
    }

    public int getResults() {
        return this.results;
    }

    public Map<String, Integer> getPaging() {
        return this.paging;
    }

    public Response getResponse() {
        return this.response;
    }

    public _Headers getHeaders() {
        return this.headers;
    }

    public void setGet(final String get) {
        this.get = get;
    }

    public void setParameters(final List<String> parameters) {
        this.parameters = parameters;
    }

    public void setErrors(final List<String> errors) {
        this.errors = errors;
    }

    public void setResults(final int results) {
        this.results = results;
    }

    public void setPaging(final Map<String, Integer> paging) {
        this.paging = paging;
    }

    public void setResponse(final Response response) {
        this.response = response;
    }

    public void setHeaders(final _Headers headers) {
        this.headers = headers;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "ExternalApiStatusResponse(get=" + this.getGet() + ", parameters=" + this.getParameters() + ", errors=" + this.getErrors() + ", results=" + this.getResults() + ", paging=" + this.getPaging() + ", response=" + this.getResponse() + ", headers=" + this.getHeaders() + ")";
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