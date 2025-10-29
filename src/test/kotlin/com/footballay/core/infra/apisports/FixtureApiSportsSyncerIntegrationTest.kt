package com.footballay.core.infra.apisports

import com.footballay.core.infra.apisports.backbone.sync.fixture.FixtureApiSportsWithCoreSyncer
import com.footballay.core.infra.apisports.shared.dto.FixtureApiSportsSyncDto
import com.footballay.core.infra.apisports.shared.dto.ScoreOfFixtureApiSportsCreateDto
import com.footballay.core.infra.apisports.shared.dto.StatusOfFixtureApiSportsCreateDto
import com.footballay.core.infra.apisports.shared.dto.TeamOfFixtureApiSportsCreateDto
import com.footballay.core.infra.apisports.shared.dto.VenueOfFixtureApiSportsCreateDto
import com.footballay.core.infra.persistence.apisports.entity.*
import com.footballay.core.infra.persistence.apisports.repository.*
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.repository.FixtureCoreRepository
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.persistence.core.repository.TeamCoreRepository
import com.footballay.core.logger
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * FixtureApiSportsSyncer 통합 테스트
 *
 * 실제 데이터베이스와 Spring Context를 사용하여 전체 플로우를 검증합니다.
 *
 * 주요 테스트 시나리오:
 * 1. 기본적인 경기 저장 플로우
 * 2. 중복 경기 처리
 * 3. Venue 처리
 * 4. Core Entity 생성 및 연관관계
 *
 * 주의사항:
 * - LeagueCore와 TeamCore 간의 LeagueTeamCore 다대다 연관관계는 의도적으로 설정하지 않습니다.
 * - 이는 임시 리그나 특별한 경우에 해당 리그에 정식으로 소속되지 않은 팀도 경기에 참여할 수 있음을 반영합니다.
 * - 향후 비즈니스 로직이 변경되어 이 연관관계가 필요해진다면, 그때 테스트도 함께 수정되어야 합니다.
 */
@SpringBootTest
@ActiveProfiles("dev", "mockapi")
@Transactional
class FixtureApiSportsSyncerIntegrationTest {
    val log = logger()

    @Autowired
    private lateinit var fixtureApiSportsSyncer: FixtureApiSportsWithCoreSyncer

    // ApiSports Repositories
    @Autowired
    private lateinit var leagueApiSportsRepository: LeagueApiSportsRepository

    @Autowired
    private lateinit var leagueApiSportsSeasonRepository: LeagueApiSportsSeasonRepository

    @Autowired
    private lateinit var teamApiSportsRepository: TeamApiSportsRepository

    @Autowired
    private lateinit var fixtureApiSportsRepository: FixtureApiSportsRepository

    @Autowired
    private lateinit var venueApiSportsRepository: VenueApiSportsRepository

    // Core Repositories
    @Autowired
    private lateinit var leagueCoreRepository: LeagueCoreRepository

    @Autowired
    private lateinit var teamCoreRepository: TeamCoreRepository

    @Autowired
    private lateinit var fixtureCoreRepository: FixtureCoreRepository

    @Autowired
    private lateinit var em: EntityManager

    // Test Data Constants
    private val TEST_LEAGUE_API_ID = 39L
    private val TEST_SEASON_YEAR = 2024
    private val TEST_ARSENAL_API_ID = 101L
    private val TEST_CHELSEA_API_ID = 102L
    private val TEST_VENUE_API_ID = 201L

    @BeforeEach
    fun setUp() {
        log.info("테스트 환경 초기화 시작")
        clearAllTestData()
        setupBasicTestData()
        log.info("테스트 환경 초기화 완료")
    }

    @Test
    fun `기본적인 경기 저장 - 정상 플로우 검증`() {
        // given: 새로운 경기 데이터
        val fixtureDto =
            createBasicFixtureDto(
                fixtureApiId = 1001L,
                homeTeamApiId = TEST_ARSENAL_API_ID,
                awayTeamApiId = TEST_CHELSEA_API_ID,
            )

        // when: 경기 저장 실행
        assertDoesNotThrow {
            fixtureApiSportsSyncer.saveFixturesOfLeague(
                TEST_LEAGUE_API_ID,
                listOf(fixtureDto),
            )
        }

        // then: 저장 결과 검증
        verifyFixtureSaved(fixtureDto.apiId!!)
        verifyFixtureCoreSaved(fixtureDto.apiId!!)
        val fixtures = fixtureApiSportsRepository.findAll()
        fixtures.forEach { assertThat { it.homeTeam } != null && assertThat { it.awayTeam } != null }

        log.info("기본 경기 저장 테스트 완료")
    }

    @Test
    fun `Venue 포함 경기 저장 - Venue 처리 검증`() {
        // given: Venue 정보가 포함된 경기 데이터
        val fixtureDto =
            createFixtureWithVenue(
                fixtureApiId = 1002L,
                venueApiId = TEST_VENUE_API_ID,
                venueName = "Emirates Stadium",
            )

        // when: 경기 저장 실행
        fixtureApiSportsSyncer.saveFixturesOfLeague(
            TEST_LEAGUE_API_ID,
            listOf(fixtureDto),
        )

        // then: Venue 처리 결과 검증
        verifyVenueCreated(TEST_VENUE_API_ID, "Emirates Stadium")
        verifyFixtureSaved(fixtureDto.apiId!!)

        log.info("Venue 포함 경기 저장 테스트 완료")
    }

    @Test
    fun `중복 경기 저장 - 업데이트 로직 검증`() {
        // given: 기존 경기 데이터
        val originalFixture = createBasicFixtureDto(fixtureApiId = 1003L)
        fixtureApiSportsSyncer.saveFixturesOfLeague(
            TEST_LEAGUE_API_ID,
            listOf(originalFixture),
        )

        em.flush()
        em.clear()

        // when: 동일한 API ID로 다시 저장 (업데이트 시나리오)
        val updatedFixture =
            originalFixture.copy(
                status =
                    StatusOfFixtureApiSportsCreateDto(
                        longStatus = "Match Finished",
                        shortStatus = "FT",
                    ),
                score =
                    ScoreOfFixtureApiSportsCreateDto(
                        fulltimeHome = 2,
                        fulltimeAway = 1,
                    ),
            )

        // TODO : TEST_LEAGUE_API_ID 로 동일한 경기 2번 저장 요청할 때, update 가 일어나야함.
        // 현재 API_ID NULLS FIRST unique 제약조건 위반 에러 발생함. update 가 아니라 save 로 들어가는듯
        log.info("--- Try to save fixture with existing API ID to trigger update logic ---")
        val leagueAll = leagueApiSportsRepository.findAll()
        val fixtureAll = fixtureApiSportsRepository.findAll()
        log.info("league season ${leagueAll.first().seasons}")
        log.info("Existing Leagues: ${leagueAll.size}, Existing Fixtures: ${fixtureAll.size}")
        log.info("fixture ${fixtureAll.first()} season=${fixtureAll.first().season}")

        fixtureApiSportsSyncer.saveFixturesOfLeague(
            TEST_LEAGUE_API_ID,
            listOf(updatedFixture),
        )

        // then: 업데이트 결과 검증
        val savedFixture = fixtureApiSportsRepository.findByApiId(1003L)
        assertNotNull(savedFixture)
        assertEquals("FT", savedFixture!!.status?.shortStatus)
        assertEquals("Match Finished", savedFixture.status?.longStatus)

        // 중복 생성이 아닌 업데이트임을 확인
        val allFixtures = fixtureApiSportsRepository.findAllByApiIdIn(listOf(1003L))
        assertEquals(1, allFixtures.size)

        log.info("중복 경기 업데이트 테스트 완료")
    }

    @Test
    fun `여러 경기 일괄 저장 - 배치 처리 검증`() {
        // given: 여러 경기 데이터
        val fixtures =
            listOf(
                createBasicFixtureDto(fixtureApiId = 2001L),
                createBasicFixtureDto(fixtureApiId = 2002L),
                createFixtureWithVenue(fixtureApiId = 2003L, venueApiId = 301L, venueName = "Stamford Bridge"),
            )

        // when: 배치 저장 실행
        fixtureApiSportsSyncer.saveFixturesOfLeague(
            TEST_LEAGUE_API_ID,
            fixtures,
        )

        // then: 모든 경기 저장 확인
        fixtures.forEach { fixture ->
            verifyFixtureSaved(fixture.apiId!!)
            verifyFixtureCoreSaved(fixture.apiId!!)
        }

        // Venue도 생성되었는지 확인
        verifyVenueCreated(301L, "Stamford Bridge")

        log.info("여러 경기 일괄 저장 테스트 완료")
    }

    // === 테스트 데이터 생성 헬퍼 메서드들 ===

    private fun createBasicFixtureDto(
        fixtureApiId: Long,
        homeTeamApiId: Long = TEST_ARSENAL_API_ID,
        awayTeamApiId: Long = TEST_CHELSEA_API_ID,
    ): FixtureApiSportsSyncDto =
        FixtureApiSportsSyncDto(
            apiId = fixtureApiId,
            leagueApiId = TEST_LEAGUE_API_ID,
            seasonYear = TEST_SEASON_YEAR.toString(),
            date = "2025-06-26T14:00:00+00:00",
            timestamp = System.currentTimeMillis(),
            status =
                StatusOfFixtureApiSportsCreateDto(
                    longStatus = "Not Started",
                    shortStatus = "NS",
                ),
            homeTeam =
                TeamOfFixtureApiSportsCreateDto(
                    apiId = homeTeamApiId,
                    name = if (homeTeamApiId == TEST_ARSENAL_API_ID) "Arsenal" else "Other Team",
                ),
            awayTeam =
                TeamOfFixtureApiSportsCreateDto(
                    apiId = awayTeamApiId,
                    name = if (awayTeamApiId == TEST_CHELSEA_API_ID) "Chelsea" else "Other Team",
                ),
            score = ScoreOfFixtureApiSportsCreateDto(),
        )

    private fun createFixtureWithVenue(
        fixtureApiId: Long,
        venueApiId: Long,
        venueName: String,
    ): FixtureApiSportsSyncDto =
        createBasicFixtureDto(fixtureApiId).copy(
            venue =
                VenueOfFixtureApiSportsCreateDto(
                    apiId = venueApiId,
                    name = venueName,
                    city = "London",
                ),
        )

    // === 테스트 환경 설정 헬퍼 메서드들 ===

    private fun clearAllTestData() {
        // 연관관계 순서에 맞춰 삭제
        fixtureApiSportsRepository.deleteAll()
        fixtureCoreRepository.deleteAll()
        venueApiSportsRepository.deleteAll()
        leagueApiSportsSeasonRepository.deleteAll()
        teamApiSportsRepository.deleteAll()
        teamCoreRepository.deleteAll()
        leagueApiSportsRepository.deleteAll()
        leagueCoreRepository.deleteAll()
    }

    private fun setupBasicTestData() {
        log.info("기본 테스트 데이터 설정 시작")

        // 1. Core Entities 생성
        val leagueCore = createAndSaveLeagueCore()
        val arsenalCore = createAndSaveTeamCore("Arsenal")
        val chelseaCore = createAndSaveTeamCore("Chelsea")

        // 2. ApiSports Entities 생성 (Core와 연결)
        createAndSaveLeagueApiSports(leagueCore)
        createAndSaveTeamApiSports(TEST_ARSENAL_API_ID, "Arsenal", arsenalCore)
        createAndSaveTeamApiSports(TEST_CHELSEA_API_ID, "Chelsea", chelseaCore)

        log.info("기본 테스트 데이터 설정 완료")
    }

    private fun createAndSaveLeagueCore(): LeagueCore {
        val leagueCore =
            LeagueCore(
                uid = "premier-league-2024",
                name = "Premier League",
            )
        return leagueCoreRepository.save(leagueCore)
    }

    private fun createAndSaveTeamCore(teamName: String): TeamCore {
        val teamCore =
            TeamCore(
                uid = "${teamName.lowercase()}-2024",
                name = teamName,
            )
        return teamCoreRepository.save(teamCore)
    }

    private fun createAndSaveLeagueApiSports(leagueCore: LeagueCore) {
        val leagueApiSports =
            LeagueApiSports(
                leagueCore = leagueCore,
                apiId = TEST_LEAGUE_API_ID,
                name = "Premier League",
                type = "League",
            )

        leagueApiSportsRepository.save(leagueApiSports)

        // 시즌 추가 (별도로 저장)
        val season =
            LeagueApiSportsSeason(
                leagueApiSports = leagueApiSports,
                seasonYear = TEST_SEASON_YEAR,
            )
        leagueApiSportsSeasonRepository.save(season)
    }

    private fun createAndSaveTeamApiSports(
        apiId: Long,
        name: String,
        teamCore: TeamCore,
    ) {
        val teamApiSports =
            TeamApiSports(
                teamCore = teamCore,
                apiId = apiId,
                name = name,
                country = "England",
            )
        teamApiSportsRepository.save(teamApiSports)
    }

    // === 검증 헬퍼 메서드들 ===

    private fun verifyFixtureSaved(fixtureApiId: Long) {
        val savedFixture = fixtureApiSportsRepository.findByApiId(fixtureApiId)
        assertNotNull(savedFixture, "FixtureApiSports가 저장되어야 합니다: $fixtureApiId")
        assertEquals(fixtureApiId, savedFixture!!.apiId)
    }

    private fun verifyFixtureCoreSaved(fixtureApiId: Long) {
        val savedFixture = fixtureApiSportsRepository.findByApiId(fixtureApiId)
        assertNotNull(savedFixture, "FixtureApiSports가 존재해야 합니다: $fixtureApiId")
        assertNotNull(savedFixture!!.core, "FixtureCore가 생성되어야 합니다: $fixtureApiId")
    }

    private fun verifyVenueCreated(
        venueApiId: Long,
        expectedName: String,
    ) {
        val venue = venueApiSportsRepository.findByApiId(venueApiId)
        assertNotNull(venue, "Venue이 생성되어야 합니다: $venueApiId")
        assertEquals(expectedName, venue!!.name)
    }
}
