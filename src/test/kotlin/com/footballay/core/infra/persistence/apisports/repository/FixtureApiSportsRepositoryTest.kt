package com.footballay.core.infra.persistence.apisports.repository

import com.footballay.core.MatchEntityGenerator
import com.footballay.core.MatchEntities
import com.footballay.core.logger
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(MatchEntityGenerator::class)
class FixtureApiSportsRepositoryTest {

    val log = logger()

    @Autowired
    private lateinit var fixtureApiSportsRepository: FixtureApiSportsRepository

    @Autowired
    private lateinit var matchEntityGenerator: MatchEntityGenerator

    private lateinit var matchEntities: MatchEntities

    @BeforeEach
    fun setUp() {
        log.info("FixtureApiSports 테스트 데이터 준비 시작")
        
        // MatchEntityGenerator를 사용해서 모든 테스트 데이터를 한 번에 생성
        matchEntities = matchEntityGenerator.createCompleteMatchEntities()
        
        log.info("테스트 데이터 준비 완료")
        log.info("생성된 FixtureApiSports ID: {}", matchEntities.fixtureApiSports.id)
        log.info("생성된 FixtureCore UID: {}", matchEntities.fixtureCore.uid)
        log.info("생성된 MatchTeam 수: 홈팀 ID {}, 원정팀 ID {}", 
            matchEntities.homeMatchTeam.id, matchEntities.awayMatchTeam.id)
        log.info("생성된 MatchPlayer 수: {}", matchEntities.matchPlayers.size)
        log.info("생성된 MatchEvent 수: {}", matchEntities.matchEvents.size)
    }

    @Test
    fun `FixtureApiSports가 정상적으로 생성되는지 확인`() {
        // Given & When - setUp()에서 이미 생성됨
        
        // Then
        with(matchEntities.fixtureApiSports) {
            assertNotNull(id, "FixtureApiSports ID가 null이면 안됩니다")
            assertEquals(12345L, apiId, "API ID가 일치해야 합니다")
            assertEquals("Michael Oliver", referee, "심판 정보가 일치해야 합니다")
            assertEquals(matchEntities.fixtureCore, core, "FixtureCore 연관관계가 정확해야 합니다")
            assertEquals(matchEntities.homeMatchTeam, homeTeam, "홈팀 MatchTeam 연관관계가 정확해야 합니다")
            assertEquals(matchEntities.awayMatchTeam, awayTeam, "원정팀 MatchTeam 연관관계가 정확해야 합니다")
        }
        
        log.info("FixtureApiSports 생성 검증 완료")
    }

    @Test
    fun `Live 매치 엔티티들이 정상적으로 생성되는지 확인`() {
        // Given & When - setUp()에서 이미 생성됨
        
        // Then
        assertTrue(!matchEntities.matchPlayers.isEmpty(), "MatchPlayer가 생성되어야 합니다")
        assertTrue(!matchEntities.matchEvents.isEmpty(), "MatchEvent가 생성되어야 합니다")
        
        // MatchPlayer 검증
        matchEntities.matchPlayers.forEach { player ->
            assertNotNull(player.id, "MatchPlayer ID가 null이면 안됩니다")
            assertTrue(player.matchPlayerUid.isNotEmpty(), "MatchPlayer UID가 있어야 합니다")
            assertNotNull(player.matchTeam, "MatchPlayer에 팀이 연결되어야 합니다")
            assertNotNull(player.playerApiSports, "MatchPlayer에 ApiSports 선수가 연결되어야 합니다")
        }
        
        // MatchEvent 검증
        matchEntities.matchEvents.forEach { event ->
            assertNotNull(event.id, "MatchEvent ID가 null이면 안됩니다")
            assertEquals(matchEntities.fixtureApiSports, event.fixtureApi, "MatchEvent가 FixtureApiSports에 연결되어야 합니다")
            assertEquals("goal", event.eventType, "이벤트 타입이 일치해야 합니다")
        }
        
        log.info("Live 매치 엔티티 생성 검증 완료")
    }

    @Test
    fun `FixtureApiSports 조회가 정상적으로 동작하는지 확인`() {
        // Given
        val savedFixtureApiSports = matchEntities.fixtureApiSports
        
        // When
        val foundFixture = fixtureApiSportsRepository.findById(savedFixtureApiSports.id!!)
        
        // Then
        assertTrue(foundFixture.isPresent, "저장된 FixtureApiSports를 찾을 수 있어야 합니다")
        
        val fixture = foundFixture.get()
        assertEquals(savedFixtureApiSports.apiId, fixture.apiId, "API ID가 일치해야 합니다")
        assertEquals(matchEntities.fixtureCore.id, fixture.core?.id, "Core 연관관계가 유지되어야 합니다")
        
        log.info("FixtureApiSports 조회 검증 완료")
    }

    @Test
    fun `Core UID로 FixtureApiSports를 조회할 수 있는지 확인`() {
        // Given
        val coreUid = matchEntities.fixtureCore.uid
        
        // When
        val foundFixture = fixtureApiSportsRepository.findByCoreUid(coreUid)
        
        // Then
        assertNotNull(foundFixture, "Core UID로 FixtureApiSports를 찾을 수 있어야 합니다")
        assertEquals(coreUid, foundFixture?.core?.uid, "조회된 FixtureApiSports의 Core UID가 일치해야 합니다")
        assertNotNull(foundFixture?.id, "조회된 FixtureApiSports ID가 있어야 합니다")
        assertNotNull(matchEntities.fixtureApiSports.id, "원본 FixtureApiSports ID가 있어야 합니다")
        assertTrue(foundFixture?.id == matchEntities.fixtureApiSports.id, "올바른 FixtureApiSports가 조회되어야 합니다")
        
        log.info("Core UID 조회 검증 완료")
    }
}