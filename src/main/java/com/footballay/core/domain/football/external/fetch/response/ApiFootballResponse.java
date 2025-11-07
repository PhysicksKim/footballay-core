package com.footballay.core.domain.football.external.fetch.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.footballay.core.domain.football.external.fetch.ApiError;
import com.footballay.core.domain.football.external.fetch.FlexibleErrorDeserializer;

import java.util.List;
import java.util.Map;

public abstract class ApiFootballResponse {
    private String get;
    private Map<String, String> parameters;
    @JsonDeserialize(using = FlexibleErrorDeserializer.class)
    private ApiError errors;  // 여기만 바꾸면 됨!
    private int results;
    private Map<String, Integer> paging;

    public String getGet() {
        return this.get;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public ApiError getErrors() {
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

    public void setErrors(final ApiError errors) {
        this.errors = errors;
    }

    public void setResults(final int results) {
        this.results = results;
    }

    public void setPaging(final Map<String, Integer> paging) {
        this.paging = paging;
    }
}
