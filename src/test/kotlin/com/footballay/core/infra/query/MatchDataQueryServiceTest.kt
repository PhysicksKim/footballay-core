package com.footballay.core.infra.query

import com.footballay.core.MatchConfig
import com.footballay.core.MatchEntityGenerator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
class MatchDataQueryServiceTest {
    @Autowired
    private lateinit var queryService: MatchDataQueryService

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
        assertThat(result).isNotNull
        assertThat(result.core?.uid).isEqualTo(fixtureUid)
        assertThat(result.referee).isEqualTo("Michael Oliver")
        assertThat(result.season).isNotNull
        assertThat(result.season?.leagueApiSports?.name).isEqualTo("Test Premier League")
    }

    @Test
    fun `getFixtureLiveStatus - UID로 라이브 상태 조회 성공`() {
        // Given
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val result = queryService.getFixtureLiveStatus(fixtureUid)

        // Then
        assertThat(result).isNotNull
        assertThat(result.core?.uid).isEqualTo(fixtureUid)
        assertThat(result.status?.shortStatus).isEqualTo("NS")
        assertThat(result.score).isNotNull
    }

    @Test
    fun `getFixtureEvents - 이벤트 목록 조회 성공`() {
        // Given: Live 데이터 포함 매치 생성
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val result = queryService.getFixtureEvents(fixtureUid)

        // Then
        assertThat(result).isNotEmpty // MatchEntityGenerator가 이벤트 생성
        assertThat(result[0].eventType).isEqualTo("goal")
        assertThat(result[0].elapsedTime).isEqualTo(25)
    }

    @Test
    fun `getFixtureLineup - 라인업 조회 성공`() {
        // Given: 라인업 포함 매치 생성
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val (homeResult, awayResult) = queryService.getFixtureLineup(fixtureUid)

        // Then
        assertThat(homeResult).isNotNull
        assertThat(homeResult?.homeTeam?.formation).isEqualTo("4-3-3")
        assertThat(homeResult?.homeTeam?.players).isNotEmpty

        assertThat(awayResult).isNotNull
        assertThat(awayResult?.awayTeam?.formation).isEqualTo("4-2-3-1")
    }

    @Test
    fun `getFixtureStatistics - 통계 조회 성공`() {
        // Given: 통계 포함 매치 생성
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val (homeResult, awayResult) = queryService.getFixtureStatistics(fixtureUid)

        // Then
        assertThat(homeResult).isNotNull
        assertThat(homeResult?.homeTeam?.teamStatistics).isNotNull
        assertThat(homeResult?.homeTeam?.teamStatistics?.shotsOnGoal).isEqualTo(5)
        assertThat(homeResult?.homeTeam?.teamStatistics?.ballPossession).isEqualTo("55%")

        assertThat(awayResult).isNotNull
        assertThat(awayResult?.awayTeam?.teamStatistics).isNotNull
    }

    @Test
    fun `getFixtureInfo - 존재하지 않는 UID 예외 발생`() {
        // Given
        val invalidUid = "apisports:999999999"

        // When & Then
        assertThatThrownBy { queryService.getFixtureInfo(invalidUid) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("not found")
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
        assertThat(result).isEmpty()
    }
}
