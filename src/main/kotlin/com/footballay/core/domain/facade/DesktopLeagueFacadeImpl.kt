package com.footballay.core.domain.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.infra.query.LeagueReadQueryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Desktop App용 League 조회 Facade 구현체
 */
@Service
class DesktopLeagueFacadeImpl(
    private val leagueReadQueryService: LeagueReadQueryService,
) : DesktopLeagueFacade {
    @Transactional(readOnly = true)
    override fun getAvailableLeagues(option: MockDataReadOption): DomainResult<List<LeagueModel>, DomainFail> =
        leagueReadQueryService.findAvailableLeagues(option)
}
