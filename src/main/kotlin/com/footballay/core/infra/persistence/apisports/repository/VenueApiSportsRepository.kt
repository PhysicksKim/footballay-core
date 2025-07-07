package com.footballay.core.infra.persistence.apisports.repository

import com.footballay.core.infra.persistence.apisports.entity.VenueApiSports
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VenueApiSportsRepository : JpaRepository<VenueApiSports, Long> {
    fun findAllByApiIdIn(apiIds: List<Long>): List<VenueApiSports>
    fun findByApiId(apiId: Long): VenueApiSports?
}