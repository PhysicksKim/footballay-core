package com.footballay.core.infra.apisports.backbone.sync.player

import com.footballay.core.infra.apisports.backbone.sync.PlayerApiSportsCreateDto
import com.footballay.core.infra.persistence.apisports.entity.PlayerApiSports
import com.footballay.core.infra.persistence.apisports.entity.TeamApiSports
import com.footballay.core.infra.persistence.apisports.repository.PlayerApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.TeamApiSportsRepository
import com.footballay.core.infra.persistence.core.entity.PlayerCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import com.footballay.core.infra.persistence.core.entity.TeamPlayerCore
import com.footballay.core.infra.persistence.core.repository.PlayerCoreRepository
import com.footballay.core.infra.persistence.core.repository.TeamCoreRepository
import com.footballay.core.infra.persistence.core.repository.TeamPlayerCoreRepository
import com.footballay.core.infra.util.UidGenerator
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

/**
 * PlayerApiSportsWithCoreSyncSyncer 통합 테스트
 *
 * ## 테스트 범위
 * - 비즈니스 로직 검증: 선수 동기화 및 연관관계 관리
 * - 성능 테스트: N+1 문제 해결 및 배치 처리 검증
 * - 예외 처리: 잘못된 입력 및 엣지 케이스 처리
 * - 데이터 무결성: Core 시스템과의 연동 검증
 *
 * ## 주요 테스트 시나리오
 * - 새로운 선수 추가 및 팀 연관관계 설정
 * - 기존 선수 업데이트 및 새 선수 생성 혼재 처리
 * - 연관관계 제거 (DTO에 없는 선수 제거)
 * - 예외 상황 처리 (존재하지 않는 팀, Core 누락 등)
 * - 성능 최적화 검증 (N+1 문제 해결)
 *
 * @author Footballay Core Team
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class PlayerApiSportsWithCoreSyncerIntegrationTest {
    val log = logger()

    @Autowired
    private lateinit var playerApiSportsSyncer: PlayerApiSportsWithCoreSyncer

    @Autowired
    private lateinit var playerApiSportsRepository: PlayerApiSportsRepository

    @Autowired
    private lateinit var playerCoreRepository: PlayerCoreRepository

    @Autowired
    private lateinit var teamCoreRepository: TeamCoreRepository

    @Autowired
    private lateinit var teamPlayerCoreRepository: TeamPlayerCoreRepository

    @Autowired
    private lateinit var teamApiSportsRepository: TeamApiSportsRepository

    @Autowired
    private lateinit var uidGenerator: UidGenerator

    @PersistenceContext
    private lateinit var em: EntityManager

    private lateinit var testTeamCore: TeamCore
    private lateinit var testTeamApi: TeamApiSports

    @BeforeEach
    fun setUp() {
        log.info("=== PlayerApiSportsWithCoreSyncSyncer 통합 테스트 시작 ===")
        createTestTeam()
        log.info("테스트 팀 생성 완료: TeamCore(id=${testTeamCore.id}), TeamApiSports(apiId=${testTeamApi.apiId})")
    }

    /**
     * 통합 테스트 1: 팀에 새로운 선수들을 추가하고 연관관계가 정상적으로 설정되는지 검증
     */
    @Test
    fun `팀에 새로운 선수들을 추가하면 연관관계가 정상적으로 설정되어야 한다`() {
        log.info("=== 테스트: 새로운 선수 추가 및 연관관계 설정 ===")

        // Given
        val playerDtos =
            listOf(
                createPlayerDto(101L, "Son Heung-min", "Forward"),
                createPlayerDto(102L, "Harry Kane", "Forward"),
            )
        log.info("처리할 선수 DTO: ${playerDtos.map { "${it.name}(${it.apiId})" }}")

        // When
        val result = playerApiSportsSyncer.syncPlayersOfTeam(testTeamApi.apiId!!, playerDtos)
        flushAndClear()
        log.info("동기화 완료: ${result.size}개 선수 처리됨")

        // Then
        // 1. PlayerApiSports 엔티티 확인
        val savedPlayers = playerApiSportsRepository.findAllByApiIdIn(listOf(101L, 102L))
        assertEquals(2, savedPlayers.size)
        log.info("PlayerApiSports 엔티티 검증 완료: ${savedPlayers.size}개 저장됨")

        // 2. PlayerCore 엔티티 확인 (PlayerApiSports를 통해 확인)
        val playerApiSports = playerApiSportsRepository.findAllByApiIdIn(listOf(101L, 102L))
        val playerCores = playerApiSports.mapNotNull { it.playerCore }
        assertEquals(2, playerCores.size)
        log.info("PlayerCore 엔티티 검증 완료: ${playerCores.size}개 생성됨")

        // 3. TeamCore-PlayerCore 연관관계 확인 (Repository를 통한 직접 조회)
        val teamPlayerRelations = teamPlayerCoreRepository.findByTeamId(testTeamCore.id!!)
        assertEquals(2, teamPlayerRelations.size)

        val playersInTeam = teamPlayerRelations.mapNotNull { it.player }
        val playerNames = playersInTeam.map { it.name }
        assertTrue(playerNames.contains("Son Heung-min"))
        assertTrue(playerNames.contains("Harry Kane"))
        log.info("팀-선수 연관관계 검증 완료: ${teamPlayerRelations.size}개 관계 설정됨")
    }

    /**
     * 통합 테스트 2: 기존 선수를 업데이트하고 새 선수를 추가하는 경우 모두 정상 처리되는지 검증
     */
    @Test
    fun `기존 선수를 업데이트하고 새 선수를 추가하는 경우 모두 정상 처리되어야 한다`() {
        // Given - 기존 선수 데이터 준비
        log.info("테스트 시나리오: 기존 선수 업데이트 + 새 선수 추가")

        // 1. 기존 선수 Core/Api/연관관계 생성
        val playerUid = uidGenerator.generateUid()
        val existingPlayerCore =
            PlayerCore(
                uid = playerUid,
                name = "Son Heung-min (Old)",
                autoGenerated = true,
            )
        val savedPlayerCore = playerCoreRepository.save(existingPlayerCore)
        log.info("   - 기존 PlayerCore 생성: ${savedPlayerCore.name} (UID: ${savedPlayerCore.uid})")

        // 2. 팀-선수 연관관계 생성
        val teamPlayerCore =
            TeamPlayerCore(
                team = testTeamCore,
                player = savedPlayerCore,
            )
        teamPlayerCoreRepository.save(teamPlayerCore)
        log.info("   - 팀-선수 연관관계 생성: ${testTeamCore.name} <-> ${savedPlayerCore.name}")

        // 3. PlayerApiSports 생성
        val existingPlayerApiSports =
            PlayerApiSports(
                playerCore = savedPlayerCore,
                apiId = 101L,
                name = "Son Heung-min (Old)",
                position = "Forward",
            )
        playerApiSportsRepository.save(existingPlayerApiSports)
        log.info("   - 기존 PlayerApiSports 생성: API ID 101 '${existingPlayerApiSports.name}'")

        // 4. 업데이트할 DTO 목록 준비 (기존 선수 업데이트 + 새 선수 추가)
        val playerDtos =
            listOf(
                createPlayerDto(101L, "Son Heung-min (Updated)", "Forward"), // 업데이트
                createPlayerDto(102L, "Harry Kane"), // 새로 추가
            )
        log.info("   - 입력 DTO: 101 업데이트 → '${playerDtos[0].name}', 102 신규 → '${playerDtos[1].name}'")

        // When - 동기화 실행
        log.info("실행: 혼합 업데이트/추가 동기화 시작")
        playerApiSportsSyncer.syncPlayersOfTeam(testTeamApi.apiId!!, playerDtos)
        flushAndClear()

        // Then - 결과 검증
        log.info("검증 시작")

        // 1. 업데이트된 PlayerApiSports 확인
        val updatedPlayer = playerApiSportsRepository.findByApiId(101L)
        assertNotNull(updatedPlayer)
        assertEquals("Son Heung-min (Updated)", updatedPlayer?.name)
        log.info("   1) 기존 선수 업데이트 확인: '${updatedPlayer?.name}' (기대값: Son Heung-min (Updated))")

        // 2. 새로 추가된 선수 확인
        val newPlayer = playerApiSportsRepository.findByApiId(102L)
        assertNotNull(newPlayer)
        assertEquals("Harry Kane", newPlayer?.name)
        log.info("   2) 새 선수 생성 확인: '${newPlayer?.name}' (기대값: Harry Kane)")

        // 3. TeamCore-PlayerCore 연관관계 확인
        val teamPlayerRelations = teamPlayerCoreRepository.findByTeamId(testTeamCore.id!!)
        assertEquals(2, teamPlayerRelations.size)
        log.info("   3) 최종 팀-선수 연관관계 수: ${teamPlayerRelations.size}개 (기대값: 2)")

        log.info("테스트 통과: 혼합 업데이트/추가 처리 성공")
    }

    /**
     * 통합 테스트 3: DTO 목록에 없는 선수는 팀과의 연관관계가 제거되는지 검증
     */
    @Test
    fun `DTO 목록에 없는 선수는 팀과의 연관관계가 제거되어야 한다`() {
        // Given - 기존에 2개 선수가 있는 상태에서 1개만 DTO에 포함
        log.info("테스트 시나리오: 연관관계 제거 (2개 선수 → 1개 선수)")

        val SON = "Son Heung-min"
        val KANE = "Harry Kane"

        // 1. 기존 선수 2개 저장 (Core + Api + 연관관계)
        log.info("   - 기존 데이터 준비: 2개 선수를 팀에 연결")
        val player1 = savePlayerCoreAndApiWithRelation(101L, SON)
        val player2 = savePlayerCoreAndApiWithRelation(102L, KANE)

        // 2. 기존 연관관계 확인
        val initialRelations = teamPlayerCoreRepository.findByTeamId(testTeamCore.id!!)
        assertEquals(2, initialRelations.size)
        log.info("   - 초기 연관관계 수: ${initialRelations.size}개")

        // 3. DTO 목록에 1개만 포함 (Son만 유지, Kane 제거)
        val playerDtos = listOf(createPlayerDto(101L, SON))
        log.info("   - 입력 DTO: 1개 선수만 포함 (${playerDtos[0].name})")

        // When - 동기화 실행
        log.info("실행: 선수 목록 동기화 (연관관계 제거)")
        playerApiSportsSyncer.syncPlayersOfTeam(testTeamApi.apiId!!, playerDtos)
        flushAndClear()

        // Then - 결과 검증
        log.info("검증 시작")

        // 1. 최종 연관관계 확인 (1개만 남아있어야 함)
        val finalRelations = teamPlayerCoreRepository.findByTeamId(testTeamCore.id!!)
        assertEquals(1, finalRelations.size)
        log.info("   1) 최종 연관관계 수: ${finalRelations.size}개 (기대값: 1)")

        // 2. 남아있는 선수 확인 (Son만 남아있어야 함)
        val remainingPlayer = finalRelations.firstOrNull()?.player
        assertNotNull(remainingPlayer)
        assertEquals(SON, remainingPlayer?.name)
        log.info("   2) 남아있는 선수: '${remainingPlayer?.name}' (기대값: $SON)")

        log.info("테스트 통과: 연관관계 제거 성공")
    }

    /**
     * 통합 테스트 4: 존재하지 않는 팀 API ID로 호출 시 예외가 발생하는지 검증
     */
    @Test
    fun `존재하지 않는 팀 API ID로 호출 시 예외가 발생해야 한다`() {
        // Given - 존재하지 않는 팀 ID로 요청
        log.info("테스트 시나리오: 예외 케이스 - 존재하지 않는 팀 ID")
        val nonExistentTeamApiId = 9999L
        val playerDtos = listOf(createPlayerDto(101L, "Son Heung-min"))
        log.info("   - 존재하지 않는 팀 API ID: $nonExistentTeamApiId")
        log.info("   - 현재 시스템에 등록된 팀 API ID: ${testTeamApi.apiId}")

        // When & Then - 예외 발생 확인
        log.info("실행: 예외 발생 예상")
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                playerApiSportsSyncer.syncPlayersOfTeam(nonExistentTeamApiId, playerDtos)
            }
        log.info("예상대로 예외 발생: ${exception.javaClass.simpleName} - '${exception.message}'")
    }

    /**
     * 통합 테스트 5: TeamApiSports는 존재하지만 TeamCore가 없는 경우 예외가 발생하는지 검증
     */
    @Test
    fun `TeamApiSports는 존재하지만 TeamCore가 없는 경우 예외가 발생해야 한다`() {
        // Given - 비정상적인 데이터 상태 (TeamCore 없는 TeamApiSports)
        log.info("테스트 시나리오: 예외 케이스 - TeamCore 누락")

        val teamApiSportsWithoutCore =
            TeamApiSports(
                teamCore = null, // Core 없음
                apiId = 999L,
                name = "Test Team Without Core",
                code = "TWC",
                country = "Test Country",
            )
        teamApiSportsRepository.save(teamApiSportsWithoutCore)
        flushAndClear()
        log.info("   - 비정상 데이터 생성: API ID 999, TeamCore=null")

        val playerDtos = listOf(createPlayerDto(101L, "Son Heung-min"))
        log.info("   - 선수 DTO 준비: Son Heung-min")

        // When & Then - 예외 발생 확인
        log.info("실행: 데이터 무결성 검증 및 예외 발생 예상")
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                playerApiSportsSyncer.syncPlayersOfTeam(999L, playerDtos)
            }
        log.info("예상대로 데이터 무결성 예외 발생: ${exception.javaClass.simpleName} - '${exception.message}'")
    }

    // ===== 추가 테스트 케이스들 (Team 테스트에서 영감을 받아 추가) =====

    @Test
    fun `preventUpdate가 true인 선수는 업데이트되지 않아야 한다`() {
        // Given - preventUpdate가 true인 기존 선수
        log.info("테스트 시나리오: preventUpdate 플래그 테스트")

        val playerUid = uidGenerator.generateUid()
        val existingPlayerCore =
            PlayerCore(
                uid = playerUid,
                name = "Protected Player",
                autoGenerated = true,
            )
        val savedPlayerCore = playerCoreRepository.save(existingPlayerCore)

        val existingPlayerApiSports =
            PlayerApiSports(
                playerCore = savedPlayerCore,
                apiId = 101L,
                name = "Protected Player (Original)",
                position = "Forward",
                preventUpdate = true, // 업데이트 방지
            )
        playerApiSportsRepository.save(existingPlayerApiSports)
        flushAndClear()

        val playerDtos =
            listOf(
                createPlayerDto(101L, "Protected Player (Updated)", "Midfielder"),
            )
        log.info("   - preventUpdate=true인 선수에 업데이트 시도")

        // When
        playerApiSportsSyncer.syncPlayersOfTeam(testTeamApi.apiId!!, playerDtos)
        flushAndClear()

        // Then
        val updatedPlayer = playerApiSportsRepository.findByApiId(101L)
        assertNotNull(updatedPlayer)
        assertEquals("Protected Player (Original)", updatedPlayer?.name) // 원래 이름 유지
        assertEquals("Forward", updatedPlayer?.position) // 원래 포지션 유지
        log.info("   - preventUpdate 확인: 이름과 포지션이 업데이트되지 않음")
    }

    @Test
    fun `대량의 선수 데이터를 배치로 처리할 때 성능이 안정적으로 유지되어야 한다`() {
        // Given - 대량의 선수 데이터 (40명)
        log.info("테스트 시나리오: 대량 배치 처리 성능 테스트")

        val playerDtos =
            (1..40).map { index ->
                createPlayerDto(
                    apiId = (1000 + index).toLong(),
                    name = "Player $index",
                    position =
                        if (index % 4 ==
                            0
                        ) {
                            "Forward"
                        } else if (index % 4 == 1) {
                            "Midfielder"
                        } else if (index % 4 == 2) {
                            "Defender"
                        } else {
                            "Goalkeeper"
                        },
                )
            }
        log.info("   - 40명의 선수 데이터 준비")

        // When
        val startTime = System.currentTimeMillis()
        playerApiSportsSyncer.syncPlayersOfTeam(testTeamApi.apiId!!, playerDtos)
        val endTime = System.currentTimeMillis()
        val processingTime = endTime - startTime
        flushAndClear()

        // Then
        val savedPlayers = playerApiSportsRepository.findAllByApiIdIn(playerDtos.map { it.apiId!! })
        assertEquals(40, savedPlayers.size)

        val teamPlayerRelations = teamPlayerCoreRepository.findByTeamId(testTeamCore.id!!)
        assertEquals(40, teamPlayerRelations.size)

        log.info("   - 처리 시간: ${processingTime}ms")
        log.info("   - 성공적으로 40명의 선수를 배치 처리")
        assertTrue(processingTime < 5000) // 5초 이내 처리
    }

    @Test
    fun `null safety가 모든 상황에서 올바르게 동작해야 한다`() {
        // Given - null 값이 포함된 선수 데이터
        log.info("테스트 시나리오: null safety 테스트")

        val playerDtos =
            listOf(
                createPlayerDto(101L, "Son Heung-min", "Forward"),
                createPlayerDto(102L, null, null), // null 값들
                createPlayerDto(103L, "Harry Kane", "Forward"),
            )
        log.info("   - null 값이 포함된 선수 데이터 준비")

        // When
        playerApiSportsSyncer.syncPlayersOfTeam(testTeamApi.apiId!!, playerDtos)
        flushAndClear()

        // Then
        val savedPlayers = playerApiSportsRepository.findAllByApiIdIn(listOf(101L, 102L, 103L))
        assertEquals(3, savedPlayers.size)

        val playerNames = savedPlayers.map { it.name }
        assertTrue(playerNames.contains("Son Heung-min"))
        assertTrue(playerNames.contains("Harry Kane"))
        // null name은 실제로는 null로 저장되므로 "Unknown" 대신 null 체크
        val nullNamePlayer = savedPlayers.find { it.apiId == 102L }
        assertNotNull(nullNamePlayer)
        assertNull(nullNamePlayer?.name)

        log.info("   - null safety 확인: 모든 선수가 정상적으로 처리됨")
    }

    @Test
    fun `반복적인 업데이트 후에도 팀에 속한 선수 목록이 정확하게 유지되어야 한다`() {
        // Given - 초기 선수 3개 생성
        val initialPlayers =
            listOf(
                createPlayerDto(101L, "Son Heung-min"),
                createPlayerDto(102L, "Harry Kane"),
                createPlayerDto(103L, "Dejan Kulusevski"),
            )
        playerApiSportsSyncer.syncPlayersOfTeam(testTeamApi.apiId!!, initialPlayers)

        // 초기 상태 확인
        val initialTeamPlayerRelations = teamPlayerCoreRepository.findByTeamId(testTeamCore.id!!)
        assertEquals(3, initialTeamPlayerRelations.size)

        // When - 두 선수 제거하고 한 선수 추가 (101은 유지, 102/103 제거, 104 추가)
        val updatedPlayers =
            listOf(
                createPlayerDto(101L, "Son Heung-min"),
                createPlayerDto(104L, "Richarlison"),
            )
        playerApiSportsSyncer.syncPlayersOfTeam(testTeamApi.apiId!!, updatedPlayers)
        flushAndClear()

        // Then - 팀에 2개의 선수만 있어야 함
        val updatedTeamPlayerRelations = teamPlayerCoreRepository.findByTeamId(testTeamCore.id!!)
        val updatedPlayersInTeam = updatedTeamPlayerRelations.mapNotNull { it.player }
        assertEquals(2, updatedPlayersInTeam.size)

        val apiPlayers = playerApiSportsRepository.findAllByApiIdIn(listOf(101L, 104L))

        // 정확한 선수가 남아있는지 확인
        val playerApiIds = apiPlayers.mapNotNull { it.apiId }
        assertTrue(playerApiIds.contains(101L))
        assertTrue(playerApiIds.contains(104L))
        assertFalse(playerApiIds.contains(102L))
        assertFalse(playerApiIds.contains(103L))

        // 제거된 선수 엔티티는 여전히 존재하지만 팀과의 연관관계만 제거되었는지 확인
        val removedPlayer = playerApiSportsRepository.findByApiId(102L)
        assertNotNull(removedPlayer)
        val removedPlayerRelations =
            teamPlayerCoreRepository.findByTeamIdAndPlayerId(
                testTeamCore.id!!,
                removedPlayer!!.playerCore!!.id!!,
            )
        assertEquals(0, removedPlayerRelations.size)
    }

    @Test
    fun `PlayerApiSports만 존재하고 PlayerCore가 없는 경우 자동으로 생성되어야 한다`() {
        // Given
        // PlayerCore 없는 PlayerApiSports 생성
        val playerApiSportsWithoutCore =
            PlayerApiSports(
                playerCore = null,
                apiId = 101L,
                name = "Son Heung-min",
                position = "Forward",
            )
        playerApiSportsRepository.save(playerApiSportsWithoutCore)
        flushAndClear()

        val playerDtos =
            listOf(
                createPlayerDto(101L, "Son Heung-min"),
            )

        // When
        playerApiSportsSyncer.syncPlayersOfTeam(testTeamApi.apiId!!, playerDtos)
        flushAndClear()

        // Then
        val updatedPlayer = playerApiSportsRepository.findByApiId(101L)
        assertNotNull(updatedPlayer?.playerCore)
        assertEquals("Son Heung-min", updatedPlayer?.playerCore?.name)

        // TeamCore-PlayerCore 연관관계 확인
        val teamPlayerRelations = teamPlayerCoreRepository.findByTeamId(testTeamCore.id!!)
        assertEquals(1, teamPlayerRelations.size)
    }

    @Test
    fun `같은 팀에 속한 여러 선수가 모두 올바르게 연결되어야 한다`() {
        // Given - 같은 팀에 속하는 5개 선수
        log.info("테스트 시나리오: 팀 선수 연결 - 같은 팀에 속하는 여러 선수")

        val playerDtos =
            listOf(
                createPlayerDto(101L, "Son Heung-min", "Forward"),
                createPlayerDto(102L, "Harry Kane", "Forward"),
                createPlayerDto(103L, "Dejan Kulusevski", "Midfielder"),
                createPlayerDto(104L, "Cristian Romero", "Defender"),
                createPlayerDto(105L, "Guglielmo Vicario", "Goalkeeper"),
            )
        log.info("   - 5개 선수가 모두 같은 팀(Tottenham)에 속함")
        log.info("   - 선수: Son Heung-min, Harry Kane, Dejan Kulusevski, Cristian Romero, Guglielmo Vicario")

        // When
        log.info("실행: 팀 선수들 동기화")
        playerApiSportsSyncer.syncPlayersOfTeam(testTeamApi.apiId!!, playerDtos)
        flushAndClear()

        // Then
        log.info("검증 시작")

        // 1. 모든 선수가 생성되었는지 확인
        val allPlayers = playerApiSportsRepository.findAllByApiIdIn(listOf(101L, 102L, 103L, 104L, 105L))
        assertEquals(5, allPlayers.size)
        log.info("   1) 모든 선수 생성 확인: ${allPlayers.size}개 (기대값: 5)")

        // 2. 모든 선수가 같은 팀을 참조하는지 확인
        val teamPlayerRelations = teamPlayerCoreRepository.findByTeamId(testTeamCore.id!!)
        assertEquals(5, teamPlayerRelations.size)
        log.info("   2) 팀 연결 확인: ${teamPlayerRelations.size}개의 선수-팀 관계 (기대값: 5)")

        // 3. 모든 선수의 팀이 동일한 객체인지 확인 (메모리 주소 확인)
        val teamIds = teamPlayerRelations.mapNotNull { it.team?.id }.distinct()
        assertEquals(1, teamIds.size)
        log.info("   3) 팀 객체 공유 확인: ${teamIds.size}개의 고유 팀 ID (기대값: 1)")

        log.info("테스트 통과: 팀 선수 연결이 올바르게 처리됨")
    }

    @Test
    fun `선수 정보가 없는 팀과 있는 팀이 혼재되어도 정상 처리되어야 한다`() {
        // Given - 선수 있는 팀과 없는 팀 혼재 (실제로는 같은 팀이지만 선수 수가 다른 경우)
        log.info("테스트 시나리오: 선수 혼재 - 있는 팀과 없는 팀")

        // 먼저 빈 팀 상태로 시작
        val emptyPlayerDtos = emptyList<PlayerApiSportsCreateDto>()
        playerApiSportsSyncer.syncPlayersOfTeam(testTeamApi.apiId!!, emptyPlayerDtos)
        flushAndClear()

        // 그 다음 선수들이 있는 상태로 업데이트
        val playerDtos =
            listOf(
                createPlayerDto(101L, "Son Heung-min", "Forward"),
                createPlayerDto(102L, "Harry Kane", "Forward"),
                createPlayerDto(103L, "Dejan Kulusevski", "Midfielder"),
            )
        log.info("   - 선수 있는 팀: Tottenham Hotspur (3명의 선수)")
        log.info("   - 선수 없는 팀: 같은 팀이지만 선수 수가 0명에서 3명으로 변경")

        // When
        log.info("실행: 선수 혼재 팀들 동기화")
        playerApiSportsSyncer.syncPlayersOfTeam(testTeamApi.apiId!!, playerDtos)
        flushAndClear()

        // Then
        log.info("검증 시작")

        // 1. 모든 선수가 생성되었는지 확인
        val allPlayers = playerApiSportsRepository.findAllByApiIdIn(listOf(101L, 102L, 103L))
        assertEquals(3, allPlayers.size)
        log.info("   1) 모든 선수 생성 확인: ${allPlayers.size}개 (기대값: 3)")

        // 2. 팀-선수 연관관계 확인
        val teamPlayerRelations = teamPlayerCoreRepository.findByTeamId(testTeamCore.id!!)
        assertEquals(3, teamPlayerRelations.size)
        log.info("   2) 팀-선수 연관관계: ${teamPlayerRelations.size}개 (기대값: 3)")

        // 3. 선수 이름 확인
        val playerNames = allPlayers.map { it.name }
        assertTrue(playerNames.contains("Son Heung-min"))
        assertTrue(playerNames.contains("Harry Kane"))
        assertTrue(playerNames.contains("Dejan Kulusevski"))
        log.info("   3) 선수 이름 확인: ${playerNames.joinToString(", ")}")

        log.info("테스트 통과: 선수 혼재가 올바르게 처리됨")
    }

    @Test
    fun `기존 선수 업데이트와 새 선수 생성이 혼재되어도 정상 처리되어야 한다`() {
        // Given - 기존 선수가 있는 팀과 새 선수가 필요한 팀
        log.info("테스트 시나리오: 선수 혼재 업데이트/생성")

        // 1. 기존 선수가 있는 팀 생성
        val existingPlayer =
            PlayerApiSports(
                apiId = 101L,
                name = "Old Player",
                position = "Forward",
            )
        val savedExistingPlayer = playerApiSportsRepository.save(existingPlayer)
        flushAndClear()
        log.info("   - 기존 선수 생성: Old Player (Position: Forward)")

        // 2. 업데이트할 DTO와 새 선수가 필요한 DTO
        val updatedPlayerDto = createPlayerDto(101L, "Updated Player", "Midfielder") // 기존 선수 업데이트
        val newPlayerDto = createPlayerDto(102L, "New Player", "Defender") // 새 선수 생성

        val playerDtos =
            listOf(
                updatedPlayerDto, // 기존 선수 업데이트
                newPlayerDto, // 새 선수 생성
            )
        log.info("   - 기존 선수 업데이트: Updated Player")
        log.info("   - 새 선수 생성: New Player")

        // When
        log.info("실행: 선수 혼재 업데이트/생성 동기화")
        playerApiSportsSyncer.syncPlayersOfTeam(testTeamApi.apiId!!, playerDtos)
        flushAndClear()

        // Then
        log.info("검증 시작")

        // 1. 기존 선수가 업데이트되었는지 확인
        val updatedPlayer = playerApiSportsRepository.findByApiId(101L)
        assertNotNull(updatedPlayer)
        assertEquals("Updated Player", updatedPlayer?.name)
        assertEquals("Midfielder", updatedPlayer?.position)
        log.info("   1) 기존 선수 업데이트 확인: ${updatedPlayer?.name}")

        // 2. 새 선수가 생성되었는지 확인
        val newPlayer = playerApiSportsRepository.findByApiId(102L)
        assertNotNull(newPlayer)
        assertEquals("New Player", newPlayer?.name)
        log.info("   2) 새 선수 생성 확인: ${newPlayer?.name}")

        // 3. 선수 개수 확인 (기존 1개 + 새로 1개 = 2개)
        val allPlayers = playerApiSportsRepository.findAllByApiIdIn(listOf(101L, 102L))
        assertEquals(2, allPlayers.size)
        log.info("   3) 총 선수 개수: ${allPlayers.size}개 (기대값: 2)")

        log.info("테스트 통과: 선수 혼재 업데이트/생성이 올바르게 처리됨")
    }

    // ===== N+1 문제 분석 테스트 케이스들 =====

    @Test
    fun `현재 구현의 쿼리 패턴 분석`() {
        log.info("=== 현재 구현 쿼리 패턴 분석 시작 ===")

        val players =
            listOf(
                createPlayerDto(77771L, "Analysis Player 1"),
                createPlayerDto(77772L, "Analysis Player 2"),
                createPlayerDto(77773L, "Analysis Player 3"),
                createPlayerDto(77774L, "Analysis Player 4"),
                createPlayerDto(77775L, "Analysis Player 5"),
            )

        log.info("=== 선수 5명 동기화 시작 ===")
        val result = playerApiSportsSyncer.syncPlayerWithoutTeam(players)
        log.info("=== 선수 5명 동기화 완료 ===")

        log.info("=== 결과 PlayerCore들에 순차 접근 ===")
        result.forEach { playerCore ->
            log.info("PlayerCore: ${playerCore.id} - ${playerCore.name}")
        }
        log.info("=== 순차 접근 완료 ===")
    }

    @Test
    fun `syncPlayerWithoutTeam에서 N+1 문제 확인 - 새로운 선수들`() {
        log.info("=== 테스트 시작: 새로운 선수 3명 생성 ===")

        val newPlayers =
            listOf(
                createPlayerDto(99991L, "Test Player 1", "FW"),
                createPlayerDto(99992L, "Test Player 2", "MF"),
                createPlayerDto(99993L, "Test Player 3", "DF"),
            )

        log.info("=== syncPlayerWithoutTeam 호출 시작 ===")
        val result: List<PlayerCore> = playerApiSportsSyncer.syncPlayerWithoutTeam(newPlayers)
        log.info("=== syncPlayerWithoutTeam 호출 완료 ===")

        log.info("=== PlayerCore 결과 접근 시작 ===")
        result.forEachIndexed { index, playerCore ->
            log.info("[$index] PlayerCore 접근: id=${playerCore.id}, name=${playerCore.name}")

            // PlayerCore의 역방향 관계 접근 시도 (N+1 확인)
            log.info("[$index] PlayerCore uid 접근: ${playerCore.uid}")
            log.info("[$index] PlayerCore position 접근: ${playerCore.position}")
        }
        log.info("=== PlayerCore 결과 접근 완료 ===")

        log.info("결과 개수: ${result.size}")
    }

    @Test
    fun `syncPlayerWithoutTeam에서 N+1 문제 확인 - 기존 선수들 업데이트`() {
        log.info("=== 테스트 시작: 기존 선수들 먼저 생성 ===")

        // 1단계: 선수들 먼저 생성
        val initialPlayers =
            listOf(
                createPlayerDto(88881L, "Existing Player 1", "GK"),
                createPlayerDto(88882L, "Existing Player 2", "CB"),
            )

        log.info("=== 1단계: 초기 선수 생성 ===")
        playerApiSportsSyncer.syncPlayerWithoutTeam(initialPlayers)
        log.info("=== 1단계 완료 ===")

        // 2단계: 같은 선수들 업데이트
        val updatedPlayers =
            listOf(
                createPlayerDto(88881L, "Updated Player 1", "GK"),
                createPlayerDto(88882L, "Updated Player 2", "CB"),
            )

        log.info("=== 2단계: 기존 선수 업데이트 시작 ===")
        val result: List<PlayerCore> = playerApiSportsSyncer.syncPlayerWithoutTeam(updatedPlayers)
        log.info("=== 2단계: 기존 선수 업데이트 완료 ===")

        log.info("=== PlayerCore 결과 접근 시작 ===")
        result.forEachIndexed { index, playerCore ->
            log.info("[$index] PlayerCore 접근: id=${playerCore.id}, name=${playerCore.name}")
            log.info("[$index] PlayerCore uid 접근: ${playerCore.uid}")
        }
        log.info("=== PlayerCore 결과 접근 완료 ===")

        log.info("결과 개수: ${result.size}")
    }

    // ===== 헬퍼 메서드들 =====

    private fun createTestTeam() {
        // TeamCore 생성
        testTeamCore =
            TeamCore(
                uid = uidGenerator.generateUid(),
                name = "Tottenham Hotspur",
                autoGenerated = true,
            )
        testTeamCore = teamCoreRepository.save(testTeamCore)

        // TeamApiSports 생성
        testTeamApi =
            TeamApiSports(
                teamCore = testTeamCore,
                apiId = 1L,
                name = "Tottenham Hotspur",
                code = "TOT",
                country = "England",
            )
        testTeamApi = teamApiSportsRepository.save(testTeamApi)

        log.info("테스트 팀 생성 완료: ${testTeamCore.name} (API ID: ${testTeamApi.apiId})")
    }

    private fun createPlayerDto(
        apiId: Long,
        name: String?,
        position: String? = null,
    ): PlayerApiSportsCreateDto =
        PlayerApiSportsCreateDto(
            apiId = apiId,
            name = name,
            firstname = name?.split(" ")?.firstOrNull(),
            lastname = name?.split(" ")?.lastOrNull(),
            age = 25,
            nationality = "South Korea",
            position = position,
            number = 7,
            photo = "son.jpg",
        )

    private fun savePlayerCoreAndApiWithRelation(
        apiId: Long,
        name: String,
    ): PlayerApiSports {
        // PlayerCore 생성
        val playerCore =
            PlayerCore(
                uid = uidGenerator.generateUid(),
                name = name,
                autoGenerated = true,
            )
        val savedPlayerCore = playerCoreRepository.save(playerCore)

        // PlayerApiSports 생성
        val playerApiSports =
            PlayerApiSports(
                playerCore = savedPlayerCore,
                apiId = apiId,
                name = name,
                position = "Forward",
            )
        val savedPlayerApiSports = playerApiSportsRepository.save(playerApiSports)

        // Team-Player 연관관계 생성
        val teamPlayerCore =
            TeamPlayerCore(
                team = testTeamCore,
                player = savedPlayerCore,
            )
        teamPlayerCoreRepository.save(teamPlayerCore)

        return savedPlayerApiSports
    }

    private inline fun flushAndClear() {
        em.flush()
        em.clear()
    }
}
