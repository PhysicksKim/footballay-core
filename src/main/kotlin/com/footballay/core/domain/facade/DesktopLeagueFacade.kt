package com.footballay.core.domain.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.model.LeagueModel

/**
 * Desktop App용 League 조회 Facade Interface
 */
interface DesktopLeagueFacade {
    /**
     * Available한 모든 리그를 조회합니다.
     */
    fun getAvailableLeagues(): DomainResult<List<LeagueModel>, DomainFail>
}
