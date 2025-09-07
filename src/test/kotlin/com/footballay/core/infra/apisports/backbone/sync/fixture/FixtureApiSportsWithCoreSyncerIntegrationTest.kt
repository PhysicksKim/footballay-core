package com.footballay.core.infra.apisports.backbone.sync.fixture

import com.footballay.core.ApiSportsBackboneEntityGenerator
import com.footballay.core.BackboneEntities
import com.footballay.core.infra.apisports.shared.dto.FixtureApiSportsCreateDto
import com.footballay.core.infra.apisports.shared.dto.ScoreOfFixtureApiSportsCreateDto
import com.footballay.core.infra.apisports.shared.dto.StatusOfFixtureApiSportsCreateDto
import com.footballay.core.infra.apisports.shared.dto.TeamOfFixtureApiSportsCreateDto
import com.footballay.core.infra.apisports.shared.dto.VenueOfFixtureApiSportsCreateDto
import com.footballay.core.infra.apisports.VenueApiSportsService
import com.footballay.core.infra.apisports.mapper.FixtureDataMapper
import com.footballay.core.infra.core.FixtureCoreSyncService
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsSeasonRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.util.UidGenerator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(ApiSportsBackboneEntityGenerator::class)
@DisplayName("FixtureApiSportsWithCoreSyncer 통합 테스트")
class FixtureApiSportsWithCoreSyncerIntegrationTest {

    @Autowired
    private lateinit var fixtureApiSportsRepository: FixtureApiSportsRepository

    @Autowired
    private lateinit var leagueApiSportsRepository: LeagueApiSportsRepository

    @Autowired
    private lateinit var leagueApiSportsSeasonRepository: LeagueApiSportsSeasonRepository

    @Autowired
    private lateinit var teamApiSportsRepository: TeamApiSportsRepository

    @Autowired
    private lateinit var fixtureCoreSyncService: FixtureCoreSyncService

    @Autowired
    private lateinit var venueApiSportsService: VenueApiSportsService

    @Autowired
    private lateinit var fixtureDataMapper: FixtureDataMapper

    @Autowired
    private lateinit var uidGenerator: UidGenerator

    @Autowired
    private lateinit var syncer: FixtureApiSportsWithCoreSyncer

    @Autowired
    private lateinit var backboneEntityGenerator: ApiSportsBackboneEntityGenerator

    private lateinit var backboneEntities: BackboneEntities

    @BeforeEach
    fun setUp() {
        // ApiSports Backbone 엔티티들을 생성
        backboneEntities = backboneEntityGenerator.createCompleteBackboneEntities()
    }

    @Test
    @DisplayName("유효한 입력으로 호출 시 정상 처리")
    fun `유효한 입력으로 호출 시 정상 처리`() {
        // given
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val validDto = createValidFixtureDto()

        // when & then - 예외가 발생하지 않아야 함
        syncer.saveFixturesOfLeague(leagueApiId, listOf(validDto))
    }

    @Test
    @DisplayName("음수 leagueApiId로 호출 시 IllegalArgumentException 발생")
    fun `음수 leagueApiId로 호출 시 IllegalArgumentException 발생`() {
        // given
        val leagueApiId = -1L
        val validDto = createValidFixtureDto()

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            syncer.saveFixturesOfLeague(leagueApiId, listOf(validDto))
        }
        assertEquals("LeagueApiId must be positive, but was: -1", exception.message)
    }

    @Test
    @DisplayName("0 leagueApiId로 호출 시 IllegalArgumentException 발생")
    fun `0 leagueApiId로 호출 시 IllegalArgumentException 발생`() {
        // given
        val leagueApiId = 0L
        val validDto = createValidFixtureDto()

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            syncer.saveFixturesOfLeague(leagueApiId, listOf(validDto))
        }
        assertEquals("LeagueApiId must be positive, but was: 0", exception.message)
    }

    @Test
    @DisplayName("빈 DTO 리스트로 호출 시 IllegalArgumentException 발생")
    fun `빈 DTO 리스트로 호출 시 IllegalArgumentException 발생`() {
        // given
        val leagueApiId = 39L
        val emptyDtos = emptyList<FixtureApiSportsCreateDto>()

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            syncer.saveFixturesOfLeague(leagueApiId, emptyDtos)
        }
        assertEquals("Fixture DTO list cannot be empty", exception.message)
    }

    @Test
    @DisplayName("null apiId가 포함된 DTO로 호출 시 IllegalArgumentException 발생")
    fun `null apiId가 포함된 DTO로 호출 시 IllegalArgumentException 발생`() {
        // given
        val leagueApiId = 39L
        val dtoWithNullApiId = createValidFixtureDto().copy(apiId = null)

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithNullApiId))
        }
        assertEquals("Fixture apiId must be positive, but was: null at index 0", exception.message)
    }

    @Test
    @DisplayName("음수 apiId가 포함된 DTO로 호출 시 IllegalArgumentException 발생")
    fun `음수 apiId가 포함된 DTO로 호출 시 IllegalArgumentException 발생`() {
        // given
        val leagueApiId = 39L
        val dtoWithNegativeApiId = createValidFixtureDto().copy(apiId = -1L)

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithNegativeApiId))
        }
        assertEquals("Fixture apiId must be positive, but was: -1 at index 0", exception.message)
    }

    @Test
    @DisplayName("0 apiId가 포함된 DTO로 호출 시 IllegalArgumentException 발생")
    fun `0 apiId가 포함된 DTO로 호출 시 IllegalArgumentException 발생`() {
        // given
        val leagueApiId = 39L
        val dtoWithZeroApiId = createValidFixtureDto().copy(apiId = 0L)

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithZeroApiId))
        }
        assertEquals("Fixture apiId must be positive, but was: 0 at index 0", exception.message)
    }

    @Test
    @DisplayName("음수 homeTeam apiId가 포함된 DTO로 호출 시 IllegalArgumentException 발생")
    fun `음수 homeTeam apiId가 포함된 DTO로 호출 시 IllegalArgumentException 발생`() {
        // given
        val leagueApiId = 39L
        val dtoWithNegativeHomeTeamApiId = createValidFixtureDto().copy(
            homeTeam = TeamOfFixtureApiSportsCreateDto(apiId = -1L, name = "Home Team")
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithNegativeHomeTeamApiId))
        }
        assertEquals("HomeTeam apiId must be positive, but was: -1 at index 0", exception.message)
    }

    @Test
    @DisplayName("음수 awayTeam apiId가 포함된 DTO로 호출 시 IllegalArgumentException 발생")
    fun `음수 awayTeam apiId가 포함된 DTO로 호출 시 IllegalArgumentException 발생`() {
        // given
        val leagueApiId = 39L
        val dtoWithNegativeAwayTeamApiId = createValidFixtureDto().copy(
            awayTeam = TeamOfFixtureApiSportsCreateDto(apiId = -1L, name = "Away Team")
        )

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithNegativeAwayTeamApiId))
        }
        assertEquals("AwayTeam apiId must be positive, but was: -1 at index 0", exception.message)
    }

    @Test
    @DisplayName("null homeTeam으로 호출 시 정상 처리")
    fun `null homeTeam으로 호출 시 정상 처리`() {
        // given
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dtoWithNullHomeTeam = createValidFixtureDto().copy(homeTeam = null)

        // when & then - 예외가 발생하지 않아야 함
        syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithNullHomeTeam))
    }

    @Test
    @DisplayName("null awayTeam으로 호출 시 정상 처리")
    fun `null awayTeam으로 호출 시 정상 처리`() {
        // given
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dtoWithNullAwayTeam = createValidFixtureDto().copy(awayTeam = null)

        // when & then - 예외가 발생하지 않아야 함
        syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithNullAwayTeam))
    }

    @Test
    @DisplayName("여러 DTO 중 하나에 문제가 있을 때 올바른 인덱스 표시")
    fun `여러 DTO 중 하나에 문제가 있을 때 올바른 인덱스 표시`() {
        // given
        val leagueApiId = 39L
        val validDto1 = createValidFixtureDto().copy(apiId = 1001L)
        val invalidDto = createValidFixtureDto().copy(apiId = -1L)
        val validDto2 = createValidFixtureDto().copy(apiId = 1003L)

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            syncer.saveFixturesOfLeague(leagueApiId, listOf(validDto1, invalidDto, validDto2))
        }
        assertEquals("Fixture apiId must be positive, but was: -1 at index 1", exception.message)
    }

    @Test
    @DisplayName("서로 다른 시즌이 포함된 DTO로 호출 시 IllegalArgumentException 발생")
    fun `서로 다른 시즌이 포함된 DTO로 호출 시 IllegalArgumentException 발생`() {
        // given
        val leagueApiId = 39L
        val dto1 = createValidFixtureDto().copy(seasonYear = "2024")
        val dto2 = createValidFixtureDto().copy(apiId = 1002L, seasonYear = "2023")

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            syncer.saveFixturesOfLeague(leagueApiId, listOf(dto1, dto2))
        }
        assertEquals("All fixtures must have the same season, but found: [2024, 2023]", exception.message)
    }

    @Test
    @DisplayName("시즌 정보가 없는 DTO로 호출 시 IllegalArgumentException 발생")
    fun `시즌 정보가 없는 DTO로 호출 시 IllegalArgumentException 발생`() {
        // given
        val leagueApiId = 39L
        val dtoWithoutSeason = createValidFixtureDto().copy(seasonYear = null)

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithoutSeason))
        }
        assertEquals("At least one fixture must have season information", exception.message)
    }

    @Test
    @DisplayName("일부 DTO에만 시즌 정보가 있는 경우 정상 처리")
    fun `일부 DTO에만 시즌 정보가 있는 경우 정상 처리`() {
        // given
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dto1 = createValidFixtureDto().copy(apiId = 1001L, seasonYear = "2024")
        val dto2 = createValidFixtureDto().copy(apiId = 1002L, seasonYear = null)

        // when & then - 예외가 발생하지 않아야 함
        syncer.saveFixturesOfLeague(leagueApiId, listOf(dto1, dto2))
    }

    @Test
    @DisplayName("존재하지 않는 League로 호출 시 IllegalStateException 발생")
    fun `존재하지 않는 League로 호출 시 IllegalStateException 발생`() {
        // given
        val nonExistentLeagueApiId = 99999L
        val validDto = createValidFixtureDto()

        // when & then
        val exception = assertThrows<IllegalStateException> {
            syncer.saveFixturesOfLeague(nonExistentLeagueApiId, listOf(validDto))
        }
        assertTrue(exception.message!!.contains("League not found"))
    }

    @Test
    @DisplayName("존재하지 않는 Season으로 호출 시 IllegalStateException 발생")
    fun `존재하지 않는 Season으로 호출 시 IllegalStateException 발생`() {
        // given
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dtoWithNonExistentSeason = createValidFixtureDto().copy(seasonYear = "9999")

        // when & then
        val exception = assertThrows<IllegalStateException> {
            syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithNonExistentSeason))
        }
        assertTrue(exception.message!!.contains("League not found"))
    }

    @Test
    @DisplayName("새로운 Fixture 생성 - 모든 Phase 정상 처리")
    fun `새로운 Fixture 생성 - 모든 Phase 정상 처리`() {
        // given
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val newFixtureDto = createValidFixtureDto().copy(apiId = 9999L)

        // when
        syncer.saveFixturesOfLeague(leagueApiId, listOf(newFixtureDto))

        // then
        val savedFixture = fixtureApiSportsRepository.findByApiId(9999L)
        assertNotNull(savedFixture)
        assertEquals(9999L, savedFixture.apiId)
        assertNotNull(savedFixture.core) // FixtureCore가 생성되었는지 확인
    }

    @Test
    @DisplayName("여러 Fixture 동시 처리")
    fun `여러 Fixture 동시 처리`() {
        // given
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val fixtures = listOf(
            createValidFixtureDto().copy(apiId = 1001L),
            createValidFixtureDto().copy(apiId = 1002L),
            createValidFixtureDto().copy(apiId = 1003L)
        )

        // when
        syncer.saveFixturesOfLeague(leagueApiId, fixtures)

        // then
        val savedFixtures = fixtureApiSportsRepository.findAllByApiIdIn(listOf(1001L, 1002L, 1003L))
        assertEquals(3, savedFixtures.size)
        
        savedFixtures.forEach { fixture ->
            assertNotNull(fixture.core) // 모든 Fixture에 Core가 생성되었는지 확인
        }
    }

    @Test
    @DisplayName("Venue 없는 Fixture 처리")
    fun `Venue 없는 Fixture 처리`() {
        // given
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val fixtureWithoutVenue = createValidFixtureDto().copy(
            apiId = 6666L,
            venue = null
        )

        // when
        syncer.saveFixturesOfLeague(leagueApiId, listOf(fixtureWithoutVenue))

        // then
        val savedFixture = fixtureApiSportsRepository.findByApiId(6666L)
        assertNotNull(savedFixture)
        assertEquals(null, savedFixture.venue) // Venue가 null인지 확인
        assertNotNull(savedFixture.core) // Core는 정상 생성되었는지 확인
    }

    @Test
    @DisplayName("Team 없는 Fixture 처리")
    fun `Team 없는 Fixture 처리`() {
        // given
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val fixtureWithoutTeams = createValidFixtureDto().copy(
            apiId = 5555L,
            homeTeam = null,
            awayTeam = null
        )

        // when
        syncer.saveFixturesOfLeague(leagueApiId, listOf(fixtureWithoutTeams))

        // then
        val savedFixture = fixtureApiSportsRepository.findByApiId(5555L)
        assertNotNull(savedFixture)
        assertEquals(null, savedFixture.homeTeam)
        assertEquals(null, savedFixture.awayTeam)
        assertNotNull(savedFixture.core) // Core는 정상 생성되었는지 확인
    }

    @Test
    @DisplayName("Identity Pairing Pattern 검증 - UID 생성 확인")
    fun `Identity Pairing Pattern 검증 - UID 생성 확인`() {
        // given
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val fixtureDto = createValidFixtureDto().copy(apiId = 4444L)

        // when
        syncer.saveFixturesOfLeague(leagueApiId, listOf(fixtureDto))

        // then
        val savedFixture = fixtureApiSportsRepository.findByApiId(4444L)
        assertNotNull(savedFixture)
        assertNotNull(savedFixture.core)
        assertNotNull(savedFixture.core!!.uid) // UID가 생성되었는지 확인
        assertTrue(savedFixture.core!!.uid.isNotBlank()) // UID가 비어있지 않은지 확인
    }

    @Test
    @DisplayName("성능 최적화 검증 - Core FK 바로 설정")
    fun `성능 최적화 검증 - Core FK 바로 설정`() {
        // given
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val fixtureDto = createValidFixtureDto().copy(apiId = 3333L)

        // when
        syncer.saveFixturesOfLeague(leagueApiId, listOf(fixtureDto))

        // then
        val savedFixture = fixtureApiSportsRepository.findByApiId(3333L)
        assertNotNull(savedFixture)
        assertNotNull(savedFixture.core) // Core FK가 바로 설정되었는지 확인
        
        // Core 엔티티가 정상적으로 저장되었는지 확인
        val core = savedFixture.core!!
        assertNotNull(core.uid)
        assertNotNull(core.league) // League가 설정되었는지 확인
    }

    @Test
    @DisplayName("누락된 Team으로 호출 시 IllegalStateException 발생")
    fun `누락된 Team으로 호출 시 IllegalStateException 발생`() {
        // given
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dtoWithNonExistentTeam = createValidFixtureDto().copy(
            apiId = 2222L,
            homeTeam = TeamOfFixtureApiSportsCreateDto(apiId = 99999L, name = "Non Existent Team")
        )

        // when & then
        val exception = assertThrows<IllegalStateException> {
            syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithNonExistentTeam))
        }
        assertTrue(exception.message!!.contains("Some teams are missing in the database"))
    }

    @Test
    @DisplayName("잘못된 날짜 형식으로 호출 시 정상 처리 (null로 처리)")
    fun `잘못된 날짜 형식으로 호출 시 정상 처리`() {
        // given
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dtoWithInvalidDate = createValidFixtureDto().copy(
            apiId = 1111L,
            date = "invalid-date-format"
        )

        // when
        syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithInvalidDate))

        // then
        val savedFixture = fixtureApiSportsRepository.findByApiId(1111L)
        assertNotNull(savedFixture)
        assertEquals(null, savedFixture.date) // 잘못된 날짜는 null로 처리
    }

    private fun createValidFixtureDto(): FixtureApiSportsCreateDto {
        return FixtureApiSportsCreateDto(
            apiId = 1000L,
            leagueApiId = 39L,
            seasonYear = "2024",
            referee = "John Doe",
            timezone = "UTC",
            date = "2024-01-01T15:00:00+00:00",
            timestamp = 1704117600L,
            round = "Regular Season - 1",
            status = StatusOfFixtureApiSportsCreateDto(
                longStatus = "Not Started",
                shortStatus = "NS",
                elapsed = null,
                extra = null
            ),
            score = ScoreOfFixtureApiSportsCreateDto(
                halftimeHome = null,
                halftimeAway = null,
                fulltimeHome = null,
                fulltimeAway = null,
                extratimeHome = null,
                extratimeAway = null,
                penaltyHome = null,
                penaltyAway = null
            ),
            homeTeam = TeamOfFixtureApiSportsCreateDto(apiId = 33L, name = "Manchester United"),
            awayTeam = TeamOfFixtureApiSportsCreateDto(apiId = 42L, name = "Arsenal"),
            venue = VenueOfFixtureApiSportsCreateDto(
                apiId = 1L,
                name = "Old Trafford",
                city = "Manchester"
            )
        )
    }
} 