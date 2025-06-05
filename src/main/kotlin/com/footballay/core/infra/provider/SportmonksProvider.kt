package com.footballay.core.infra.provider

import com.footballay.core.infra.provider.dto.*
import org.springframework.stereotype.Component

/**
 * 향후 Sportmonks 연동을 위한 스켈레톤
 */
@Component
class SportmonksProvider : FootballDataProvider {
    
    override fun fetchAllCurrentLeagues(): List<ApiLeagueDto> {
        // TODO: Sportmonks API 호출 로직 작성 (예: 리그 목록, 시즌 정보 등)
        throw NotImplementedError("SportmonksProvider는 아직 구현되지 않았습니다.")
    }

    override fun fetchLeagueById(leagueId: Long): ApiLeagueDto {
        // TODO: Sportmonks를 통해 리그 상세 정보 가져오기
        throw NotImplementedError("SportmonksProvider는 아직 구현되지 않았습니다.")
    }

    override fun fetchTeamsOfLeague(leagueId: Long, season: Int): List<ApiTeamDto> {
        // TODO: Sportmonks를 통해 팀 목록 가져오기
        throw NotImplementedError("SportmonksProvider는 아직 구현되지 않았습니다.")
    }

    override fun fetchPlayersOfTeam(teamId: Long): List<ApiPlayerDto> {
        // TODO: Sportmonks를 통해 선수 목록 가져오기
        throw NotImplementedError("SportmonksProvider는 아직 구현되지 않았습니다.")
    }

    override fun fetchFixturesOfLeagueSeason(leagueId: Long, season: Int): List<ApiFixtureDto> {
        // TODO: Sportmonks를 통해 경기 일정 가져오기
        throw NotImplementedError("SportmonksProvider는 아직 구현되지 않았습니다.")
    }
} 