package com.footballay.core.infra.apisports.match.persist.event.planner

import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent

/**
 * MatchEvent 변경 작업 명세서
 *
 * 데이터베이스에 적용할 구체적인 Event 변경 작업들을 담는 컨테이너입니다.
 *
 * **구성 요소:**
 * - **toCreate**: 새로 생성할 Event 엔티티들 (sequence 순으로 정렬됨)
 * - **toUpdate**: 기존 Event 엔티티에서 변경할 것들 (sequence 순으로 정렬됨)
 * - **toDelete**: 삭제할 Event 엔티티들 (sequence 순으로 정렬됨)
 *
 * **사용 목적:**
 * - MatchEventChangePlanner에서 생성된 변경 계획을 담아서 전달
 * - MatchEventManager에서 실제 데이터베이스 작업에 사용
 * - 변경 작업의 통계 정보 제공 (생성/수정/삭제 개수)
 *
 * **특징:**
 * - 불변 객체로 설계되어 안전한 데이터 전달
 * - 편의 메서드로 통계 정보 쉽게 접근 가능
 * - 빈 변경 작업 생성 가능
 *
 * **처리 순서:**
 * 1. 삭제 작업 (toDelete) - 고아 엔티티들을 먼저 삭제
 * 2. 생성 및 수정 작업 (toCreate + toUpdate) - 새로운 이벤트 생성과 기존 이벤트 수정을 배치로 처리
 */
data class MatchEventChangeSet(
    val toCreate: List<ApiSportsMatchEvent>,
    val toUpdate: List<ApiSportsMatchEvent>,
    val toDelete: List<ApiSportsMatchEvent>,
) {
    /**
     * 전체 변경 작업 개수
     *
     * 생성, 수정, 삭제 작업의 총 개수를 반환합니다.
     *
     * @return 전체 변경 작업 개수
     */
    val totalCount: Int get() = toCreate.size + toUpdate.size + toDelete.size

    /**
     * 생성할 Event 개수
     *
     * 새로 생성할 Event 엔티티의 개수를 반환합니다.
     *
     * @return 생성할 Event 개수
     */
    val createCount: Int get() = toCreate.size

    /**
     * 수정할 Event 개수
     *
     * 기존 Event 엔티티에서 수정할 개수를 반환합니다.
     *
     * @return 수정할 Event 개수
     */
    val updateCount: Int get() = toUpdate.size

    /**
     * 삭제할 Event 개수
     *
     * 삭제할 Event 엔티티의 개수를 반환합니다.
     *
     * @return 삭제할 Event 개수
     */
    val deleteCount: Int get() = toDelete.size

    /**
     * 변경 작업이 있는지 확인
     *
     * 생성, 수정, 삭제 작업 중 하나라도 있으면 true를 반환합니다.
     *
     * @return 변경 작업이 있으면 true, 없으면 false
     */
    fun hasChanges(): Boolean = totalCount > 0

    companion object {
        /**
         * 빈 변경 작업 명세서 생성
         *
         * 모든 변경 작업이 비어있는 상태의 ChangeSet을 생성합니다.
         *
         * @return 빈 변경 작업 명세서
         */
        fun empty(): MatchEventChangeSet =
            MatchEventChangeSet(
                toCreate = emptyList(),
                toUpdate = emptyList(),
                toDelete = emptyList(),
            )
    }
}
