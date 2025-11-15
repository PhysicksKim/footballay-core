package com.footballay.core.infra.query

import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchEventRepository
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Match Data 조회 서비스
 *
 * FixtureUid 기반으로 라이브 매치 데이터를 조회합니다.
 *
 * **책임:**
 * - uid → entity 조회
 * - 복잡한 join query 실행
 * - null 안전성 보장
 *
 * **반환 데이터:**
 * - Entity 그대로 반환 (Mapper가 DTO로 변환)
 * - Lazy loading 방지를 위해 필요한 연관관계 모두 fetch
 */
@Service
@Transactional(readOnly = true)
class MatchDataQueryService(
    private val fixtureApiSportsRepository: FixtureApiSportsRepository,
    private val matchEventRepository: ApiSportsMatchEventRepository,
) {
    private val log = logger()

    /**
     * Fixture 기본 정보 조회
     *
     * @param fixtureUid Fixture UID
     * @return FixtureApiSports (core, season, venue 포함)
     * @throws IllegalArgumentException Fixture를 찾을 수 없는 경우
     */
    fun getFixtureInfo(fixtureUid: String): FixtureApiSports {
        val fixture =
            fixtureApiSportsRepository.findByCoreUid(fixtureUid)
                ?: throw IllegalArgumentException("Fixture not found for uid: $fixtureUid")

        log.debug("Fetched fixture info for uid: {}", fixtureUid)
        return fixture
    }

    /**
     * Fixture 라이브 상태 조회 (스코어, 경기 시간, 상태)
     *
     * @param fixtureUid Fixture UID
     * @return FixtureApiSports (core 포함)
     */
    fun getFixtureLiveStatus(fixtureUid: String): FixtureApiSports {
        val fixture =
            fixtureApiSportsRepository.findByCoreUid(fixtureUid)
                ?: throw IllegalArgumentException("Fixture not found for uid: $fixtureUid")

        log.debug("Fetched fixture live status for uid: {}", fixtureUid)
        return fixture
    }

    /**
     * 경기 이벤트 조회 (골, 카드, 교체 등)
     *
     * @param fixtureUid Fixture UID
     * @return 경기 이벤트 목록 (sequence 순 정렬)
     */
    fun getFixtureEvents(fixtureUid: String): List<ApiSportsMatchEvent> {
        // FixtureApiSports를 먼저 조회해서 존재 여부 확인
        val fixture =
            fixtureApiSportsRepository.findByCoreUid(fixtureUid)
                ?: throw IllegalArgumentException("Fixture not found for uid: $fixtureUid")

        val events = matchEventRepository.findByFixtureUidOrderBySequenceAsc(fixtureUid)
        log.debug("Fetched {} events for fixture uid: {}", events.size, fixtureUid)
        return events
    }

    /**
     * 경기 라인업 조회 (홈/원정 선수 정보)
     *
     * 통계 제외한 가벼운 쿼리로 라인업만 조회
     *
     * @param fixtureUid Fixture UID
     * @return Pair<FixtureApiSports(홈팀), FixtureApiSports(원정팀)>
     */
    fun getFixtureLineup(fixtureUid: String): Pair<FixtureApiSports?, FixtureApiSports?> {
        val homeTeam = fixtureApiSportsRepository.findFixtureHomeTeamLineupByUid(fixtureUid)
        val awayTeam = fixtureApiSportsRepository.findFixtureAwayTeamLineupByUid(fixtureUid)

        if (homeTeam == null && awayTeam == null) {
            throw IllegalArgumentException("Fixture not found for uid: $fixtureUid")
        }

        log.debug("Fetched lineup for fixture uid: {}", fixtureUid)
        return Pair(homeTeam, awayTeam)
    }

    /**
     * 경기 통계 조회 (팀/선수별 통계)
     *
     * @param fixtureUid Fixture UID
     * @return Pair<FixtureApiSports(홈팀 통계), FixtureApiSports(원정팀 통계)>
     */
    fun getFixtureStatistics(fixtureUid: String): Pair<FixtureApiSports?, FixtureApiSports?> {
        val homeTeam = fixtureApiSportsRepository.findFixtureHomeTeamLineupAndStatsByUid(fixtureUid)
        val awayTeam = fixtureApiSportsRepository.findFixtureAwayTeamLineupAndStatsByUid(fixtureUid)

        if (homeTeam == null && awayTeam == null) {
            throw IllegalArgumentException("Fixture not found for uid: $fixtureUid")
        }

        log.debug("Fetched statistics for fixture uid: {}", fixtureUid)
        return Pair(homeTeam, awayTeam)
    }
}
