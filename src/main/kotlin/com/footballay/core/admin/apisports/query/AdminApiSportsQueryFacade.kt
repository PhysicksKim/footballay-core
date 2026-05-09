package com.footballay.core.admin.apisports.query

import com.footballay.core.admin.apisports.query.model.AdminApiSportsFixtureSummaryView
import com.footballay.core.admin.apisports.query.model.AdminApiSportsPlayerView
import com.footballay.core.admin.apisports.query.model.AdminApiSportsTeamView
import com.footballay.core.common.logging.logger
import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import com.footballay.core.common.time.DateQueryResolver
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.PlayerApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * Admin API - ApiSports 조회 Facade (Aggregate Root)
 *
 * ApiSports provider id 기반 admin 조회를 담당합니다.
 * Admin 응답에 필요한 provider-specific 값을 admin view로 조립해 web 계층의 extension downcast를 막습니다.
 */
@Component
class AdminApiSportsQueryFacade(
    private val teamApiSportsRepository: TeamApiSportsRepository,
    private val playerApiSportsRepository: PlayerApiSportsRepository,
    private val leagueApiSportsRepository: LeagueApiSportsRepository,
    private val fixtureCoreRepository: FixtureCoreRepository,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val log = logger()

    /**
     * LeagueApiSports apiId로 해당 리그의 팀 목록 조회
     *
     * **사용 시나리오:**
     * 1. Admin이 리그의 팀 sync 완료 후 결과 확인
     * 2. Players sync를 위한 팀 선택 시 드롭다운 목록 제공
     *
     * @param leagueApiId LeagueApiSports의 apiId (예: 39 = Premier League)
     * @return DomainResult<List<AdminApiSportsTeamView>, DomainFail>
     */
    @Transactional(readOnly = true)
    fun findTeamsByLeagueApiId(leagueApiId: Long): DomainResult<List<AdminApiSportsTeamView>, DomainFail> {
        val teamApiSportsList = teamApiSportsRepository.findAllByLeagueApiSportsApiId(leagueApiId)

        val teams =
            teamApiSportsList
                .mapNotNull { api ->
                    val core = api.teamCore ?: return@mapNotNull null
                    val apiId = api.apiId ?: return@mapNotNull null
                    AdminApiSportsTeamView(
                        apiId = apiId,
                        uid = core.uid,
                        name = core.name,
                        nameKo = core.nameKo,
                        logo = api.logo,
                        code = core.code,
                    )
                }

        return DomainResult.Success(teams)
    }

    /**
     * TeamApiSports apiId로 해당 팀의 선수 목록 조회
     *
     * **사용 시나리오:**
     * 1. Admin이 팀의 선수 sync 완료 후 결과 확인
     * 2. 선수 데이터 검증 및 관리
     *
     * @param teamApiId TeamApiSports의 apiId (예: 50 = Manchester City)
     * @return DomainResult<List<AdminApiSportsPlayerView>, DomainFail>
     */
    @Transactional(readOnly = true)
    fun findPlayersByTeamApiId(teamApiId: Long): DomainResult<List<AdminApiSportsPlayerView>, DomainFail> {
        val playerApiSportsList = playerApiSportsRepository.findAllByTeamApiSportsApiId(teamApiId)

        val players =
            playerApiSportsList.mapNotNull { api ->
                val core = api.playerCore ?: return@mapNotNull null
                val apiId = api.apiId ?: return@mapNotNull null
                AdminApiSportsPlayerView(
                    apiId = apiId,
                    uid = core.uid,
                    name = core.name,
                    nameKo = core.nameKo,
                    photo = api.photo,
                    position = api.position,
                    number = api.number,
                    nationality = api.nationality,
                )
            }
        return DomainResult.Success(players)
    }

    /**
     * LeagueApiSports apiId로 관리자용 fixture 요약 목록을 조회합니다.
     *
     * admin 요청은 provider id로 시작하지만 fixture 목록 조회는 core league uid 기준 repository query로 이어집니다.
     * 따라서 provider id -> core uid 전환을 facade 안에 둡니다.
     */
    @Transactional(readOnly = true)
    fun findFixturesByLeague(
        leagueApiId: Long,
        at: Instant?,
        mode: String,
        zoneId: ZoneId,
    ): List<AdminApiSportsFixtureSummaryView> {
        val targetInstant = at ?: Instant.now(clock)
        log.info("findFixturesByLeague called with leagueApiId={}, at={}, mode={}, zoneId={}", leagueApiId, targetInstant, mode, zoneId)

        val leagueApiSports =
            leagueApiSportsRepository.findByApiId(leagueApiId)
                ?: return emptyList()
        val leagueUid = leagueApiSports.leagueCore?.uid ?: return emptyList()

        val fixtures =
            when (mode) {
                "exact" -> findFixturesOnExactDate(leagueUid, targetInstant, zoneId)
                "nearest" -> findFixturesOnNearestDate(leagueUid, targetInstant, zoneId)
                else -> emptyList()
            }

        log.info("Fetched Fixtures size={} for leagueUid={}, mode={}, at={}, zoneId={}", fixtures.size, leagueUid, mode, targetInstant, zoneId)

        return fixtures.map { (core, api) ->
            AdminApiSportsFixtureSummaryView(
                apiId = api.apiId,
                uid = core.uid,
                kickoffAt = core.kickoff ?: return@map null,
                home = core.homeTeam?.let { team ->
                    AdminApiSportsFixtureSummaryView.TeamSide(
                        name = team.name,
                        nameKo = team.nameKo,
                        logo = team.teamApiSports?.logo,
                    )
                },
                away = core.awayTeam?.let { team ->
                    AdminApiSportsFixtureSummaryView.TeamSide(
                        name = team.name,
                        nameKo = team.nameKo,
                        logo = team.teamApiSports?.logo,
                    )
                },
                status = core.statusCode.code,
                statusText = core.statusText,
                available = core.available,
            )
        }.filterNotNull()
    }

    private fun findFixturesOnExactDate(
        leagueUid: String,
        at: Instant,
        zoneId: ZoneId,
    ): List<Pair<FixtureCore, FixtureApiSports>> {
        val (start, end) = DateQueryResolver.resolveExactRangeAt(at, clock, zoneId)
        return fixtureCoreRepository
            .findFixturesByLeagueUidInKickoffRange(
                leagueUid = leagueUid,
                startInclusive = start,
                endExclusive = end,
            ).mapNotNull { core -> core.toApiSportsPair() }
    }

    private fun findFixturesOnNearestDate(
        leagueUid: String,
        from: Instant,
        zoneId: ZoneId,
    ): List<Pair<FixtureCore, FixtureApiSports>> {
        val nearestKickoff =
            fixtureCoreRepository.findMinKickoffAfterByLeagueUid(leagueUid, from)
                ?: return emptyList()

        val date = nearestKickoff.atZone(zoneId).toLocalDate()
        val dayStart = date.atStartOfDay(zoneId).toInstant()
        val dayEnd = date.plusDays(1).atStartOfDay(zoneId).toInstant()

        return fixtureCoreRepository
            .findFixturesByLeagueUidInKickoffRange(
                leagueUid = leagueUid,
                startInclusive = dayStart,
                endExclusive = dayEnd,
            ).mapNotNull { core -> core.toApiSportsPair() }
    }

    private fun FixtureCore.toApiSportsPair(): Pair<FixtureCore, FixtureApiSports>? {
        val apiSports = apiSports
        if (apiSports == null) {
            log.warn("FixtureCore(uid={}) has no apiSports - skipping", uid)
            return null
        }
        return Pair(this, apiSports)
    }
}
