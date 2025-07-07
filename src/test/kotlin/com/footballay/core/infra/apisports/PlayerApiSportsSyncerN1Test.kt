package com.footballay.core.infra.apisports

import com.footballay.core.infra.apisports.syncer.PlayerSyncRequest
import com.footballay.core.infra.apisports.syncer.PlayerApiSportsSyncer
import com.footballay.core.infra.persistence.core.entity.PlayerCore
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory

@SpringBootTest
@ActiveProfiles("dev", "mockapi")
@Transactional
class PlayerApiSportsSyncerN1Test {

    private val log = LoggerFactory.getLogger(PlayerApiSportsSyncerN1Test::class.java)

    @Autowired
    private lateinit var playerApiSportsSyncer: PlayerApiSportsSyncer

    @Test
    fun `syncPlayerWithoutTeam에서 N+1 문제 확인 - 새로운 선수들`() {
        log.info("=== 테스트 시작: 새로운 선수 3명 생성 ===")
        
        val newPlayers = listOf(
            PlayerSyncRequest(
                apiId = 99991L,
                name = "Test Player 1",
                position = "FW"
            ),
            PlayerSyncRequest(
                apiId = 99992L, 
                name = "Test Player 2",
                position = "MF"
            ),
            PlayerSyncRequest(
                apiId = 99993L,
                name = "Test Player 3", 
                position = "DF"
            )
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
        val initialPlayers = listOf(
            PlayerSyncRequest(
                apiId = 88881L,
                name = "Existing Player 1",
                position = "GK"
            ),
            PlayerSyncRequest(
                apiId = 88882L,
                name = "Existing Player 2", 
                position = "CB"
            )
        )
        
        log.info("=== 1단계: 초기 선수 생성 ===")
        playerApiSportsSyncer.syncPlayerWithoutTeam(initialPlayers)
        log.info("=== 1단계 완료 ===")

        // 2단계: 같은 선수들 업데이트
        val updatedPlayers = listOf(
            PlayerSyncRequest(
                apiId = 88881L,
                name = "Updated Player 1",
                position = "GK"
            ),
            PlayerSyncRequest(
                apiId = 88882L,
                name = "Updated Player 2",
                position = "CB"
            )
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

    @Test
    fun `현재 구현의 쿼리 패턴 분석`() {
        log.info("=== 현재 구현 쿼리 패턴 분석 시작 ===")
        
        val players = listOf(
            PlayerSyncRequest(apiId = 77771L, name = "Analysis Player 1"),
            PlayerSyncRequest(apiId = 77772L, name = "Analysis Player 2"), 
            PlayerSyncRequest(apiId = 77773L, name = "Analysis Player 3"),
            PlayerSyncRequest(apiId = 77774L, name = "Analysis Player 4"),
            PlayerSyncRequest(apiId = 77775L, name = "Analysis Player 5")
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
} 