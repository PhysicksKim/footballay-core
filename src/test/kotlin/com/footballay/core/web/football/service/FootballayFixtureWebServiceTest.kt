package com.footballay.core.web.football.service

import com.footballay.core.MatchConfig
import com.footballay.core.MatchEntityGenerator
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
 * ApiResponseV2 구조: { success: boolean, data: T, error: ErrorDetail }
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
        val response = webService.getFixtureInfo(fixtureUid)

        // Then
        assertThat(response.success).isTrue()
        assertThat(response.data).isNotNull
        assertThat(response.data!!.fixtureUid).isEqualTo(fixtureUid)
        assertThat(response.data!!.home.name).isEqualTo("Manchester United")
        assertThat(response.data!!.away.name).isEqualTo("Arsenal")
        assertThat(response.data!!.referee).isEqualTo("Michael Oliver")
    }

    @Test
    fun `getFixtureLiveStatus - 성공 응답 반환`() {
        // Given
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val response = webService.getFixtureLiveStatus(fixtureUid)

        // Then
        assertThat(response.success).isTrue()
        assertThat(response.data).isNotNull
        assertThat(response.data!!.liveStatus.shortStatus).isEqualTo("NS")
        assertThat(response.data!!.liveStatus.score).isNotNull
    }

    @Test
    fun `getFixtureEvents - 성공 응답 반환`() {
        // Given: Live 데이터 포함 매치
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val response = webService.getFixtureEvents(fixtureUid)

        // Then
        assertThat(response.success).isTrue()
        assertThat(response.data).isNotNull
        assertThat(response.data!!.events).isNotEmpty
        assertThat(response.data!!.events[0].type).isEqualTo("goal")
    }

    @Test
    fun `getFixtureEvents - 빈 이벤트 목록 성공 응답`() {
        // Given: Live 데이터 없는 매치
        val config = MatchConfig(createLiveData = false)
        val entities = entityGenerator.createCompleteMatchEntities(config)
        val fixtureUid = entities.fixtureCore.uid

        // When
        val response = webService.getFixtureEvents(fixtureUid)

        // Then
        assertThat(response.success).isTrue()
        assertThat(response.data).isNotNull
        assertThat(response.data!!.events).isEmpty()
    }

    @Test
    fun `getFixtureLineup - 성공 응답 반환`() {
        // Given
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val response = webService.getFixtureLineup(fixtureUid)

        // Then
        assertThat(response.success).isTrue()
        assertThat(response.data).isNotNull
        assertThat(
            response.data!!
                .lineup.home.teamName,
        ).isEqualTo("Manchester United")
        assertThat(
            response.data!!
                .lineup.home.formation,
        ).isEqualTo("4-3-3")
        assertThat(
            response.data!!
                .lineup.home.players,
        ).isNotEmpty
        assertThat(
            response.data!!
                .lineup.away.teamName,
        ).isEqualTo("Arsenal")
        assertThat(
            response.data!!
                .lineup.away.formation,
        ).isEqualTo("4-2-3-1")
    }

    @Test
    fun `getFixtureStatistics - 성공 응답 반환`() {
        // Given
        val entities = entityGenerator.createCompleteMatchEntities()
        val fixtureUid = entities.fixtureCore.uid

        // When
        val response = webService.getFixtureStatistics(fixtureUid)

        // Then
        assertThat(response.success).isTrue()
        assertThat(response.data).isNotNull
        assertThat(response.data!!.fixture.uid).isEqualTo(fixtureUid)
        assertThat(
            response.data!!
                .home.teamStatistics.shotsOnGoal,
        ).isEqualTo(5)
        assertThat(
            response.data!!
                .home.teamStatistics.ballPossession,
        ).isEqualTo(55)
        assertThat(
            response.data!!
                .away.teamStatistics.shotsOnGoal,
        ).isEqualTo(5)
    }

    @Test
    fun `getFixtureInfo - 존재하지 않는 UID 실패 응답`() {
        // Given
        val invalidUid = "invaliduid99999"

        // When
        val response = webService.getFixtureInfo(invalidUid)

        // Then
        assertThat(response.success).isFalse()
        assertThat(response.data).isNull()
        assertThat(response.error).isNotNull
        assertThat(response.error!!.message).contains("not found")
        assertThat(response.code).isEqualTo(404)
    }

    @Test
    fun `getFixtureLiveStatus - 존재하지 않는 UID 실패 응답`() {
        // Given
        val invalidUid = "invaliduid99999"

        // When
        val response = webService.getFixtureLiveStatus(invalidUid)

        // Then
        assertThat(response.success).isFalse()
        assertThat(response.data).isNull()
        assertThat(response.error).isNotNull
        assertThat(response.error!!.message).contains("not found")
        assertThat(response.code).isEqualTo(404)
    }

    @Test
    fun `getFixtureEvents - 존재하지 않는 UID 실패 응답`() {
        // Given
        val invalidUid = "invaliduid99999"

        // When
        val response = webService.getFixtureEvents(invalidUid)

        // Then
        assertThat(response.success).isFalse()
        assertThat(response.data).isNull()
        assertThat(response.error).isNotNull
        assertThat(response.error!!.message).contains("not found")
        assertThat(response.code).isEqualTo(404)
    }
}
