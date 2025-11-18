package com.footballay.core.infra.apisports.match.sync.persist

import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.sync.dto.FixtureApiSportsDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchLineupPlanDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchEventPlanDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchPlayerStatPlanDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchTeamStatPlanDto
import com.footballay.core.infra.apisports.syncer.match.persist.result.MatchEntitySyncResult

/**
 * Match Data 관련된 dto 를 여럿 한번에 받아서, DB Entity 로 저장하는 서비스입니다.
 * [MatchPlayerContext] 를 통해서 dto 들에 등장하는 MatchPlayer 들의 정보를 제공해줘야 합니다.
 * [MatchPlayerContext] 에 적절하게 MatchPlayer 들이 준비되어 있지 않으면, 올바르게 저장되지 않을 수 있습니다.
 *
 * 추출된 DTO 를 바탕으로 엔티티를 create, update, delete 합니다.
 */
interface MatchEntityPersistManager {
    /**
     * 매치 엔티티들을 동기화합니다.
     * [MatchPlayerContext] 는 dto 들에서 등장한 선수들로 올바르게 채워져 있어야 합니다.
     *
     * @param fixtureApiId 경기 API ID
     * @param baseDto 기본 경기 정보
     * @param lineupDto 라인업 정보
     * @param eventDto 이벤트 정보
     * @param teamStatDto 팀 통계
     * @param playerStatDto 선수 통계
     * @param playerContext MatchPlayer 컨텍스트
     * @return 동기화 결과 (생성/수정/삭제 건수)
     */
    fun syncMatchEntities(
        fixtureApiId: Long,
        baseDto: FixtureApiSportsDto,
        lineupDto: MatchLineupPlanDto,
        eventDto: MatchEventPlanDto,
        teamStatDto: MatchTeamStatPlanDto,
        playerStatDto: MatchPlayerStatPlanDto,
        playerContext: MatchPlayerContext,
    ): MatchEntitySyncResult
}
