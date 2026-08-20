package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.TeamCoreLocalization
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/** Team Core localization persistence를 제공합니다. */
@Repository
interface TeamCoreLocalizationRepository : JpaRepository<TeamCoreLocalization, Long>
