package com.footballay.core.mockserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 특정 시간의 경기 스냅샷
 */
public record MatchSnapshot(
        @JsonProperty("minute") int minute,
        @JsonProperty("status") String status, // "NS", "1H", "HT", "2H", "FT"
        @JsonProperty("elapsed") int elapsed,
        @JsonProperty("info") JsonNode info, // FixtureInfoResponse 형태
        @JsonProperty("events") JsonNode events, // FixtureEventsResponse 형태
        @JsonProperty("lineup") JsonNode lineup, // FixtureLineupResponse 형태
        @JsonProperty("statistics") JsonNode statistics // MatchStatisticsResponse 형태
) {
}
