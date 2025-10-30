package com.footballay.core.web.admin.fixture.service

import com.footballay.core.domain.fixture.model.FixtureModel
import com.footballay.core.domain.fixture.model.Score
import com.footballay.core.domain.fixture.model.TeamSide
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.web.admin.common.util.DateQueryResolver
import com.footballay.core.web.admin.fixture.dto.FixtureSummaryDto
import com.footballay.core.web.admin.fixture.mapper.FixtureWebMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class AdminFixtureQueryWebServiceImpl(
    private val fixtureCoreRepository: FixtureCoreRepository,
    private val clock: Clock = Clock.systemUTC(),
) : AdminFixtureQueryWebService {
    @Transactional(readOnly = true)
    override fun findFixturesByLeague(
        leagueId: Long,
        at: Instant?,
        mode: String,
    ): List<FixtureSummaryDto> {
        val targetInstant = at ?: Instant.now(clock)

        val fixtures =
            when (mode) {
                "exact" -> findFixturesOnExactDate(leagueId, targetInstant)
                "nearest" -> findFixturesOnNearestDate(leagueId, targetInstant)
                else -> emptyList()
            }

        return fixtures
            .map { toFixtureModel(it) }
            .map { FixtureWebMapper.toSummaryDto(it) }
    }

    private fun findFixturesOnExactDate(
        leagueId: Long,
        at: Instant,
    ): List<FixtureCore> {
        val (start, end) = DateQueryResolver.resolveExactRangeAt(at, clock)
        return fixtureCoreRepository
            .findByLeague_IdAndKickoffBetweenOrderByKickoffAsc(leagueId, start, end)
    }

    private fun findFixturesOnNearestDate(
        leagueId: Long,
        from: Instant,
    ): List<FixtureCore> {
        // FootballDataService.findFixturesOnNearestDate() 로직 참고
        // 1. from 이후 첫 번째 fixture 찾기
        val nearestFixture =
            fixtureCoreRepository
                .findFirstByLeague_IdAndKickoffGreaterThanEqualOrderByKickoffAsc(leagueId, from)
                ?: return emptyList()

        // 2. 해당 fixture의 날짜(day 단위)로 모든 fixtures 조회
        val nearestDate = nearestFixture.kickoff!! // 이미 greaterThanEqual로 non-null 보장
        val (start, end) = DateQueryResolver.resolveExactRangeAt(nearestDate, clock)
        return fixtureCoreRepository
            .findByLeague_IdAndKickoffBetweenOrderByKickoffAsc(leagueId, start, end)
    }

    private fun toFixtureModel(entity: FixtureCore): FixtureModel =
        FixtureModel(
            uid = entity.uid,
            kickoffAt = entity.kickoff!!, // Repository 쿼리는 kickoff 기준이므로 non-null 보장
            homeTeam =
                TeamSide(
                    uid = entity.homeTeam?.uid ?: "",
                    name = entity.homeTeam?.name ?: "",
                ),
            awayTeam =
                TeamSide(
                    uid = entity.awayTeam?.uid ?: "",
                    name = entity.awayTeam?.name ?: "",
                ),
            status = entity.status,
            score =
                Score(
                    home = entity.goalsHome,
                    away = entity.goalsAway,
                ),
            available = entity.available,
        )
}
