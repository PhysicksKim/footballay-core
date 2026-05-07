package com.footballay.core.backbone.apisports.fixture

import com.footballay.core.ApiSportsBackboneEntityGenerator
import com.footballay.core.BackboneEntities
import com.footballay.core.backbone.apisports.dto.FixtureApiSportsSyncDto
import com.footballay.core.backbone.apisports.dto.ScoreOfFixtureApiSportsCreateDto
import com.footballay.core.backbone.apisports.dto.StatusOfFixtureApiSportsCreateDto
import com.footballay.core.backbone.apisports.dto.TeamOfFixtureApiSportsCreateDto
import com.footballay.core.backbone.apisports.dto.VenueOfFixtureApiSportsCreateDto
import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(ApiSportsBackboneEntityGenerator::class)
@DisplayName("FixtureApiSportsWithCoreSyncer 검증 통합 테스트")
class FixtureApiSportsWithCoreSyncerValidationIntegrationTest {
    @Autowired
    private lateinit var fixtureApiSportsRepository: FixtureApiSportsRepository

    @Autowired
    private lateinit var fixtureCoreRepository: FixtureCoreRepository

    @Autowired
    private lateinit var teamApiSportsRepository: TeamApiSportsRepository

    @Autowired
    private lateinit var syncer: FixtureApiSportsWithCoreSyncer

    @Autowired
    private lateinit var backboneEntityGenerator: ApiSportsBackboneEntityGenerator

    private lateinit var backboneEntities: BackboneEntities

    @BeforeEach
    fun setUp() {
        backboneEntities = backboneEntityGenerator.createCompleteBackboneEntities()
    }

    @Test
    @DisplayName("유효한 입력으로 호출 시 정상 처리")
    fun `유효한 입력으로 호출 시 정상 처리`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val validDto = createValidFixtureDto()

        syncer.saveFixturesOfLeague(leagueApiId, listOf(validDto))
    }

    @Test
    @DisplayName("음수 leagueApiId로 호출 시 IllegalArgumentException 발생")
    fun `음수 leagueApiId로 호출 시 IllegalArgumentException 발생`() {
        val leagueApiId = -1L
        val validDto = createValidFixtureDto()

        val exception =
            assertThrows<IllegalArgumentException> {
                syncer.saveFixturesOfLeague(leagueApiId, listOf(validDto))
            }
        assertThat(exception.message).isEqualTo("LeagueApiId must be positive, but was: -1")
    }

    @Test
    @DisplayName("0 leagueApiId로 호출 시 IllegalArgumentException 발생")
    fun `0 leagueApiId로 호출 시 IllegalArgumentException 발생`() {
        val leagueApiId = 0L
        val validDto = createValidFixtureDto()

        val exception =
            assertThrows<IllegalArgumentException> {
                syncer.saveFixturesOfLeague(leagueApiId, listOf(validDto))
            }
        assertThat(exception.message).isEqualTo("LeagueApiId must be positive, but was: 0")
    }

    @Test
    @DisplayName("빈 DTO 리스트로 호출 시 빈 맵 반환")
    fun `빈 DTO 리스트로 호출 시 빈 맵 반환`() {
        val leagueApiId = 39L
        val emptyDtos = emptyList<FixtureApiSportsSyncDto>()

        val result = syncer.saveFixturesOfLeague(leagueApiId, emptyDtos)

        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("null apiId가 포함된 DTO는 필터링되어 제외됨")
    fun `null apiId가 포함된 DTO는 필터링되어 제외됨`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dtoWithNullApiId = createValidFixtureDto().copy(apiId = null)
        val validDto = createValidFixtureDto().copy(apiId = 1001L)

        val result = syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithNullApiId, validDto))

        assertThat(result).hasSize(1)
        assertThat(result[1001L]).isNotNull
    }

    @Test
    @DisplayName("음수 apiId가 포함된 DTO는 필터링되어 제외됨")
    fun `음수 apiId가 포함된 DTO는 필터링되어 제외됨`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dtoWithNegativeApiId = createValidFixtureDto().copy(apiId = -1L)
        val validDto = createValidFixtureDto().copy(apiId = 1001L)

        val result = syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithNegativeApiId, validDto))

        assertThat(result).hasSize(1)
        assertThat(result[1001L]).isNotNull
    }

    @Test
    @DisplayName("0 apiId가 포함된 DTO는 필터링되어 제외됨")
    fun `0 apiId가 포함된 DTO는 필터링되어 제외됨`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dtoWithZeroApiId = createValidFixtureDto().copy(apiId = 0L)
        val validDto = createValidFixtureDto().copy(apiId = 1001L)

        val result = syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithZeroApiId, validDto))

        assertThat(result).hasSize(1)
        assertThat(result[1001L]).isNotNull
    }

    @Test
    @DisplayName("음수 homeTeam apiId가 포함된 DTO는 필터링되어 제외됨")
    fun `음수 homeTeam apiId가 포함된 DTO는 필터링되어 제외됨`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dtoWithNegativeHomeTeamApiId =
            createValidFixtureDto().copy(
                apiId = 1001L,
                homeTeam = TeamOfFixtureApiSportsCreateDto(apiId = -1L, name = "Home Team"),
            )
        val validDto = createValidFixtureDto().copy(apiId = 1002L)

        val result = syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithNegativeHomeTeamApiId, validDto))

        assertThat(result).hasSize(1)
        assertThat(result[1002L]).isNotNull
    }

    @Test
    @DisplayName("음수 awayTeam apiId가 포함된 DTO는 필터링되어 제외됨")
    fun `음수 awayTeam apiId가 포함된 DTO는 필터링되어 제외됨`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dtoWithNegativeAwayTeamApiId =
            createValidFixtureDto().copy(
                apiId = 1001L,
                awayTeam = TeamOfFixtureApiSportsCreateDto(apiId = -1L, name = "Away Team"),
            )
        val validDto = createValidFixtureDto().copy(apiId = 1002L)

        val result = syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithNegativeAwayTeamApiId, validDto))

        assertThat(result).hasSize(1)
        assertThat(result[1002L]).isNotNull
    }

    @Test
    @DisplayName("null homeTeam으로 호출 시 정상 처리")
    fun `null homeTeam으로 호출 시 정상 처리`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dtoWithNullHomeTeam = createValidFixtureDto().copy(homeTeam = null)

        syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithNullHomeTeam))
    }

    @Test
    @DisplayName("null awayTeam으로 호출 시 정상 처리")
    fun `null awayTeam으로 호출 시 정상 처리`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dtoWithNullAwayTeam = createValidFixtureDto().copy(awayTeam = null)

        syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithNullAwayTeam))
    }

    @Test
    @DisplayName("여러 DTO 중 일부가 invalid하면 유효한 것만 저장됨")
    fun `여러 DTO 중 일부가 invalid하면 유효한 것만 저장됨`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val validDto1 = createValidFixtureDto().copy(apiId = 1001L)
        val invalidDto = createValidFixtureDto().copy(apiId = -1L)
        val validDto2 = createValidFixtureDto().copy(apiId = 1003L)

        val result = syncer.saveFixturesOfLeague(leagueApiId, listOf(validDto1, invalidDto, validDto2))

        assertThat(result).hasSize(2)
        assertThat(result[1001L]).isNotNull
        assertThat(result[1003L]).isNotNull
        assertThat(result[-1L]).isNull()
    }

    @Test
    @DisplayName("서로 다른 시즌이 포함된 DTO로 호출 시 IllegalArgumentException 발생")
    fun `서로 다른 시즌이 포함된 DTO로 호출 시 IllegalArgumentException 발생`() {
        val leagueApiId = 39L
        val dto1 = createValidFixtureDto().copy(seasonYear = "2024")
        val dto2 = createValidFixtureDto().copy(apiId = 1002L, seasonYear = "2023")

        val exception =
            assertThrows<IllegalArgumentException> {
                syncer.saveFixturesOfLeague(leagueApiId, listOf(dto1, dto2))
            }
        assertThat(exception.message).isEqualTo("All fixtures must have the same season, but found: [2024, 2023]")
    }

    @Test
    @DisplayName("시즌 정보가 없는 DTO로 호출 시 IllegalArgumentException 발생")
    fun `시즌 정보가 없는 DTO로 호출 시 IllegalArgumentException 발생`() {
        val leagueApiId = 39L
        val dtoWithoutSeason = createValidFixtureDto().copy(seasonYear = null)

        val exception =
            assertThrows<IllegalArgumentException> {
                syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithoutSeason))
            }
        assertThat(exception.message).isEqualTo("At least one fixture must have season information")
    }

    @Test
    @DisplayName("일부 DTO에만 시즌 정보가 있는 경우 정상 처리")
    fun `일부 DTO에만 시즌 정보가 있는 경우 정상 처리`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dto1 = createValidFixtureDto().copy(apiId = 1001L, seasonYear = "2024")
        val dto2 = createValidFixtureDto().copy(apiId = 1002L, seasonYear = null)

        syncer.saveFixturesOfLeague(leagueApiId, listOf(dto1, dto2))
    }

    @Test
    @DisplayName("존재하지 않는 League로 호출 시 IllegalStateException 발생")
    fun `존재하지 않는 League로 호출 시 IllegalStateException 발생`() {
        val nonExistentLeagueApiId = 99999L
        val validDto = createValidFixtureDto()

        val exception =
            assertThrows<IllegalStateException> {
                syncer.saveFixturesOfLeague(nonExistentLeagueApiId, listOf(validDto))
            }
        assertThat(exception.message).contains("League not found")
    }

    @Test
    @DisplayName("존재하지 않는 Season으로 호출 시 IllegalStateException 발생")
    fun `존재하지 않는 Season으로 호출 시 IllegalStateException 발생`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dtoWithNonExistentSeason = createValidFixtureDto().copy(seasonYear = "9999")

        val exception =
            assertThrows<IllegalStateException> {
                syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithNonExistentSeason))
            }
        assertThat(exception.message).contains("League not found")
    }

    @Test
    @DisplayName("새로운 Fixture 생성 - 모든 Phase 정상 처리")
    fun `새로운 Fixture 생성 - 모든 Phase 정상 처리`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val newFixtureDto = createValidFixtureDto().copy(apiId = 9999L)

        syncer.saveFixturesOfLeague(leagueApiId, listOf(newFixtureDto))

        val savedFixture = fixtureApiSportsRepository.findByApiId(9999L)
        assertThat(savedFixture).isNotNull
        assertThat(savedFixture!!.apiId).isEqualTo(9999L)
        assertThat(savedFixture.core).isNotNull()
    }

    @Test
    @DisplayName("기존 Fixture 재동기화 시 FixtureApiSports date와 FixtureCore kickoff가 함께 갱신됨")
    fun `기존 Fixture 재동기화 시 FixtureApiSports date와 FixtureCore kickoff가 함께 갱신됨`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val fixtureApiId = 8888L
        val originalDto = createValidFixtureDto().copy(apiId = fixtureApiId, date = "2024-01-01T15:00:00+00:00")
        syncer.saveFixturesOfLeague(leagueApiId, listOf(originalDto))

        val originalFixture = fixtureApiSportsRepository.findByApiId(fixtureApiId)
        val originalCoreId = originalFixture!!.core!!.id!!
        assertThat(originalFixture.date).isEqualTo(Instant.parse("2024-01-01T15:00:00Z"))
        assertThat(originalFixture.core!!.kickoff).isEqualTo(Instant.parse("2024-01-01T15:00:00Z"))

        val updatedDto =
            originalDto.copy(
                date = "2024-02-02T18:30:00+00:00",
                status =
                    StatusOfFixtureApiSportsCreateDto(
                        longStatus = "Time Changed",
                        shortStatus = "NS",
                        elapsed = null,
                        extra = null,
                    ),
            )
        syncer.saveFixturesOfLeague(leagueApiId, listOf(updatedDto))

        val updatedFixture = fixtureApiSportsRepository.findByApiId(fixtureApiId)
        val updatedCore = fixtureCoreRepository.findById(originalCoreId).orElseThrow()
        val expectedKickoff = Instant.parse("2024-02-02T18:30:00Z")

        assertThat(updatedFixture).isNotNull
        assertThat(updatedFixture!!.date).isEqualTo(expectedKickoff)
        assertThat(updatedFixture.core!!.id).isEqualTo(originalCoreId)
        assertThat(updatedCore.kickoff).isEqualTo(expectedKickoff)
    }

    @Test
    @DisplayName("팀 미정 Fixture 재동기화 시 기존 TeamCore가 FixtureCore에 연결됨")
    fun `팀 미정 Fixture 재동기화 시 기존 TeamCore가 FixtureCore에 연결됨`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val fixtureApiId = 7777L
        val unresolvedFixture =
            createValidFixtureDto().copy(
                apiId = fixtureApiId,
                homeTeam = null,
                awayTeam = null,
            )
        syncer.saveFixturesOfLeague(leagueApiId, listOf(unresolvedFixture))

        val originalFixture = fixtureApiSportsRepository.findByApiId(fixtureApiId)
        val originalCoreId = originalFixture!!.core!!.id!!
        assertThat(originalFixture.core!!.homeTeam).isNull()
        assertThat(originalFixture.core!!.awayTeam).isNull()

        val resolvedFixture =
            unresolvedFixture.copy(
                homeTeam = TeamOfFixtureApiSportsCreateDto(apiId = 33L, name = "Manchester United"),
                awayTeam = TeamOfFixtureApiSportsCreateDto(apiId = 42L, name = "Arsenal"),
            )
        syncer.saveFixturesOfLeague(leagueApiId, listOf(resolvedFixture))

        val updatedCore = fixtureCoreRepository.findById(originalCoreId).orElseThrow()
        val existingHomeTeam = teamApiSportsRepository.findByApiId(33L)
        val existingAwayTeam = teamApiSportsRepository.findByApiId(42L)
        assertThat(updatedCore.homeTeam).isNotNull
        assertThat(updatedCore.awayTeam).isNotNull
        assertThat(updatedCore.homeTeam!!.id).isEqualTo(existingHomeTeam!!.teamCore!!.id)
        assertThat(updatedCore.awayTeam!!.id).isEqualTo(existingAwayTeam!!.teamCore!!.id)
    }

    @Test
    @DisplayName("여러 Fixture 동시 처리")
    fun `여러 Fixture 동시 처리`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val fixtures =
            listOf(
                createValidFixtureDto().copy(apiId = 1001L),
                createValidFixtureDto().copy(apiId = 1002L),
                createValidFixtureDto().copy(apiId = 1003L),
            )

        syncer.saveFixturesOfLeague(leagueApiId, fixtures)

        val savedFixtures = fixtureApiSportsRepository.findAllByApiIdIn(listOf(1001L, 1002L, 1003L))
        assertThat(savedFixtures).hasSize(3)

        savedFixtures.forEach { fixture ->
            assertThat(fixture.core).isNotNull()
        }
    }

    @Test
    @DisplayName("Venue 없는 Fixture 처리")
    fun `Venue 없는 Fixture 처리`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val fixtureWithoutVenue =
            createValidFixtureDto().copy(
                apiId = 6666L,
                venue = null,
            )

        syncer.saveFixturesOfLeague(leagueApiId, listOf(fixtureWithoutVenue))

        val savedFixture = fixtureApiSportsRepository.findByApiId(6666L)
        assertThat(savedFixture).isNotNull
        assertThat(savedFixture!!.venue).isNull()
        assertThat(savedFixture.core).isNotNull()
    }

    @Test
    @DisplayName("Venue apiId가 0인 Fixture는 Venue 없이 저장")
    fun `Venue apiId가 0인 Fixture는 Venue 없이 저장`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val fixtureWithUnmanagedVenue =
            createValidFixtureDto().copy(
                apiId = 6667L,
                venue =
                    VenueOfFixtureApiSportsCreateDto(
                        apiId = 0L,
                        name = "Pohang Steel Yard",
                        city = "Pohang",
                    ),
            )

        syncer.saveFixturesOfLeague(leagueApiId, listOf(fixtureWithUnmanagedVenue))

        val savedFixture = fixtureApiSportsRepository.findByApiId(6667L)
        assertThat(savedFixture).isNotNull
        assertThat(savedFixture!!.venue).isNull()
        assertThat(savedFixture.core).isNotNull()
    }

    @Test
    @DisplayName("Team 없는 Fixture 처리")
    fun `Team 없는 Fixture 처리`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val fixtureWithoutTeams =
            createValidFixtureDto().copy(
                apiId = 5555L,
                homeTeam = null,
                awayTeam = null,
            )

        syncer.saveFixturesOfLeague(leagueApiId, listOf(fixtureWithoutTeams))

        val savedFixture = fixtureApiSportsRepository.findByApiId(5555L)
        assertThat(savedFixture).isNotNull
        assertThat(savedFixture!!.homeTeam).isNull()
        assertThat(savedFixture.awayTeam).isNull()
        assertThat(savedFixture.core).isNotNull()
    }

    @Test
    @DisplayName("Identity Pairing Pattern 검증 - UID 생성 확인")
    fun `Identity Pairing Pattern 검증 - UID 생성 확인`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val fixtureDto = createValidFixtureDto().copy(apiId = 4444L)

        syncer.saveFixturesOfLeague(leagueApiId, listOf(fixtureDto))

        val savedFixture = fixtureApiSportsRepository.findByApiId(4444L)
        assertThat(savedFixture).isNotNull
        assertThat(savedFixture!!.core).isNotNull()
        assertThat(savedFixture.core!!.uid).isNotBlank()
    }

    @Test
    @DisplayName("성능 최적화 검증 - Core FK 바로 설정")
    fun `성능 최적화 검증 - Core FK 바로 설정`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val fixtureDto = createValidFixtureDto().copy(apiId = 3333L)

        syncer.saveFixturesOfLeague(leagueApiId, listOf(fixtureDto))

        val savedFixture = fixtureApiSportsRepository.findByApiId(3333L)
        assertThat(savedFixture).isNotNull
        assertThat(savedFixture!!.core).isNotNull()

        val core = savedFixture.core!!
        assertThat(core.uid).isNotNull()
        assertThat(core.league).isNotNull()
    }

    @Test
    @DisplayName("누락된 Team은 암시적으로 생성되어 FixtureCore에 연결됨")
    fun `누락된 Team으로 호출 시 IllegalStateException 발생`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dtoWithNonExistentTeam =
            createValidFixtureDto().copy(
                apiId = 2222L,
                homeTeam = TeamOfFixtureApiSportsCreateDto(apiId = 99999L, name = "Non Existent Team"),
                awayTeam = TeamOfFixtureApiSportsCreateDto(apiId = 99998L, name = "Another Missing Team"),
            )

        val exception =
            assertThrows<IllegalStateException> {
                syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithNonExistentTeam))
            }
        assertThat(exception.message).contains("Some teams are missing in the database")
        assertThat(exception.message).contains("syncTeamsOfLeague")
    }

    @Test
    @DisplayName("TeamApiSports는 있으나 TeamCore가 없는 경우 error 로그 후 계속 진행")
    fun `TeamApiSports는 있으나 TeamCore가 없는 경우 error 로그 후 계속 진행`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        teamApiSportsRepository.save(
            TeamApiSports(
                teamCore = null,
                apiId = 99997L,
                name = "Broken Team",
                logo = "broken-logo",
            ),
        )
        val fixtureDto =
            createValidFixtureDto().copy(
                apiId = 2223L,
                homeTeam = TeamOfFixtureApiSportsCreateDto(apiId = 99997L, name = "Broken Team"),
                awayTeam = TeamOfFixtureApiSportsCreateDto(apiId = 42L, name = "Arsenal"),
            )

        syncer.saveFixturesOfLeague(leagueApiId, listOf(fixtureDto))

        val savedFixture = fixtureApiSportsRepository.findByApiId(2223L)
        assertThat(savedFixture).isNotNull
        assertThat(savedFixture!!.core).isNotNull
        assertThat(savedFixture.core!!.homeTeam).isNull()
        assertThat(savedFixture.core!!.awayTeam).isNotNull
        assertThat(savedFixture.core!!.awayTeam!!.id).isEqualTo(teamApiSportsRepository.findByApiId(42L)!!.teamCore!!.id)
    }

    @Test
    @DisplayName("잘못된 날짜 형식으로 호출 시 정상 처리 (null로 처리)")
    fun `잘못된 날짜 형식으로 호출 시 정상 처리`() {
        val leagueApiId = backboneEntities.leagueApiSports.apiId
        val dtoWithInvalidDate =
            createValidFixtureDto().copy(
                apiId = 1111L,
                date = "invalid-date-format",
            )

        syncer.saveFixturesOfLeague(leagueApiId, listOf(dtoWithInvalidDate))

        val savedFixture = fixtureApiSportsRepository.findByApiId(1111L)
        assertThat(savedFixture).isNotNull
        assertThat(savedFixture!!.date).isNull()
    }

    private fun createValidFixtureDto(): FixtureApiSportsSyncDto =
        FixtureApiSportsSyncDto(
            apiId = 1000L,
            leagueApiId = 39L,
            seasonYear = "2024",
            referee = "John Doe",
            date = "2024-01-01T15:00:00+00:00",
            timestamp = 1704117600L,
            round = "Regular Season - 1",
            status =
                StatusOfFixtureApiSportsCreateDto(
                    longStatus = "Not Started",
                    shortStatus = "NS",
                    elapsed = null,
                    extra = null,
                ),
            score =
                ScoreOfFixtureApiSportsCreateDto(
                    halftimeHome = null,
                    halftimeAway = null,
                    fulltimeHome = null,
                    fulltimeAway = null,
                    extratimeHome = null,
                    extratimeAway = null,
                    penaltyHome = null,
                    penaltyAway = null,
                ),
            homeTeam = TeamOfFixtureApiSportsCreateDto(apiId = 33L, name = "Manchester United"),
            awayTeam = TeamOfFixtureApiSportsCreateDto(apiId = 42L, name = "Arsenal"),
            venue =
                VenueOfFixtureApiSportsCreateDto(
                    apiId = 1L,
                    name = "Old Trafford",
                    city = "Manchester",
                ),
        )
}
