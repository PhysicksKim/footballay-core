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
class TeamApiSportsRepositoryTest {

    val log = logger()

    @Autowired
    private lateinit var teamApiSportsRepository: TeamApiSportsRepository

    @Autowired
    private lateinit var matchEntityGenerator: MatchEntityGenerator

    private lateinit var matchEntities: MatchEntities

    @BeforeEach
    fun setUp() {
        log.info("TeamApiSports 테스트 데이터 준비 시작")
        
        // MatchEntityGenerator를 사용해서 모든 테스트 데이터를 한 번에 생성
        matchEntities = matchEntityGenerator.createCompleteMatchEntities()
        
        log.info("테스트 데이터 준비 완료")
        log.info("생성된 HomeTeamApiSports ID: {}", matchEntities.homeTeamApiSports.id)
        log.info("생성된 AwayTeamApiSports ID: {}", matchEntities.awayTeamApiSports.id)
        log.info("생성된 HomeTeamCore ID: {}", matchEntities.homeTeam.id)
        log.info("생성된 AwayTeamCore ID: {}", matchEntities.awayTeam.id)
    }

    @Test
    fun `TeamApiSports가 정상적으로 생성되는지 확인`() {
        // Given & When - setUp()에서 이미 생성됨
        
        // Then - HomeTeam 검증
        with(matchEntities.homeTeamApiSports) {
            assertNotNull(id, "HomeTeamApiSports ID가 null이면 안됩니다")
            assertEquals(33L, apiId, "HomeTeam API ID가 일치해야 합니다")
            assertEquals("Manchester United", name, "팀 이름이 일치해야 합니다")
            assertTrue(teamCore == matchEntities.homeTeam, "HomeTeamCore 연관관계가 정확해야 합니다")
        }
        
        // Then - AwayTeam 검증
        with(matchEntities.awayTeamApiSports) {
            assertNotNull(id, "AwayTeamApiSports ID가 null이면 안됩니다")
            assertEquals(42L, apiId, "AwayTeam API ID가 일치해야 합니다")
            assertEquals("Arsenal", name, "팀 이름이 일치해야 합니다")
            assertTrue(teamCore == matchEntities.awayTeam, "AwayTeamCore 연관관계가 정확해야 합니다")
        }
        
        log.info("TeamApiSports 생성 검증 완료")
    }

    @Test
    fun `findAllWithTeamCoreByPkOrApiIds - PK 기반 조회가 정상적으로 동작하는지 확인`() {
        // Given
        val homeTeamPk = matchEntities.homeTeamApiSports.id!!
        val awayTeamPk = matchEntities.awayTeamApiSports.id!!
        val teamPks = listOf(homeTeamPk, awayTeamPk)
        val emptyTeamApiIds = emptyList<Long>()
        
        // When
        val foundTeams = teamApiSportsRepository.findAllWithTeamCoreByPkOrApiIds(teamPks, emptyTeamApiIds)
        
        // Then
        assertTrue(foundTeams.isNotEmpty(), "PK 기반으로 Team을 찾을 수 있어야 합니다")
        assertEquals(2, foundTeams.size, "2개의 팀이 조회되어야 합니다")
        
        val homeTeam = foundTeams.find { it.id == homeTeamPk }
        val awayTeam = foundTeams.find { it.id == awayTeamPk }
        
        assertNotNull(homeTeam, "홈팀이 조회되어야 합니다")
        assertNotNull(awayTeam, "원정팀이 조회되어야 합니다")
        
        assertTrue(homeTeam?.teamCore == matchEntities.homeTeam, "홈팀 Core 연관관계가 유지되어야 합니다")
        assertTrue(awayTeam?.teamCore == matchEntities.awayTeam, "원정팀 Core 연관관계가 유지되어야 합니다")
        
        log.info("PK 기반 조회 검증 완료 - 조회된 Team 수: ${foundTeams.size}")
    }

    @Test
    fun `findAllWithTeamCoreByPkOrApiIds - ApiId 기반 조회가 정상적으로 동작하는지 확인`() {
        // Given
        val homeTeamApiId = matchEntities.homeTeamApiSports.apiId!!
        val awayTeamApiId = matchEntities.awayTeamApiSports.apiId!!
        val teamApiIds = listOf(homeTeamApiId, awayTeamApiId)
        val emptyTeamPks = emptyList<Long>()
        
        // When
        val foundTeams = teamApiSportsRepository.findAllWithTeamCoreByPkOrApiIds(emptyTeamPks, teamApiIds)
        
        // Then
        assertTrue(foundTeams.isNotEmpty(), "ApiId 기반으로 Team을 찾을 수 있어야 합니다")
        assertEquals(2, foundTeams.size, "2개의 팀이 조회되어야 합니다")
        
        val homeTeam = foundTeams.find { it.apiId == homeTeamApiId }
        val awayTeam = foundTeams.find { it.apiId == awayTeamApiId }
        
        assertNotNull(homeTeam, "홈팀이 조회되어야 합니다")
        assertNotNull(awayTeam, "원정팀이 조회되어야 합니다")
        
        assertTrue(homeTeam?.teamCore == matchEntities.homeTeam, "홈팀 Core 연관관계가 유지되어야 합니다")
        assertTrue(awayTeam?.teamCore == matchEntities.awayTeam, "원정팀 Core 연관관계가 유지되어야 합니다")
        
        log.info("ApiId 기반 조회 검증 완료 - 조회된 Team 수: ${foundTeams.size}")
    }

    @Test
    fun `findAllWithTeamCoreByPkOrApiIds - PK와 ApiId 모두로 조회했을 때 중복이 제거되는지 확인`() {
        // Given
        val homeTeamPk = matchEntities.homeTeamApiSports.id!!
        val awayTeamPk = matchEntities.awayTeamApiSports.id!!
        val homeTeamApiId = matchEntities.homeTeamApiSports.apiId!!
        val awayTeamApiId = matchEntities.awayTeamApiSports.apiId!!
        val teamPks = listOf(homeTeamPk, awayTeamPk)
        val teamApiIds = listOf(awayTeamApiId, homeTeamApiId)
        
        // When
        val foundTeams = teamApiSportsRepository.findAllWithTeamCoreByPkOrApiIds(teamPks, teamApiIds)
        
        // Then
        assertTrue(foundTeams.isNotEmpty(), "PK와 ApiId 모두로 조회했을 때 Team을 찾을 수 있어야 합니다")
        
        // 중복 제거 확인 (동일한 Team이 두 번 조회되지 않아야 함)
        val uniqueTeamIds = foundTeams.map { it.id }.distinct()
        assertEquals(foundTeams.size, uniqueTeamIds.size, "중복된 Team이 제거되어야 합니다")
        
        val homeTeam = foundTeams.find { it.id == homeTeamPk }
        val awayTeam = foundTeams.find { it.apiId == awayTeamApiId }
        
        assertNotNull(homeTeam, "홈팀이 조회되어야 합니다")
        assertNotNull(awayTeam, "원정팀이 조회되어야 합니다")
        
        log.info("중복 제거 검증 완료 - 조회된 Team 수: ${foundTeams.size}, 고유 ID 수: ${uniqueTeamIds.size}")
    }

    @Test
    fun `findAllWithTeamCoreByPkOrApiIds - 빈 리스트로 조회했을 때 빈 결과가 반환되는지 확인`() {
        // Given
        val emptyTeamPks = emptyList<Long>()
        val emptyTeamApiIds = emptyList<Long>()
        
        // When
        val foundTeams = teamApiSportsRepository.findAllWithTeamCoreByPkOrApiIds(emptyTeamPks, emptyTeamApiIds)
        
        // Then
        assertTrue(foundTeams.isEmpty(), "빈 리스트로 조회했을 때 빈 결과가 반환되어야 합니다")
        
        log.info("빈 리스트 조회 검증 완료")
    }

    @Test
    fun `findAllWithTeamCoreByPkOrApiIds - 존재하지 않는 PK와 존재하는 ApiId로 조회했을 때 ApiId 기반으로만 조회되는지 확인`() {
        // Given
        val nonExistentPk = 999999L
        val homeTeamApiId = matchEntities.homeTeamApiSports.apiId!!
        val teamPks = listOf(nonExistentPk)
        val teamApiIds = listOf(homeTeamApiId)
        
        // When
        val foundTeams = teamApiSportsRepository.findAllWithTeamCoreByPkOrApiIds(teamPks, teamApiIds)
        
        // Then
        assertTrue(foundTeams.isNotEmpty(), "존재하지 않는 PK와 존재하는 ApiId로 조회했을 때 ApiId 기반으로 Team을 찾을 수 있어야 합니다")
        
        val homeTeam = foundTeams.find { it.apiId == homeTeamApiId }
        assertNotNull(homeTeam, "홈팀이 조회되어야 합니다")
        assertTrue(homeTeam?.teamCore == matchEntities.homeTeam, "홈팀 Core 연관관계가 유지되어야 합니다")
        
        log.info("존재하지 않는 PK와 존재하는 ApiId 조회 검증 완료 - 조회된 Team 수: ${foundTeams.size}")
    }
} 