package com.footballay.core.mockserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Mock Server용 경기 시나리오 모델
 */
public record MatchScenario(
        @JsonProperty("fixtureId") long fixtureId,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("mode") String mode, // "realtime" or "accelerated"
        @JsonProperty("speedMultiplier") int speedMultiplier, // 1초 = N분
        @JsonProperty("snapshots") List<MatchSnapshot> snapshots
) {

    /**
     * 주어진 경과 시간(분)에 해당하는 스냅샷 반환
     * @param elapsedMinutes 경과 시간(분)
     * @return 해당 시간의 스냅샷 (없으면 가장 최근 스냅샷)
     */
    public MatchSnapshot getSnapshotAt(int elapsedMinutes) {
        MatchSnapshot result = snapshots.get(0);

        for (MatchSnapshot snapshot : snapshots) {
            if (snapshot.minute() <= elapsedMinutes) {
                result = snapshot;
            } else {
                break;
            }
        }

        return result;
    }

    /**
     * 경기 종료 여부
     * @param elapsedMinutes 경과 시간(분)
     * @return true if match finished
     */
    public boolean isFinished(int elapsedMinutes) {
        MatchSnapshot snapshot = getSnapshotAt(elapsedMinutes);
        return snapshot.status().equals("FT");
    }
}
