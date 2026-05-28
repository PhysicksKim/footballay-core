package com.footballay.core.web.admin.core.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.map
import com.footballay.core.infra.facade.AvailableFixtureFacade
import com.footballay.core.infra.facade.AvailableLeagueFacade
import com.footballay.core.web.admin.apisports.dto.ToggleAvailableResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class AdminCoreAvailableWebService(
    private val availableLeagueFacade: AvailableLeagueFacade,
    private val availableFixtureFacade: AvailableFixtureFacade,
) {
    @PreAuthorize("hasRole('ADMIN')")
    fun setLeagueAvailable(
        leagueCoreUid: String,
        available: Boolean,
    ): DomainResult<ToggleAvailableResponse, DomainFail> =
        availableLeagueFacade
            .setLeagueAvailableByCoreUid(leagueCoreUid, available)
            .map { uid -> ToggleAvailableResponse(uid = uid, available = available) }

    @PreAuthorize("hasRole('ADMIN')")
    fun setFixtureAvailable(
        fixtureCoreUid: String,
        available: Boolean,
    ): DomainResult<ToggleAvailableResponse, DomainFail> =
        if (available) {
            availableFixtureFacade.addAvailableFixtureByCoreUid(fixtureCoreUid)
        } else {
            availableFixtureFacade.removeAvailableFixtureByCoreUid(fixtureCoreUid)
        }.map { uid -> ToggleAvailableResponse(uid = uid, available = available) }
}
