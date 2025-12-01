package com.footballay.core.domain.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.domain.model.FixtureModel
import com.footballay.core.domain.model.mapper.DomainModelMapper
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.logger
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
 * - previous: 기준 날짜를 포함하여 이전 가장 가까운 날짜의 경기들
 * - exact: 정확히 해당 날짜의 경기들
 * - nearest: 기준 날짜를 포함하여 이후 가장 가까운 날짜의 경기들
 */
@Service
class DesktopFixtureFacadeImpl(
    private val fixtureCoreRepository: FixtureCoreRepository,
    private val domainModelMapper: DomainModelMapper,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val clock: Clock = Clock.systemUTC(),
) : DesktopFixtureFacade {
    val log = logger()

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
                            core.homeTeam,
                            core.awayTeam,
                        )
                    val apiTeamHomeAndAway =
                        Pair(
                            core.homeTeam?.teamApiSports,
                            core.awayTeam?.teamApiSports,
                        )
                    domainModelMapper.toFixtureModel(
                        core,
                        apiSports,
                        teamHomeAndAway,
                        apiTeamHomeAndAway,
                        leagueUid,
                    )
                }

            log.info("Fetched Fixtures size=${fixtureModels.size} \n for leagueUid=$leagueUid, mode=$mode, at=$at, zoneId=$zoneId")

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
     * 기준 날짜를 포함하여 이전 가장 가까운 날짜의 경기들을 조회합니다.
     */
    private fun findFixturesOnPreviousDate(
        leagueUid: String,
        before: Instant,
        zoneId: ZoneId,
    ) = let {
        // before를 해당 날짜 끝 시점(다음날 시작)으로 변경하여 당일 포함
        val date = before.atZone(zoneId).toLocalDate()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant()

        val previousKickoff =
            fixtureCoreRepository.findMaxKickoffBeforeByLeagueUid(leagueUid, endOfDay)
                ?: return@let emptyList()

        val previousDate = previousKickoff.atZone(zoneId).toLocalDate()
        val dayStart = previousDate.atStartOfDay(zoneId).toInstant()
        val dayEnd = previousDate.plusDays(1).atStartOfDay(zoneId).toInstant()

        fixtureCoreRepository.findFixturesByLeagueUidInKickoffRange(leagueUid, dayStart, dayEnd)
    }
}
