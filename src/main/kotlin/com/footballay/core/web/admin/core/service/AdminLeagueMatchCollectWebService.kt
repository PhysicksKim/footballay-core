package com.footballay.core.web.admin.core.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.map
import com.footballay.core.domain.league.MatchCollect
import com.footballay.core.infra.facade.LeagueMatchCollectFacade
import com.footballay.core.web.admin.core.dto.MatchCollectUpdateResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class AdminLeagueMatchCollectWebService(
    private val leagueMatchCollectFacade: LeagueMatchCollectFacade,
) {
    @PreAuthorize("hasRole('ADMIN')")
    fun setLeagueMatchCollect(
        leagueCoreUid: String,
        matchCollect: MatchCollect,
    ): DomainResult<MatchCollectUpdateResponse, DomainFail> =
        leagueMatchCollectFacade
            .setLeagueMatchCollectByCoreUid(leagueCoreUid, matchCollect)
            .map {
                MatchCollectUpdateResponse(
                    uid = it.leagueCoreUid,
                    matchCollect = it.matchCollect,
                    reconcileSuccess = it.reconcileResult.success,
                )
            }
}
