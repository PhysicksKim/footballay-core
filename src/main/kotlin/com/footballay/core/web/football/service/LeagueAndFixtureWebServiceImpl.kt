package com.footballay.core.web.football.service

import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.result.map
import com.footballay.core.domain.facade.DesktopFixtureFacade
import com.footballay.core.domain.facade.DesktopLeagueFacade
import com.footballay.core.domain.model.FixtureModel
import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.logger
import com.footballay.core.web.football.dto.AvailableLeagueResponse
import com.footballay.core.web.football.dto.FixtureByLeagueResponse
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Desktop App용 League 및 Fixture 조회 WebService 구현체
 */
@Service
class LeagueAndFixtureWebServiceImpl(
    private val desktopLeagueFacade: DesktopLeagueFacade,
    private val desktopFixtureFacade: DesktopFixtureFacade,
) : LeagueAndFixtureWebService {
    val log = logger()

    override fun getAvailableLeagues(): DomainResult<List<AvailableLeagueResponse>, DomainFail> =
        desktopLeagueFacade.getAvailableLeagues().map { leagues ->
            leagues.map { toAvailableLeagueResponse(it) }
        }

    override fun getFixturesByLeague(
        leagueUid: String,
        at: Instant?,
        mode: String,
        zoneId: ZoneId,
    ): DomainResult<List<FixtureByLeagueResponse>, DomainFail> =
        desktopFixtureFacade.getFixturesByLeague(leagueUid, at, mode, zoneId).map { fixtures ->
            fixtures.map { toFixtureByLeagueResponse(it) }
        }

    private fun toAvailableLeagueResponse(model: LeagueModel): AvailableLeagueResponse =
        AvailableLeagueResponse(
            uid = model.uid,
            name = model.name,
            nameKo = model.nameKo,
            logo = model.photo,
        )

    private fun toFixtureByLeagueResponse(model: FixtureModel): FixtureByLeagueResponse =
        FixtureByLeagueResponse(
            uid = model.uid,
            kickoff = model.schedule.kickoffAt?.let { DateTimeFormatter.ISO_INSTANT.format(it) },
            round = model.schedule.round,
            homeTeam =
                model.homeTeam?.let { team ->
                    FixtureByLeagueResponse.TeamInfo(
                        uid = team.uid,
                        name = team.name,
                        nameKo = team.nameKo,
                        logo = team.logo,
                    )
                },
            awayTeam =
                model.awayTeam?.let { team ->
                    FixtureByLeagueResponse.TeamInfo(
                        uid = team.uid,
                        name = team.name,
                        nameKo = team.nameKo,
                        logo = team.logo,
                    )
                },
            status =
                FixtureByLeagueResponse.StatusInfo(
                    longStatus = model.status.statusText,
                    shortStatus = model.status.code.value,
                    elapsed = model.status.elapsed,
                ),
            score =
                FixtureByLeagueResponse.ScoreInfo(
                    home = model.score.home,
                    away = model.score.away,
                ),
            available = model.available,
        )
}
