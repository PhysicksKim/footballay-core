package com.footballay.core.web.admin.match.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.matchdata.facade.MatchDataSyncFacade
import com.footballay.core.matchdata.sync.dispatcher.MatchDataSyncResult
import org.springframework.stereotype.Service

@Service
class MatchSyncWebService(
    private val matchDataSyncFacade: MatchDataSyncFacade,
) {
    /**
     * Fixture UID로 Match Data를 한 번만 동기화합니다.
     *
     * @param fixtureUid 경기 고유 식별자
     * @return 동기화 성공 여부
     */
    fun syncMatchOnce(fixtureUid: String): DomainResult<Unit, DomainFail> {
        val result = matchDataSyncFacade.syncByFixtureUid(fixtureUid)

        when (result) {
            is MatchDataSyncResult.PreMatch -> (
                return DomainResult.Success(Unit)
            )
            is MatchDataSyncResult.Live -> (
                return DomainResult.Success(Unit)
            )
            is MatchDataSyncResult.PostMatch -> (
                return DomainResult.Success(Unit)
            )
            is MatchDataSyncResult.Error -> (
                return DomainResult.Fail(
                    DomainFail.Unknown(
                        "매치 데이터 동기화에 실패했습니다: ${result.message}",
                    ),
                )
            )
        }
    }
}
