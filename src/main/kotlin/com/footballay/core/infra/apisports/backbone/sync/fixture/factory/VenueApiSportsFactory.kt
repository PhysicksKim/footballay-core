package com.footballay.core.infra.apisports.backbone.sync.fixture.factory

import com.footballay.core.infra.apisports.shared.dto.VenueOfFixtureApiSportsCreateDto
import com.footballay.core.infra.persistence.apisports.entity.VenueApiSports
import org.springframework.stereotype.Component

/**
 * VenueApiSports 엔티티 생성 및 업데이트를 담당하는 Factory 클래스
 *
 * @author Footballay Core Team
 * @since 1.0.0
 */
@Component
class VenueApiSportsFactory {
    /**
     * 새 VenueApiSports 생성
     *
     * @param dto Venue 생성 DTO
     * @return 새로 생성된 VenueApiSports 엔티티
     */
    fun createVenueApiSports(dto: VenueOfFixtureApiSportsCreateDto): VenueApiSports =
        VenueApiSports(
            apiId = dto.apiId,
            name = dto.name,
            city = dto.city,
            // capacity, surface, image는 DTO에 없으므로 null로 설정
            capacity = null,
            surface = null,
            image = null,
            preventUpdate = false, // 새로 생성하는 것은 preventUpdate = false
        )

    /**
     * 기존 VenueApiSports 업데이트
     *
     * @param existingVenue 기존 VenueApiSports 엔티티
     * @param dto 업데이트할 Venue DTO
     * @return 업데이트된 VenueApiSports 엔티티
     */
    fun updateVenueApiSports(
        existingVenue: VenueApiSports,
        dto: VenueOfFixtureApiSportsCreateDto,
    ): VenueApiSports =
        existingVenue.apply {
            this.name = dto.name
            this.city = dto.city
            // capacity, surface, image는 DTO에 없으므로 기존 값 유지
            // preventUpdate는 기존 값 유지
        }
}
