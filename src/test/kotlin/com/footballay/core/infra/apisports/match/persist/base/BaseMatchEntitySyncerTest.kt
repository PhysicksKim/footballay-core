package com.footballay.core.infra.apisports.match.persist.base

import com.footballay.core.infra.apisports.match.persist.base.BaseMatchEntityManager
import com.footballay.core.infra.apisports.match.plan.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.plan.dto.FixtureApiSportsDto
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchTeamRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.ActiveProfiles

/**
 * BaseMatchEntitySyncer 단위 테스트
 *
 * **테스트 범위:**
 * - FixtureApiSports 업데이트
 * - ApiSportsMatchTeam 생성/업데이트
 * - 팀 정보 연결
 * - 에러 처리
 */
@ExtendWith(MockitoExtension::class)
@ActiveProfiles("test")
class BaseMatchEntitySyncerTest {
    @Mock
    private lateinit var fixtureRepository: FixtureApiSportsRepository

    @Mock
    private lateinit var matchTeamRepository: ApiSportsMatchTeamRepository

    @Mock
    private lateinit var teamApiSportsRepository: TeamApiSportsRepository

    private lateinit var baseMatchEntityManager: BaseMatchEntityManager

    @BeforeEach
    fun setUp() {
        baseMatchEntityManager =
            BaseMatchEntityManager(
                matchTeamRepository = matchTeamRepository,
                teamApiSportsRepository = teamApiSportsRepository,
            )
    }

    @Test
    @DisplayName("기존 Fixture가 있는 경우 Base DTO로 업데이트하고 MatchTeam을 생성하여 EntityBundle에 저장합니다")
    fun `기존 Fixture가 있는 경우 Base DTO로 업데이트하고 MatchTeam 생성`() {
        // Given
        val fixtureApiId = 12345L
        val baseDto = createBaseDto()
        val existingFixture = createExistingFixture(fixtureApiId)
        val entityBundle =
            MatchEntityBundle.createEmpty().apply {
                fixture = existingFixture
            }
        val homeTeamApiSports = createTeamApiSports(33L, "Manchester United")
        val awayTeamApiSports = createTeamApiSports(42L, "Arsenal")

        `when`(teamApiSportsRepository.findByApiId(33L)).thenReturn(homeTeamApiSports)
        `when`(teamApiSportsRepository.findByApiId(42L)).thenReturn(awayTeamApiSports)
        `when`(matchTeamRepository.save(any())).thenAnswer { it.arguments[0] }

        // When
        val result = baseMatchEntityManager.syncBaseEntities(fixtureApiId, baseDto, entityBundle)

        // Then
        assert(result.success)
        assert(result.fixture != null)
        assert(result.homeMatchTeam != null)
        assert(result.awayMatchTeam != null)

        // Fixture 업데이트 확인 (이미 영속 상태이므로 save 호출 안함)
        verify(fixtureRepository, never()).findByApiId(fixtureApiId)
        verify(fixtureRepository, never()).save(any())

        // MatchTeam 생성 확인
        verify(matchTeamRepository, times(2)).save(any())

        // 팀 정보 연결 확인
        assert(result.homeMatchTeam?.teamApiSports?.apiId == 33L)
        assert(result.awayMatchTeam?.teamApiSports?.apiId == 42L)

        // EntityBundle 업데이트 확인
        assert(entityBundle.homeTeam == result.homeMatchTeam)
        assert(entityBundle.awayTeam == result.awayMatchTeam)
    }

    @Test
    @DisplayName("EntityBundle에 Fixture가 없는 경우 동기화가 실패하고 에러 메시지를 반환합니다")
    fun `EntityBundle에 Fixture가 없는 경우 실패 반환`() {
        // Given
        val fixtureApiId = 99999L
        val baseDto = createBaseDto()
        val entityBundle = MatchEntityBundle.createEmpty() // fixture가 null

        // When
        val result = baseMatchEntityManager.syncBaseEntities(fixtureApiId, baseDto, entityBundle)

        // Then
        assert(!result.success)
        assert(result.errorMessage?.contains("Fixture not found in entityBundle") == true)

        verify(fixtureRepository, never()).findByApiId(any(Long::class.java))
        verify(fixtureRepository, never()).save(any())
        verify(matchTeamRepository, never()).save(any())
    }

    @Test
    @DisplayName("TeamApiSports가 존재하지 않는 경우 동기화가 실패하고 에러 메시지를 반환합니다")
    fun `TeamApiSports가 없는 경우 예외 발생`() {
        // Given
        val fixtureApiId = 12345L
        val baseDto = createBaseDto()
        val existingFixture = createExistingFixture(fixtureApiId)
        val entityBundle =
            MatchEntityBundle.createEmpty().apply {
                fixture = existingFixture
            }

        `when`(teamApiSportsRepository.findByApiId(33L)).thenReturn(null)

        // When & Then
        val result = baseMatchEntityManager.syncBaseEntities(fixtureApiId, baseDto, entityBundle)

        assert(!result.success)
        assert(result.errorMessage?.contains("TeamApiSports not found") == true)
    }

    @Test
    @DisplayName("기존 MatchTeam이 있는 경우 새로운 MatchTeam을 생성하지 않고 기존 것을 업데이트합니다")
    fun `기존 MatchTeam이 있는 경우 업데이트`() {
        // Given
        val fixtureApiId = 12345L
        val baseDto = createBaseDto()
        val existingFixture = createExistingFixture(fixtureApiId)
        val existingHomeMatchTeam = createMatchTeam(33L, "Manchester United")
        val homeTeamApiSports = createTeamApiSports(33L, "Manchester United")
        val awayTeamApiSports = createTeamApiSports(42L, "Arsenal")

        existingFixture.homeTeam = existingHomeMatchTeam

        val entityBundle =
            MatchEntityBundle.createEmpty().apply {
                fixture = existingFixture
            }

        `when`(teamApiSportsRepository.findByApiId(42L)).thenReturn(awayTeamApiSports)
        `when`(matchTeamRepository.save(any())).thenAnswer { it.arguments[0] }

        // When
        val result = baseMatchEntityManager.syncBaseEntities(fixtureApiId, baseDto, entityBundle)

        // Then
        assert(result.success)
        assert(result.homeMatchTeam == existingHomeMatchTeam) // 기존 MatchTeam 재사용
        assert(result.awayMatchTeam != null) // 새로운 away MatchTeam 생성됨

        // 기존 MatchTeam 업데이트 확인
        verify(matchTeamRepository).save(existingHomeMatchTeam)
        // 새로운 away MatchTeam 생성 확인
        verify(matchTeamRepository, times(2)).save(any(ApiSportsMatchTeam::class.java))
    }

    @Test
    @DisplayName("Team DTO가 null인 경우 해당 MatchTeam도 null로 설정하고 동기화를 계속 진행합니다")
    fun `Team DTO가 null인 경우 해당 MatchTeam은 null`() {
        // Given
        val fixtureApiId = 12345L
        val baseDto = createBaseDtoWithNullTeams()
        val existingFixture = createExistingFixture(fixtureApiId)
        val entityBundle =
            MatchEntityBundle.createEmpty().apply {
                fixture = existingFixture
            }

        // When
        val result = baseMatchEntityManager.syncBaseEntities(fixtureApiId, baseDto, entityBundle)

        // Then
        assert(result.success)
        assert(result.homeMatchTeam == null)
        assert(result.awayMatchTeam == null)

        verify(matchTeamRepository, never()).save(any())
    }

    // Helper methods

    private fun createBaseDto(): FixtureApiSportsDto =
        FixtureApiSportsDto(
            apiId = 12345L,
            referee = "Michael Oliver",
            timezone = "UTC",
            date = java.time.Instant.now(),
            round = "Regular Season - 10",
            status =
                FixtureApiSportsDto.StatusDto(
                    longStatus = "Not Started",
                    shortStatus = "NS",
                    elapsed = null,
                    extra = null,
                ),
            score =
                FixtureApiSportsDto.ScoreDto(
                    totalHome = 0,
                    totalAway = 0,
                    halftimeHome = null,
                    halftimeAway = null,
                    fulltimeHome = null,
                    fulltimeAway = null,
                    extratimeHome = null,
                    extratimeAway = null,
                    penaltyHome = null,
                    penaltyAway = null,
                ),
            homeTeam =
                FixtureApiSportsDto.BaseTeamDto(
                    apiId = 33L,
                    name = "Manchester United",
                    logo = "https://example.com/mun-logo.png",
                    winner = null,
                ),
            awayTeam =
                FixtureApiSportsDto.BaseTeamDto(
                    apiId = 42L,
                    name = "Arsenal",
                    logo = "https://example.com/ars-logo.png",
                    winner = null,
                ),
        )

    private fun createBaseDtoWithNullTeams(): FixtureApiSportsDto =
        FixtureApiSportsDto(
            apiId = 12345L,
            referee = "Michael Oliver",
            timezone = "UTC",
            date = java.time.Instant.now(),
            round = "Regular Season - 10",
            homeTeam = null,
            awayTeam = null,
        )

    private fun createExistingFixture(fixtureApiId: Long): FixtureApiSports =
        FixtureApiSports(
            apiId = fixtureApiId,
            season = mock(com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsSeason::class.java),
            homeTeam = null,
            awayTeam = null,
        )

    private fun createTeamApiSports(
        apiId: Long,
        name: String,
    ): TeamApiSports =
        TeamApiSports(
            apiId = apiId,
            name = name,
            code = name.substring(0, 3).uppercase(),
            country = "England",
            founded = 1900,
            national = false,
        )

    private fun createMatchTeam(
        teamApiId: Long,
        teamName: String,
    ): ApiSportsMatchTeam {
        val teamApiSports = createTeamApiSports(teamApiId, teamName)
        return ApiSportsMatchTeam(
            teamApiSports = teamApiSports,
            formation = "4-3-3",
            playerColor =
                com.footballay.core.infra.persistence.apisports.entity.live.UniformColor(
                    primary = "ea0000",
                    number = "ffffff",
                    border = "ea0000",
                ),
            goalkeeperColor =
                com.footballay.core.infra.persistence.apisports.entity.live.UniformColor(
                    primary = "000000",
                    number = "ffffff",
                    border = "000000",
                ),
            winner = null,
        )
    }
}
