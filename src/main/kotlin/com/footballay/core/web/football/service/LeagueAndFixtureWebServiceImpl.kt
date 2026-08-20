package com.footballay.core.web.football.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.map
import com.footballay.core.domain.facade.DesktopFixtureFacade
import com.footballay.core.domain.facade.DesktopLeagueFacade
import com.footballay.core.domain.facade.MockDataReadOption
import com.footballay.core.logger
import com.footballay.core.localization.SupportedLocale
import com.footballay.core.web.football.dto.AvailableLeagueResponse
import com.footballay.core.web.football.dto.FixtureByLeagueResponse
import com.footballay.core.web.football.dto.FixtureDatesByLeagueResponse
import com.footballay.core.web.football.localization.FootballResponseLocalizationService
import com.footballay.core.web.football.mapper.MatchDataMapper
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId

/**
 * Desktop App용 League 및 Fixture 조회 WebService 구현체
 */
@Service
class LeagueAndFixtureWebServiceImpl(
    private val desktopLeagueFacade: DesktopLeagueFacade,
    private val desktopFixtureFacade: DesktopFixtureFacade,
    private val localizationService: FootballResponseLocalizationService,
    private val matchDataMapper: MatchDataMapper,
) : LeagueAndFixtureWebService {
    val log = logger()

    override fun getFixtureDatesByLeague(
        leagueUid: String,
        startInclusive: Instant,
        endExclusive: Instant,
        zoneId: ZoneId,
        option: MockDataReadOption,
    ): DomainResult<FixtureDatesByLeagueResponse, DomainFail> =
        desktopFixtureFacade
            .getFixtureDatesByLeague(leagueUid, startInclusive, endExclusive, zoneId, option)
            .map { dates -> FixtureDatesByLeagueResponse(dates.map { it.toString() }) }

    override fun getAvailableLeagues(
        option: MockDataReadOption,
        locale: SupportedLocale,
    ): DomainResult<List<AvailableLeagueResponse>, DomainFail> =
        desktopLeagueFacade
            .getAvailableLeagues(option)
            .map { localizationService.localizeAvailableLeagues(it, locale) }
            .map(matchDataMapper::toAvailableLeagueResponses)

    override fun getFixturesByLeague(
        leagueUid: String,
        at: Instant?,
        mode: String,
        zoneId: ZoneId,
        option: MockDataReadOption,
        locale: SupportedLocale,
    ): DomainResult<List<FixtureByLeagueResponse>, DomainFail> =
        desktopFixtureFacade
            .getFixturesByLeague(leagueUid, at, mode, zoneId, option)
            .map { localizationService.localizeFixturesByLeague(it, locale) }
            .map(matchDataMapper::toFixtureByLeagueResponses)
}
