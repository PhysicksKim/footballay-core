package com.footballay.core.infra.apisports.backbone.sync.fixture.factory

import com.footballay.core.infra.apisports.backbone.sync.fixture.model.FixtureDataCollection
import com.footballay.core.infra.apisports.mapper.FixtureDataMapper
import com.footballay.core.infra.apisports.shared.dto.FixtureApiSportsCreateDto
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.VenueApiSports
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.logger
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

/**
 * FixtureApiSports 엔티티 생성 및 업데이트를 담당하는 Factory 클래스
 * 
 * @author Footballay Core Team
 * @since 1.0.0
 */
@Component
class FixtureApiSportsFactory(
    private val fixtureDataMapper: FixtureDataMapper
) {
    
    private val log = logger()

    /**
     * 새 FixtureApiSports 생성
     * 
     * @param dto Fixture 생성 DTO
     * @param fixtureData 수집된 데이터
     * @param venueMap VenueApiSports 맵
     * @param coreMap FixtureCore 맵
     * @return 새로 생성된 FixtureApiSports 엔티티
     */
    fun createFixtureApiSports(
        dto: FixtureApiSportsCreateDto,
        fixtureData: FixtureDataCollection,
        venueMap: Map<Long, VenueApiSports>,
        coreMap: Map<Long, FixtureCore>
    ): FixtureApiSports {
        val apiId = dto.apiId ?: throw IllegalArgumentException("ApiId is required for FixtureApiSports creation")
        
        // Core FK 설정
        val core = coreMap[apiId] ?: run {
            log.warn("Core not found for API ID: $apiId. Skipping fixture creation.")
            throw IllegalStateException("Core must exist for FixtureApiSports creation")
        }
        
        // Venue FK 설정 (nullable)
        val venue = dto.venue?.apiId?.let { venueMap[it] }
        
        // Season 설정
        val season = fixtureData.league.seasons.find { it.seasonYear.toString() == dto.seasonYear }
        
        return FixtureApiSports(
            apiId = apiId,
            core = core,
            venue = venue,
            season = season,
            referee = dto.referee,
            timezone = dto.timezone,
            date = parseOffsetDateTime(dto.date),
            timestamp = dto.timestamp,
            round = dto.round,
            status = fixtureDataMapper.mapStatusToApi(dto.status),
            score = fixtureDataMapper.mapScoreToApi(dto.score),
            preventUpdate = false // 새로 생성하는 것은 preventUpdate = false
        )
    }

    /**
     * 기존 FixtureApiSports 업데이트
     * 
     * @param existingFixture 기존 FixtureApiSports 엔티티
     * @param dto 업데이트할 Fixture DTO
     * @param fixtureData 수집된 데이터
     * @param venueMap VenueApiSports 맵
     * @param coreMap FixtureCore 맵
     * @return 업데이트된 FixtureApiSports 엔티티
     */
    fun updateFixtureApiSports(
        existingFixture: FixtureApiSports,
        dto: FixtureApiSportsCreateDto,
        fixtureData: FixtureDataCollection,
        venueMap: Map<Long, VenueApiSports>,
        coreMap: Map<Long, FixtureCore>
    ): FixtureApiSports {
        val apiId = dto.apiId ?: throw IllegalArgumentException("ApiId is required for FixtureApiSports update")
        
        // Core FK 설정 (기존에 없었다면 새로 연결)
        val core = coreMap[apiId] ?: run {
            log.warn("Core not found for API ID: $apiId. Skipping fixture update.")
            throw IllegalStateException("Core must exist for FixtureApiSports update")
        }
        
        // Venue FK 설정 (nullable)
        val venue = dto.venue?.apiId?.let { venueMap[it] }
        
        // Season 설정
        val season = fixtureData.league.seasons.find { it.seasonYear.toString() == dto.seasonYear }
        
        return existingFixture.apply {
            this.core = core
            this.venue = venue
            this.season = season
            this.referee = dto.referee
            this.timezone = dto.timezone
            this.date = parseOffsetDateTime(dto.date)
            this.timestamp = dto.timestamp
            this.round = dto.round
            this.status = fixtureDataMapper.mapStatusToApi(dto.status)
            this.score = fixtureDataMapper.mapScoreToApi(dto.score)
            // preventUpdate는 기존 값 유지
        }
    }

    /**
     * 날짜 문자열을 OffsetDateTime으로 파싱
     * 
     * @param dateString 파싱할 날짜 문자열
     * @return 파싱된 OffsetDateTime 또는 null
     */
    private fun parseOffsetDateTime(dateString: String?): OffsetDateTime? {
        return dateString?.let { 
            try {
                OffsetDateTime.parse(it)
            } catch (e: Exception) {
                null
            }
        }
    }
}
