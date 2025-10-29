package com.footballay.core.infra.apisports

import com.footballay.core.infra.apisports.shared.dto.VenueOfFixtureApiSportsCreateDto
import com.footballay.core.infra.persistence.apisports.entity.VenueApiSports
import com.footballay.core.infra.persistence.apisports.repository.VenueApiSportsRepository
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * Venue 처리를 별도 트랜잭션으로 분리한 서비스
 * FixtureApiSports 저장과 독립적으로 Venue 처리를 수행
 */
@Service
class VenueApiSportsService(
    private val venueApiSportsRepository: VenueApiSportsRepository,
) {
    private val log = logger()

    /**
     * Venue 목록을 미리 조회하여 맵으로 반환
     * 신규 Venue가 있는 경우 별도 트랜잭션에서 저장 후 포함
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun processVenuesWithNewTransaction(venueDtos: List<VenueOfFixtureApiSportsCreateDto>): Map<Long, VenueApiSports> {
        if (venueDtos.isEmpty()) {
            return emptyMap()
        }

        val venueApiIds = venueDtos.mapNotNull { it.apiId }.distinct()
        val existingVenueMap =
            venueApiSportsRepository
                .findAllByApiIdIn(venueApiIds)
                .associateBy { it.apiId }

        val resultMap = mutableMapOf<Long, VenueApiSports>()

        venueDtos.forEach { venueDto ->
            val apiId = venueDto.apiId ?: return@forEach

            val venue =
                existingVenueMap[apiId]?.let { existingVenue ->
                    // 기존 Venue 업데이트
                    updateExistingVenue(existingVenue, venueDto)
                } ?: run {
                    // 새 Venue 생성
                    createNewVenue(venueDto)
                }

            resultMap[apiId] = venue
        }

        log.info("Processed ${resultMap.size} venues in separate transaction")
        return resultMap
    }

    /**
     * 기존 Venue 업데이트
     */
    private fun updateExistingVenue(
        venue: VenueApiSports,
        dto: VenueOfFixtureApiSportsCreateDto,
    ): VenueApiSports {
        venue.apply {
            name = dto.name
            city = dto.city
            // address, capacity, surface, image는 DTO에 없으므로 기존 값 유지
        }
        return venueApiSportsRepository.save(venue)
    }

    /**
     * 새 Venue 생성
     */
    private fun createNewVenue(dto: VenueOfFixtureApiSportsCreateDto): VenueApiSports {
        val newVenue =
            VenueApiSports(
                apiId = dto.apiId,
                name = dto.name,
                city = dto.city,
                // address, capacity, surface, image는 DTO에 없으므로 null
            )
        val savedVenue = venueApiSportsRepository.save(newVenue)
        log.info("Created new venue: ${dto.name} (API ID: ${dto.apiId})")
        return savedVenue
    }
}
