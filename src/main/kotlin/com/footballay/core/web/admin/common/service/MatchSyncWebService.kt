package com.footballay.core.web.admin.common.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.infra.dispatcher.match.MatchDataSyncResult
import com.footballay.core.infra.match.MatchSyncOrchestrator
import org.springframework.stereotype.Service

@Service
class MatchSyncWebService(
    private val matchSyncOrchestrator: MatchSyncOrchestrator,
) {
    /**
     * Fixture UID로 Match Data를 한 번만 동기화합니다.
     *
     * @param fixtureUid 경기 고유 식별자 (예: "apisports:1208021")
     * @return 동기화 성공 여부
     * @throws IllegalArgumentException 지원하지 않는 UID인 경우
     * @throws RuntimeException 동기화 중 오류 발생 시
     */
    fun syncMatchOnce(fixtureUid: String): DomainResult<Unit, DomainFail> {
        if (!matchSyncOrchestrator.isSupport(fixtureUid)) {
            return DomainResult.Fail(
                DomainFail.Validation.single(
                    "NOT_SUPPORTED_FIXTURE_UID",
                    "지원하지 않는 Fixture UID 입니다: $fixtureUid",
                ),
            )
        }

        val result = matchSyncOrchestrator.syncMatchData(fixtureUid)

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
