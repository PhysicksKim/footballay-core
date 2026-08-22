package com.footballay.core.infra.persistence.core.repository

import com.footballay.core.infra.persistence.core.entity.TeamCoreLocalization
import com.footballay.core.localization.SupportedLocale
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/** Team Core localization persistence를 제공합니다. */
@Repository
interface TeamCoreLocalizationRepository : JpaRepository<TeamCoreLocalization, Long> {
    @Query(
        """
        SELECT localization
        FROM TeamCoreLocalization localization
        JOIN FETCH localization.teamCore core
        WHERE core.uid = :coreUid
          AND localization.locale = :locale
        """,
    )
    fun findByCoreUidAndLocale(
        @Param("coreUid") coreUid: String,
        @Param("locale") locale: SupportedLocale,
    ): TeamCoreLocalization?

    @Query(
        """
        SELECT localization
        FROM TeamCoreLocalization localization
        JOIN FETCH localization.teamCore core
        WHERE core.uid IN :coreUids
          AND localization.locale IN :locales
        """,
    )
    fun findAllByCoreUidInAndLocaleIn(
        @Param("coreUids") coreUids: Collection<String>,
        @Param("locales") locales: Collection<SupportedLocale>,
    ): List<TeamCoreLocalization>
}
