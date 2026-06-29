package com.footballay.core.domain.matchcollect

/**
 * MatchCollect 작업 상태를 나타내는 enum 클래스
 * MatchCollect는 시간차를 두고 여러 번의 요청이 일어나며, 그 중 어느 상태에 있는지를 나타냄.
 */
enum class MatchCollectStatus {
    /**
     * 아직 한번도 MatchCollect가 수행되지 않음
     */
    PENDING,

    /**
     * MatchCollect 가 한 번이라도 수행되었으며, 이후의 MatchCollect 재시행이 남아있음
     */
    EARLY_SYNCED,

    /**
     * 모든 MatchCollect 시행이
     */
    SUCCESS,

    /**
     * MatchCollect가 수행되었으나 경기가 진행되지 않은 상태
     */
    NOT_PLAYED,

    /**
     * MatchCollect가 수행되었으나 데이터가 불완전하여 관리자 개입이 필요한 상태
     */
    DATA_INCOMPLETE_NEEDS_ADMIN,

    /**
     * MatchCollect가 에러 등 원인으로 실패하여 관리자 개입이 필요한 상태
     */
    FAIL_END,
    ;

    fun isIncomplete(): Boolean = this in INCOMPLETE_STATUSES

    companion object {
        val INCOMPLETE_STATUSES: List<MatchCollectStatus> =
            listOf(
                DATA_INCOMPLETE_NEEDS_ADMIN,
                FAIL_END,
            )
    }
}
