package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.LeagueCoreLocalization
import com.footballay.core.localization.SupportedLocale
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/** League Core localization persistence를 제공합니다. */
@Repository
interface LeagueCoreLocalizationRepository : JpaRepository<LeagueCoreLocalization, Long> {
    @Query(
        """
        SELECT localization
        FROM LeagueCoreLocalization localization
        JOIN FETCH localization.leagueCore core
        WHERE core.uid IN :coreUids
          AND localization.locale IN :locales
        """,
    )
    fun findAllByCoreUidInAndLocaleIn(
        @Param("coreUids") coreUids: Collection<String>,
        @Param("locales") locales: Collection<SupportedLocale>,
    ): List<LeagueCoreLocalization>
}
