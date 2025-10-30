package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.FixtureCore
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface FixtureCoreRepository : JpaRepository<FixtureCore, Long> {
    fun findByUid(fixtureUid: String): FixtureCore

    fun findByLeague_IdAndKickoffBetweenOrderByKickoffAsc(
        leagueId: Long,
        start: Instant,
        end: Instant,
    ): List<FixtureCore>

    fun findFirstByLeague_IdAndKickoffGreaterThanEqualOrderByKickoffAsc(
        leagueId: Long,
        from: Instant,
    ): FixtureCore?
}
