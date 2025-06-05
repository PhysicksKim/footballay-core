package com.footballay.core.domain.repository

import com.footballay.core.domain.entity.TeamCore
import org.springframework.data.jpa.repository.JpaRepository

interface TeamCoreRepository : JpaRepository<TeamCore, Long> {
    fun findByApiId(apiId: Long): TeamCore?
    fun findByUid(uid: String): TeamCore?
    fun findByName(name: String): TeamCore?
    fun findByNameAndCountry(name: String, country: String): TeamCore?
} 