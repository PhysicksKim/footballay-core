package com.footballay.core.infra.apisports.match.sync.persist

import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.sync.dto.FixtureApiSportsDto
import com.footballay.core.infra.apisports.match.sync.dto.LineupSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchEventSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.PlayerStatSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.TeamStatSyncDto
import com.footballay.core.infra.apisports.syncer.match.persist.result.MatchEntitySyncResult

/**
 * 매치 엔티티 동기화 서비스
 * 
 * 추출된 DTO들을 바탕으로 실제 데이터베이스에 엔티티를 저장/업데이트/삭제하는
 * 핵심 동기화 서비스입니다.
 * 
 * **핵심 책임:**
 * - 기존 저장된 엔티티들과 새로운 DTO들의 비교/분석
 * - MatchPlayer 중복 방지 및 Orphan 엔티티 관리
 * - 트랜잭션 내에서 안전한 엔티티 저장/업데이트/삭제
 * 
 * **동기화 순서 (Phase 1-8):**
 * 1. **기존 엔티티 로드**: MatchDataLoader를 통한 기존 데이터 조회
 * 2. **Base DTO 처리**: Fixture, MatchTeam 생성/업데이트
 * 3. **MatchPlayer 처리**: 선수 정보 수집 → 계획 → 저장
 * 4. **Lineup 업데이트**: MatchPlayer 저장 후 라인업 정보 업데이트
 * 5. **Event 처리**: 이벤트 생성/업데이트/삭제
 * 6. **PlayerStats 처리**: 선수 통계 정보 저장
 * 7. **TeamStats 처리**: 팀 통계 정보 저장
 * 8. **실제 저장**: 모든 변경사항을 데이터베이스에 반영
 * 
 * **트랜잭션 최적화:**
 * - DTO 추출과 엔티티 저장을 분리하여 트랜잭션 범위 최소화
 * - 엔티티 조회 후 저장까지의 과정만 트랜잭션으로 관리
 * - 안전하고 효율적인 데이터 동기화 보장
 * 
 * @see MatchApiSportsSyncer
 * @see MatchDataLoader
 * 
 * AI가 작성한 주석
 */
interface MatchEntitySyncService {
    
    /**
     * 매치 엔티티들을 동기화합니다.
     * 
     * **동기화 과정:**
     * 1. 기존 엔티티 로드 (MatchDataLoader)
     * 2. Base DTO 처리 (Fixture, MatchTeam)
     * 3. MatchPlayer 처리 (수집 → 계획 → 저장)
     * 4. Lineup 업데이트
     * 5. Event 처리
     * 6. PlayerStats 처리
     * 7. TeamStats 처리
     * 8. 실제 저장
     * 
     * **트랜잭션 관리:**
     * - 전체 메서드가 하나의 트랜잭션으로 실행
     * - 실패 시 모든 변경사항 롤백
     * - 안전한 데이터 동기화 보장
     * 
     * @param fixtureApiId 경기 API ID
     * @param baseDto 기본 경기 정보 DTO
     * @param lineupDto 라인업 정보 DTO
     * @param eventDto 이벤트 정보 DTO
     * @param teamStatDto 팀 통계 DTO
     * @param playerStatDto 선수 통계 DTO
     * @param playerContext MatchPlayer DTO들이 담긴 컨텍스트
     * @return 동기화 결과 정보
     */
    fun syncMatchEntities(
        fixtureApiId: Long,
        baseDto: FixtureApiSportsDto,
        lineupDto: LineupSyncDto,
        eventDto: MatchEventSyncDto,
        teamStatDto: TeamStatSyncDto,
        playerStatDto: PlayerStatSyncDto,
        playerContext: MatchPlayerContext
    ): MatchEntitySyncResult
}

// 동기화 결과 클래스들은 MatchEntitySyncResult.kt 파일로 분리됨 