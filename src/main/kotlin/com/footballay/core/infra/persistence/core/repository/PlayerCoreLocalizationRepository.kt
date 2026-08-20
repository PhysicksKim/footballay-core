package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.PlayerCoreLocalization
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/** Player Core localization persistence를 제공합니다. */
@Repository
interface PlayerCoreLocalizationRepository : JpaRepository<PlayerCoreLocalization, Long>
