package com.footballay.core.infra.apisports.match.sync.loader

import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Live 경기 엔티티 조회 서비스 구현체
 * 
 * 영속 상태의 엔티티들을 직접 반환하여 EntityBundle 구성에 사용됩니다.
 * 3개의 최적화된 쿼리를 통해 Context에 필요한 엔티티들을 직접 설정합니다.
 * 실용적이고 간단한 구조로 JPA 영속성을 보장합니다.
 * 
 * **DTO vs Entity 구분:**
 * - 이 서비스는 영속 상태의 엔티티를 반환 (EntityBundle용)
 * - DTO 변환이 필요한 경우 별도의 DTO 서비스를 사용
 */
@Service
@Transactional(readOnly = true)
class MatchEntityQueryServiceImpl(
    private val fixtureRepository: FixtureApiSportsRepository
) : MatchEntityQueryService {

    private val log = logger()

    /**
     * Home Team + Team Statistics + Players + Player Statistics 조회
     */
    override fun loadHomeTeamWithPlayersAndStats(fixtureApiId: Long): ApiSportsMatchTeam? {
        log.debug("Loading home team with players and stats for fixtureApiId=$fixtureApiId")
        return fixtureRepository.findFixtureHomeTeamLineupAndStats(fixtureApiId)?.homeTeam
    }

    /**
     * Away Team + Team Statistics + Players + Player Statistics 조회
     */
    override fun loadAwayTeamWithPlayersAndStats(fixtureApiId: Long): ApiSportsMatchTeam? {
        log.debug("Loading away team with players and stats for fixtureApiId=$fixtureApiId")
        return fixtureRepository.findFixtureAwayTeamLineupAndStats(fixtureApiId)?.awayTeam
    }

    /**
     * Fixture 핵심 데이터 + Events 조회
     */
    override fun loadFixtureWithEvents(fixtureApiId: Long): FixtureApiSports? {
        log.debug("Loading fixture with events for fixture: $fixtureApiId")
        return fixtureRepository.findEventsByFixtureApiId(fixtureApiId)
    }

} 