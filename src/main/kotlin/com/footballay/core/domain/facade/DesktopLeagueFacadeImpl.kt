package com.footballay.core.domain.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.domain.model.mapper.DomainModelMapper
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Desktop App용 League 조회 Facade 구현체
 */
@Service
class DesktopLeagueFacadeImpl(
    private val leagueCoreRepository: LeagueCoreRepository,
    private val mapper: DomainModelMapper,
) : DesktopLeagueFacade {
    @Transactional(readOnly = true)
    override fun getAvailableLeagues(): DomainResult<List<LeagueModel>, DomainFail> {
        return try {
            val leagues = leagueCoreRepository.findByAvailableTrue()
            val leagueModels =
                leagues.mapNotNull { core ->
                    val api = core.apiSportsLeague ?: return@mapNotNull null
                    mapper.toLeagueModel(core, api)
                }
            DomainResult.Success(leagueModels)
        } catch (ex: Exception) {
            DomainResult.Fail(
                DomainFail.Unknown("Failed to fetch available leagues: ${ex.message}"),
            )
        }
    }
}
