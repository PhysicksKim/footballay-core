package com.footballay.core.infra.apisports.match.persist.base

import com.footballay.core.infra.apisports.match.persist.base.BaseMatchEntityManager
import com.footballay.core.infra.apisports.match.plan.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.plan.dto.FixtureApiSportsDto
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSportsSeason
import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsSeasonRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchTeamRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * BaseMatchEntitySyncer 통합 테스트
 *
 * **테스트 범위:**
 * - 실제 데이터베이스와의 상호작용
 * - 엔티티 생성/업데이트/저장
 * - 트랜잭션 처리
 * - 연관관계 설정
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BaseMatchEntitySyncerIntegrationTest {
    @Autowired
    private lateinit var baseMatchEntityManager: BaseMatchEntityManager

    @Autowired
    private lateinit var fixtureRepository: FixtureApiSportsRepository

    @Autowired
    private lateinit var matchTeamRepository: ApiSportsMatchTeamRepository

    @Autowired
    private lateinit var teamApiSportsRepository: TeamApiSportsRepository

    @Autowired
    private lateinit var leagueApiSportsSeasonRepository: LeagueApiSportsSeasonRepository

    @BeforeEach
    fun setUp() {
        // 테스트용 엔티티 직접 생성
        // season은 필수이므로 임시로 생성하고 먼저 저장
        val season =
            LeagueApiSportsSeason(
                seasonYear = 2024,
                leagueApiSports = null, // 테스트에서는 null로 설정
            )
        val savedSeason = leagueApiSportsSeasonRepository.save(season)

        val fixtureApiSports =
            FixtureApiSports(
                apiId = 12345L,
                referee = "Michael Oliver",
                round = "Regular Season - 10",
                score = null,
                status = null,
                season = savedSeason,
            )

        val homeTeamApiSports =
            TeamApiSports(
                apiId = 33L,
                name = "Manchester United",
                code = "MUN",
                logo = "https://example.com/mun-logo.png",
            )

        val awayTeamApiSports =
            TeamApiSports(
                apiId = 42L,
                name = "Arsenal",
                code = "ARS",
                logo = "https://example.com/ars-logo.png",
            )

        // 저장
        fixtureRepository.save(fixtureApiSports)
        teamApiSportsRepository.save(homeTeamApiSports)
        teamApiSportsRepository.save(awayTeamApiSports)
    }

    @Test
    @DisplayName("실제 데이터베이스에서 Base DTO를 사용하여 Fixture를 업데이트하고 MatchTeam을 생성하여 저장합니다")
    fun `실제 데이터베이스에서 Base DTO로 Fixture 업데이트 및 MatchTeam 생성`() {
        // Given
        val fixtureApiId = 12345L
        val baseDto = createBaseDto(fixtureApiId)
        val fixture = fixtureRepository.findByApiId(fixtureApiId)
        val entityBundle =
            MatchEntityBundle.createEmpty().apply {
                this.fixture = fixture
            }

        // When
        val result = baseMatchEntityManager.syncBaseEntities(fixtureApiId, baseDto, entityBundle)

        // Then
        assert(result.success)
        assert(result.fixture != null)
        assert(result.homeMatchTeam != null)
        assert(result.awayMatchTeam != null)

        // 데이터베이스에서 실제 저장 확인
        val savedFixture = fixtureRepository.findByApiId(fixtureApiId)
        assert(savedFixture != null)
        assert(savedFixture?.referee == "Michael Oliver")
        assert(savedFixture?.round == "Regular Season - 10")

        // MatchTeam 저장 확인
        val homeMatchTeam = savedFixture?.homeTeam
        val awayMatchTeam = savedFixture?.awayTeam

        assert(homeMatchTeam != null)
        assert(awayMatchTeam != null)
        assert(homeMatchTeam?.teamApiSports?.name == "Manchester United")
        assert(awayMatchTeam?.teamApiSports?.name == "Arsenal")

        // 유니폼 색상 설정 확인
        assert(homeMatchTeam?.playerColor?.primary == "000000")
        assert(homeMatchTeam?.goalkeeperColor?.primary == "ffffff")

        // EntityBundle 업데이트 확인
        assert(entityBundle.homeTeam == result.homeMatchTeam)
        assert(entityBundle.awayTeam == result.awayMatchTeam)
    }

    @Test
    @DisplayName("기존 MatchTeam이 데이터베이스에 저장되어 있는 경우 새로운 MatchTeam을 생성하지 않고 기존 것을 업데이트합니다")
    fun `기존 MatchTeam이 있는 경우 업데이트`() {
        // Given
        val fixtureApiId = 12345L
        val baseDto = createBaseDto(fixtureApiId)

        // 기존 MatchTeam 설정 - 먼저 MatchTeam을 저장하고 Fixture에 연결
        val existingFixture = fixtureRepository.findByApiId(fixtureApiId)!!
        val homeTeamApiSports = teamApiSportsRepository.findByApiId(33L)!!
        val existingHomeMatchTeam = createMatchTeam(homeTeamApiSports, "4-4-2")

        // MatchTeam을 먼저 저장
        val savedHomeMatchTeam = matchTeamRepository.save(existingHomeMatchTeam)

        // Fixture에 연결하고 저장
        existingFixture.homeTeam = savedHomeMatchTeam
        fixtureRepository.save(existingFixture)

        val entityBundle =
            MatchEntityBundle.createEmpty().apply {
                this.fixture = existingFixture
            }

        // When
        val result = baseMatchEntityManager.syncBaseEntities(fixtureApiId, baseDto, entityBundle)

        // Then
        assert(result.success)
        assert(result.homeMatchTeam == savedHomeMatchTeam) // 기존 MatchTeam 재사용

        // 승리 여부 업데이트 확인
        val updatedFixture = fixtureRepository.findByApiId(fixtureApiId)!!
        assert(updatedFixture.homeTeam?.winner == false) // BaseDto에서 설정한 값
    }

    @Test
    @DisplayName("EntityBundle에 Fixture가 없는 경우 동기화가 실패하고 에러 메시지를 반환합니다")
    fun `EntityBundle에 Fixture가 없는 경우 실패 반환`() {
        // Given
        val nonExistentFixtureApiId = 99999L
        val baseDto = createBaseDto(nonExistentFixtureApiId)
        val entityBundle = MatchEntityBundle.createEmpty() // fixture가 null

        // When
        val result = baseMatchEntityManager.syncBaseEntities(nonExistentFixtureApiId, baseDto, entityBundle)

        // Then
        assert(!result.success)
        assert(result.errorMessage?.contains("Fixture not found in entityBundle") == true)
    }

    @Test
    @DisplayName("TeamApiSports가 데이터베이스에 존재하지 않는 경우 동기화가 실패하고 에러 메시지를 반환합니다")
    fun `TeamApiSports가 없는 경우 예외 발생`() {
        // Given
        val fixtureApiId = 12345L
        val baseDto = createBaseDtoWithNonExistentTeam(fixtureApiId)
        val fixture = fixtureRepository.findByApiId(fixtureApiId)
        val entityBundle =
            MatchEntityBundle.createEmpty().apply {
                this.fixture = fixture
            }

        // When & Then
        val result = baseMatchEntityManager.syncBaseEntities(fixtureApiId, baseDto, entityBundle)

        assert(!result.success)
        assert(result.errorMessage?.contains("TeamApiSports not found") == true)
    }

    @Test
    @DisplayName("Base DTO의 Score 정보를 Fixture 엔티티에 올바르게 업데이트합니다")
    fun `Score 정보 업데이트`() {
        // Given
        val fixtureApiId = 12345L
        val baseDto = createBaseDtoWithScore(fixtureApiId)
        val fixture = fixtureRepository.findByApiId(fixtureApiId)
        val entityBundle =
            MatchEntityBundle.createEmpty().apply {
                this.fixture = fixture
            }

        // When
        val result = baseMatchEntityManager.syncBaseEntities(fixtureApiId, baseDto, entityBundle)

        // Then
        assert(result.success)

        val updatedFixture = fixtureRepository.findByApiId(fixtureApiId)!!
        assert(updatedFixture.score != null)
        assert(updatedFixture.score?.totalHome == 2)
        assert(updatedFixture.score?.totalAway == 1)
        assert(updatedFixture.score?.halftimeHome == 1)
        assert(updatedFixture.score?.halftimeAway == 0)
    }

    @Test
    @DisplayName("Base DTO의 Status 정보를 Fixture 엔티티에 올바르게 업데이트합니다")
    fun `Status 정보 업데이트`() {
        // Given
        val fixtureApiId = 12345L
        val baseDto = createBaseDtoWithStatus(fixtureApiId)
        val fixture = fixtureRepository.findByApiId(fixtureApiId)
        val entityBundle =
            MatchEntityBundle.createEmpty().apply {
                this.fixture = fixture
            }

        // When
        val result = baseMatchEntityManager.syncBaseEntities(fixtureApiId, baseDto, entityBundle)

        // Then
        assert(result.success)

        val updatedFixture = fixtureRepository.findByApiId(fixtureApiId)!!
        assert(updatedFixture.status != null)
        assert(updatedFixture.status?.longStatus == "First Half")
        assert(updatedFixture.status?.shortStatus == "1H")
        assert(updatedFixture.status?.elapsed == 25)
    }

    private fun createBaseDto(fixtureApiId: Long): FixtureApiSportsDto =
        FixtureApiSportsDto(
            apiId = fixtureApiId,
            referee = "Michael Oliver",
            timezone = "UTC",
            date = java.time.Instant.now(),
            round = "Regular Season - 10",
            homeTeam =
                FixtureApiSportsDto.BaseTeamDto(
                    apiId = 33L,
                    name = "Manchester United",
                    logo = "https://example.com/mun-logo.png",
                    winner = false,
                ),
            awayTeam =
                FixtureApiSportsDto.BaseTeamDto(
                    apiId = 42L,
                    name = "Arsenal",
                    logo = "https://example.com/ars-logo.png",
                    winner = null,
                ),
        )

    private fun createBaseDtoWithNonExistentTeam(fixtureApiId: Long): FixtureApiSportsDto =
        FixtureApiSportsDto(
            apiId = fixtureApiId,
            referee = "Michael Oliver",
            timezone = "UTC",
            date = java.time.Instant.now(),
            round = "Regular Season - 10",
            homeTeam =
                FixtureApiSportsDto.BaseTeamDto(
                    apiId = 99999L, // 존재하지 않는 팀
                    name = "Non Existent Team",
                    logo = "https://example.com/logo.png",
                    winner = false,
                ),
            awayTeam = null,
        )

    private fun createBaseDtoWithScore(fixtureApiId: Long): FixtureApiSportsDto =
        FixtureApiSportsDto(
            apiId = fixtureApiId,
            referee = "Michael Oliver",
            timezone = "UTC",
            date = java.time.Instant.now(),
            round = "Regular Season - 10",
            score =
                FixtureApiSportsDto.ScoreDto(
                    totalHome = 2,
                    totalAway = 1,
                    halftimeHome = 1,
                    halftimeAway = 0,
                    fulltimeHome = 2,
                    fulltimeAway = 1,
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
                    winner = true,
                ),
            awayTeam =
                FixtureApiSportsDto.BaseTeamDto(
                    apiId = 42L,
                    name = "Arsenal",
                    logo = "https://example.com/ars-logo.png",
                    winner = false,
                ),
        )

    private fun createBaseDtoWithStatus(fixtureApiId: Long): FixtureApiSportsDto =
        FixtureApiSportsDto(
            apiId = fixtureApiId,
            referee = "Michael Oliver",
            timezone = "UTC",
            date = java.time.Instant.now(),
            round = "Regular Season - 10",
            status =
                FixtureApiSportsDto.StatusDto(
                    longStatus = "First Half",
                    shortStatus = "1H",
                    elapsed = 25,
                    extra = null,
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

    private fun createMatchTeam(
        teamApiSports: TeamApiSports,
        formation: String,
    ): ApiSportsMatchTeam =
        ApiSportsMatchTeam(
            teamApiSports = teamApiSports,
            formation = formation,
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
