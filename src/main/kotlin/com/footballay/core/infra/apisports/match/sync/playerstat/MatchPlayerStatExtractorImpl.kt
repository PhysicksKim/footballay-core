package com.footballay.core.infra.apisports.match.sync.playerstat

import com.footballay.core.infra.apisports.match.dto.FullMatchSyncDto
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerContext
import com.footballay.core.infra.apisports.match.sync.context.MatchPlayerKeyGenerator
import com.footballay.core.infra.apisports.match.sync.dto.MatchPlayerDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchPlayerStatPlanDto
import com.footballay.core.logger
import org.springframework.stereotype.Component

/**
 * 선수 통계 데이터를 동기화합니다.
 *
 * 책임:
 * - 선수별 경기 통계 데이터 처리
 * - Context를 통한 선수 매칭
 * - 통계 전용 선수 생성 (드물지만 가능)
 */
@Component
class MatchPlayerStatExtractorImpl : MatchPlayerStatDtoExtractor {
    companion object {
        private const val DEFAULT_SUBSTITUTE_VALUE = true
    }

    private val log = logger()

    override fun extractPlayerStats(
        dto: FullMatchSyncDto,
        context: MatchPlayerContext,
    ): MatchPlayerStatPlanDto {
        if (dto.players.isEmpty()) {
            log.info("Not exist player statistics for match: {}", dto.fixture.id)
            return MatchPlayerStatPlanDto.Companion.empty()
        }

        val homeId = dto.teams.home.id
        val awayId = dto.teams.away.id
        if (homeId == null || awayId == null) {
            log.warn(
                "Home or Away team ID is null for match: ${dto.fixture.id}\n" +
                    "Home ID: $homeId found=${homeId == null}, Away ID: $awayId found=${awayId == null}",
            )

            return MatchPlayerStatPlanDto.Companion.empty()
        }

        val homePlayerStatList = dto.players.find { it.team.id == homeId }
        val awayPlayerStatList = dto.players.find { it.team.id == awayId }

        if (homePlayerStatList == null && awayPlayerStatList == null) {
            log.warn(
                "Both Home and Away player statistics not found for match: ${dto.fixture.id}\n" +
                    "Home ID: $homeId, Away ID: $awayId",
            )
            return MatchPlayerStatPlanDto.Companion.empty()
        }

        val homePlayerStats =
            if (homePlayerStatList != null) {
                processPlayerStatistics(homePlayerStatList, context)
            } else {
                emptyList()
            }
        val awayPlayerStats =
            if (awayPlayerStatList != null) {
                processPlayerStatistics(awayPlayerStatList, context)
            } else {
                emptyList()
            }

        return MatchPlayerStatPlanDto(
            homePlayerStatList = homePlayerStats,
            awayPlayerStatList = awayPlayerStats,
        )
    }

    private fun processPlayerStatistics(
        playerStatList: FullMatchSyncDto.PlayerStatisticsDto,
        context: MatchPlayerContext,
    ): List<MatchPlayerStatPlanDto.PlayerStatSyncItemDto> {
        val teamApiId =
            playerStatList.team.id ?: run {
                log.error("Team ID is null in player statistics")
                return emptyList()
            }

        return playerStatList.players.mapNotNull { playerDetail ->
            val player = playerDetail.player
            val statistics = playerDetail.statistics.firstOrNull()

            if (player.name.isNullOrBlank()) {
                log.warn("Player name is null or blank for player ID: {}", player.id)
                return@mapNotNull null
            }

            val mpKey = MatchPlayerKeyGenerator.generateMatchPlayerKey(player.id, player.name)
            var matchPlayer = context.lineupMpDtoMap[mpKey]

            if (matchPlayer == null) {
                matchPlayer = context.eventMpDtoMap[mpKey]
                if (matchPlayer == null) {
                    log.warn(
                        "Match player not found for key: $mpKey (player: ${player.name}, team: ${playerStatList.team.name})",
                    )
                    val matchPlayerDto = createStatOnlyMatchPlayerDto(player, player.name, statistics, teamApiId)
                    context.statMpDtoMap[mpKey] = matchPlayerDto
                }
            }

            createPlayerStatDto(player, statistics)
        }
    }

    /**
     * id=null 선수인 경우 특히 이름이 일관되게 등장하지 않아서 라인업과 통계에서 다른 이름으로 등장하곤 합니다.
     *
     * **중요한 비즈니스 로직:**
     * - ID null 선수이고 매칭에 실패한 경우, 무조건 substitute=true로 설정
     * - 이는 선발 선수 중복 문제를 방지하기 위함
     * - 특히 아시아권/중동권에서 이름 표기 방식 차이로 인한 문제 해결
     */
    private fun createStatOnlyMatchPlayerDto(
        player: FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.PlayerDetailInfoDto,
        name: String,
        statistics: FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto?,
        teamApiId: Long,
    ): MatchPlayerDto {
        // ID null 선수이고 매칭에 실패한 경우, 무조건 substitute=true로 설정
        val isSubstitute =
            if (player.id == null) {
                log.info("ID null 선수 매칭 실패로 인한 통계 전용 선수 생성: {} (무조건 substitute=true로 설정)", name)
                true
            } else {
                statistics?.games?.substitute ?: DEFAULT_SUBSTITUTE_VALUE
            }

        return MatchPlayerDto(
            apiId = player.id,
            name = name,
            number = statistics?.games?.number,
            position = statistics?.games?.position,
            grid = null,
            substitute = isSubstitute,
            nonLineupPlayer = true,
            teamApiId = teamApiId,
            playerApiSportsInfo = null,
        )
    }

    private fun createPlayerStatDto(
        player: FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.PlayerDetailInfoDto,
        statistics: FullMatchSyncDto.PlayerStatisticsDto.PlayerDetailDto.StatDetailDto?,
    ): MatchPlayerStatPlanDto.PlayerStatSyncItemDto =
        MatchPlayerStatPlanDto.PlayerStatSyncItemDto(
            playerApiId = player.id,
            name = player.name!!,
            // Games statistics
            minutesPlayed = statistics?.games?.minutes,
            shirtNumber = statistics?.games?.number,
            position = statistics?.games?.position,
            rating = statistics?.games?.rating?.toDoubleOrNull(),
            isCaptain = statistics?.games?.captain ?: false,
            isSubstitute = statistics?.games?.substitute ?: false,
            // Offsides
            offsides = statistics?.offsides,
            // Shots
            shotsTotal = statistics?.shots?.total,
            shotsOnTarget = statistics?.shots?.on,
            // Goals
            goalsTotal = statistics?.goals?.total,
            goalsConceded = statistics?.goals?.conceded,
            assists = statistics?.goals?.assists,
            saves = statistics?.goals?.saves,
            // Passes
            passesTotal = statistics?.passes?.total,
            keyPasses = statistics?.passes?.key,
            passesAccuracy = statistics?.passes?.accuracy?.toIntOrNull(),
            // Tackles
            tacklesTotal = statistics?.tackles?.total,
            blocks = statistics?.tackles?.blocks,
            interceptions = statistics?.tackles?.interceptions,
            // Duels
            duelsTotal = statistics?.duels?.total,
            duelsWon = statistics?.duels?.won,
            // Dribbles
            dribblesAttempts = statistics?.dribbles?.attempts,
            dribblesSuccess = statistics?.dribbles?.success,
            dribblesPast = statistics?.dribbles?.past,
            // Fouls
            foulsDrawn = statistics?.fouls?.drawn,
            foulsCommitted = statistics?.fouls?.committed,
            // Cards
            yellowCards = statistics?.cards?.yellow ?: 0,
            redCards = statistics?.cards?.red ?: 0,
            // Penalty
            penaltyWon = statistics?.penalty?.won,
            penaltyCommitted = statistics?.penalty?.commited, // JSON의 오타 반영
            penaltyScored = statistics?.penalty?.scored ?: 0,
            penaltyMissed = statistics?.penalty?.missed ?: 0,
            penaltySaved = statistics?.penalty?.saved ?: 0,
        )
}
