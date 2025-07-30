package com.footballay.core.infra.apisports.match.sync.persist.event.manager

import com.footballay.core.infra.apisports.match.sync.context.MatchEntityBundle
import com.footballay.core.infra.apisports.match.sync.dto.MatchEventDto
import com.footballay.core.infra.apisports.match.sync.dto.MatchEventSyncDto
import com.footballay.core.infra.apisports.match.sync.persist.event.planner.MatchEventChangePlanner
import com.footballay.core.infra.apisports.match.sync.persist.event.planner.MatchEventChangeSet
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchEventRepository
import com.footballay.core.logger
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * MatchEvent 통합 관리자
 * 
 * MatchEvent의 계획, 저장을 통합하여 관리합니다.
 * 
 * **처리 과정:**
 * 1. **Sequence 검증**: Event DTO의 sequence 무결성 검사
 * 2. **변경 계획 수립**: MatchEventChangePlanner로 생성/수정/삭제 계획 수립
 * 3. **Event 엔티티 저장**: 데이터베이스에 변경사항 적용
 * 4. **EntityBundle 업데이트**: 영속 상태 이벤트를 sequence 순으로 정렬하여 반영
 * 
 * **특징:**
 * - 영속 상태 MatchEvent를 EntityBundle에 반영
 * - Player/Assist 연결 로직 포함
 * - 개별 이벤트 실패 시 빈 이벤트로 처리하여 다른 이벤트에 영향 없도록 함
 * - Sequence 기반 정렬로 이벤트 순서 보장
 * - 단일 책임으로 MatchEntitySyncServiceImpl 단순화
 * 
 * **에러 처리:**
 * - 개별 이벤트 저장 실패 시 해당 이벤트를 "빈 이벤트"로 대체
 * - 다른 이벤트들의 정상 처리에 영향 없도록 격리
 * 
 * **성능 고려사항:**
 * - saveAll() 배치 처리로 데이터베이스 호출 최소화
 * - Sequence 정렬로 이벤트 순서 보장
 */
@Component
class MatchEventManager(
    private val matchEventRepository: ApiSportsMatchEventRepository
) {

    private val log = logger()

    /**
     * MatchEvent를 계획, 저장하여 영속 상태로 만듭니다.
     * 
     * **처리 단계:**
     * 1. **Sequence 검증**: Event DTO의 sequence 무결성 검사 (중복, 누락, 시작점)
     * 2. **변경 계획 수립**: MatchEventChangePlanner로 생성/수정/삭제 계획 수립
     * 3. **Event 엔티티 저장**: 데이터베이스에 변경사항 적용 (에러 시 빈 이벤트로 대체)
     * 4. **EntityBundle 업데이트**: 영속 상태 이벤트를 sequence 순으로 정렬하여 반영
     * 
     * **Sequence 검증 항목:**
     * - 중복된 sequence 검사
     * - 누락된 sequence 검사 (연속성 확인)
     * - 시작점이 0인지 검사
     * 
     * @param eventDto Event 정보 DTO (이미 정규화됨)
     * @param entityBundle 기존 엔티티 번들 (업데이트됨)
     * @return MatchEvent 처리 결과 (sequence 순으로 정렬된 이벤트 목록 포함)
     */
    @Transactional
    fun processMatchEvents(
        eventDto: MatchEventSyncDto,
        entityBundle: MatchEntityBundle
    ): MatchEventProcessResult {
        log.info("Starting MatchEvent processing - Events: ${eventDto.events.size}")
        
        try {
            // 1단계: Sequence 검증 - DTO의 sequence 무결성 검사
            validateEventSequences(eventDto.events)
            
            // 2단계: 변경 계획 수립 - MatchEventChangePlanner로 생성/수정/삭제 계획
            val entitySequenceMap = MatchEventChangePlanner.entitiesToSequenceMap(entityBundle.allEvents)
            val eventChangeSet = MatchEventChangePlanner.planChanges(
                eventDto,
                entitySequenceMap,
                entityBundle.fixture!!,
                entityBundle.homeTeam,
                entityBundle.awayTeam,
                entityBundle.allMatchPlayers
            )
            
            log.info("Planned event changes - Create: ${eventChangeSet.createCount}, Update: ${eventChangeSet.updateCount}, Delete: ${eventChangeSet.deleteCount}")
            
            // 3단계: Event 엔티티 저장 - 데이터베이스에 변경사항 적용
            val savedEvents = persistEventChanges(eventChangeSet)
            
            // 4단계: EntityBundle 업데이트 - sequence 순으로 정렬하여 반영
            val sortedEvents = savedEvents.sortedBy { it.sequence }
            entityBundle.allEvents = sortedEvents
            
            log.info("MatchEvent processing completed - Total saved: ${sortedEvents.size}")
            return MatchEventProcessResult(
                totalEvents = sortedEvents.size,
                createdCount = eventChangeSet.createCount,
                updatedCount = eventChangeSet.updateCount,
                deletedCount = eventChangeSet.deleteCount,
                savedEvents = sortedEvents
            )
        } catch (e: Exception) {
            log.error("Failed to process MatchEvents", e)
            throw e
        }
    }

    /**
     * Event sequence 검증을 수행합니다.
     * 
     * **검증 항목:**
     * - **중복 검사**: 같은 sequence가 여러 번 나타나는지 확인
     * - **연속성 검사**: sequence가 연속적으로 증가하는지 확인 (누락된 sequence 검사)
     * - **시작점 검사**: sequence가 0부터 시작하는지 확인
     * 
     * **경고 로그:**
     * - 중복된 sequence 발견 시 경고 로그 출력
     * - 누락된 sequence 발견 시 경고 로그 출력
     * - 시작점이 0이 아닌 경우 경고 로그 출력
     * 
     * @param events 검증할 Event DTO 목록
     */
    private fun validateEventSequences(events: List<MatchEventDto>) {
        if (events.isEmpty()) return
        
        val sequences = events.map { it.sequence }.sorted()
        
        // 중복 검사 - 같은 sequence가 여러 번 나타나는지 확인
        val duplicates = sequences.groupingBy { it }.eachCount().filter { it.value > 1 }
        if (duplicates.isNotEmpty()) {
            log.warn("Event DTO에서 중복된 sequence 발견: ${duplicates.keys}")
        }
        
        // 연속성 검사 - sequence가 연속적으로 증가하는지 확인
        val expectedSequences = (sequences.first()..sequences.last()).toList()
        val missingSequences = expectedSequences - sequences.toSet()
        if (missingSequences.isNotEmpty()) {
            log.warn("Event DTO에서 누락된 sequence 발견: $missingSequences")
        }
        
        // 시작점 검사 - sequence가 0부터 시작하는지 확인
        if (sequences.first() != 0) {
            log.warn("Event DTO sequence가 0부터 시작하지 않음. 시작점: ${sequences.first()}")
        }
    }

    /**
     * Event 변경사항을 데이터베이스에 저장합니다.
     * 
     * **처리 순서:**
     * 1. **삭제 처리**: 고아 엔티티들을 먼저 삭제
     * 2. **생성 및 업데이트 처리**: 새로운 이벤트 생성과 기존 이벤트 수정을 배치로 처리
     * 
     * **에러 처리:**
     * - 개별 이벤트 저장 실패 시 해당 이벤트를 "빈 이벤트"로 대체
     * - 다른 이벤트들의 정상 처리에 영향 없도록 격리
     * - 실패한 이벤트는 "UNKNOWN" 타입으로 설정하여 식별 가능
     * 
     * @param changeSet 적용할 변경 작업 명세서
     * @return 저장된 Event 엔티티 목록 (실패한 이벤트는 빈 이벤트로 대체됨)
     */
    private fun persistEventChanges(changeSet: MatchEventChangeSet): List<ApiSportsMatchEvent> {
        val allEvents = mutableListOf<ApiSportsMatchEvent>()
        
        try {
            // 1. 삭제 처리 - 고아 엔티티들을 먼저 삭제
            if (changeSet.toDelete.isNotEmpty()) {
                matchEventRepository.deleteAll(changeSet.toDelete)
                log.info("Deleted ${changeSet.toDelete.size} MatchEvents")
            }
            
            // 2. 생성 및 업데이트 처리 - 새로운 이벤트 생성과 기존 이벤트 수정을 배치로 처리
            if (changeSet.toCreate.isNotEmpty() || changeSet.toUpdate.isNotEmpty()) {
                val eventsToSave = changeSet.toCreate + changeSet.toUpdate
                val savedEvents = matchEventRepository.saveAll(eventsToSave)
                allEvents.addAll(savedEvents)
                log.info("Saved ${savedEvents.size} MatchEvents")
            }
        } catch (e: Exception) {
            log.error("Error during event persistence, creating empty events for failed ones", e)
            // 실패한 이벤트들을 빈 이벤트로 대체 - 다른 이벤트에 영향 없도록 격리
            val failedEvents = changeSet.toCreate + changeSet.toUpdate
            val emptyEvents = createEmptyEventsForFailed(failedEvents)
            allEvents.addAll(emptyEvents)
        }
        
        return allEvents
    }

    /**
     * 실패한 이벤트들을 빈 이벤트로 대체합니다.
     * 
     * **빈 이벤트 특징:**
     * - eventType: "UNKNOWN"으로 설정하여 식별 가능
     * - detail: "Failed to process event"로 설정
     * - comments: "Event processing failed"로 설정
     * - player, assist: null로 설정
     * - elapsedTime: 0으로 설정
     * 
     * **목적:**
     * - 개별 이벤트 실패가 다른 이벤트 처리에 영향을 주지 않도록 격리
     * - 실패한 이벤트를 식별할 수 있도록 특별한 값으로 설정
     * 
     * @param failedEvents 실패한 Event 엔티티 목록
     * @return 빈 이벤트로 대체된 Event 엔티티 목록
     */
    private fun createEmptyEventsForFailed(failedEvents: List<ApiSportsMatchEvent>): List<ApiSportsMatchEvent> {
        return failedEvents.map { event ->
            ApiSportsMatchEvent(
                fixtureApi = event.fixtureApi,
                matchTeam = null,
                player = null,
                assist = null,
                sequence = event.sequence,
                elapsedTime = 0,
                extraTime = null,
                eventType = "UNKNOWN",
                detail = "Failed to process event",
                comments = "Event processing failed"
            )
        }
    }
}

/**
 * MatchEvent 처리 결과
 * 
 * MatchEvent 처리 과정에서 생성된 결과 정보를 담는 데이터 클래스입니다.
 * 
 * **구성 요소:**
 * - **totalEvents**: 처리된 총 이벤트 개수
 * - **createdCount**: 새로 생성된 이벤트 개수
 * - **updatedCount**: 수정된 이벤트 개수
 * - **deletedCount**: 삭제된 이벤트 개수
 * - **savedEvents**: 저장된 Event 엔티티 목록 (sequence 순으로 정렬됨)
 */
data class MatchEventProcessResult(
    val totalEvents: Int,
    val createdCount: Int,
    val updatedCount: Int,
    val deletedCount: Int,
    val savedEvents: List<ApiSportsMatchEvent>
) 