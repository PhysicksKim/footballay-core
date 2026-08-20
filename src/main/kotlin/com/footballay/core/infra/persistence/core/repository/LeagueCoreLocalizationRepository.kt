package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.LeagueCoreLocalization
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/** League Core localization persistence를 제공합니다. */
@Repository
interface LeagueCoreLocalizationRepository : JpaRepository<LeagueCoreLocalization, Long>
