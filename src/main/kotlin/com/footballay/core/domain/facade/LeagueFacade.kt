package com.footballay.core.domain.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.domain.model.mapper.DomainModelMapper
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * UID 기반 추상화된 League 흐름을 처리하는 Facade 클래스
 */
@Service
class LeagueFacade(
    val leagueCoreRepository: LeagueCoreRepository,
    val mapper: DomainModelMapper,
) {
    // - Read ALL Available Leagues
    // - Read Fixtures by League Uid

    @Transactional(readOnly = true)
    fun getAvailableLeagues(): DomainResult<List<LeagueModel>, DomainFail> {
        try {
            val leagues = leagueCoreRepository.findByAvailableTrue()
            val leagueModels =
                leagues.mapNotNull { core ->
                    val api = core.apiSportsLeague ?: return@mapNotNull null
                    mapper.toLeagueModel(core, api)
                }
            return DomainResult.Success(leagueModels)
        } catch (ex: Exception) {
            return DomainResult.Fail(
                DomainFail.Unknown("Failed to fetch available leagues: ${ex.message}"),
            )
        }
    }
}
