package com.footballay.core.infra.query

import com.footballay.core.MatchConfig
import com.footballay.core.MatchEntityGenerator
import com.footballay.core.common.result.DomainFail
import com.footballay.core.common.result.DomainResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * MatchDataQueryService 통합 테스트
 *
 * **테스트 목적:**
 * - UID 기반 조회 동작 검증
 * - DomainResult 반환 검증
 * - Repository query method 정상 작동 확인
 * - 실제 DB 연동 흐름 검증
 *
 * **프로파일:**
 * - test: H2 in-memory DB 사용 (PostgreSQL 대신)
 * - 각 테스트는 @Transactional로 격리됨
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(MatchEntityGenerator::class)
@Transactional
class MatchDataQueryServiceImplTest {
    @Autowired
    private lateinit var queryService: MatchDataQueryServiceImpl

    @Autowired
    private lateinit var entityGenerator: MatchEntityGenerator

    @Test
    fun `getFixtureInfo - UID로 기본 정보 조회 성공`() {
        // Given: MatchEntityGenerator로 완전한 매치 생성
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val result = queryService.getFixtureInfo(fixtureUid)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val model = (result as DomainResult.Success).value
        assertThat(model.fixtureUid).isEqualTo(fixtureUid)
        assertThat(model.referee).isEqualTo("Michael Oliver")
        assertThat(model.league.name).isEqualTo("Test Premier League")
    }

    @Test
    fun `getFixtureLiveStatus - UID로 라이브 상태 조회 성공`() {
        // Given
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val result = queryService.getFixtureLiveStatus(fixtureUid)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val model = (result as DomainResult.Success).value
        assertThat(model.fixtureUid).isEqualTo(fixtureUid)
        assertThat(model.liveStatus.shortStatus).isEqualTo("NS")
    }

    @Test
    fun `getFixtureEvents - 이벤트 목록 조회 성공`() {
        // Given: Live 데이터 포함 매치 생성
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val result = queryService.getFixtureEvents(fixtureUid)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val model = (result as DomainResult.Success).value
        assertThat(model.events).isNotEmpty // MatchEntityGenerator가 이벤트 생성
        assertThat(model.events[0].type).isEqualTo("Goal")
        assertThat(model.events[0].elapsed).isEqualTo(25)
    }

    @Test
    fun `getFixtureLineup - 라인업 조회 성공`() {
        // Given: 라인업 포함 매치 생성
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val result = queryService.getFixtureLineup(fixtureUid)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val model = (result as DomainResult.Success).value
        assertThat(model.lineup.home.formation).isEqualTo("4-3-3")
        assertThat(model.lineup.home.players).isNotEmpty
        assertThat(model.lineup.away.formation).isEqualTo("4-2-3-1")
    }

    @Test
    fun `getFixtureStatistics - 통계 조회 성공`() {
        // Given: 통계 포함 매치 생성
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val result = queryService.getFixtureStatistics(fixtureUid)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val model = (result as DomainResult.Success).value
        assertThat(model.home.teamStatistics.shotsOnGoal).isEqualTo(5)
        assertThat(model.home.teamStatistics.ballPossession).isEqualTo(55)
    }

    @Test
    fun `getFixtureInfo - 존재하지 않는 UID DomainFail 반환`() {
        // Given
        val invalidUid = "invaliduid99999"

        // When
        val result = queryService.getFixtureInfo(invalidUid)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Fail::class.java)
        val error = (result as DomainResult.Fail).error
        assertThat(error).isInstanceOf(DomainFail.NotFound::class.java)
        assertThat((error as DomainFail.NotFound).id).isEqualTo(invalidUid)
    }

    @Test
    fun `getFixtureEvents - 빈 이벤트 목록 반환`() {
        // Given: Live 데이터 없는 매치 생성
        val config = MatchConfig(createLiveData = false)
        val entities = entityGenerator.createCompleteMatchEntities(config)
        val fixtureUid = entities.fixtureCore.uid

        // When
        val result = queryService.getFixtureEvents(fixtureUid)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val model = (result as DomainResult.Success).value
        assertThat(model.events).isEmpty()
    }
}
