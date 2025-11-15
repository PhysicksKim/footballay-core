package com.footballay.core.web.football.mapper

import com.footballay.core.infra.persistence.apisports.entity.*
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchEvent
import com.footballay.core.infra.persistence.apisports.entity.live.ApiSportsMatchTeam
import com.footballay.core.infra.persistence.core.entity.FixtureCore
import com.footballay.core.infra.persistence.core.entity.FixtureStatusShort
import com.footballay.core.infra.persistence.core.entity.LeagueCore
import com.footballay.core.infra.persistence.core.entity.TeamCore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * MatchDataMapper 단위 테스트
 *
 * **테스트 목적:**
 * - Entity → DTO 변환 로직 검증
 * - Null 안전성 확인
 * - 필드 매핑 정확성 검증
 *
 * **간소화:** 복잡한 entity 생성을 최소화하고 핵심 로직만 테스트
 */
class MatchDataMapperTest {
    private lateinit var mapper: MatchDataMapper

    @BeforeEach
    fun setUp() {
        mapper = MatchDataMapper()
    }

    @Test
    fun `toFixtureInfoDto - 기본 정보 변환 성공`() {
        // Given: Minimal fixture setup
        val leagueCore = LeagueCore(name = "Premier League", uid = "league:1")
        val leagueApiSports =
            LeagueApiSports(
                apiId = 39,
                leagueCore = leagueCore,
                name = "Premier League",
                logo = "https://logo.png",
            )
        val season =
            LeagueApiSportsSeason(
                seasonYear = 2024,
                leagueApiSports = leagueApiSports,
            )

        val homeTeamCore = TeamCore(name = "Manchester City", uid = "team:100")
        val awayTeamCore = TeamCore(name = "Liverpool", uid = "team:200")

        val fixtureCore =
            FixtureCore(
                uid = "apisports:1208021",
                kickoff = Instant.parse("2025-01-15T20:00:00Z"),
                status = "Not Started",
                statusShort = FixtureStatusShort.NS,
                league = leagueCore,
                homeTeam = homeTeamCore,
                awayTeam = awayTeamCore,
            )

        val fixture =
            FixtureApiSports(
                apiId = 1208021,
                core = fixtureCore,
                season = season,
                referee = "Michael Oliver",
                date = Instant.parse("2025-01-15T20:00:00Z"),
            )

        // When
        val dto = mapper.toFixtureInfoDto(fixture)

        // Then
        assertThat(dto.fixtureUid).isEqualTo("apisports:1208021")
        assertThat(dto.referee).isEqualTo("Michael Oliver")
        assertThat(dto.league.name).isEqualTo("Premier League")
        assertThat(dto.home.name).isEqualTo("Manchester City")
        assertThat(dto.away.name).isEqualTo("Liverpool")
    }

    @Test
    fun `toFixtureLiveStatusDto - 라이브 상태 변환 성공`() {
        // Given
        val leagueCore = LeagueCore(name = "Test", uid = "league:1")
        val homeTeam = TeamCore(name = "Home", uid = "team:home")
        val awayTeam = TeamCore(name = "Away", uid = "team:away")

        val fixtureCore =
            FixtureCore(
                uid = "apisports:123",
                kickoff = null,
                status = "First Half",
                statusShort = FixtureStatusShort.FIRST_HALF,
                league = leagueCore,
                homeTeam = homeTeam,
                awayTeam = awayTeam,
            )
        val status =
            ApiSportsStatus(
                elapsed = 45,
                shortStatus = "1H",
                longStatus = "First Half",
            )
        val score =
            ApiSportsScore(
                totalHome = 2,
                totalAway = 1,
            )

        val leagueApiSports = LeagueApiSports(apiId = 1, leagueCore = leagueCore, name = "Test League")
        val season = LeagueApiSportsSeason(seasonYear = 2024, leagueApiSports = leagueApiSports)

        val fixture =
            FixtureApiSports(
                apiId = 123,
                core = fixtureCore,
                season = season,
                status = status,
                score = score,
            )

        // When
        val dto = mapper.toFixtureLiveStatusDto(fixture)

        // Then
        assertThat(dto.fixtureUid).isEqualTo("apisports:123")
        assertThat(dto.liveStatus.elapsed).isEqualTo(45)
        assertThat(dto.liveStatus.shortStatus).isEqualTo("1H")
        assertThat(dto.liveStatus.score.home).isEqualTo(2)
        assertThat(dto.liveStatus.score.away).isEqualTo(1)
    }

    @Test
    fun `toFixtureEventsDto - 빈 이벤트 목록 변환`() {
        // Given: Empty events
        val events = emptyList<ApiSportsMatchEvent>()

        // When
        val dto = mapper.toFixtureEventsDto("apisports:123", events)

        // Then
        assertThat(dto.fixtureUid).isEqualTo("apisports:123")
        assertThat(dto.events).isEmpty()
    }

    @Test
    fun `toFixtureLineupDto - null 라인업 처리`() {
        // When
        val dto = mapper.toFixtureLineupDto("apisports:123", null, null)

        // Then
        assertThat(dto.fixtureUid).isEqualTo("apisports:123")
        assertThat(dto.lineup.home.teamName).isEmpty()
        assertThat(dto.lineup.away.teamName).isEmpty()
    }
}
