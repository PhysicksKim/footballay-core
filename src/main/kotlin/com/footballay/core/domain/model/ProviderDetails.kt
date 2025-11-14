package com.footballay.core.domain.model

/**
 * Provider별 상세 정보를 담는 공통 인터페이스
 *
 * Frontend에서 when-is 패턴으로 타입 체크 및 형변환에 사용됩니다.
 * 각 구현체는 type 필드를 통해 detailsType을 제공하여 프론트엔드가
 * 타입을 쉽게 구분할 수 있도록 합니다.
 *
 * **사용 예시 (Backend - Kotlin):**
 * ```kotlin
 * when (val details = team.details) {
 *     is ApiSportsTeamDetails -> {
 *         println("Founded: ${details.founded}")
 *         println("Logo: ${details.logo}")
 *     }
 * }
 * ```
 *
 * **사용 예시 (Frontend - TypeScript):**
 * ```typescript
 * if (team.detailsType === "ApiSports") {
 *     const details = team.details as ApiSportsTeamDetails;
 *     console.log("Founded:", details.founded);
 * }
 * ```
 */
sealed interface ProviderDetails {
    /**
     * Provider 타입 식별자
     * JSON 직렬화 시 detailsType으로 노출되어 프론트엔드에서 타입 구분에 사용
     */
    val type: String
}

/**
 * ApiSports League 상세 정보
 *
 * League 엔티티의 ApiSports provider 전용 상세 데이터
 */
data class ApiSportsLeagueDetails(
    /**
     * 리그 로고 URL
     */
    val logo: String?,
    /**
     * 국가 코드 (ISO 3166-1 alpha-2, 예: "GB", "ES", "IT")
     */
    val countryCode: String?,
    /**
     * 국가 국기 이미지 URL
     */
    val countryFlag: String?,
) : ProviderDetails {
    override val type: String = "ApiSports"
}

/**
 * ApiSports Team 상세 정보
 *
 * Team 엔티티의 ApiSports provider 전용 상세 데이터
 */
data class ApiSportsTeamDetails(
    /**
     * 팀 창단 연도 (예: 1880)
     */
    val founded: Int?,
    /**
     * 국가 대표팀 여부
     * - true: 국가 대표팀
     * - false: 클럽 팀
     */
    val national: Boolean,
    /**
     * 팀 로고 URL
     */
    val logo: String?,
) : ProviderDetails {
    override val type: String = "ApiSports"
}

/**
 * ApiSports Player 상세 정보
 *
 * Player 엔티티의 ApiSports provider 전용 상세 데이터
 */
data class ApiSportsPlayerDetails(
    /**
     * 선수 나이
     */
    val age: Int?,
    /**
     * 국적 (예: "Belgium", "England")
     */
    val nationality: String?,
    /**
     * 신장 (예: "181 cm")
     */
    val height: String?,
    /**
     * 체중 (예: "68 kg")
     */
    val weight: String?,
    /**
     * 선수 사진 URL
     */
    val photo: String?,
) : ProviderDetails {
    override val type: String = "ApiSports"
}

/**
 * 향후 다른 Provider 추가 예시:
 *
 * ```kotlin
 * data class SportMonksTeamDetails(
 *     val venue: String?,
 *     val capacity: Int?,
 *     val website: String?
 * ) : ProviderDetails {
 *     override val type: String = "SportMonks"
 * }
 * ```
 */
