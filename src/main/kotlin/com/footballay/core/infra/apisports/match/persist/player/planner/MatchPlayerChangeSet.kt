package com.footballay.core.infra.apisports.match.persist.player.planner

import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer

/**
 * MatchPlayer 변경 작업 명세서
 *
 * 데이터베이스에 적용할 구체적인 변경 작업들을 담는 컨테이너입니다.
 *
 * **구성 요소:**
 * - **toCreate**: 새로 생성할 엔티티들
 * - **toRetain**: DTO와 매칭되어 유지될 엔티티들 (변경 여부와 무관)
 * - **toDelete**: 삭제할 엔티티들
 */
data class MatchPlayerChangeSet(
    val toCreate: List<ApiSportsMatchPlayer>,
    val toRetain: List<ApiSportsMatchPlayer>,
    val toDelete: List<ApiSportsMatchPlayer>,
    // 변경 없는 것들
    val toSame: List<ApiSportsMatchPlayer> = emptyList(),
) {
    // 편의 메서드들
    val totalCount: Int get() = toCreate.size + toRetain.size + toDelete.size + toSame.size
    val createCount: Int get() = toCreate.size
    val retainedCount: Int get() = toRetain.size
    val deleteCount: Int get() = toDelete.size

    /**
     * 변경 작업이 있는지 확인
     */
    fun hasRetained(): Boolean = totalCount > 0

    companion object {
        /**
         * 빈 변경 작업 명세서 생성
         */
        fun empty(): MatchPlayerChangeSet =
            MatchPlayerChangeSet(
                toCreate = emptyList(),
                toRetain = emptyList(),
                toDelete = emptyList(),
            )
    }
}
