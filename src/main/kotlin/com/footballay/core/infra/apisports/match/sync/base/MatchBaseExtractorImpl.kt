package com.footballay.core.infra.apisports.match.sync.base

import com.footballay.core.infra.apisports.match.dto.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.sync.dto.FixtureApiSportsDto
import com.footballay.core.logger
import org.springframework.stereotype.Component

/**
 * 기본 Match 엔티티를 동기화합니다.
 *
 * 책임:
 * - FixtureApiSports 기본 정보 업데이트
 * - VenueApiSports 연결 및 업데이트
 * - ApiSportsMatchTeam (home/away) 초기화
 */
@Component
class MatchBaseExtractorImpl : MatchBaseDtoExtractor {
    private val log = logger()

    override fun extractBaseMatch(dto: FullMatchSyncDto): FixtureApiSportsDto {
        log.info("기본 매치 정보 dto: fixtureId={}", dto.fixture.id)

        return FixtureApiSportsDto(
            apiId = dto.fixture.id,
            referee = dto.fixture.referee,
            timezone = dto.fixture.timezone,
            date = dto.fixture.date?.toInstant(),
            round = dto.league.round,
            venue = mapVenue(dto.fixture.venue),
            status = mapStatus(dto.fixture.status),
            score = mapScore(dto.score, dto.goals.home ?: 0, dto.goals.away ?: 0),
            homeTeam = mapBaseTeam(dto.teams.home),
            awayTeam = mapBaseTeam(dto.teams.away),
        )
    }

    private fun mapVenue(venue: FullMatchSyncDto.FixtureDto.VenueDto): FixtureApiSportsDto.VenueDto? {
        if (venue.id == null && venue.name == null && venue.city == null) {
            return null
        }

        return FixtureApiSportsDto.VenueDto(
            apiId = venue.id,
            name = venue.name,
            city = venue.city,
        )
    }

    private fun mapStatus(status: FullMatchSyncDto.FixtureDto.StatusDto): FixtureApiSportsDto.StatusDto =
        FixtureApiSportsDto.StatusDto(
            longStatus = status.long,
            shortStatus = status.short,
            elapsed = status.elapsed,
            extra = status.extra,
        )

    private fun mapScore(
        score: FullMatchSyncDto.ScoreDto,
        homeTotal: Int,
        awayTotal: Int,
    ): FixtureApiSportsDto.ScoreDto =
        FixtureApiSportsDto.ScoreDto(
            totalHome = homeTotal,
            totalAway = awayTotal,
            halftimeHome = score.halftime?.home,
            halftimeAway = score.halftime?.away,
            fulltimeHome = score.fulltime?.home,
            fulltimeAway = score.fulltime?.away,
            extratimeHome = score.extratime?.home,
            extratimeAway = score.extratime?.away,
            penaltyHome = score.penalty?.home,
            penaltyAway = score.penalty?.away,
        )

    private fun mapBaseTeam(team: FullMatchSyncDto.TeamsDto.TeamDto): FixtureApiSportsDto.BaseTeamDto? =
        if (team.id != null) {
            FixtureApiSportsDto.BaseTeamDto(
                apiId = team.id,
                name = team.name,
                logo = team.logo,
                winner = team.winner,
            )
        } else {
            null
        }
}
