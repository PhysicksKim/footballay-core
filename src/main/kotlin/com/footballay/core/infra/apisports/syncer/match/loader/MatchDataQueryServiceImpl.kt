package com.footballay.core.infra.apisports.syncer.match.loader

import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchTeamRepository
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Live 경기 데이터 조회 서비스 구현체
 * 
 * 3개의 최적화된 쿼리를 통해 Context에 필요한 엔티티들을 직접 설정합니다.
 * 실용적이고 간단한 구조로 JPA 영속성을 보장합니다.
 */
@Service
@Transactional(readOnly = true)
class MatchDataQueryServiceImpl(
    private val fixtureRepository: FixtureApiSportsRepository,
    private val matchTeamRepository: ApiSportsMatchTeamRepository
) : MatchDataQueryService {

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
        log.debug("TODO: Loading fixture with events for fixture: $fixtureApiId")
        return fixtureRepository.findEventsByFixtureApiId(fixtureApiId)
    }

}