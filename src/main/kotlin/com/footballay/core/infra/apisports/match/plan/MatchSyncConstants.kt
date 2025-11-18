package com.footballay.core.infra.apisports.match.plan

/**
 * Match 동기화 관련 상수
 *
 * Job 전환 및 polling 중단 기준 시간을 정의합니다.
 * 구현체에서 독립적으로 사용 가능하도록 분리했습니다.
 */
object MatchSyncConstants {
    /**
     * PreMatchJob 종료 기준 (킥오프 임박 판단)
     *
     * 킥오프까지 남은 시간이 이 값 이하이면 PreMatchJob을 종료하고
     * LiveMatchJob이 시작될 준비가 된 것으로 판단합니다.
     */
    const val KICKOFF_IMMINENT_THRESHOLD_MINUTES = 1L

    /**
     * PostMatchJob 중단 기준
     *
     * 경기 종료 후 이 시간이 경과하면 더 이상 데이터 변경이 없다고 판단하여
     * PostMatchJob을 중단합니다.
     */
    const val POST_MATCH_POLLING_CUTOFF_MINUTES = 60L
}
