package com.footballay.core.domain.model.mapper

import com.footballay.core.domain.model.TeamApiSportsExtension
import com.footballay.core.domain.model.FixtureApiSportsExtension
import com.footballay.core.domain.model.FixtureModel
import com.footballay.core.domain.model.LeagueApiSportsExtension
import com.footballay.core.domain.model.LeagueModel
import com.footballay.core.domain.model.PlayerApiSportsExtension
import com.footballay.core.domain.model.PlayerModel
import com.footballay.core.domain.model.TeamModel
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.entity.PlayerApiSports
import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.entity.FixtureStatusCode
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.PlayerCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import org.springframework.stereotype.Component

/**
 * Domain Model Mapper
 *
 * Repository Entity를 Domain Model로 변환하는 책임을 가집니다.
 * Facade에서 변환 로직을 분리하여 단일 책임 원칙을 준수합니다.
 */
@Component
class DomainModelMapper {
    /**
     * LeagueCore Entity를 LeagueModel로 변환
     *
     * @param leagueCore LeagueCore 엔티티
     * @return LeagueModel
     */
    fun toLeagueModel(
        leagueCore: LeagueCore,
        leagueApiSports: LeagueApiSports,
    ): LeagueModel {
        val photo = leagueApiSports.logo
        val extra =
            LeagueApiSportsExtension(
                apiId = leagueApiSports.apiId,
            )

        return LeagueModel(
            uid = leagueCore.uid,
            name = leagueCore.name,
            nameKo = leagueCore.nameKo,
            photo = photo,
            available = leagueCore.available,
            extension = extra,
        )
    }

    /**
     * TeamApiSports Entity를 TeamModel로 변환
     *
     * @param teamApiSports TeamApiSports 엔티티
     * @return TeamModel
     * @throws IllegalStateException apiId가 null인 경우
     */
    fun toTeamModel(
        teamCore: TeamCore,
        teamApiSports: TeamApiSports,
    ): TeamModel =
        TeamModel(
            uid = teamCore.uid,
            name = teamCore.name,
            nameKo = teamCore.nameKo,
            code = teamCore.code,
            extension =
                TeamApiSportsExtension(
                    apiId =
                        teamApiSports.apiId
                            ?: throw IllegalStateException("TeamApiSports apiId is null when TeamModel mapping"),
                    founded = teamApiSports.founded,
                    national = teamApiSports.national ?: false,
                    logo = teamApiSports.logo,
                ),
        )

    /**
     * PlayerApiSports Entity를 PlayerModel로 변환
     *
     * @param playerApiSports PlayerApiSports 엔티티
     * @return PlayerModel
     * @throws IllegalStateException apiId가 null인 경우
     */
    fun toPlayerModel(
        playerCore: PlayerCore,
        playerApiSports: PlayerApiSports,
    ): PlayerModel {
        if (playerApiSports.apiId == null) {
            throw IllegalStateException("PlayerApiSports apiId is null when PlayerModel mapping")
        }

        val photo = playerApiSports.photo
        val position = playerApiSports.position
        val number = playerApiSports.number
        val extension =
            PlayerApiSportsExtension(
                apiId = playerApiSports.apiId!!,
                nationality = playerApiSports.nationality,
            )

        return PlayerModel(
            uid = playerCore.uid,
            name = playerCore.name,
            nameKo = playerCore.nameKo,
            photo = photo,
            position = position,
            number = number,
            extension = extension,
        )
    }

    /**
     * [leagueUid]가 필요 없는 경우 생략 가능합니다.
     *
     * Status와 같이 Core와 Api 두 엔티티 모두 다 존재하는 값들은 Core를 사용합니다.
     */
    fun toFixtureModel(
        fixtureCore: FixtureCore,
        fixtureApiSports: FixtureApiSports,
        leagueUid: String? = null,
    ): FixtureModel {
        val homeCore = fixtureCore.homeTeam
        val awayCore = fixtureCore.awayTeam

        val fixtureSchedule =
            FixtureModel.FixtureSchedule(
                kickoffAt = fixtureCore.kickoff,
                round = fixtureCore.apiSports?.round ?: "",
            )

        val homeSide = createTeamSide(homeCore)
        val awaySide = createTeamSide(awayCore)

        val modelStatusCode = mapStatusCode(fixtureCore.statusCode)
        val status =
            FixtureModel.Status(
                statusText = fixtureCore.statusText,
                code = modelStatusCode,
                elapsed = fixtureCore.elapsedMin,
                extra = fixtureCore.apiSports?.status?.extra,
            )

        val score = createScore(fixtureCore)

        val apiSportsExtension =
            FixtureApiSportsExtension(
                apiId = fixtureApiSports.apiId,
                refereeName = fixtureApiSports.referee,
            )

        return FixtureModel(
            uid = fixtureCore.uid,
            leagueUid = leagueUid,
            schedule = fixtureSchedule,
            homeTeam = homeSide,
            awayTeam = awaySide,
            status = status,
            score = score,
            available = fixtureCore.available,
            extension = apiSportsExtension,
        )
    }

    private fun mapStatusCode(statusShort: FixtureStatusCode): FixtureModel.StatusCode =
        when (statusShort) {
            FixtureStatusCode.NS -> FixtureModel.StatusCode.NS
            FixtureStatusCode.TBD -> FixtureModel.StatusCode.TBD
            FixtureStatusCode.FIRST_HALF -> FixtureModel.StatusCode.FIRST_HALF
            FixtureStatusCode.HT -> FixtureModel.StatusCode.HT
            FixtureStatusCode.SECOND_HALF -> FixtureModel.StatusCode.SECOND_HALF
            FixtureStatusCode.FT -> FixtureModel.StatusCode.FT
            FixtureStatusCode.ET -> FixtureModel.StatusCode.ET
            FixtureStatusCode.PST -> FixtureModel.StatusCode.PST
            FixtureStatusCode.CANC -> FixtureModel.StatusCode.CANC
            else -> FixtureModel.StatusCode.ETC
        }

    private fun createScore(fixtureCore: FixtureCore): FixtureModel.Score {
        val homeGoal = fixtureCore.goalsHome
        val awayGoal = fixtureCore.goalsAway
        return FixtureModel.Score(
            home = homeGoal,
            away = awayGoal,
        )
    }

    private fun createTeamSide(teamCore: TeamCore?): FixtureModel.TeamSide? =
        if (teamCore != null) {
            FixtureModel.TeamSide(
                uid = teamCore.uid,
                name = teamCore.name,
                nameKo = teamCore.nameKo,
                logo = null, // 로고 정보는 FixtureCore에서 직접 제공되지 않으므로 null로 설정
            )
        } else {
            null
        }
}
