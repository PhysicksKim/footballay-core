package com.footballay.core.infra.core

import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * FixtureCore 조회 서비스
 *
 * FixtureCore 엔티티를 조회하는 서비스입니다.
 * 동기화 서비스(FixtureCoreSyncService)와 분리하여 책임을 명확히 합니다.
 */
@Service
@Transactional(readOnly = true)
class FixtureCoreQueryService(
    private val fixtureCoreRepository: FixtureCoreRepository,
) {
    /**
     * ID로 FixtureCore 조회
     *
     * @param id FixtureCore의 ID
     * @return FixtureCore 엔티티 (없으면 null)
     */
    fun findById(id: Long): FixtureCore? = fixtureCoreRepository.findByIdOrNull(id)

    /**
     * UID로 FixtureCore 조회
     *
     * @param uid FixtureCore의 UID
     * @return FixtureCore 엔티티 (없으면 null)
     */
    fun findByUid(uid: String): FixtureCore? =
        try {
            fixtureCoreRepository.findByUid(uid)
        } catch (e: Exception) {
            null
        }
}

