package com.footballay.core.infra.query

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.facade.MockDataReadOption
import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.domain.model.mapper.DomainModelMapper
import com.footballay.core.infra.backbone.mock.resource.MockBackboneModelMapper
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.persistence.mockbackbone.repository.MockBackboneLeagueRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class LeagueReadQueryServiceImpl(
    private val leagueCoreRepository: LeagueCoreRepository,
    private val mockBackboneLeagueRepository: MockBackboneLeagueRepository,
    private val domainModelMapper: DomainModelMapper,
    private val mockBackboneModelMapper: MockBackboneModelMapper,
) : LeagueReadQueryService {
    override fun findAvailableLeagues(option: MockDataReadOption): DomainResult<List<LeagueModel>, DomainFail> =
        try {
            val apiSportsLeagues =
                leagueCoreRepository
                    .findApiSportsBackedAvailableLeagues()
                    .map { league ->
                        domainModelMapper.toLeagueModel(
                            league,
                            requireNotNull(league.apiSportsLeague) {
                                "ApiSports-backed league query returned league without ApiSports data: ${league.uid}"
                            },
                        )
                    }
            val mockLeagues =
                if (option.includeMockData) {
                    mockBackboneLeagueRepository
                        .findMockBackedAvailableLeagues()
                        .map(mockBackboneModelMapper::toLeagueModel)
                } else {
                    emptyList()
                }

            DomainResult.Success(
                (apiSportsLeagues + mockLeagues)
                    .distinctBy { it.uid }
                    .sortedBy { it.name },
            )
        } catch (ex: Exception) {
            DomainResult.Fail(DomainFail.Unknown("Failed to fetch readable leagues: ${ex.message}"))
        }
}
