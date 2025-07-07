package com.footballay.core.infra.apisports

import com.footballay.core.infra.apisports.dto.TeamApiSportsCreateDto
import com.footballay.core.infra.apisports.dto.VenueApiSportsCreateDto
import com.footballay.core.infra.apisports.syncer.TeamApiSportsSyncer
import com.footballay.core.infra.core.util.UidGenerator
import com.footballay.core.infra.persistence.apisports.entity.LeagueApiSports
import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports
import com.footballay.core.infra.persistence.apisports.repository.LeagueApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.VenueApiSportsRepository
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.entity.LeagueTeamCore
import com.footballay.core.infra.persistence.core.repository.LeagueCoreRepository
import com.footballay.core.infra.persistence.core.repository.LeagueTeamCoreRepository
import com.footballay.core.infra.persistence.core.repository.TeamCoreRepository
import com.footballay.core.logger
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("dev", "mocks")
@Transactional
class TeamApiSportsSyncerIntegrationTest {

    val log = logger()

    @Autowired
    private lateinit var teamApiSportsSyncer: TeamApiSportsSyncer

    @Autowired
    private lateinit var leagueApiSportsRepository: LeagueApiSportsRepository

    @Autowired
    private lateinit var teamApiRepository: TeamApiSportsRepository

    @Autowired
    private lateinit var venueRepository: VenueApiSportsRepository

    @Autowired
    private lateinit var leagueCoreRepository: LeagueCoreRepository

    @Autowired
    private lateinit var teamCoreRepository: TeamCoreRepository

    @Autowired
    private lateinit var leagueTeamCoreRepository: LeagueTeamCoreRepository

    @Autowired
    private lateinit var uidGenerator: UidGenerator

    @PersistenceContext
    private lateinit var em: EntityManager

    private var leagueApiId: Long = 0
    private lateinit var leagueCore: LeagueCore
    private lateinit var leagueApiSports: LeagueApiSports

    @BeforeEach
    fun `테스트용 리그 데이터 초기화`() {
        log.info("테스트 시작: 기본 데이터 세팅")
        createTestLeague()
        log.info("기본 데이터 세팅 완료 - League ID: $leagueApiId, League Core: ${leagueCore.name}")
    }

    @Test
    fun `리그에 새로운 팀들을 추가하면 연관관계가 정상적으로 설정되어야 한다`() {
        // Given - 2개 팀의 DTO 준비
        val MANUTD = "Manchester United"
        val LIVERPOOL = "Liverpool FC"
        val teamApiDtos = listOf(
            createTeamDto(101L, MANUTD),
            createTeamDto(102L, LIVERPOOL)
        )
        log.info("테스트 시나리오: 새로운 팀 2개 추가")
        log.info("   - 입력 데이터: $MANUTD (API ID: 101), $LIVERPOOL (API ID: 102)")
        log.info("   - 현재 리그: ${leagueCore.name} (API ID: $leagueApiId)")

        // When - 팀 동기화 실행
        log.info("실행: saveTeamsOfLeague() 호출")
        teamApiSportsSyncer.saveTeamsOfLeague(leagueApiId, teamApiDtos)
        flushAndClear()

        // Then - 결과 검증
        log.info("검증 시작")
        
        // 1. TeamApiSports 엔티티 확인
        val teamApiSports = teamApiRepository.findAllByApiIdIn(listOf(101L, 102L))
        log.info("   1) TeamApiSports 엔티티 생성 확인: ${teamApiSports.size}개 (기대값: 2)")
        assertEquals(2, teamApiSports.size)

        // 2. TeamCore 엔티티 확인 (TeamApiSports를 통해 확인)
        val teamCores = teamApiSports.mapNotNull { it.teamCore }
        log.info("   2) TeamCore 엔티티 생성 확인: ${teamCores.size}개 (기대값: 2)")
        assertEquals(2, teamCores.size)

        // 3. LeagueCore-TeamCore 연관관계 확인 (Repository를 통한 직접 조회)
        val leagueTeamRelations = leagueTeamCoreRepository.findByLeagueId(leagueCore.id!!)
        log.info("   3) 리그-팀 연관관계 생성 확인: ${leagueTeamRelations.size}개 (기대값: 2)")
        assertEquals(2, leagueTeamRelations.size)
        
        val teamsInLeague = leagueTeamRelations.mapNotNull { it.team }
        val teamNames = teamsInLeague.map { it.name }
        log.info("   4) 리그에 속한 팀 이름 확인: ${teamNames.joinToString(", ")}")
        assertTrue(teamNames.contains(MANUTD))
        assertTrue(teamNames.contains(LIVERPOOL))
        
        log.info("테스트 통과: 새로운 팀 추가 및 연관관계 설정 성공")
    }

    @Test
    fun `기존 팀을 업데이트하고 새 팀을 추가하는 경우 모두 정상 처리되어야 한다`() {
        // Given - 기존 팀 데이터 준비
        log.info("테스트 시나리오: 기존 팀 업데이트 + 새 팀 추가")
        
        // 1. 기존 팀 Core/Api/연관관계 생성
        val teamUid = uidGenerator.generateUid()
        val existingTeamCore = TeamCore(
            uid = teamUid,
            name = "Manchester United (Old)",
            autoGenerated = true
        )
        val savedTeamCore = teamCoreRepository.save(existingTeamCore)
        log.info("   - 기존 TeamCore 생성: ${savedTeamCore.name} (UID: ${savedTeamCore.uid})")
        
        // 2. 리그-팀 연관관계 생성
        val leagueTeamCore = LeagueTeamCore(
            league = leagueCore,
            team = savedTeamCore
        )
        leagueTeamCoreRepository.save(leagueTeamCore)
        log.info("   - 리그-팀 연관관계 생성: ${leagueCore.name} <-> ${savedTeamCore.name}")

        // 3. TeamApiSports 생성
        val existingTeamApiSports = TeamApiSports(
            teamCore = savedTeamCore,
            apiId = 101L,
            name = "Manchester United (Old)",
            code = "MNU"
        )
        teamApiRepository.save(existingTeamApiSports)
        log.info("   - 기존 TeamApiSports 생성: API ID 101 '${existingTeamApiSports.name}'")

        // 4. 업데이트할 DTO 목록 준비 (기존 팀 업데이트 + 새 팀 추가)
        val teamDtos = listOf(
            createTeamDto(101L, "Manchester United (Updated)"), // 업데이트
            createTeamDto(102L, "Liverpool FC") // 새로 추가
        )
        log.info("   - 입력 DTO: 101 업데이트 → '${teamDtos[0].name}', 102 신규 → '${teamDtos[1].name}'")

        // When - 동기화 실행
        log.info("실행: 혼합 업데이트/추가 동기화 시작")
        teamApiSportsSyncer.saveTeamsOfLeague(leagueApiId, teamDtos)
        flushAndClear()

        // Then - 결과 검증
        log.info("검증 시작")
        
        // 1. 업데이트된 TeamApiSports 확인
        val updatedTeam = teamApiRepository.findTeamApiSportsByApiIdWithTeamCore(101L)
        assertNotNull(updatedTeam)
        assertEquals("Manchester United (Updated)", updatedTeam?.name)
        log.info("   1) 기존 팀 업데이트 확인: '${updatedTeam?.name}' (기대값: Manchester United (Updated))")

        // 2. 새로 추가된 팀 확인
        val newTeam = teamApiRepository.findTeamApiSportsByApiIdWithTeamCore(102L)
        assertNotNull(newTeam)
        assertEquals("Liverpool FC", newTeam?.name)
        log.info("   2) 새 팀 생성 확인: '${newTeam?.name}' (기대값: Liverpool FC)")

        // 3. LeagueCore-TeamCore 연관관계 확인
        val leagueTeamRelations = leagueTeamCoreRepository.findByLeagueId(leagueCore.id!!)
        assertEquals(2, leagueTeamRelations.size)
        log.info("   3) 최종 리그-팀 연관관계 수: ${leagueTeamRelations.size}개 (기대값: 2)")
        
        log.info("테스트 통과: 혼합 업데이트/추가 처리 성공")
    }

    @Test
    fun `DTO 목록에 없는 팀은 리그와의 연관관계가 제거되어야 한다`() {
        // Given - 기존에 2개 팀이 있는 상태에서 1개만 DTO에 포함
        log.info("테스트 시나리오: 연관관계 제거 (2개 팀 → 1개 팀)")
        
        val MANUTD = "Manchester United"
        val LIVERPOOL = "Liverpool FC"
        
        // 1. 기존 팀 2개 저장 (Core + Api + 연관관계)
        log.info("   - 기존 데이터 준비: 2개 팀을 리그에 연결")
        val team1 = saveTeamCoreAndApiWithRelation(101L, MANUTD)
        val team2 = saveTeamCoreAndApiWithRelation(102L, LIVERPOOL)
        log.info("     + $MANUTD (API ID: 101) 저장 완료")
        log.info("     + $LIVERPOOL (API ID: 102) 저장 완료")

        // 2. 한 팀만 포함된 DTO 목록 (102L Liverpool은 제거될 예정)
        val teamDtos = listOf(
            createTeamDto(101L, MANUTD)
        )
        log.info("   - 입력 DTO: $MANUTD (101)만 포함 → $LIVERPOOL (102)는 제거될 예정")

        // When - 동기화 실행
        log.info("실행: 부분 동기화 (일부 팀 제거)")
        teamApiSportsSyncer.saveTeamsOfLeague(leagueApiId, teamDtos)
        flushAndClear()

        // Then - 결과 검증
        log.info("검증 시작")
        
        // 1. 리그에 남아있는 팀 확인 (1개여야 함)
        val leagueTeamRelations = leagueTeamCoreRepository.findByLeagueId(leagueCore.id!!)
        val teamsInLeague = leagueTeamRelations.mapNotNull { it.team }
        log.info("   1) 리그에 남은 팀: ${teamsInLeague.map { it.name }} (기대값: [$MANUTD])")
        assertEquals(1, teamsInLeague.size)

        // 2. 제거된 팀이 여전히 존재하는지 확인 (엔티티는 삭제되지 않음)
        val removedTeamCore = teamApiRepository.findTeamApiSportsByApiIdWithTeamCore(102L)
        assertNotNull(removedTeamCore)
        log.info("   2) 제거된 팀 엔티티 존재 확인: '${removedTeamCore?.teamCore?.name}' (엔티티는 보존됨)")
        
        // 3. 연관관계만 제거되었는지 확인
        val removedTeamRelations = leagueTeamCoreRepository.findByLeagueIdAndTeamId(leagueCore.id!!, removedTeamCore!!.teamCore!!.id!!)
        assertEquals(0, removedTeamRelations.size)
        log.info("   3) 제거된 팀의 리그 연관관계: ${removedTeamRelations.size}개 (기대값: 0)")
        
        log.info("테스트 통과: 부분 제거 시 연관관계만 정확히 제거됨")
    }

    /**
     * 통합 테스트 4: 경기장(Venue) 정보가 팀 데이터와 함께 정상적으로 처리되는지 검증
     */
    @Test
    fun `경기장 정보가 팀 데이터와 함께 정상적으로 처리되어야 한다`() {
        // Given
        val venueDto = VenueApiSportsCreateDto(
            apiId = 201L,
            name = "Old Trafford",
            address = "Sir Matt Busby Way",
            city = "Manchester",
            capacity = 75000,
            surface = "grass",
            image = "venue-image-url"
        )

        val teamDtos = listOf(
            createTeamDto(101L, "Manchester United", venueDto)
        )

        // When
        teamApiSportsSyncer.saveTeamsOfLeague(leagueApiId, teamDtos)
        flushAndClear()

        // Then
        val savedTeam = teamApiRepository.findTeamApiSportsByApiIdWithTeamCore(101L)
        assertNotNull(savedTeam?.venue)
        assertEquals("Old Trafford", savedTeam?.venue?.name)
        assertEquals(75000, savedTeam?.venue?.capacity)
    }

    /**
     * 통합 테스트 5: TeamApiSports 엔티티는 존재하지만 TeamCore가 없는 비정상 케이스 처리 검증
     */
    @Test
    fun `TeamApiSports만 존재하고 TeamCore가 없는 경우 자동으로 생성되어야 한다`() {
        // Given
        // TeamCore 없는 TeamApiSports 생성
        val teamApiSportsWithoutCore = TeamApiSports(
            teamCore = null,
            apiId = 101L,
            name = "Manchester United",
            code = "MUN"
        )
        teamApiRepository.save(teamApiSportsWithoutCore)
        flushAndClear()

        val teamDtos = listOf(
            createTeamDto(101L, "Manchester United")
        )

        // When
        teamApiSportsSyncer.saveTeamsOfLeague(leagueApiId, teamDtos)
        flushAndClear()

        // Then
        val updatedTeam = teamApiRepository.findTeamApiSportsByApiIdWithTeamCore(101L)
        assertNotNull(updatedTeam?.teamCore)
        assertEquals("Manchester United", updatedTeam?.teamCore?.name)

        // LeagueCore-TeamCore 연관관계 확인
        val leagueTeamRelations = leagueTeamCoreRepository.findByLeagueId(leagueCore.id!!)
        assertEquals(1, leagueTeamRelations.size)
    }

    /**
     * 통합 테스트 6: 반복적인 업데이트 후에도 리그에 속한 팀 목록이 정확하게 유지되는지 검증
     */
    @Test
    fun `반복적인 업데이트 후에도 리그에 속한 팀 목록이 정확하게 유지되어야 한다`() {
        // Given - 초기 팀 3개 생성
        val initialTeams = listOf(
            createTeamDto(101L, "Manchester United"),
            createTeamDto(102L, "Liverpool FC"),
            createTeamDto(103L, "Arsenal FC")
        )
        teamApiSportsSyncer.saveTeamsOfLeague(leagueApiId, initialTeams)

        // 초기 상태 확인
        val initialLeagueTeamRelations = leagueTeamCoreRepository.findByLeagueId(leagueCore.id!!)
        assertEquals(3, initialLeagueTeamRelations.size)

        // When - 두 팀 제거하고 한 팀 추가 (101은 유지, 102/103 제거, 104 추가)
        val updatedTeams = listOf(
            createTeamDto(101L, "Manchester United"),
            createTeamDto(104L, "Chelsea FC")
        )
        teamApiSportsSyncer.saveTeamsOfLeague(leagueApiId, updatedTeams)
        flushAndClear()

        // Then - 리그에 2개의 팀만 있어야 함
        val updatedLeagueTeamRelations = leagueTeamCoreRepository.findByLeagueId(leagueCore.id!!)
        val updatedTeamsInLeague = updatedLeagueTeamRelations.mapNotNull { it.team }
        assertEquals(2, updatedTeamsInLeague.size)

        val apiTeams = teamApiRepository.findTeamApiSportsInTeamCore(updatedTeamsInLeague)

        // 정확한 팀이 남아있는지 확인
        val teamApiIds = apiTeams.mapNotNull { it.apiId }
        assertTrue(teamApiIds.contains(101L))
        assertTrue(teamApiIds.contains(104L))
        assertFalse(teamApiIds.contains(102L))
        assertFalse(teamApiIds.contains(103L))

        // 제거된 팀 엔티티는 여전히 존재하지만 리그와의 연관관계만 제거되었는지 확인
        val removedTeam = teamApiRepository.findTeamApiSportsByApiId(102L)
        assertNotNull(removedTeam)
        val removedTeamRelations = leagueTeamCoreRepository.findByLeagueIdAndTeamId(leagueCore.id!!, removedTeam!!.teamCore!!.id!!)
        assertEquals(0, removedTeamRelations.size)
    }

    /**
     * 테스트용 기본 리그 데이터 생성
     * 모든 테스트에서 공통으로 사용할 Premier League 데이터를 초기화합니다.
     */
    private fun createTestLeague() {
        log.info("기본 리그 데이터 생성 시작")
        
        // 1. LeagueCore 생성
        val leagueUid = uidGenerator.generateUid()
        if (!uidGenerator.isValidUid(leagueUid)) {
            throw IllegalStateException("생성된 리그 UID가 유효하지 않습니다: $leagueUid")
        }
        log.info("   - League UID 생성: $leagueUid")

        leagueCore = LeagueCore(
            uid = leagueUid,
            name = "Premier League",
            available = true,
            autoGenerated = false
        )
        val savedLeagueCore = leagueCoreRepository.save(leagueCore)
        log.info("   - LeagueCore 저장: '${savedLeagueCore.name}' (ID: ${savedLeagueCore.id})")

        // 2. LeagueApiSports 생성
        leagueApiId = 39L
        leagueApiSports = LeagueApiSports(
            leagueCore = savedLeagueCore,
            apiId = leagueApiId,
            name = "Premier League",
            type = "League",
            countryName = "England"
        )
        leagueApiSportsRepository.save(leagueApiSports)
        log.info("   - LeagueApiSports 저장: API ID $leagueApiId, 국가: England")

        flushAndClear()
        leagueCore = em.merge(leagueCore)
        leagueApiSports = em.merge(leagueApiSports)
        
        log.info("기본 리그 데이터 생성 완료 - 이제 팀 테스트 시작 가능")
    }

    private fun saveTeamCoreAndApiWithRelation(apiId: Long, name: String): TeamCore {
        // TeamCore 생성
        val teamUid = uidGenerator.generateUid() // 고유한 UID 생성
        log.info("teamUid 생성 확인: $teamUid")

        // UID 유효성 확인
        if (!uidGenerator.isValidUid(teamUid)) {
            throw IllegalStateException("생성된 UID가 유효하지 않습니다: $teamUid")
        }

        val teamCore = TeamCore(
            uid = teamUid,
            name = name,
            autoGenerated = true
        )
        val savedTeamCore = teamCoreRepository.save(teamCore)

        // 명시적으로 연관관계 생성
        val leagueTeamCore = LeagueTeamCore(
            league = leagueCore,
            team = savedTeamCore
        )
        leagueTeamCoreRepository.save(leagueTeamCore)

        // TeamApiSports 생성
        val teamApiSports = TeamApiSports(
            teamCore = savedTeamCore,
            apiId = apiId,
            name = name,
            code = name.take(3).uppercase()
        )
        teamApiRepository.save(teamApiSports)

        return savedTeamCore
    }

    private fun createTeamDto(
        apiId: Long,
        name: String,
        venue: VenueApiSportsCreateDto? = null
    ): TeamApiSportsCreateDto {
        return TeamApiSportsCreateDto(
            apiId = apiId,
            name = name,
            code = "${name.take(3).uppercase()}",
            country = "England",
            founded = 1900,
            national = false,
            logo = "https://example.com/logos/$apiId.png",
            venue = venue
        )
    }

    private inline fun flushAndClear() {
        em.flush()
        em.clear()
    }

    @Test
    fun `존재하지 않는 리그 API ID로 호출 시 예외가 발생해야 한다`() {
        // Given - 존재하지 않는 리그 ID로 요청
        log.info("테스트 시나리오: 예외 케이스 - 존재하지 않는 리그 ID")
        val nonExistentLeagueApiId = 9999L
        val teamDtos = listOf(createTeamDto(101L, "Manchester United"))
        log.info("   - 존재하지 않는 리그 API ID: $nonExistentLeagueApiId")
        log.info("   - 현재 시스템에 등록된 리그 API ID: $leagueApiId")

        // When & Then - 예외 발생 확인
        log.info("실행: 예외 발생 예상")
        val exception = assertThrows(IllegalArgumentException::class.java) {
            teamApiSportsSyncer.saveTeamsOfLeague(nonExistentLeagueApiId, teamDtos)
        }
        log.info("예상대로 예외 발생: ${exception.javaClass.simpleName} - '${exception.message}'")
    }

    @Test
    fun `LeagueApiSports는 존재하지만 LeagueCore가 없는 경우 예외가 발생해야 한다`() {
        // Given - 비정상적인 데이터 상태 (LeagueCore 없는 LeagueApiSports)
        log.info("테스트 시나리오: 예외 케이스 - LeagueCore 누락")
        
        val leagueApiSportsWithoutCore = LeagueApiSports(
            leagueCore = null, // Core 없음
            apiId = 999L,
            name = "Test League Without Core",
            type = "League",
            countryName = "Test Country"
        )
        leagueApiSportsRepository.save(leagueApiSportsWithoutCore)
        flushAndClear()
        log.info("   - 비정상 데이터 생성: API ID 999, LeagueCore=null")

        val teamDtos = listOf(createTeamDto(101L, "Manchester United"))
        log.info("   - 팀 DTO 준비: Manchester United")

        // When & Then - 예외 발생 확인
        log.info("실행: 데이터 무결성 검증 및 예외 발생 예상")
        val exception = assertThrows(IllegalArgumentException::class.java) {
            teamApiSportsSyncer.saveTeamsOfLeague(999L, teamDtos)
        }
        log.info("예상대로 데이터 무결성 예외 발생: ${exception.javaClass.simpleName} - '${exception.message}'")
    }

}
