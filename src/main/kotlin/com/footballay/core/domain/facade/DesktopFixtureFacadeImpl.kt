package com.footballay.core.domain.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.model.FixtureModel
import com.footballay.core.domain.model.mapper.DomainModelMapper
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.web.util.DateQueryResolver
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * Desktop App용 Fixture 조회 Facade 구현체
 *
 * Admin용 AdminFixtureQueryWebServiceImpl과 분리된 구현입니다.
 * 모드별 동작:
 * - previous: 기준 날짜 이전 가장 가까운 날짜의 경기들
 * - exact: 정확히 해당 날짜의 경기들
 * - nearest: 기준 날짜 이후 가장 가까운 날짜의 경기들
 */
@Service
class DesktopFixtureFacadeImpl(
    private val fixtureCoreRepository: FixtureCoreRepository,
    private val domainModelMapper: DomainModelMapper,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val clock: Clock = Clock.systemUTC(),
) : DesktopFixtureFacade {
    @Transactional(readOnly = true)
    override fun getFixturesByLeague(
        leagueUid: String,
        at: Instant?,
        mode: String,
        zoneId: ZoneId,
    ): DomainResult<List<FixtureModel>, DomainFail> {
        return try {
            val targetInstant = at ?: Instant.now(clock)

            val fixtures =
                when (mode) {
                    "previous" -> findFixturesOnPreviousDate(leagueUid, targetInstant, zoneId)
                    "exact" -> findFixturesOnExactDate(leagueUid, targetInstant, zoneId)
                    "nearest" -> findFixturesOnNearestDate(leagueUid, targetInstant, zoneId)
                    else -> emptyList()
                }

            val fixtureModels =
                fixtures.mapNotNull { core ->
                    val apiSports = core.apiSports ?: return@mapNotNull null
                    val teamHomeAndAway =
                        Pair(
                            core.homeTeam ?: return@mapNotNull null,
                            core.awayTeam ?: return@mapNotNull null,
                        )
                    val apiTeamHomeAndAway =
                        Pair(
                            core.homeTeam?.teamApiSports ?: return@mapNotNull null,
                            core.awayTeam?.teamApiSports ?: return@mapNotNull null,
                        )
                    domainModelMapper.toFixtureModel(
                        core,
                        apiSports,
                        teamHomeAndAway,
                        apiTeamHomeAndAway,
                        leagueUid,
                    )
                }

            DomainResult.Success(fixtureModels)
        } catch (ex: Exception) {
            DomainResult.Fail(
                DomainFail.Unknown("Failed to fetch fixtures: ${ex.message}"),
            )
        }
    }

    /**
     * 정확한 날짜의 경기들을 조회합니다.
     */
    private fun findFixturesOnExactDate(
        leagueUid: String,
        at: Instant,
        zoneId: ZoneId,
    ) = let {
        val (start, end) = DateQueryResolver.resolveExactRangeAt(at, clock, zoneId)
        fixtureCoreRepository.findFixturesByLeagueUidInKickoffRange(leagueUid, start, end)
    }

    /**
     * 기준 날짜 이후 가장 가까운 날짜의 경기들을 조회합니다.
     */
    private fun findFixturesOnNearestDate(
        leagueUid: String,
        from: Instant,
        zoneId: ZoneId,
    ) = let {
        val nearestKickoff =
            fixtureCoreRepository.findMinKickoffAfterByLeagueUid(leagueUid, from)
                ?: return@let emptyList()

        val date = nearestKickoff.atZone(zoneId).toLocalDate()
        val dayStart = date.atStartOfDay(zoneId).toInstant()
        val dayEnd = date.plusDays(1).atStartOfDay(zoneId).toInstant()

        fixtureCoreRepository.findFixturesByLeagueUidInKickoffRange(leagueUid, dayStart, dayEnd)
    }

    /**
     * 기준 날짜 이전 가장 가까운 날짜의 경기들을 조회합니다.
     */
    private fun findFixturesOnPreviousDate(
        leagueUid: String,
        before: Instant,
        zoneId: ZoneId,
    ) = let {
        val previousKickoff =
            fixtureCoreRepository.findMaxKickoffBeforeByLeagueUid(leagueUid, before)
                ?: return@let emptyList()

        val date = previousKickoff.atZone(zoneId).toLocalDate()
        val dayStart = date.atStartOfDay(zoneId).toInstant()
        val dayEnd = date.plusDays(1).atStartOfDay(zoneId).toInstant()

        fixtureCoreRepository.findFixturesByLeagueUidInKickoffRange(leagueUid, dayStart, dayEnd)
    }
}
