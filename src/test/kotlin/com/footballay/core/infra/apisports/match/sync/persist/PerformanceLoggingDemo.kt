package com.footballay.core.infra.apisports.match.sync.persist

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

// TODO : 이전에 AI 가 작성하다가 token 초과해서 session 이 끊긴 코드입니다. 향후 확인 필요

/**
 * 성능 로깅 데모
 *
 * **용도:**
 * - 개발 중 성능 로그 출력 확인
 * - 로그 포맷 검증
 * - 임계값 테스트
 *
 * **주의:**
 * - 실제 테스트 아님 (assert 없음)
 * - @Disabled로 CI/CD에서 제외
 * - 로컬에서만 실행하여 확인
 *
 * **실행 방법:**
 * ```
 * ./gradlew test --tests "PerformanceLoggingDemo" -Dtest.disable=false
 * ```
 */
@Disabled("개발 중 확인용 - CI/CD에서 실행 안 함")
class PerformanceLoggingDemo {
    @Test
    @DisplayName("성능 로그 출력 확인 - 정상 케이스")
    fun `데모 - 정상 성능 로그`() {
        // 실제 MatchEntitySyncServiceImpl을 호출하면
        // 아래와 같은 로그가 출력됩니다:

        println(
            """
            ========================================
            예상 로그 출력:
            ========================================

            2025-01-15 10:30:15.123 INFO  MatchEntitySyncServiceImpl - Starting entity sync for fixture: 1208021
            2025-01-15 10:30:15.623 DEBUG MatchEntitySyncServiceImpl - [Phase1_LoadContext] completed in 500ms
            2025-01-15 10:30:15.923 DEBUG MatchEntitySyncServiceImpl - [Phase2_BaseEntities] completed in 300ms
            2025-01-15 10:30:17.423 DEBUG MatchEntitySyncServiceImpl - [Phase3_MatchPlayers] completed in 1500ms
            2025-01-15 10:30:17.923 DEBUG MatchEntitySyncServiceImpl - [Phase4_MatchEvents] completed in 500ms
            2025-01-15 10:30:19.423 DEBUG MatchEntitySyncServiceImpl - [Phase5_PlayerStats] completed in 1500ms
            2025-01-15 10:30:19.623 DEBUG MatchEntitySyncServiceImpl - [Phase6_TeamStats] completed in 200ms

            2025-01-15 10:30:19.623 DEBUG MatchEntitySyncServiceImpl -
            ========================================
            Performance Report - Fixture: 1208021
            ========================================
            Total Transaction Time: 4500ms
            ----------------------------------------
            Phase5_PlayerStats  :  1500ms ( 33%) ██████
            Phase3_MatchPlayers :  1500ms ( 33%) ██████
            Phase1_LoadContext  :   500ms ( 11%) ██
            Phase4_MatchEvents  :   500ms ( 11%) ██
            Phase2_BaseEntities :   300ms (  6%) █
            Phase6_TeamStats    :   200ms (  4%)
            ----------------------------------------
            Slowest Phase: Phase5_PlayerStats (1500ms, 33%)
            ========================================
            """.trimIndent(),
        )
    }

    @Test
    @DisplayName("성능 로그 출력 확인 - 경고 케이스")
    fun `데모 - 느린 Phase 경고 로그`() {
        println(
            """
            ========================================
            예상 로그 출력 (느린 Phase):
            ========================================

            2025-01-15 14:30:15.123 INFO  MatchEntitySyncServiceImpl - Starting entity sync for fixture: 1208022
            2025-01-15 14:30:16.323 WARN  MatchEntitySyncServiceImpl - [Phase1_LoadContext] took too long: 1200ms (threshold: 1000ms)
            2025-01-15 14:30:19.823 WARN  MatchEntitySyncServiceImpl - [Phase3_MatchPlayers] took too long: 3500ms (threshold: 3000ms)

            2025-01-15 14:30:22.323 INFO  MatchEntitySyncServiceImpl -
            ========================================
            Performance Report - Fixture: 1208022
            ========================================
            Total Transaction Time: 7200ms
            ----------------------------------------
            Phase3_MatchPlayers :  3500ms ( 48%) █████████
            Phase5_PlayerStats  :  1800ms ( 25%) █████
            Phase1_LoadContext  :  1200ms ( 16%) ███
            Phase4_MatchEvents  :   700ms (  9%) █
            Phase2_BaseEntities :   500ms (  6%) █
            Phase6_TeamStats    :   300ms (  4%)
            ----------------------------------------
            Slowest Phase: Phase3_MatchPlayers (3500ms, 48%)
            ========================================
            """.trimIndent(),
        )
    }

    @Test
    @DisplayName("성능 로그 출력 확인 - 병목 탐지")
    fun `데모 - 병목 탐지 로그`() {
        println(
            """
            ========================================
            예상 로그 출력 (병목 탐지):
            ========================================

            2025-01-15 20:30:15.123 INFO  MatchEntitySyncServiceImpl - Starting entity sync for fixture: 1208024
            2025-01-15 20:30:21.123 WARN  MatchEntitySyncServiceImpl - [Phase3_MatchPlayers] took too long: 6000ms (threshold: 3000ms)

            2025-01-15 20:30:22.123 WARN  MatchEntitySyncServiceImpl -
            ========================================
            Performance Report - Fixture: 1208024
            ========================================
            Total Transaction Time: 7000ms
            ----------------------------------------
            Phase3_MatchPlayers :  6000ms ( 85%) █████████████████
            Phase5_PlayerStats  :   500ms (  7%) █
            Phase1_LoadContext  :   200ms (  2%)
            Phase4_MatchEvents  :   200ms (  2%)
            Phase2_BaseEntities :   100ms (  1%)
            Phase6_TeamStats    :   100ms (  1%)
            ----------------------------------------
            Slowest Phase: Phase3_MatchPlayers (6000ms, 85%)
            ========================================

            2025-01-15 20:30:22.123 WARN  MatchEntitySyncServiceImpl - BOTTLENECK DETECTED: Phase3_MatchPlayers takes 85% of total time (6000ms / 7000ms)
            """.trimIndent(),
        )
    }

    @Test
    @DisplayName("성능 로그 출력 확인 - 심각 케이스")
    fun `데모 - 10초 초과 WARN 로그`() {
        println(
            """
            ========================================
            예상 로그 출력 (10초 초과):
            ========================================

            2025-01-15 18:30:15.123 INFO  MatchEntitySyncServiceImpl - Starting entity sync for fixture: 1208023
            2025-01-15 18:30:20.123 WARN  MatchEntitySyncServiceImpl - [Phase3_MatchPlayers] took too long: 5000ms (threshold: 3000ms)
            2025-01-15 18:30:25.123 WARN  MatchEntitySyncServiceImpl - [Phase5_PlayerStats] took too long: 5000ms (threshold: 3000ms)

            2025-01-15 18:30:26.123 WARN  MatchEntitySyncServiceImpl -
            ========================================
            Performance Report - Fixture: 1208023
            ========================================
            Total Transaction Time: 11000ms
            ----------------------------------------
            Phase5_PlayerStats  :  5000ms ( 45%) █████████
            Phase3_MatchPlayers :  5000ms ( 45%) █████████
            Phase1_LoadContext  :   500ms (  4%)
            Phase4_MatchEvents  :   300ms (  2%)
            Phase2_BaseEntities :   200ms (  1%)
            Phase6_TeamStats    :   100ms (  0%)
            ----------------------------------------
            Slowest Phase: Phase5_PlayerStats (5000ms, 45%)
            ========================================

            💡 이 경우 Slack 알림이나 APM 알림을 보내는 것을 고려하세요!
            """.trimIndent(),
        )
    }
}
