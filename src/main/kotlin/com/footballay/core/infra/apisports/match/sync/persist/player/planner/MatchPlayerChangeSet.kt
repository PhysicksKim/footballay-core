package com.footballay.core.infra.apisports.match.sync.persist.player.planner

import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchPlayer

/**
 * MatchPlayer 변경 작업 명세서
 * 
 * 데이터베이스에 적용할 구체적인 변경 작업들을 담는 컨테이너입니다.
 * 
 * **구성 요소:**
 * - **toCreate**: 새로 생성할 엔티티들
 * - **toUpdate**: 기존 엔티티에서 변경할 것들  
 * - **toDelete**: 삭제할 엔티티들
 */
data class MatchPlayerChangeSet(
    val toCreate: List<ApiSportsMatchPlayer>,
    val toUpdate: List<ApiSportsMatchPlayer>,
    val toDelete: List<ApiSportsMatchPlayer>
) {
    // 편의 메서드들
    val totalCount: Int get() = toCreate.size + toUpdate.size + toDelete.size
    val createCount: Int get() = toCreate.size
    val updateCount: Int get() = toUpdate.size
    val deleteCount: Int get() = toDelete.size
    
    /**
     * 변경 작업이 있는지 확인
     */
    fun hasChanges(): Boolean = totalCount > 0
    
    companion object {
        /**
         * 빈 변경 작업 명세서 생성
         */
        fun empty(): MatchPlayerChangeSet {
            return MatchPlayerChangeSet(
                toCreate = emptyList(),
                toUpdate = emptyList(),
                toDelete = emptyList()
            )
        }
    }
} 