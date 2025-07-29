package com.footballay.core.infra.apisports.syncer.match.loader

import com.footballay.core.MatchEntityGenerator
import com.footballay.core.MatchEntities
import com.footballay.core.infra.apisports.syncer.match.context.MatchEntityBundle
import com.footballay.core.infra.apisports.syncer.match.context.MatchPlayerContext
import com.footballay.core.logger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(MatchEntityGenerator::class)
@DisplayName("MatchDataLoaderImpl 테스트")
class MatchDataLoaderImplTest {

    private val log = logger()

    @Autowired
    private lateinit var matchDataLoader: MatchDataLoader

    @Autowired
    private lateinit var matchEntityGenerator: MatchEntityGenerator

    private lateinit var matchEntities: MatchEntities

    @BeforeEach
    fun setUp() {
        log.info("MatchDataLoader 테스트 데이터 준비 시작")
        
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
    @DisplayName("정상적인 경기 데이터 로드 - 모든 엔티티가 올바르게 로드되는지 확인")
    fun `정상적인 경기 데이터가 올바르게 로드된다`() {
        // given
        val fixtureApiId = matchEntities.fixtureApiSports.apiId
        val playerContext = MatchPlayerContext()
        val entityBundle = MatchEntityBundle.createEmpty()
        
        // 디버깅: 생성된 데이터 확인
        log.info("테스트용 fixtureApiId: {}", fixtureApiId)
        log.info("생성된 선수 수: {}", matchEntities.matchPlayers.size)
        log.info("생성된 이벤트 수: {}", matchEntities.matchEvents.size)
        
        // when
        matchDataLoader.loadContext(fixtureApiId, playerContext, entityBundle)
        
        // 디버깅: 로드된 데이터 확인
        log.info("로드된 선수 수: {}", entityBundle.allMatchPlayers.size)
        log.info("로드된 이벤트 수: {}", entityBundle.allEvents.size)
        
        // then
        assertThat(entityBundle.fixture).isNotNull()
        assertThat(entityBundle.fixture?.id).isEqualTo(matchEntities.fixtureApiSports.id)
        assertThat(entityBundle.fixture?.apiId).isEqualTo(fixtureApiId)
        
        // 홈팀 검증
        assertThat(entityBundle.homeTeam).isNotNull()
        assertThat(entityBundle.homeTeam?.id).isEqualTo(matchEntities.homeMatchTeam.id)
        assertThat(entityBundle.homeTeam?.teamApiSports?.name).isEqualTo("Manchester United")
        
        // 원정팀 검증
        assertThat(entityBundle.awayTeam).isNotNull()
        assertThat(entityBundle.awayTeam?.id).isEqualTo(matchEntities.awayMatchTeam.id)
        assertThat(entityBundle.awayTeam?.teamApiSports?.name).isEqualTo("Arsenal")
        
        // 선수들 검증
        assertThat(entityBundle.allMatchPlayers).isNotEmpty()
        assertThat(entityBundle.allMatchPlayers).hasSizeGreaterThanOrEqualTo(matchEntities.matchPlayers.size)
        
        // 이벤트 검증
        assertThat(entityBundle.allEvents).isNotEmpty()
        assertThat(entityBundle.allEvents).hasSizeGreaterThanOrEqualTo(matchEntities.matchEvents.size)
        
        log.info("로드된 데이터 검증 완료 - Players: {}, Events: {}", 
            entityBundle.allMatchPlayers.size, entityBundle.allEvents.size)
    }

    @Test
    @DisplayName("홈팀 선수들이 올바르게 로드되는지 확인")
    fun `홈팀 선수들이 올바르게 로드된다`() {
        // given
        val fixtureApiId = matchEntities.fixtureApiSports.apiId
        val playerContext = MatchPlayerContext()
        val entityBundle = MatchEntityBundle.createEmpty()
        
        // 디버깅: 생성된 데이터 확인
        log.info("생성된 홈팀 ID: {}", matchEntities.homeMatchTeam.id)
        log.info("생성된 원정팀 ID: {}", matchEntities.awayMatchTeam.id)
        log.info("생성된 선수들: {}", matchEntities.matchPlayers.map { "${it.name} (팀: ${it.matchTeam?.id})" })
        
        // when
        matchDataLoader.loadContext(fixtureApiId, playerContext, entityBundle)
        
        // 디버깅: 로드된 데이터 확인
        log.info("로드된 홈팀: {}", entityBundle.homeTeam?.id)
        log.info("로드된 원정팀: {}", entityBundle.awayTeam?.id)
        log.info("로드된 모든 선수들: {}", entityBundle.allMatchPlayers.map { "${it.name} (팀: ${it.matchTeam?.id})" })
        
        // then
        val homeTeamPlayers = entityBundle.allMatchPlayers.filter { it.matchTeam?.id == matchEntities.homeMatchTeam.id }
        assertThat(homeTeamPlayers).isNotEmpty()
        
        // 홈팀 선수들이 올바른 팀에 속해있는지 확인
        homeTeamPlayers.forEach { player ->
            assertThat(player.matchTeam?.id).isEqualTo(matchEntities.homeMatchTeam.id)
            assertThat(player.playerApiSports).isNotNull()
            assertThat(player.matchPlayerUid).isNotEmpty()
        }
        
        log.info("홈팀 선수 검증 완료 - 홈팀 선수 수: {}", homeTeamPlayers.size)
    }

    @Test
    @DisplayName("원정팀 선수들이 올바르게 로드되는지 확인")
    fun `원정팀 선수들이 올바르게 로드된다`() {
        // given
        val fixtureApiId = matchEntities.fixtureApiSports.apiId
        val playerContext = MatchPlayerContext()
        val entityBundle = MatchEntityBundle.createEmpty()
        
        // when
        matchDataLoader.loadContext(fixtureApiId, playerContext, entityBundle)
        
        // then
        val awayTeamPlayers = entityBundle.allMatchPlayers.filter { it.matchTeam?.id == matchEntities.awayMatchTeam.id }
        assertThat(awayTeamPlayers).isNotEmpty()
        
        // 원정팀 선수들이 올바른 팀에 속해있는지 확인
        awayTeamPlayers.forEach { player ->
            assertThat(player.matchTeam?.id).isEqualTo(matchEntities.awayMatchTeam.id)
            assertThat(player.playerApiSports).isNotNull()
            assertThat(player.matchPlayerUid).isNotEmpty()
        }
        
        log.info("원정팀 선수 검증 완료 - 원정팀 선수 수: {}", awayTeamPlayers.size)
    }

    @Test
    @DisplayName("이벤트에서 player와 assist 선수들이 중복 없이 로드되는지 확인")
    fun `이벤트에서 player와 assist 선수들이 중복 없이 로드된다`() {
        // given
        val fixtureApiId = matchEntities.fixtureApiSports.apiId
        val playerContext = MatchPlayerContext()
        val entityBundle = MatchEntityBundle.createEmpty()
        
        // when
        matchDataLoader.loadContext(fixtureApiId, playerContext, entityBundle)
        
        // then
        val eventPlayers = entityBundle.allEvents.flatMap { event ->
            listOfNotNull(event.player, event.assist)
        }
        
        // 이벤트 선수들이 allMatchPlayers에 포함되어 있는지 확인
        eventPlayers.forEach { eventPlayer ->
            val foundInAllPlayers = entityBundle.allMatchPlayers.any { it.id == eventPlayer.id }
            assertThat(foundInAllPlayers).isTrue()
        }
        
        // 중복 제거가 제대로 되었는지 확인 (ID 기준)
        val uniquePlayerIds = entityBundle.allMatchPlayers.map { it.id }.distinct()
        assertThat(uniquePlayerIds).hasSize(entityBundle.allMatchPlayers.size)
        
        log.info("이벤트 선수 중복 제거 검증 완료 - 이벤트 선수 수: {}, 전체 선수 수: {}", 
            eventPlayers.size, entityBundle.allMatchPlayers.size)
    }

    @Test
    @DisplayName("존재하지 않는 fixtureApiId에 대해 빈 결과 반환")
    fun `존재하지 않는 fixtureApiId에 대해 빈 결과를 반환한다`() {
        // given
        val nonExistentFixtureApiId = 999999L
        val playerContext = MatchPlayerContext()
        val entityBundle = MatchEntityBundle.createEmpty()
        
        // when
        matchDataLoader.loadContext(nonExistentFixtureApiId, playerContext, entityBundle)
        
        // then
        assertThat(entityBundle.fixture).isNull()
        assertThat(entityBundle.homeTeam).isNull()
        assertThat(entityBundle.awayTeam).isNull()
        assertThat(entityBundle.allMatchPlayers).isEmpty()
        assertThat(entityBundle.allEvents).isEmpty()
        
        log.info("존재하지 않는 fixture 처리 검증 완료")
    }

    @Test
    @DisplayName("Fixture와 연관된 모든 엔티티들이 올바른 관계를 가지고 있는지 확인")
    fun `Fixture와 연관된 모든 엔티티들이 올바른 관계를 가지고 있다`() {
        // given
        val fixtureApiId = matchEntities.fixtureApiSports.apiId
        val playerContext = MatchPlayerContext()
        val entityBundle = MatchEntityBundle.createEmpty()
        
        // when
        matchDataLoader.loadContext(fixtureApiId, playerContext, entityBundle)
        
        // then
        val fixture = entityBundle.fixture
        assertThat(fixture).isNotNull()
        
        // 홈팀과 원정팀이 올바른 fixture에 연결되어 있는지 확인
        assertThat(entityBundle.fixture?.homeTeam?.id).isEqualTo(entityBundle.homeTeam?.id)
        assertThat(entityBundle.fixture?.awayTeam?.id).isEqualTo(entityBundle.awayTeam?.id)
        
        // 모든 선수들이 올바른 팀에 연결되어 있는지 확인
        entityBundle.allMatchPlayers.forEach { player ->
            val isHomeTeamPlayer = entityBundle.homeTeam?.players?.any { it.id == player.id } == true
            val isAwayTeamPlayer = entityBundle.awayTeam?.players?.any { it.id == player.id } == true
            assertThat(isHomeTeamPlayer || isAwayTeamPlayer).isTrue()
        }
        
        // 모든 이벤트가 올바른 fixture에 연결되어 있는지 확인
        entityBundle.allEvents.forEach { event ->
            assertThat(event.fixtureApi?.id).isEqualTo(fixture?.id)
        }
        
        log.info("엔티티 관계 검증 완료")
    }
}