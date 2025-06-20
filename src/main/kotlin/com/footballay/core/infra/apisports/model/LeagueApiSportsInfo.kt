package com.footballay.core.infra.apisports.model

/**
 * ApiSports 리그 정보를 담는 도메인 모델
 * infra/apisports 내부에서 사용하는 경량 도메인 객체
 */
data class LeagueApiSportsInfo(
    val apiId: Long,
    val name: String,
    val currentSeason: Int?,
    val type: String? = null,
    val countryName: String? = null,
    val countryCode: String? = null,
) {
    
    /**
     * 현재 시즌이 설정되어 있는지 확인
     */
    fun hasCurrentSeason(): Boolean = currentSeason != null
    
    /**
     * 현재 시즌을 반환하거나 예외 발생
     */
    fun getCurrentSeasonOrThrow(): Int {
        return currentSeason ?: throw IllegalStateException("Current season is not set for league $name (apiId: $apiId)")
    }
} 