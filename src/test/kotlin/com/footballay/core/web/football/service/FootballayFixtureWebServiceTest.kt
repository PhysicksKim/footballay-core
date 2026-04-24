package com.footballay.core.web.football.service

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
 * 전체 API 흐름 검증 (WebService → QueryService → Mapper → Repository)
 *
 * DomainResult 구조: Success(value) | Fail(error)
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(MatchEntityGenerator::class)
@Transactional
class FootballayFixtureWebServiceTest {
    @Autowired
    private lateinit var webService: FixtureWebService

    @Autowired
    private lateinit var entityGenerator: MatchEntityGenerator

    @Test
    fun `getFixtureInfo - 성공 응답 반환`() {
        // Given
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val result = webService.getFixtureInfo(fixtureUid)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Success::class.java)
        val data = result.getOrNull()!!
        assertThat(data.fixtureUid).isEqualTo(fixtureUid)
        assertThat(data.home?.name).isEqualTo("Manchester United")
        assertThat(data.away?.name).isEqualTo("Arsenal")
        assertThat(data.referee).isEqualTo("Michael Oliver")
    }

    @Test
    fun `getFixtureLiveStatus - 성공 응답 반환`() {
        // Given
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val result = webService.getFixtureLiveStatus(fixtureUid, null)

        // Then
        assertThat(result).isInstanceOf(FixtureWebResult.Ok::class.java)
        val data = result as FixtureWebResult.Ok
        assertThat(data.snapshotJson).contains(""""shortStatus":"NS"""")
        assertThat(data.etagHash).isNotBlank()
    }

    @Test
    fun `getFixtureEvents - 성공 응답 반환`() {
        // Given: Live 데이터 포함 매치
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val result = webService.getFixtureEvents(fixtureUid, null)

        // Then
        assertThat(result).isInstanceOf(FixtureWebResult.Ok::class.java)
        val data = result as FixtureWebResult.Ok
        assertThat(data.snapshotJson).contains("Goal")
        assertThat(data.etagHash).isNotBlank()
    }

    @Test
    fun `getFixtureEvents - 빈 이벤트 목록 성공 응답`() {
        // Given: Live 데이터 없는 매치
        val config = MatchConfig(createLiveData = false)
        val entities = entityGenerator.createCompleteMatchEntities(config)
        val fixtureUid = entities.fixtureCore.uid

        // When
        val result = webService.getFixtureEvents(fixtureUid, null)

        // Then
        assertThat(result).isInstanceOf(FixtureWebResult.Ok::class.java)
        val data = result as FixtureWebResult.Ok
        assertThat(data.snapshotJson).contains(""""events":[]""")
    }

    @Test
    fun `getFixtureLineup - 성공 응답 반환`() {
        // Given
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val result = webService.getFixtureLineup(fixtureUid, null)

        // Then
        assertThat(result).isInstanceOf(FixtureWebResult.Ok::class.java)
        val data = result as FixtureWebResult.Ok
        assertThat(data.snapshotJson).contains("Manchester United")
        assertThat(data.snapshotJson).contains("4-3-3")
        assertThat(data.snapshotJson).contains("Arsenal")
        assertThat(data.snapshotJson).contains("4-2-3-1")
    }

    @Test
    fun `getFixtureStatistics - 성공 응답 반환`() {
        // Given
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val result = webService.getFixtureStatistics(fixtureUid, null)

        // Then
        assertThat(result).isInstanceOf(FixtureWebResult.Ok::class.java)
        val data = result as FixtureWebResult.Ok
        assertThat(data.snapshotJson).contains(fixtureUid)
        assertThat(data.snapshotJson).contains(""""shotsOnGoal":5""")
        assertThat(data.snapshotJson).contains(""""ballPossession":55""")
    }

    @Test
    fun `getFixtureInfo - 존재하지 않는 UID 실패 응답`() {
        // Given
        val invalidUid = "invaliduid99999"

        // When
        val result = webService.getFixtureInfo(invalidUid)

        // Then
        assertThat(result).isInstanceOf(DomainResult.Fail::class.java)
        val error = result.errorOrNull()!!
        assertThat(error).isInstanceOf(DomainFail.NotFound::class.java)
        val notFoundError = error as DomainFail.NotFound
        assertThat(notFoundError.resource).contains("Fixture")
        assertThat(notFoundError.id).isEqualTo(invalidUid)
    }

    @Test
    fun `getFixtureLiveStatus - 존재하지 않는 UID 실패 응답`() {
        // Given
        val invalidUid = "invaliduid99999"

        // When
        val result = webService.getFixtureLiveStatus(invalidUid, null)

        // Then
        assertThat(result).isInstanceOf(FixtureWebResult.Fail::class.java)
        val error = (result as FixtureWebResult.Fail).error
        assertThat(error).isInstanceOf(DomainFail.NotFound::class.java)
        val notFoundError = error as DomainFail.NotFound
        assertThat(notFoundError.resource).contains("Fixture")
        assertThat(notFoundError.id).isEqualTo(invalidUid)
    }

    @Test
    fun `getFixtureEvents - 존재하지 않는 UID 실패 응답`() {
        // Given
        val invalidUid = "invaliduid99999"

        // When
        val result = webService.getFixtureEvents(invalidUid, null)

        // Then
        assertThat(result).isInstanceOf(FixtureWebResult.Fail::class.java)
        val error = (result as FixtureWebResult.Fail).error
        assertThat(error).isInstanceOf(DomainFail.NotFound::class.java)
        val notFoundError = error as DomainFail.NotFound
        assertThat(notFoundError.resource).contains("Fixture")
        assertThat(notFoundError.id).isEqualTo(invalidUid)
    }
}
