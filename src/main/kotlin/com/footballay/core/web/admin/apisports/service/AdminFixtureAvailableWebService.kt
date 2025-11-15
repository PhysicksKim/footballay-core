package com.footballay.core.web.admin.apisports.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.map
import com.footballay.core.infra.facade.AvailableFixtureFacade
import com.footballay.core.web.admin.apisports.dto.ToggleAvailableResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class AdminFixtureAvailableWebService(
    private val availableFixtureFacade: AvailableFixtureFacade,
) {
    @PreAuthorize("hasRole('ADMIN')")
    fun setFixtureAvailable(
        fixtureApiId: Long,
        available: Boolean,
    ): DomainResult<ToggleAvailableResponse, DomainFail> =
        if (available) {
            availableFixtureFacade.addAvailableFixture(fixtureApiId)
        } else {
            availableFixtureFacade.removeAvailableFixture(fixtureApiId)
        }.map { uid -> ToggleAvailableResponse(uid = uid, available = available) }
}
