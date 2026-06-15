package com.footballay.core.web.admin.core.service

import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.league.MatchCollect
import com.footballay.core.infra.facade.LeagueMatchCollectFacade
import com.footballay.core.infra.facade.LeagueMatchCollectUpdateResult
import com.footballay.core.infra.scheduler.ReconcileResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class AdminLeagueMatchCollectWebServiceTest {
    @Mock
    private lateinit var leagueMatchCollectFacade: LeagueMatchCollectFacade

    @Test
    fun `matchCollect 변경 결과를 admin response로 변환한다`() {
        val leagueUid = "league-1"
        val webService = AdminLeagueMatchCollectWebService(leagueMatchCollectFacade)
        given(leagueMatchCollectFacade.setLeagueMatchCollectByCoreUid(leagueUid, MatchCollect.FINISHED))
            .willReturn(
                DomainResult.Success(
                    LeagueMatchCollectUpdateResult(
                        leagueCoreUid = leagueUid,
                        matchCollect = MatchCollect.FINISHED,
                        reconcileResult = ReconcileResult.empty(fixtureUid = null, leagueUid = leagueUid),
                    ),
                ),
            )

        val result = webService.setLeagueMatchCollect(leagueUid, MatchCollect.FINISHED)

        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val response = (result as DomainResult.Success).value
        assertThat(response.uid).isEqualTo(leagueUid)
        assertThat(response.matchCollect).isEqualTo(MatchCollect.FINISHED)
        assertThat(response.reconcileSuccess).isTrue()
    }
}
