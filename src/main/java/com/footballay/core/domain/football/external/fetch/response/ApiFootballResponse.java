package com.footballay.core.domain.football.external.fetch.response;

import java.util.List;
import java.util.Map;

public abstract class ApiFootballResponse {
    private String get;
    private Map<String, String> parameters;
    private List<String> errors;
    private int results;
    private Map<String, Integer> paging;

    public String getGet() {
        return this.get;
    }

    public Map<String, String> getParameters() {
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

    public void setGet(final String get) {
        this.get = get;
    }

    public void setParameters(final Map<String, String> parameters) {
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
}
