package com.footballay.core.infra.dispatcher.match

import java.time.Instant

/**
 * Match 동기화 결과를 나타내는 sealed class
 *
 * Pre/Live/Post 단계별로 상세한 정보를 제공하여
 * Dispatcher가 다음 Job 전환을 결정할 수 있도록 합니다.
 *
 * **단계별 Result:**
 * - [PreMatch]: 경기 전 라인업 캐싱 단계
 * - [Live]: 경기 진행 중 라이브 데이터 동기화
 * - [PostMatch]: 경기 종료 후 최종 데이터 동기화
 * - [Error]: 동기화 실패
 */
sealed class MatchDataSyncResult {
    /**
     * 경기 전 단계 (Pre-Match)
     *
     * 라인업이 발표되기 전~경기 시작 전까지의 단계입니다.
     * 주로 라인업 데이터 캐싱을 시도합니다.
     *
     * @property lineupCached 라인업 데이터가 성공적으로 저장되었는지 여부
     * @property kickoffTime 경기 킥오프 시각
     * @property shouldTerminatePreMatchJob PreMatchJob을 종료해야 하는지 여부 (완전한 라인업 저장 OR 킥오프 1분 전)
     */
    data class PreMatch(
        val lineupCached: Boolean,
        val kickoffTime: Instant?,
        val shouldTerminatePreMatchJob: Boolean,
    ) : MatchDataSyncResult()

    /**
     * 경기 진행 중 단계 (Live)
     *
     * 경기가 시작되어 실시간으로 진행되는 단계입니다.
     * 이벤트, 스탯, 스코어 등을 실시간으로 동기화합니다.
     *
     * @property kickoffTime 경기 킥오프 시각
     * @property isMatchFinished 경기가 종료되었는지 여부 (FT, AET, PEN 등)
     * @property elapsedMin 경기 경과 시간 (분)
     * @property statusShort 경기 상태 축약 코드 (NS, 1H, HT, 2H, FT 등)
     */
    data class Live(
        val kickoffTime: Instant?,
        val isMatchFinished: Boolean,
        val elapsedMin: Int?,
        val statusShort: String,
    ) : MatchDataSyncResult()

    /**
     * 경기 종료 후 단계 (Post-Match)
     *
     * 경기가 종료된 후 최종 데이터를 확정하는 단계입니다.
     * 일정 시간 후에는 더 이상 변경이 없으므로 polling을 중단합니다.
     *
     * @property kickoffTime 경기 킥오프 시각
     * @property shouldStopPolling 충분한 시간이 지나 polling을 중단해야 하는지 여부
     * @property minutesSinceFinish 경기 종료 후 경과 시간 (분)
     */
    data class PostMatch(
        val kickoffTime: Instant?,
        val shouldStopPolling: Boolean,
        val minutesSinceFinish: Long,
    ) : MatchDataSyncResult()

    /**
     * 동기화 실패
     *
     * API 호출 실패, 데이터 오류 등으로 동기화가 실패한 경우입니다.
     * Job은 계속 실행되며 자동으로 재시도합니다.
     *
     * @property message 에러 메시지
     * @property kickoffTime 경기 킥오프 시각 (알 수 있는 경우)
     */
    data class Error(
        val message: String,
        val kickoffTime: Instant?,
    ) : MatchDataSyncResult()

    companion object {
        /**
         * 경기가 종료된 경우의 인스턴스 생성 (하위 호환성)
         * @deprecated Use PostMatch instead
         */
        @Deprecated("Use PostMatch for more detailed result", ReplaceWith("PostMatch"))
        fun finished(kickoffTime: Instant?): Live =
            Live(
                kickoffTime = kickoffTime,
                isMatchFinished = true,
                elapsedMin = 90,
                statusShort = "FT",
            )

        /**
         * 경기가 진행 중인 경우의 인스턴스 생성 (하위 호환성)
         * @deprecated Use Live or PreMatch instead
         */
        @Deprecated("Use Live or PreMatch for more detailed result", ReplaceWith("Live"))
        fun ongoing(kickoffTime: Instant?): Live =
            Live(
                kickoffTime = kickoffTime,
                isMatchFinished = false,
                elapsedMin = null,
                statusShort = "LIVE",
            )
    }
}
