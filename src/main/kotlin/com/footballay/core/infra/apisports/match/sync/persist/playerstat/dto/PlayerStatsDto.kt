package com.footballay.core.infra.apisports.match.sync.persist.playerstat.dto

/**
 * PlayerStats 처리용 내부 DTO
 *
 * PlayerStats 처리 과정에서 사용되는 내부 데이터 구조입니다.
 * PlayerStatSyncDto에서 추출된 데이터를 MatchPlayer와 연결하기 위한 중간 형태입니다.
 *
 * @param playerKey MatchPlayer 키 (MatchPlayerKeyGenerator로 생성)
 * @param minutesPlayed 출전 시간
 * @param shirtNumber 등번호
 * @param position 포지션
 * @param rating 평점
 * @param isCaptain 주장 여부
 * @param isSubstitute 후보 선수 여부
 * @param offsides 오프사이드
 * @param shotsTotal 총 슈팅
 * @param shotsOnTarget 유효 슈팅
 * @param goalsTotal 총 득점
 * @param goalsConceded 실점
 * @param assists 어시스트
 * @param saves 세이브
 * @param passesTotal 총 패스
 * @param keyPasses 키 패스
 * @param passesAccuracy 패스 정확도
 * @param tacklesTotal 총 태클
 * @param blocks 블록
 * @param interceptions 인터셉트
 * @param duelsTotal 총 듀얼
 * @param duelsWon 듀얼 승리
 * @param dribblesAttempts 드리블 시도
 * @param dribblesSuccess 드리블 성공
 * @param dribblesPast 드리블 패스
 * @param foulsDrawn 파울 유도
 * @param foulsCommitted 파울 범함
 * @param yellowCards 옐로카드
 * @param redCards 레드카드
 * @param penaltyWon 페널티 유도
 * @param penaltyCommitted 페널티 범함
 * @param penaltyScored 페널티 득점
 * @param penaltyMissed 페널티 실축
 * @param penaltySaved 페널티 세이브
 */
data class PlayerStatsDto(
    val playerKey: String,
    val minutesPlayed: Int?,
    val shirtNumber: Int?,
    val position: String?,
    val rating: Double?,
    val isCaptain: Boolean,
    val isSubstitute: Boolean,
    val offsides: Int?,
    val shotsTotal: Int?,
    val shotsOnTarget: Int?,
    val goalsTotal: Int?,
    val goalsConceded: Int?,
    val assists: Int?,
    val saves: Int?,
    val passesTotal: Int?,
    val keyPasses: Int?,
    val passesAccuracy: Int?,
    val tacklesTotal: Int?,
    val blocks: Int?,
    val interceptions: Int?,
    val duelsTotal: Int?,
    val duelsWon: Int?,
    val dribblesAttempts: Int?,
    val dribblesSuccess: Int?,
    val dribblesPast: Int?,
    val foulsDrawn: Int?,
    val foulsCommitted: Int?,
    val yellowCards: Int,
    val redCards: Int,
    val penaltyWon: Int?,
    val penaltyCommitted: Int?,
    val penaltyScored: Int,
    val penaltyMissed: Int,
    val penaltySaved: Int,
)
