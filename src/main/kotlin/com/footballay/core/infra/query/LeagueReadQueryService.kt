package com.footballay.core.infra.query

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.MockDataReadOption
import com.footballay.core.domain.model.LeagueModel

interface LeagueReadQueryService {
    fun findAvailableLeagues(
        option: MockDataReadOption = MockDataReadOption.DEFAULT,
    ): DomainResult<List<LeagueModel>, DomainFail>
}
