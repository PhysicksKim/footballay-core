package com.footballay.core.infra.core

import com.footballay.core.infra.core.dto.LeagueCoreCreateDto
import com.footballay.core.infra.persistence.core.entity.LeagueCore

/**
 * League Core 저장을 담당하는 서비스 인터페이스
 *
 * **주요 책임:**
 * - LeagueCore 엔티티 생성 및 저장
 * - LeagueCore 영속성 보장
 * - League 관련 트랜잭션 관리
 */
interface LeagueCoreSyncService {
    /**
     * LeagueCore 엔티티를 생성하고 저장합니다.
     *
     * @param dto LeagueCore 생성에 필요한 데이터
     * @return 저장된 LeagueCore 엔티티 (영속 상태)
     */
    fun saveLeagueCore(dto: LeagueCoreCreateDto): LeagueCore
}
