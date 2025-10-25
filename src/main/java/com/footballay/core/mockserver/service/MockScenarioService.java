package com.footballay.core.mockserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.footballay.core.mockserver.model.MatchScenario;
import com.footballay.core.mockserver.model.MatchSnapshot;
import com.footballay.core.web.common.dto.ApiResponse;
import com.footballay.core.web.common.service.ApiCommonResponseService;
import com.footballay.core.web.football.response.FixtureOfLeagueResponse;
import com.footballay.core.web.football.response.LeagueResponse;
import com.footballay.core.web.football.response.MatchStatisticsResponse;
import com.footballay.core.web.football.response.fixture.FixtureEventsResponse;
import com.footballay.core.web.football.response.fixture.FixtureInfoResponse;
import com.footballay.core.web.football.response.fixture.FixtureLineupResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock Server 시나리오 관리 및 재생 서비스
 */
@Service
@Profile("mockserver")
public class MockScenarioService {
    private static final Logger log = LoggerFactory.getLogger(MockScenarioService.class);

    private final ObjectMapper objectMapper;
    private final ApiCommonResponseService apiCommonResponseService;

    // 시나리오 캐시: fixtureId -> MatchScenario
    private final Map<Long, MatchScenario> scenarios = new ConcurrentHashMap<>();

    // 경기 시작 시간 추적: fixtureId -> Instant
    private final Map<Long, Instant> matchStartTimes = new ConcurrentHashMap<>();

    // 리그 목록 (정적)
    private JsonNode leaguesData;

    // 경기 목록 (정적)
    private JsonNode fixturesData;

    public MockScenarioService(ObjectMapper objectMapper,
                               ApiCommonResponseService apiCommonResponseService) {
        this.objectMapper = objectMapper;
        this.apiCommonResponseService = apiCommonResponseService;
    }

    @PostConstruct
    public void init() {
        loadScenarios();
        loadStaticData();
        log.info("MockScenarioService initialized with {} scenarios", scenarios.size());
    }

    /**
     * 시나리오 파일 로드
     */
    private void loadScenarios() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:mockserver/scenarios/match-*.json");

            for (Resource resource : resources) {
                try {
                    MatchScenario scenario = objectMapper.readValue(
                            resource.getInputStream(), MatchScenario.class);
                    scenarios.put(scenario.fixtureId(), scenario);
                    log.info("Loaded scenario: {} (fixtureId={})", scenario.name(), scenario.fixtureId());
                } catch (IOException e) {
                    log.error("Failed to load scenario from {}", resource.getFilename(), e);
                }
            }
        } catch (IOException e) {
            log.error("Failed to load scenarios", e);
        }
    }

    /**
     * 정적 데이터 (리그, 경기 목록) 로드
     */
    private void loadStaticData() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            // 리그 목록
            Resource leaguesResource = resolver.getResource("classpath:mockserver/scenarios/leagues.json");
            leaguesData = objectMapper.readTree(leaguesResource.getInputStream());

            // 경기 목록
            Resource fixturesResource = resolver.getResource("classpath:mockserver/scenarios/fixtures.json");
            fixturesData = objectMapper.readTree(fixturesResource.getInputStream());

            log.info("Loaded static data: leagues, fixtures");
        } catch (IOException e) {
            log.error("Failed to load static data", e);
        }
    }

    /**
     * 리그 목록 조회
     */
    public ApiResponse<LeagueResponse> getLeagueList(String requestUrl) {
        try {
            LeagueResponse[] leagues = objectMapper.treeToValue(
                    leaguesData.get("response"), LeagueResponse[].class);
            return apiCommonResponseService.createSuccessResponse(leagues, requestUrl);
        } catch (Exception e) {
            log.error("Failed to get league list", e);
            return apiCommonResponseService.createFailureResponse("Failed to get league list", requestUrl);
        }
    }

    /**
     * 경기 목록 조회
     */
    public ApiResponse<FixtureOfLeagueResponse> getFixtures(String requestUrl, Map<String, String> params) {
        try {
            // 모든 시나리오의 fixtureId를 기반으로 경기 목록 반환
            List<FixtureOfLeagueResponse> fixtures = new ArrayList<>();
            JsonNode fixturesArray = fixturesData.get("response");

            if (fixturesArray != null && fixturesArray.isArray()) {
                for (JsonNode node : fixturesArray) {
                    FixtureOfLeagueResponse fixture = objectMapper.treeToValue(
                            node, FixtureOfLeagueResponse.class);
                    fixtures.add(fixture);
                }
            }

            return apiCommonResponseService.createSuccessResponse(
                    fixtures.toArray(new FixtureOfLeagueResponse[0]), requestUrl, params);
        } catch (Exception e) {
            log.error("Failed to get fixtures", e);
            return apiCommonResponseService.createFailureResponse("Failed to get fixtures", requestUrl, params);
        }
    }

    /**
     * 경기 정보 조회 (시간 흐름 시뮬레이션)
     */
    public ApiResponse<FixtureInfoResponse> getFixtureInfo(Long fixtureId, String requestUrl, Map<String, String> params) {
        try {
            MatchSnapshot snapshot = getCurrentSnapshot(fixtureId);
            FixtureInfoResponse info = objectMapper.treeToValue(
                    snapshot.info(), FixtureInfoResponse.class);

            return apiCommonResponseService.createSuccessResponse(
                    new FixtureInfoResponse[]{info}, requestUrl, params);
        } catch (Exception e) {
            log.error("Failed to get fixture info for fixtureId={}", fixtureId, e);
            return apiCommonResponseService.createFailureResponse(
                    "Failed to get fixture info", requestUrl, params);
        }
    }

    /**
     * 경기 이벤트 조회 (시간 흐름 시뮬레이션)
     */
    public ApiResponse<FixtureEventsResponse> getFixtureEvents(Long fixtureId, String requestUrl, Map<String, String> params) {
        try {
            MatchSnapshot snapshot = getCurrentSnapshot(fixtureId);
            FixtureEventsResponse events = objectMapper.treeToValue(
                    snapshot.events(), FixtureEventsResponse.class);

            return apiCommonResponseService.createSuccessResponse(
                    new FixtureEventsResponse[]{events}, requestUrl, params);
        } catch (Exception e) {
            log.error("Failed to get fixture events for fixtureId={}", fixtureId, e);
            return apiCommonResponseService.createFailureResponse(
                    "Failed to get fixture events", requestUrl, params);
        }
    }

    /**
     * 라인업 조회 (시간 흐름 시뮬레이션)
     */
    public ApiResponse<FixtureLineupResponse> getFixtureLineup(Long fixtureId, String requestUrl, Map<String, String> params) {
        try {
            MatchSnapshot snapshot = getCurrentSnapshot(fixtureId);
            FixtureLineupResponse lineup = objectMapper.treeToValue(
                    snapshot.lineup(), FixtureLineupResponse.class);

            return apiCommonResponseService.createSuccessResponse(
                    new FixtureLineupResponse[]{lineup}, requestUrl, params);
        } catch (Exception e) {
            log.error("Failed to get fixture lineup for fixtureId={}", fixtureId, e);
            return apiCommonResponseService.createFailureResponse(
                    "Failed to get fixture lineup", requestUrl, params);
        }
    }

    /**
     * 경기 통계 조회 (시간 흐름 시뮬레이션)
     */
    public ApiResponse<MatchStatisticsResponse> getFixtureStatistics(Long fixtureId, String requestUrl, Map<String, String> params) {
        try {
            MatchSnapshot snapshot = getCurrentSnapshot(fixtureId);
            MatchStatisticsResponse statistics = objectMapper.treeToValue(
                    snapshot.statistics(), MatchStatisticsResponse.class);

            return apiCommonResponseService.createSuccessResponse(
                    new MatchStatisticsResponse[]{statistics}, requestUrl, params);
        } catch (Exception e) {
            log.error("Failed to get fixture statistics for fixtureId={}", fixtureId, e);
            return apiCommonResponseService.createFailureResponse(
                    "Failed to get fixture statistics", requestUrl, params);
        }
    }

    /**
     * 현재 시간에 해당하는 스냅샷 조회
     */
    private MatchSnapshot getCurrentSnapshot(Long fixtureId) {
        // 1. 시나리오 확인
        MatchScenario scenario = scenarios.get(fixtureId);
        if (scenario == null) {
            throw new IllegalArgumentException("No scenario found for fixtureId: " + fixtureId);
        }

        // 2. 경기 시작 시간 확인 (없으면 지금 시작)
        matchStartTimes.putIfAbsent(fixtureId, Instant.now());

        // 3. 경과 시간 계산
        int elapsedMinutes = calculateElapsedMinutes(fixtureId, scenario);

        // 4. 현재 시간 스냅샷 반환
        MatchSnapshot snapshot = scenario.getSnapshotAt(elapsedMinutes);

        log.debug("fixtureId={}, elapsed={}min, status={}, snapshot.minute={}",
                fixtureId, elapsedMinutes, snapshot.status(), snapshot.minute());

        return snapshot;
    }

    /**
     * 경과 시간 계산
     */
    private int calculateElapsedMinutes(Long fixtureId, MatchScenario scenario) {
        Instant startTime = matchStartTimes.get(fixtureId);
        long realSeconds = Duration.between(startTime, Instant.now()).getSeconds();

        // 가속 모드: speedMultiplier 적용
        // 예: speedMultiplier=1 -> 1초 = 1분
        //     speedMultiplier=90 -> 90초 = 90분 (1초에 1분씩)
        return (int) (realSeconds * scenario.speedMultiplier());
    }

    /**
     * 경기 시작 시간 리셋 (테스트용)
     */
    public void resetMatchStartTime(Long fixtureId) {
        matchStartTimes.remove(fixtureId);
        log.info("Reset match start time for fixtureId={}", fixtureId);
    }
}
