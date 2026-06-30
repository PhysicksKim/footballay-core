package com.footballay.core.domain.matchcollect

import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 관리자가 [MatchCollectStatus]를 조회하기 위한 Facade 입니다.
 * MatchCollect 과정에서 문제가 발생한 내용들을 관리자가 확인하고 조치할 수 있도록 데이터를 제공합니다.
 */
@Component
class AdminMatchCollectQueryFacade(
    private val leagueCoreRepository: LeagueCoreRepository,
    private val queryDomainService: AdminMatchCollectQueryDomainService,
    private val mapper: AdminMatchCollectQueryModelMapper,
) {
    /**
     * 특정 [MatchCollectStatus]를 가진 모든 match Collect status를 페이징 처리하여 조회
     */
    @Transactional(readOnly = true)
    fun findAllStatePage(
        status: MatchCollectStatus?,
        incompleteOnly: Boolean,
        pageable: Pageable,
    ): Page<AdminMatchCollectStateModel> =
        queryDomainService.findAllMatchCollectStatesByStatuses(
            statuses = resolveStatuses(status = status, incompleteOnly = incompleteOnly),
            pageable = pageable,
        )

    /**
     * [fixtureUid]로 특정 경기에 대한 match Collect status를 페이징 처리하여 조회
     */
    @Transactional(readOnly = true)
    fun findStatePageByFixtureUid(
        fixtureUid: String,
        status: MatchCollectStatus?,
        incompleteOnly: Boolean,
        pageable: Pageable,
    ): Page<AdminMatchCollectStateModel> =
        queryDomainService.findMatchCollectStateByFixtureUidAndStatuses(
            fixtureUid = fixtureUid,
            statuses = resolveStatuses(status = status, incompleteOnly = incompleteOnly),
            pageable = pageable,
        )

    /**
     * 특정 리그의 [MatchCollectStatus]를 페이징 처리하여 조회
     *
     * @param incompleteOnly 비정상적인 상태만 조회할지 여부. true일 경우, status가 null이더라도 INCOMPLETE_STATUSES에 해당하는 상태만 조회됨
     */
    @Transactional(readOnly = true)
    fun findLeagueStatePage(
        leagueUid: String,
        status: MatchCollectStatus?,
        incompleteOnly: Boolean,
        pageable: Pageable,
    ): AdminMatchCollectLeagueStatePage {
        val league =
            leagueCoreRepository.findByUid(leagueUid)
                ?: throw NoSuchElementException("LeagueCore not found: $leagueUid")
        val states =
            queryDomainService.findMatchCollectStatesByLeagueUidAndStatuses(
                leagueUid = leagueUid,
                statuses = resolveStatuses(status = status, incompleteOnly = incompleteOnly),
                pageable = pageable,
            )
        return AdminMatchCollectLeagueStatePage(league = mapper.toLeagueModel(league), states = states)
    }

    private fun resolveStatuses(
        status: MatchCollectStatus?,
        incompleteOnly: Boolean,
    ): List<MatchCollectStatus> {
        if (status != null) {
            return when {
                !incompleteOnly -> listOf(status)
                status.isIncomplete() -> listOf(status)
                else -> emptyList()
            }
        }

        return if (incompleteOnly) {
            MatchCollectStatus.INCOMPLETE_STATUSES
        } else {
            MatchCollectStatus.entries
        }
    }
}
