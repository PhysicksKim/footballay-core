package com.footballay.core.infra.apisports

import com.footballay.core.infra.apisports.dto.FixtureApiSportsCreateDto
import com.footballay.core.infra.apisports.dto.TeamOfFixtureApiSportsCreateDto
import com.footballay.core.infra.apisports.syncer.FixtureApiSportsSyncer
import com.footballay.core.infra.persistence.apisports.entity.*
import com.footballay.core.infra.persistence.apisports.repository.*
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.util.UidGenerator
import com.footballay.core.logger
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import io.mockk.*

class FixtureApiSportsSyncerTest {

    val log = logger()

    // Mock repositories
    private val fixtureApiSportsRepository: FixtureApiSportsRepository = mockk()
    private val leagueApiSportsRepository: LeagueApiSportsRepository = mockk()
    private val leagueApiSportsSeasonRepository: LeagueApiSportsSeasonRepository = mockk()
    private val teamApiSportsRepository: TeamApiSportsRepository = mockk()
    private val fixtureCoreRepository: FixtureCoreRepository = mockk()
    private val leagueCoreRepository: LeagueCoreRepository = mockk()
    
    // Mock services
    private val venueApiSportsService: VenueApiSportsService = mockk()
    
    // Mock utilities
    private val uidGenerator: UidGenerator = mockk()

    // System under test
    private lateinit var fixtureApiSportsSyncer: FixtureApiSportsSyncer

    @BeforeEach
    fun setUp() {
        fixtureApiSportsSyncer = FixtureApiSportsSyncer(
            fixtureApiSportsRepository = fixtureApiSportsRepository,
            leagueApiSportsRepository = leagueApiSportsRepository,
            leagueApiSportsSeasonRepository = leagueApiSportsSeasonRepository,
            teamApiSportsRepository = teamApiSportsRepository,
            fixtureCoreRepository = fixtureCoreRepository,
            leagueCoreRepository = leagueCoreRepository,
            venueApiSportsService = venueApiSportsService,
            uidGenerator = uidGenerator
        )
    }

    @Test
    fun `빈 DTO 리스트로 호출 시 예외 발생`() {
        // given
        val leagueApiId = 39L
        val emptyDtos = emptyList<FixtureApiSportsCreateDto>()

        // when & then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            fixtureApiSportsSyncer.saveFixturesOfLeagueWithCurrentSeason(leagueApiId, emptyDtos)
        }
        assertEquals("Fixtures list cannot be empty", exception.message)
    }

    @Test
    fun `존재하지 않는 리그 API ID로 호출 시 예외 발생`() {
        // given
        val nonExistentLeagueApiId = 9999L
        val dtos = listOf(
            FixtureApiSportsCreateDto(
                apiId = 1L,
                leagueApiId = nonExistentLeagueApiId,
                seasonYear = "2024"
            )
        )
        
        every { leagueApiSportsRepository.findByApiId(nonExistentLeagueApiId) } returns null

        // when & then
        val exception = assertThrows(IllegalStateException::class.java) {
            fixtureApiSportsSyncer.saveFixturesOfLeagueWithCurrentSeason(nonExistentLeagueApiId, dtos)
        }
        assertTrue(exception.message!!.contains("League with API ID $nonExistentLeagueApiId must be synced first"))
    }

    @Test
    fun `LeagueCore가 없는 상태에서 호출 시 예외 발생`() {
        // given
        val leagueApiId = 39L
        val dtos = listOf(
            FixtureApiSportsCreateDto(
                apiId = 1L,
                leagueApiId = leagueApiId,
                seasonYear = "2024"
            )
        )
        
        val leagueApiSportsWithoutCore = LeagueApiSports(
            leagueCore = null, // LeagueCore가 없는 상태
            apiId = leagueApiId,
            name = "Premier League",
            type = "League"
        )
        
        every { leagueApiSportsRepository.findByApiId(leagueApiId) } returns leagueApiSportsWithoutCore

        // when & then
        val exception = assertThrows(IllegalStateException::class.java) {
            fixtureApiSportsSyncer.saveFixturesOfLeagueWithCurrentSeason(leagueApiId, dtos)
        }
        assertTrue(exception.message!!.contains("League core not found for API ID"))
    }

    @Test
    fun `시즌이 동기화되지 않은 상태에서 호출 시 예외 발생`() {
        // given
        val leagueApiId = 39L
        val dtos = listOf(
            FixtureApiSportsCreateDto(
                apiId = 1L,
                leagueApiId = leagueApiId,
                seasonYear = "2024"
            )
        )
        
        val leagueCore = mockk<LeagueCore>()
        val leagueApiSports = LeagueApiSports(
            leagueCore = leagueCore,
            apiId = leagueApiId,
            name = "Premier League",
            type = "League"
        )

        every { leagueApiSportsRepository.findByApiId(leagueApiId) } returns leagueApiSports
        every { leagueApiSportsSeasonRepository.findAllByLeagueApiSports(leagueApiSports) } returns emptyList()

        // when & then
        val exception = assertThrows(IllegalStateException::class.java) {
            fixtureApiSportsSyncer.saveFixturesOfLeagueWithCurrentSeason(leagueApiId, dtos)
        }
        assertTrue(exception.message!!.contains("Season 2024 not found for league"))
    }

    @Test
    fun `DTO에 일관성 없는 리그 ID가 포함된 경우 예외 발생`() {
        // given
        val leagueApiId = 39L
        val dtos = listOf(
            FixtureApiSportsCreateDto(
                apiId = 1L,
                leagueApiId = leagueApiId,
                seasonYear = "2024"
            ),
            FixtureApiSportsCreateDto(
                apiId = 2L,
                leagueApiId = 40L, // 다른 리그 ID
                seasonYear = "2024"
            )
        )
        
        val leagueCore = mockk<LeagueCore>()
        val season = LeagueApiSportsSeason(seasonYear = 2024)
        val leagueApiSports = LeagueApiSports(
            leagueCore = leagueCore,
            apiId = leagueApiId,
            name = "Premier League",
            type = "League"
        )
        
        every { leagueApiSportsRepository.findByApiId(leagueApiId) } returns leagueApiSports
        every { leagueApiSportsSeasonRepository.findAllByLeagueApiSports(leagueApiSports) } returns listOf(season)

        // when & then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            fixtureApiSportsSyncer.saveFixturesOfLeagueWithCurrentSeason(leagueApiId, dtos)
        }
        assertTrue(exception.message!!.contains("inconsistent league API ID"))
    }

    @Test
    fun `DTO에 일관성 없는 시즌년도가 포함된 경우 예외 발생`() {
        // given
        val leagueApiId = 39L
        val dtos = listOf(
            FixtureApiSportsCreateDto(
                apiId = 1L,
                leagueApiId = leagueApiId,
                seasonYear = "2024"
            ),
            FixtureApiSportsCreateDto(
                apiId = 2L,
                leagueApiId = leagueApiId,
                seasonYear = "2023" // 다른 시즌년도
            )
        )
        
        val leagueCore = mockk<LeagueCore>()
        val season = LeagueApiSportsSeason(seasonYear = 2024)
        val leagueApiSports = LeagueApiSports(
            leagueCore = leagueCore,
            apiId = leagueApiId,
            name = "Premier League",
            type = "League"
        )
        
        every { leagueApiSportsRepository.findByApiId(leagueApiId) } returns leagueApiSports
        every { leagueApiSportsSeasonRepository.findAllByLeagueApiSports(leagueApiSports) } returns listOf(season)

        // when & then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            fixtureApiSportsSyncer.saveFixturesOfLeagueWithCurrentSeason(leagueApiId, dtos)
        }
        assertTrue(exception.message!!.contains("inconsistent season year"))
    }

    @Test
    fun `동기화되지 않은 팀이 포함된 경우 예외 발생`() {
        // given
        val leagueApiId = 39L
        val dtos = listOf(
            FixtureApiSportsCreateDto(
                apiId = 1L,
                leagueApiId = leagueApiId,
                seasonYear = "2024",
                homeTeam = TeamOfFixtureApiSportsCreateDto(apiId = 101L, name = "Arsenal"),
                awayTeam = TeamOfFixtureApiSportsCreateDto(apiId = 102L, name = "Chelsea")
            )
        )
        
        setupValidLeagueAndSeason(leagueApiId)
        
        // 팀 중 하나만 존재하는 상태 (Chelsea 없음)
        val mockTeamCore = mockk<TeamCore>()
        val arsenalTeam = TeamApiSports(apiId = 101L, name = "Arsenal", teamCore = mockTeamCore)
        every { teamApiSportsRepository.findAllByApiIdIn(listOf(101L, 102L)) } returns listOf(arsenalTeam)

        // when & then
        val exception = assertThrows(IllegalStateException::class.java) {
            fixtureApiSportsSyncer.saveFixturesOfLeagueWithCurrentSeason(leagueApiId, dtos)
        }
        assertTrue(exception.message!!.contains("Teams with API IDs [102] must be synced first"))
    }

    @Test
    fun `TeamCore가 없는 팀이 포함된 경우 예외 발생`() {
        // given
        val leagueApiId = 39L
        val dtos = listOf(
            FixtureApiSportsCreateDto(
                apiId = 1L,
                leagueApiId = leagueApiId,
                seasonYear = "2024",
                homeTeam = TeamOfFixtureApiSportsCreateDto(apiId = 101L, name = "Arsenal"),
                awayTeam = TeamOfFixtureApiSportsCreateDto(apiId = 102L, name = "Chelsea")
            )
        )
        
        setupValidLeagueAndSeason(leagueApiId)
        
        // TeamCore가 없는 팀이 존재하는 상태
        val mockTeamCore = mockk<TeamCore>()
        val arsenalTeam = TeamApiSports(apiId = 101L, name = "Arsenal", teamCore = mockTeamCore)
        val chelseaTeam = TeamApiSports(apiId = 102L, name = "Chelsea", teamCore = null) // TeamCore 없음
        every { teamApiSportsRepository.findAllByApiIdIn(listOf(101L, 102L)) } returns listOf(arsenalTeam, chelseaTeam)

        // when & then
        val exception = assertThrows(IllegalStateException::class.java) {
            fixtureApiSportsSyncer.saveFixturesOfLeagueWithCurrentSeason(leagueApiId, dtos)
        }
        assertTrue(exception.message!!.contains("TeamCore missing for teams with API IDs"))
    }

    @Test
    fun `정상적인 경우 경기 저장 성공`() {
        // given
        val leagueApiId = 39L
        val dtos = listOf(
            FixtureApiSportsCreateDto(
                apiId = 1L,
                leagueApiId = leagueApiId,
                seasonYear = "2024"
            )
        )
        
        setupValidLeagueAndSeason(leagueApiId)
        
        // 기존 fixture 없음
        every { fixtureApiSportsRepository.findAllByApiIdIn(listOf(1L)) } returns emptyList()
        
        // venue 처리
        every { venueApiSportsService.processVenuesWithNewTransaction(any()) } returns emptyMap()
        
        // UID 생성
        every { uidGenerator.generateUid() } returns "test-uid"
        
        // FixtureCore 저장
        val mockFixtureCore = mockk<com.footballay.core.infra.persistence.core.entity.FixtureCore>()
        every { fixtureCoreRepository.save(any()) } returns mockFixtureCore
        
        // FixtureApiSports 저장
        every { fixtureApiSportsRepository.saveAll(any<List<FixtureApiSports>>()) } returns emptyList()

        // when & then (예외 발생하지 않아야 함)
        assertDoesNotThrow {
            fixtureApiSportsSyncer.saveFixturesOfLeagueWithCurrentSeason(leagueApiId, dtos)
        }
        
        // 저장 메서드가 호출되었는지 확인
        verify { fixtureApiSportsRepository.saveAll(any<List<FixtureApiSports>>()) }
    }

    private fun setupValidLeagueAndSeason(leagueApiId: Long) {
        val leagueCore = mockk<LeagueCore>()
        val season = LeagueApiSportsSeason(seasonYear = 2024)
        val leagueApiSports = LeagueApiSports(
            leagueCore = leagueCore,
            apiId = leagueApiId,
            name = "Premier League",
            type = "League"
        )
        
        every { leagueApiSportsRepository.findByApiId(leagueApiId) } returns leagueApiSports
        every { leagueApiSportsSeasonRepository.findAllByLeagueApiSports(leagueApiSports) } returns listOf(season)
    }
} 