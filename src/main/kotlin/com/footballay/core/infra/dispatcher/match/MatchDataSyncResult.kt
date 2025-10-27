package com.footballay.core.infra.dispatcher.match

import java.time.OffsetDateTime

/**
 * Live match 동기화 결과로, 호출자가 다음 동작(추가 폴링 or 종료)을 결정하도록 안내합니다.
 *
 * @property isFinished    경기가 종료되어 더 이상 동기화가 필요 없는 경우 true
 * @property kickoffTime   경기 시작(킥오프) 시각
 */
data class MatchDataSyncResult(
    val isFinished: Boolean,
    val kickoffTime: OffsetDateTime?
) {
    companion object {
        /**
         * 경기가 종료된 경우의 인스턴스 생성
         */
        fun finished(kickoffTime: OffsetDateTime?): MatchDataSyncResult {
            return MatchDataSyncResult(true, kickoffTime)
        }

        /**
         * 경기가 진행 중인 경우의 인스턴스 생성
         */
        fun ongoing(kickoffTime: OffsetDateTime?): MatchDataSyncResult {
            return MatchDataSyncResult(false, kickoffTime)
        }
    }
}