package com.footballay.core.domain.live.fetcher

import com.footballay.core.infra.persistence.core.entity.PlayerCore
import com.footballay.core.infra.persistence.core.entity.TeamCore

/**
 * 라이브 경기의 라인업 단계에서 **신규 선수**를 탐지하여
 * - PlayerCore 엔티티 생성
 * - ApiPlayer ↔ PlayerCore ↔ TeamCore 연관관계 설정
 *
 * 의 과정을 템플릿 방식으로 제공하는 추상 클래스입니다.
 *
 * @param Resp       API 응답의 타입 (예시: ApiSportsLiveMatchResponse)
 * @param ApiPlayer  API 측 선수 데이터 타입 (예시: PlayerApiSports)
 */
abstract class LiveLineupSyncTemplate<Resp, ApiPlayer> : LiveMatchDataSyncer {

    /** 주어진 fixture UID 를 지원하는지 여부를 반환합니다. */
    abstract override fun isSupport(uid: String): Boolean

    final override fun syncLiveMatchData(uid: String): LiveMatchSyncInstruction =
        sync(uid)

    /**
     * 라이브 경기 동기화의 진입점입니다.
     * 1. API로부터 응답을 조회
     * 2. 홈/어웨이 라인업에서 신규 선수 캐싱
     * 3. 전체 라이브 데이터 동기화 수행
     *
     * @param fixtureUid  대상 경기의 고유 식별자
     * @return            다음 동작(폴링 계속 또는 중단)을 지시하는 객체
     */
    fun sync(fixtureUid: String): LiveMatchSyncInstruction {
        val resp = fetchLiveResponse(fixtureUid)
        cacheNewPlayers(resp, Side.HOME)
        cacheNewPlayers(resp, Side.AWAY)
        return executeFullSync(resp)
    }

    /**
     * 지정된 팀(side) 라인업에서 **신규 선수**를 찾아
     * PlayerCore 생성 및 연관관계 설정을 수행합니다.
     *
     * @param resp   API 응답 데이터
     * @param side   홈/어웨이 구분
     */
    private fun cacheNewPlayers(resp: Resp, side: Side) {
        beforeCacheNewPlayers(resp, side)

        val team = resolveTeam(resp, side)
        val apiNewPlayers = extractNewApiPlayers(resp, side)
        val corePlayers = generateCorePlayers(apiNewPlayers)
        linkPlayersToCoreAndTeam(corePlayers, team)

        afterCacheNewPlayers(resp, side)
    }

    /**
     * 라이브 경기 데이터를 API로부터 조회합니다.
     *
     * @param fixtureUid  경기 식별자
     * @return            API 응답 객체
     */
    protected abstract fun fetchLiveResponse(fixtureUid: String): Resp

    /**
     * API 응답과 side 정보를 기반으로
     * TeamCore 엔티티를 조회하여 반환합니다.
     *
     * @param resp   API 응답 데이터
     * @param side   홈/어웨이 구분
     * @return       대응하는 TeamCore
     */
    protected abstract fun resolveTeam(resp: Resp, side: Side): TeamCore

    /**
     * API 응답에서 아직 DB에 없는 **신규 선수**만 추출합니다.
     *
     * @param resp   API 응답 데이터
     * @param side   홈/어웨이 구분
     * @return       CreatePlayerCoreDto 를 키로, ApiPlayer 를 값으로 하는 맵
     */
    protected abstract fun extractNewApiPlayers(
        resp: Resp,
        side: Side
    ): Map<CreatePlayerCoreDto, ApiPlayer>

    /**
     * 신규 ApiPlayer 데이터를 바탕으로
     * PlayerCore 엔티티를 생성하여 반환합니다.
     *
     * @param apiNew  신규 선수 데이터 맵
     * @return        생성된 PlayerCore ↔ ApiPlayer 맵
     */
    protected abstract fun generateCorePlayers(
        apiNew: Map<CreatePlayerCoreDto, ApiPlayer>
    ): Map<PlayerCore, ApiPlayer>

    /**
     * 생성된 PlayerCore와 ApiPlayer를 TeamCore에 연관지어 저장합니다.
     *
     * @param coreMap  PlayerCore ↔ ApiPlayer 맵
     * @param team     대상 TeamCore
     */
    protected abstract fun linkPlayersToCoreAndTeam(
        coreMap: Map<PlayerCore, ApiPlayer>,
        team: TeamCore
    )

    /**
     * 신규 선수 캐싱 직전 호출되는 훅 메서드입니다.
     *
     * @param resp   API 응답 데이터
     * @param side   홈/어웨이 구분
     */
    protected open fun beforeCacheNewPlayers(resp: Resp, side: Side) {}

    /**
     * 신규 선수 캐싱 후 호출되는 훅 메서드입니다.
     *
     * @param resp   API 응답 데이터
     * @param side   홈/어웨이 구분
     */
    protected open fun afterCacheNewPlayers(resp: Resp, side: Side) {}

    /**
     * 신규 선수 캐싱 이후, 실제 경기 상태·이벤트·통계까지 포함한
     * 전체 라이브 데이터 동기화를 수행합니다.
     *
     * @param resp   API 응답 데이터
     * @return       다음 폴링 동작 여부 등을 담은 지시 객체
     */
    protected abstract fun executeFullSync(resp: Resp): LiveMatchSyncInstruction
}