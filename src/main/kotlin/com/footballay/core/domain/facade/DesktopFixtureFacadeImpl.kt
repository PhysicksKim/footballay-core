package com.footballay.core.domain.facade

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.map
import com.footballay.core.domain.model.FixtureModel
import com.footballay.core.infra.query.FixtureScheduleReadQueryService
import com.footballay.core.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
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
    private val fixtureScheduleReadQueryService: FixtureScheduleReadQueryService,
) : DesktopFixtureFacade {
    val log = logger()

    @Transactional(readOnly = true)
    override fun getFixtureDatesByLeague(
        leagueUid: String,
        startInclusive: Instant,
        endExclusive: Instant,
        zoneId: ZoneId,
        option: MockDataReadOption,
    ): DomainResult<List<LocalDate>, DomainFail> =
        fixtureScheduleReadQueryService
            .findFixtureKickoffsByLeague(leagueUid, startInclusive, endExclusive, option)
            .map { kickoffs -> kickoffs.map { it.atZone(zoneId).toLocalDate() }.distinct().sorted() }

    @Transactional(readOnly = true)
    override fun getFixturesByLeague(
        leagueUid: String,
        at: Instant?,
        mode: String,
        zoneId: ZoneId,
        option: MockDataReadOption,
    ): DomainResult<List<FixtureModel>, DomainFail> =
        fixtureScheduleReadQueryService.findFixturesByLeague(
            leagueUid = leagueUid,
            at = at,
            mode = mode,
            zoneId = zoneId,
            option = option,
        ).also { result ->
            if (result is DomainResult.Success) {
                log.info(
                    "Fetched Fixtures size=${result.value.size} \n for leagueUid=$leagueUid, mode=$mode, at=$at, zoneId=$zoneId",
                )
            }
        }
}
