package com.footballay.core.infra.apisports.syncer

import com.footballay.core.infra.apisports.live.ActionAfterMatchSync

/**
 * 특정 uid 에 대해 live match data 를 가져오는 기능을 제공합니다. <br>
 * uid 를 기반으로 해당 구현체가 라이브 데이터를 가져올 수 있는지 판단한 뒤, <br>
 * 지원되는 경우 실시간 데이터를 가져오는 메소드를 제공합니다. <br>
 */
interface MatchDataSyncer {

    /**
     * uid 기반으로 해당 fixture 가 이 Fetcher 에 의해 지원되는지 여부를 판단합니다. <br>
     * 예를 들어, Data Provider Health Check, Fallback Flag 에 따라서 Runtime 에 동적으로 지원 여부가 달라질 수 있습니니다. <br>
     */
    fun isSupport(uid: String): Boolean

    /**
     * uid 를 통해 해당 fixture 의 실시간 데이터를 가져옵니다. <br>
     * 이 메소드는 해당 fixture 가 이 Fetcher 에 의해 지원되는 경우에만 호출되어야 합니다. <br>
     */
    fun syncMatchData(uid: String): ActionAfterMatchSync

}