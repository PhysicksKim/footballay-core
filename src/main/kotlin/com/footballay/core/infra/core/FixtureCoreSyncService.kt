package com.footballay.core.infra.core

import com.footballay.core.infra.core.dto.FixtureCoreCreateDto
import com.footballay.core.infra.core.dto.FixtureCoreUpdateDto
import com.footballay.core.infra.persistence.core.entity.FixtureCore

/**
 * FixtureCore 엔티티 동기화를 위한 서비스 인터페이스
 * 
 * FixtureCore 엔티티의 생성, 업데이트를 배치로 처리합니다.
 * API 계층의 명세에 의존하지 않는 순수한 Core 계층 서비스입니다.
 */
interface FixtureCoreSyncService {

    /**
     * 요청 목록에 대해 UID를 생성하여 Pair로 매핑합니다.
     *
     * **Identity Pairing Pattern**을 구현하여 Domain Entity의 필수 Identity(UID)를
     * Domain Service가 책임지고 생성하고 매칭 전략을 지원합니다.
     *
     * ## 사용 배경
     *
     * 외부 시스템의 데이터(`FixtureAnother`)를 기반으로 `FixtureCore`를 생성할 때
     * 발생하는 매칭 문제를 해결하기 위해 도입되었습니다.
     *
     * ### Pairing 전략이 없는 경우의 문제점
     *
     * ```kotlin
     * // 단순히 DTO만 전달하는 경우
     * val createDtos = anotherFixtures.map { it.toCreateDto() }
     * val createdCores = service.createFixtureCores(createDtos)
     *
     * // 문제: 어떤 FixtureAnother가 어떤 FixtureCore와 대응되는지 불분명
     * // 데이터 내용으로 추론해야 하므로 불안정함
     * ```
     *
     * ### Pairing 전략 적용 시의 해결
     *
     * ```kotlin
     * // UID 페어링을 통한 명확한 매칭
     * val uidPairs = service.generateUidPairs(anotherFixtures)
     * val createPairs = uidPairs.map { (uid, another) ->
     *     uid to another.toCreateDto()
     * }
     * val createdCores = service.createFixtureCores(createPairs)
     *
     * // 결과: uid를 통해 정확한 매칭 보장
     * ```
     *
     * ## 아키텍처적 이점
     *
     * - **책임 분리**: UID 생성 로직이 Domain Service에 집중됨
     * - **의존성 역전**: 외부 호출자는 UID 생성 방식에 의존하지 않음
     * - **타입 안전성**: 제네릭을 통해 다양한 타입의 요청 처리 가능
     * - **매칭 보장**: 확실한 키를 통한 안정적인 데이터 매칭
     *
     * @param T 요청 객체의 타입 (임의의 타입 지원)
     * @param requests 처리할 요청 목록
     * @return UID와 요청 객체의 쌍 목록 (매칭 전략 지원)
     */
    fun <T> generateUidPairs(requests: List<T>): List<Pair<String, T>>
    
    /**
     * FixtureCore들을 배치로 생성합니다.
     * 
     * @param createPairs UID와 FixtureCore 생성 DTO의 쌍 목록
     * @return UID -> FixtureCore 맵 (영속 상태)
     */
    fun createFixtureCores(createPairs: List<Pair<String, FixtureCoreCreateDto>>): Map<String, FixtureCore>
    
    /**
     * FixtureCore들을 배치로 업데이트합니다.
     * 
     * @param updatePairs FixtureCore 엔티티와 업데이트 DTO의 쌍 목록
     * @return UID -> FixtureCore 맵 (영속 상태)
     */
    fun updateFixtureCores(updatePairs: List<Pair<FixtureCore, FixtureCoreUpdateDto>>): Map<String, FixtureCore>
} 