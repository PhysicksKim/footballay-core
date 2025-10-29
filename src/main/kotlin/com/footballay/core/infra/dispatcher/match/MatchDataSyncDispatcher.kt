package com.footballay.core.infra.dispatcher.match

/**
 * Match data sync를 위한 인터페이스
 *
 * [com.footballay.core.infra.persistence.core.entity.FixtureCore] 의 uid 를 기반으로
 * 해당 경기의 Match data 를 저장합니다.
 *
 * **Job 관리:**
 * Job에서 호출할 때는 [JobContext]를 전달하여 Dispatcher가 Job 전환을 관리할 수 있도록 합니다.
 * JobContext가 없으면 단순히 동기화만 수행하고 Job 관리는 하지 않습니다.
 *
 * @see MatchDataSyncResult
 * @see JobContext
 */
interface MatchDataSyncDispatcher {
    /**
     * 주어진 fixture UID에 대해 지원 가능한 Provider를 찾아 라이브 경기 데이터를 sync합니다.
     *
     * @param fixtureUid 경기 고유 식별자
     * @param jobContext Job 실행 컨텍스트 (Job에서 호출 시 전달, 없으면 Job 관리 안 함)
     * @return 다음 폴링 액션 지시사항
     */
    fun syncByFixtureUid(
        fixtureUid: String,
        jobContext: JobContext? = null,
    ): MatchDataSyncResult
}
