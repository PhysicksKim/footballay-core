package com.footballay.core.domain.admin.apisports.mapper

import com.footballay.core.domain.admin.apisports.model.ApiSportsPlayerDetails
import com.footballay.core.domain.admin.apisports.model.ApiSportsTeamDetails
import com.footballay.core.domain.admin.apisports.model.PlayerModel
import com.footballay.core.domain.admin.apisports.model.TeamModel
import com.footballay.core.infra.persistence.apisports.entity.PlayerApiSports
import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports
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
     * TeamApiSports Entity를 TeamModel로 변환
     *
     * @param teamApiSports TeamApiSports 엔티티
     * @return TeamModel
     * @throws IllegalStateException apiId가 null인 경우
     */
    fun toTeamModel(teamApiSports: TeamApiSports): TeamModel {
        val teamCore = teamApiSports.teamCore

        return TeamModel(
            teamApiId = teamApiSports.apiId ?: throw IllegalStateException("TeamApiSports apiId is null"),
            teamCoreId = teamCore?.id,
            uid = teamCore?.uid ?: "apisports:${teamApiSports.apiId}",
            name = teamApiSports.name ?: "Unknown Team",
            code = teamApiSports.code,
            country = teamApiSports.country,
            details =
                ApiSportsTeamDetails(
                    founded = teamApiSports.founded,
                    national = teamApiSports.national ?: false,
                    logo = teamApiSports.logo,
                ),
            detailsType = "ApiSports",
        )
    }

    /**
     * PlayerApiSports Entity를 PlayerModel로 변환
     *
     * @param playerApiSports PlayerApiSports 엔티티
     * @return PlayerModel
     * @throws IllegalStateException apiId가 null인 경우
     */
    fun toPlayerModel(playerApiSports: PlayerApiSports): PlayerModel {
        val playerCore = playerApiSports.playerCore

        return PlayerModel(
            playerApiId = playerApiSports.apiId ?: throw IllegalStateException("PlayerApiSports apiId is null"),
            playerCoreId = playerCore?.id,
            uid = playerCore?.uid ?: "apisports:${playerApiSports.apiId}",
            name = playerApiSports.name ?: "Unknown Player",
            firstname = playerApiSports.firstname,
            lastname = playerApiSports.lastname,
            position = playerApiSports.position,
            number = playerApiSports.number,
            details =
                ApiSportsPlayerDetails(
                    age = playerApiSports.age,
                    nationality = playerApiSports.nationality,
                    height = playerApiSports.height,
                    weight = playerApiSports.weight,
                    photo = playerApiSports.photo,
                ),
            detailsType = "ApiSports",
        )
    }
}
