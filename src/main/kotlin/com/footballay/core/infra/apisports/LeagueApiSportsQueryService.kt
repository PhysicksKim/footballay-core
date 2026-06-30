package com.footballay.core.infra.apisports

import com.footballay.core.infra.apisports.shared.model.LeagueApiSportsInfo
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * ApiSports 리그 데이터 읽기 전용 서비스
 * infra/apisports 내부에서 반복적으로 사용되는 리그 조회 로직을 담당
 */
@Service
class LeagueApiSportsQueryService(
    private val leagueApiSportsRepository: LeagueApiSportsRepository,
) {
    private val log = logger()

    /**
     * API ID로 리그 정보 조회
     */
    @Transactional(readOnly = true)
    fun findByApiId(apiId: Long): LeagueApiSportsInfo? {
        log.debug("Querying league by apiId: $apiId")

        return leagueApiSportsRepository
            .findByApiId(apiId)
            ?.let { mapToInfo(it) }
    }

    /**
     * API ID로 리그 정보 조회 (필수)
     */
    @Transactional(readOnly = true)
    fun findByApiIdOrThrow(apiId: Long): LeagueApiSportsInfo =
        findByApiId(apiId)
            ?: throw IllegalArgumentException("League not found with apiId: $apiId")

    /**
     * 특정 국가의 리그들 조회
     */
    @Transactional(readOnly = true)
    fun findByCountryCode(countryCode: String): List<LeagueApiSportsInfo> {
        log.debug("Querying leagues by countryCode: $countryCode")

        return leagueApiSportsRepository
            .findByCountryCode(countryCode)
            .map { mapToInfo(it) }
    }

    /**
     * 엔티티를 도메인 모델로 변환
     */
    private fun mapToInfo(entity: LeagueApiSports): LeagueApiSportsInfo =
        LeagueApiSportsInfo(
            apiId = entity.apiId,
            name = entity.name,
            currentSeason = entity.currentSeason,
            type = entity.type,
            countryName = entity.countryName,
            countryCode = entity.countryCode,
        )
}
