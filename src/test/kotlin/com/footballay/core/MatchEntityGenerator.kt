package com.footballay.core

import com.footballay.core.infra.persistence.apisports.entity.*
import com.footballay.core.infra.persistence.apisports.entity.live.*
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.entity.PlayerCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.persistence.core.repository.TeamCoreRepository
import com.footballay.core.infra.persistence.core.repository.PlayerCoreRepository
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsSeasonRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.PlayerApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchTeamRepository
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchPlayerRepository
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchEventRepository
import com.footballay.core.infra.util.UidGenerator
import com.footballay.core.logger
import org.springframework.boot.test.context.TestComponent
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

/**
 * 테스트용 매치 엔티티 팩토리
 * 
 * 복잡한 매치 엔티티 생태계를 쉽게 생성할 수 있도록 도와주는 테스트 전용 유틸리티입니다.
 * 스프링 테스트 컨텍스트에서 빈으로 관리되어 모든 repository가 자동 주입됩니다.
 */
@TestComponent
class MatchEntityGenerator(
    // Core repositories
    private val leagueCoreRepository: LeagueCoreRepository,
    private val teamCoreRepository: TeamCoreRepository,
    private val playerCoreRepository: PlayerCoreRepository,
    private val fixtureCoreRepository: FixtureCoreRepository,
    
    // ApiSports repositories
    private val leagueApiSportsRepository: LeagueApiSportsRepository,
    private val leagueApiSportsSeasonRepository: LeagueApiSportsSeasonRepository,
    private val teamApiSportsRepository: TeamApiSportsRepository,
    private val playerApiSportsRepository: PlayerApiSportsRepository,
    private val fixtureApiSportsRepository: FixtureApiSportsRepository,
    
    // Live match repositories
    private val apiSportsMatchTeamRepository: ApiSportsMatchTeamRepository,
    private val apiSportsMatchPlayerRepository: ApiSportsMatchPlayerRepository,
    private val apiSportsMatchEventRepository: ApiSportsMatchEventRepository,
    
    // Utilities
    private val uidGenerator: UidGenerator
) {
    
    private val log = logger()
    
    /**
     * 완전한 매치 엔티티 생태계를 생성합니다.
     * Core → ApiSports → Live 엔티티까지 모든 연관관계를 포함합니다.
     */
    fun createCompleteMatchEntities(config: MatchConfig = MatchConfig()): MatchEntities {
        log.info("완전한 매치 엔티티 생태계 생성 시작: {}", config)
        
        // 1. Core entities
        val leagueCore = createLeagueCore(config.leagueName)
        val homeTeam = createTeamCore(config.homeTeamName, config.homeTeamCode)
        val awayTeam = createTeamCore(config.awayTeamName, config.awayTeamCode)
        val players = createPlayerCores(config.playerConfigs)
        val fixtureCore = createFixtureCore(leagueCore, homeTeam, awayTeam, config.kickoffTime)
        
        // 2. ApiSports entities
        val leagueApiSports = createLeagueApiSports(leagueCore, config.leagueApiId)
        val season = createLeagueApiSportsSeason(leagueApiSports, config.seasonYear)
        val homeTeamApiSports = createTeamApiSports(homeTeam, config.homeTeamApiId)
        val awayTeamApiSports = createTeamApiSports(awayTeam, config.awayTeamApiId)
        val playersApiSports = createPlayersApiSports(players)
        
        // 3. Live match entities
        val homeMatchTeam = createApiSportsMatchTeam(homeTeamApiSports, config.homeTeamFormation)
        val awayMatchTeam = createApiSportsMatchTeam(awayTeamApiSports, config.awayTeamFormation)
        val fixtureApiSports = createFixtureApiSports(fixtureCore, season, homeMatchTeam, awayMatchTeam, config)
        val matchPlayers = createMatchPlayers(playersApiSports, homeMatchTeam, awayMatchTeam, config)
        val matchEvents = createMatchEvents(fixtureApiSports, homeMatchTeam, matchPlayers, config)
        
        val result = MatchEntities(
            // Core
            leagueCore = leagueCore,
            homeTeam = homeTeam,
            awayTeam = awayTeam,
            players = players,
            fixtureCore = fixtureCore,
            
            // ApiSports
            leagueApiSports = leagueApiSports,
            season = season,
            homeTeamApiSports = homeTeamApiSports,
            awayTeamApiSports = awayTeamApiSports,
            playersApiSports = playersApiSports,
            
            // Live
            homeMatchTeam = homeMatchTeam,
            awayMatchTeam = awayMatchTeam,
            fixtureApiSports = fixtureApiSports,
            matchPlayers = matchPlayers,
            matchEvents = matchEvents
        )
        
        log.info("완전한 매치 엔티티 생태계 생성 완료")
        return result
    }
    
    /**
     * 기본 매치 엔티티만 생성합니다. (Live 데이터 제외)
     */
    fun createBasicMatchEntities(config: MatchConfig = MatchConfig()): MatchEntities {
        return createCompleteMatchEntities(config.copy(createLiveData = false))
    }
    
    // --- Private helper methods ---
    
    private fun createLeagueCore(name: String): LeagueCore {
        return leagueCoreRepository.save(
            LeagueCore(
                uid = uidGenerator.generateUid(),
                name = name,
                available = true,
                autoGenerated = false
            )
        )
    }
    
    private fun createTeamCore(name: String, code: String): TeamCore {
        return teamCoreRepository.save(
            TeamCore(
                uid = uidGenerator.generateUid(),
                name = name,
                code = code,
                country = "England",
                founded = 1900,
                autoGenerated = false
            )
        )
    }
    
    private fun createPlayerCores(configs: List<PlayerConfig>): List<PlayerCore> {
        return configs.map { config ->
            playerCoreRepository.save(
                PlayerCore(
                    uid = uidGenerator.generateUid(),
                    name = config.name,
                    firstname = config.firstname,
                    lastname = config.lastname,
                    nationality = config.nationality,
                    position = config.position,
                    autoGenerated = false
                )
            )
        }
    }
    
    private fun createFixtureCore(
        league: LeagueCore, 
        homeTeam: TeamCore, 
        awayTeam: TeamCore, 
        kickoffTime: OffsetDateTime
    ): FixtureCore {
        return fixtureCoreRepository.save(
            FixtureCore(
                uid = uidGenerator.generateUid(),
                kickoff = kickoffTime,
                timestamp = kickoffTime.toEpochSecond(),
                status = "Not Started",
                statusShort = "NS",
                elapsedMin = null,
                league = league,
                homeTeam = homeTeam,
                awayTeam = awayTeam,
                goalsHome = null,
                goalsAway = null,
                finished = false,
                available = true,
                autoGenerated = false
            )
        )
    }
    
    private fun createLeagueApiSports(leagueCore: LeagueCore, apiId: Long): LeagueApiSports {
        return leagueApiSportsRepository.save(
            LeagueApiSports(
                leagueCore = leagueCore,
                apiId = apiId,
                name = leagueCore.name,
                type = "League",
                countryName = "England",
                countryCode = "GB",
                currentSeason = 2024
            )
        )
    }
    
    private fun createLeagueApiSportsSeason(leagueApiSports: LeagueApiSports, seasonYear: Int): LeagueApiSportsSeason {
        return leagueApiSportsSeasonRepository.save(
            LeagueApiSportsSeason(
                seasonYear = seasonYear,
                seasonStart = "$seasonYear-08-17",
                seasonEnd = "${seasonYear + 1}-05-25",
                coverage = LeagueApiSportsCoverage(
                    fixturesEvents = true,
                    fixturesLineups = true,
                    fixturesStatistics = true,
                    fixturesPlayers = true,
                    standings = true
                ),
                leagueApiSports = leagueApiSports
            )
        )
    }
    
    private fun createTeamApiSports(teamCore: TeamCore, apiId: Long): TeamApiSports {
        return teamApiSportsRepository.save(
            TeamApiSports(
                teamCore = teamCore,
                apiId = apiId,
                name = teamCore.name,
                code = teamCore.code,
                country = teamCore.country ?: "England",
                founded = teamCore.founded,
                national = false
            )
        )
    }
    
    private fun createPlayersApiSports(playerCores: List<PlayerCore>): List<PlayerApiSports> {
        return playerCores.mapIndexed { index, playerCore ->
            playerApiSportsRepository.save(
                PlayerApiSports(
                    playerCore = playerCore,
                    apiId = 1000L + index + 1,
                    name = playerCore.name,
                    firstname = playerCore.firstname,
                    lastname = playerCore.lastname,
                    nationality = playerCore.nationality,
                    position = when(playerCore.position) {
                        "F" -> "Attacker"
                        "M" -> "Midfielder"
                        "D" -> "Defender"
                        "G" -> "Goalkeeper"
                        else -> "Unknown"
                    }
                )
            )
        }
    }
    
    private fun createApiSportsMatchTeam(teamApiSports: TeamApiSports, formation: String): ApiSportsMatchTeam {
        return apiSportsMatchTeamRepository.save(
            ApiSportsMatchTeam(
                teamApiSports = teamApiSports,
                formation = formation,
                playerColor = UniformColor(
                    primary = "ea0000",
                    number = "ffffff",
                    border = "ea0000"
                ),
                goalkeeperColor = UniformColor(
                    primary = "000000",
                    number = "ffffff",
                    border = "000000"
                ),
                winner = null
            )
        )
    }
    
    private fun createFixtureApiSports(
        fixtureCore: FixtureCore,
        season: LeagueApiSportsSeason,
        homeTeam: ApiSportsMatchTeam,
        awayTeam: ApiSportsMatchTeam,
        config: MatchConfig
    ): FixtureApiSports {
        return fixtureApiSportsRepository.save(
            FixtureApiSports(
                core = fixtureCore,
                apiId = config.fixtureApiId,
                referee = config.referee,
                timezone = "UTC",
                date = config.kickoffTime,
                timestamp = config.kickoffTime.toEpochSecond(),
                round = config.round,
                status = ApiSportsStatus(
                    longStatus = "Not Started",
                    shortStatus = "NS",
                    elapsed = null,
                    extra = null
                ),
                score = ApiSportsScore(
                    totalHome = null,
                    totalAway = null,
                    halftimeHome = null,
                    halftimeAway = null,
                    fulltimeHome = null,
                    fulltimeAway = null
                ),
                venue = null,
                season = season,
                homeTeam = homeTeam,
                awayTeam = awayTeam
            )
        )
    }
    
    private fun createMatchPlayers(
        playersApiSports: List<PlayerApiSports>,
        homeMatchTeam: ApiSportsMatchTeam,
        awayMatchTeam: ApiSportsMatchTeam,
        config: MatchConfig
    ): List<ApiSportsMatchPlayer> {
        if (!config.createLiveData) return emptyList()
        
        val matchPlayers = playersApiSports.mapIndexed { index, playerApiSports ->
            val matchTeam = if (index < playersApiSports.size / 2) homeMatchTeam else awayMatchTeam
            val matchPlayer = ApiSportsMatchPlayer(
                matchPlayerUid = uidGenerator.generateUid(),
                playerApiSports = playerApiSports,
                name = playerApiSports.name ?: "Unknown",
                number = index + 1,
                position = playerApiSports.position?.substring(0, 1) ?: "F",
                grid = "${index + 1}:${index + 1}",
                substitute = false,
                matchTeam = matchTeam
            )
            
            // 양방향 연관관계 설정
            matchTeam.players.add(matchPlayer)
            
            apiSportsMatchPlayerRepository.save(matchPlayer)
        }
        
        // 팀 엔티티들도 저장 (연관관계 업데이트)
        apiSportsMatchTeamRepository.save(homeMatchTeam)
        apiSportsMatchTeamRepository.save(awayMatchTeam)
        
        return matchPlayers
    }
    
    private fun createMatchEvents(
        fixtureApiSports: FixtureApiSports,
        homeMatchTeam: ApiSportsMatchTeam,
        matchPlayers: List<ApiSportsMatchPlayer>,
        config: MatchConfig
    ): List<ApiSportsMatchEvent> {
        if (!config.createLiveData || matchPlayers.isEmpty()) return emptyList()
        
        val event = apiSportsMatchEventRepository.save(
            ApiSportsMatchEvent(
                fixtureApi = fixtureApiSports,
                matchTeam = homeMatchTeam,
                player = matchPlayers.first(),
                assist = null,
                sequence = 1,
                elapsedTime = 25,
                extraTime = null,
                eventType = "goal",
                detail = "Normal Goal",
                comments = "Great goal!"
            )
        )
        
        // 양방향 연관관계 설정
        fixtureApiSports.events.add(event)
        fixtureApiSportsRepository.save(fixtureApiSports)
        
        return listOf(event)
    }
}

/**
 * 매치 엔티티 생성을 위한 설정 클래스
 */
data class MatchConfig(
    // Basic info
    val leagueName: String = "Test Premier League",
    val homeTeamName: String = "Manchester United",
    val awayTeamName: String = "Arsenal",
    val homeTeamCode: String = "MUN",
    val awayTeamCode: String = "ARS",
    val homeTeamFormation: String = "4-3-3",
    val awayTeamFormation: String = "4-2-3-1",
    
    // API IDs
    val leagueApiId: Long = 39L,
    val homeTeamApiId: Long = 33L,
    val awayTeamApiId: Long = 42L,
    val fixtureApiId: Long = 12345L,
    
    // Match info
    val kickoffTime: OffsetDateTime = OffsetDateTime.now().plusDays(1),
    val referee: String = "Michael Oliver",
    val round: String = "Regular Season - 10",
    val seasonYear: Int = 2024,
    
    // Player configurations
    val playerConfigs: List<PlayerConfig> = defaultPlayerConfigs(),
    
    // Control flags
    val createLiveData: Boolean = true
) {
    companion object {
        fun defaultPlayerConfigs(): List<PlayerConfig> = listOf(
            PlayerConfig("Marcus Rashford", "Marcus", "Rashford", "England", "F"),
            PlayerConfig("Bruno Fernandes", "Bruno", "Fernandes", "Portugal", "M"),
            PlayerConfig("Bukayo Saka", "Bukayo", "Saka", "England", "F")
        )
    }
}

/**
 * 선수 생성을 위한 설정 클래스
 */
data class PlayerConfig(
    val name: String,
    val firstname: String,
    val lastname: String,
    val nationality: String,
    val position: String
)

/**
 * 생성된 모든 매치 엔티티들을 담는 컨테이너 클래스
 */
data class MatchEntities(
    // Core entities
    val leagueCore: LeagueCore,
    val homeTeam: TeamCore,
    val awayTeam: TeamCore,
    val players: List<PlayerCore>,
    val fixtureCore: FixtureCore,
    
    // ApiSports entities
    val leagueApiSports: LeagueApiSports,
    val season: LeagueApiSportsSeason,
    val homeTeamApiSports: TeamApiSports,
    val awayTeamApiSports: TeamApiSports,
    val playersApiSports: List<PlayerApiSports>,
    
    // Live entities
    val homeMatchTeam: ApiSportsMatchTeam,
    val awayMatchTeam: ApiSportsMatchTeam,
    val fixtureApiSports: FixtureApiSports,
    val matchPlayers: List<ApiSportsMatchPlayer>,
    val matchEvents: List<ApiSportsMatchEvent>
)
