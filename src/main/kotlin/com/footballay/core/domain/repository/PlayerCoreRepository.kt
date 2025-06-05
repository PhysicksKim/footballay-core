package com.footballay.core.domain.repository

import com.footballay.core.domain.entity.PlayerCore
import org.springframework.data.jpa.repository.JpaRepository

interface PlayerCoreRepository : JpaRepository<PlayerCore, Long> {
    fun findByApiId(apiId: Long): PlayerCore?
    fun findByUid(uid: String): PlayerCore?
    fun findByName(name: String): List<PlayerCore>
    fun findByNameAndBirthDate(name: String, birthDate: java.time.LocalDate): PlayerCore?
} 