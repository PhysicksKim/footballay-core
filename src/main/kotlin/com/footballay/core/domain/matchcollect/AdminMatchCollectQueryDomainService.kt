package com.footballay.core.domain.matchcollect

import com.footballay.core.infra.persistence.core.entity.FixtureMatchCollectState
import com.footballay.core.infra.persistence.core.repository.FixtureMatchCollectStateRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class AdminMatchCollectQueryDomainService(
    private val stateRepository: FixtureMatchCollectStateRepository,
    private val mapper: AdminMatchCollectQueryModelMapper,
) {
    /**
     * 주어진 [statuses] 를 가진 모든 [MatchCollectStatus] 페이징 처리하여 조회
     */
    fun findAllMatchCollectStatesByStatuses(
        statuses: Collection<MatchCollectStatus>,
        pageable: Pageable,
    ): Page<AdminMatchCollectStateModel> {
        if (statuses.isEmpty()) {
            return emptyPage(pageable)
        }
        return stateRepository
            .findAdminStatesByStatuses(statuses = statuses, pageable = pageable)
            .map(mapper::toStateModel)
    }

    /**
     * 특정 리그에서 주어진 statuses 를 가진 모든 [MatchCollectStatus] 페이징 처리하여 조회
     */
    fun findMatchCollectStatesByLeagueUidAndStatuses(
        leagueUid: String,
        statuses: Collection<MatchCollectStatus>,
        pageable: Pageable,
    ): Page<AdminMatchCollectStateModel> {
        if (statuses.isEmpty()) {
            return emptyPage(pageable)
        }
        return stateRepository
            .findAdminStatesByLeagueUidAndStatuses(
                leagueUid = leagueUid,
                statuses = statuses,
                pageable = pageable,
            ).map(mapper::toStateModel)
    }

    /**
     * 특정 경기에서 주어진 statuses 를 가진 모든 [MatchCollectStatus] 페이징 처리하여 조회
     */
    fun findMatchCollectStateByFixtureUidAndStatuses(
        fixtureUid: String,
        statuses: Collection<MatchCollectStatus>,
        pageable: Pageable,
    ): Page<AdminMatchCollectStateModel> =
        pageSingleState(
            state = stateRepository.findAdminStateByFixture_Uid(fixtureUid),
            statuses = statuses,
            pageable = pageable,
        ).map(mapper::toStateModel)

    private fun <T> emptyPage(pageable: Pageable): Page<T> = PageImpl(emptyList(), pageable, 0)

    private fun pageSingleState(
        state: FixtureMatchCollectState?,
        statuses: Collection<MatchCollectStatus>,
        pageable: Pageable,
    ): Page<FixtureMatchCollectState> {
        val content =
            state
                ?.takeIf { it.matchCollectStatus in statuses }
                ?.let(::listOf)
                ?: emptyList()
        return PageImpl(content, pageable, content.size.toLong())
    }
}
