package com.footballay.core.web.football.service

import com.footballay.core.web.football.dto.*
import com.footballay.core.web.football.mapper.MatchDataMapper
import com.footballay.core.infra.query.MatchDataQueryService
import com.footballay.core.logger
import com.footballay.core.web.common.dto.ApiResponseV2
import com.footballay.core.web.common.dto.ErrorDetail
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Footballay Fixture Web Service
 *
 * **책임:**
 * - Controller와 Domain Service 중재
 * - UID 기반 match data 조회 및 DTO 변환
 * - ApiResponseV2 생성
 * - 예외 처리 및 에러 응답 생성
 */
@Service
@Transactional(readOnly = true)
class FootballayFixtureWebService(
    private val matchDataQueryService: MatchDataQueryService,
    private val matchDataMapper: MatchDataMapper,
) {
    private val log = logger()

    /**
     * Fixture 기본 정보 조회
     *
     * @param fixtureUid Fixture UID
     * @return ApiResponseV2<FixtureInfoDto>
     */
    fun getFixtureInfo(fixtureUid: String): ApiResponseV2<FixtureInfoDto> {
        log.info("getFixtureInfo. fixtureUid={}", fixtureUid)

        return try {
            val fixture = matchDataQueryService.getFixtureInfo(fixtureUid)
            val dto = matchDataMapper.toFixtureInfoDto(fixture)
            ApiResponseV2.success(dto)
        } catch (e: IllegalArgumentException) {
            log.warn("Fixture not found: {}", fixtureUid, e)
            ApiResponseV2.failure(
                error = ErrorDetail(message = e.message ?: "Fixture not found"),
                code = 404,
            )
        } catch (e: Exception) {
            log.error("Error while fetching fixture info for uid: {}", fixtureUid, e)
            ApiResponseV2.failure(
                error = ErrorDetail(message = "Failed to fetch fixture info"),
                code = 500,
            )
        }
    }

    /**
     * Fixture 라이브 상태 조회
     *
     * @param fixtureUid Fixture UID
     * @return ApiResponseV2<FixtureLiveStatusDto>
     */
    fun getFixtureLiveStatus(fixtureUid: String): ApiResponseV2<FixtureLiveStatusDto> {
        log.info("getFixtureLiveStatus. fixtureUid={}", fixtureUid)

        return try {
            val fixture = matchDataQueryService.getFixtureLiveStatus(fixtureUid)
            val dto = matchDataMapper.toFixtureLiveStatusDto(fixture)
            ApiResponseV2.success(dto)
        } catch (e: IllegalArgumentException) {
            log.warn("Fixture not found: {}", fixtureUid, e)
            ApiResponseV2.failure(
                error = ErrorDetail(message = e.message ?: "Fixture not found"),
                code = 404,
            )
        } catch (e: Exception) {
            log.error("Error while fetching fixture live status for uid: {}", fixtureUid, e)
            ApiResponseV2.failure(
                error = ErrorDetail(message = "Failed to fetch fixture live status"),
                code = 500,
            )
        }
    }

    /**
     * Fixture 이벤트 조회
     *
     * @param fixtureUid Fixture UID
     * @return ApiResponseV2<FixtureEventsDto>
     */
    fun getFixtureEvents(fixtureUid: String): ApiResponseV2<FixtureEventsDto> {
        log.info("getFixtureEvents. fixtureUid={}", fixtureUid)

        return try {
            val events = matchDataQueryService.getFixtureEvents(fixtureUid)
            val dto = matchDataMapper.toFixtureEventsDto(fixtureUid, events)
            ApiResponseV2.success(dto)
        } catch (e: IllegalArgumentException) {
            log.warn("Fixture not found: {}", fixtureUid, e)
            ApiResponseV2.failure(
                error = ErrorDetail(message = e.message ?: "Fixture not found"),
                code = 404,
            )
        } catch (e: Exception) {
            log.error("Error while fetching fixture events for uid: {}", fixtureUid, e)
            ApiResponseV2.failure(
                error = ErrorDetail(message = "Failed to fetch fixture events"),
                code = 500,
            )
        }
    }

    /**
     * Fixture 라인업 조회
     *
     * @param fixtureUid Fixture UID
     * @return ApiResponseV2<FixtureLineupDto>
     */
    fun getFixtureLineup(fixtureUid: String): ApiResponseV2<FixtureLineupDto> {
        log.info("getFixtureLineup. fixtureUid={}", fixtureUid)

        return try {
            val (homeTeamFixture, awayTeamFixture) = matchDataQueryService.getFixtureLineup(fixtureUid)
            val dto = matchDataMapper.toFixtureLineupDto(fixtureUid, homeTeamFixture, awayTeamFixture)
            ApiResponseV2.success(dto)
        } catch (e: IllegalArgumentException) {
            log.warn("Fixture not found: {}", fixtureUid, e)
            ApiResponseV2.failure(
                error = ErrorDetail(message = e.message ?: "Fixture not found"),
                code = 404,
            )
        } catch (e: Exception) {
            log.error("Error while fetching fixture lineup for uid: {}", fixtureUid, e)
            ApiResponseV2.failure(
                error = ErrorDetail(message = "Failed to fetch fixture lineup"),
                code = 500,
            )
        }
    }

    /**
     * Fixture 통계 조회
     *
     * @param fixtureUid Fixture UID
     * @return ApiResponseV2<FixtureStatisticsDto>
     */
    fun getFixtureStatistics(fixtureUid: String): ApiResponseV2<FixtureStatisticsDto> {
        log.info("getFixtureStatistics. fixtureUid={}", fixtureUid)

        return try {
            // Fixture 기본 정보 조회 (상태, elapsed 등)
            val fixture = matchDataQueryService.getFixtureInfo(fixtureUid)

            // 홈/원정 팀 통계 조회
            val (homeTeamFixture, awayTeamFixture) = matchDataQueryService.getFixtureStatistics(fixtureUid)

            val dto = matchDataMapper.toFixtureStatisticsDto(fixtureUid, fixture, homeTeamFixture, awayTeamFixture)
            ApiResponseV2.success(dto)
        } catch (e: IllegalArgumentException) {
            log.warn("Fixture not found: {}", fixtureUid, e)
            ApiResponseV2.failure(
                error = ErrorDetail(message = e.message ?: "Fixture not found"),
                code = 404,
            )
        } catch (e: Exception) {
            log.error("Error while fetching fixture statistics for uid: {}", fixtureUid, e)
            ApiResponseV2.failure(
                error = ErrorDetail(message = "Failed to fetch fixture statistics"),
                code = 500,
            )
        }
    }
}
