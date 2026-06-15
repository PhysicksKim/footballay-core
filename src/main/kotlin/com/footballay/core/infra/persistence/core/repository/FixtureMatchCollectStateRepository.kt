package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.FixtureMatchCollectState
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FixtureMatchCollectStateRepository : JpaRepository<FixtureMatchCollectState, Long> {
    fun findByFixture_Uid(fixtureUid: String): FixtureMatchCollectState?

    fun existsByFixture_Uid(fixtureUid: String): Boolean
}
