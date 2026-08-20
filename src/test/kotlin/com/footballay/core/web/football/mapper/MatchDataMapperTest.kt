package com.footballay.core.web.football.mapper

import com.footballay.core.domain.model.match.*
import com.footballay.core.web.football.localization.LocalizedFixtureInfoModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * MatchDataMapper 단위 테스트
 *
 * **테스트 목적:**
 * - Domain Model → Response DTO 변환 로직 검증
 * - Null 안전성 확인
 * - 필드 매핑 정확성 검증
 */
class MatchDataMapperTest {
    private lateinit var mapper: MatchDataMapper

    @BeforeEach
    fun setUp() {
        mapper = MatchDataMapper()
    }

    @Test
    fun `toFixtureInfoResponse - 기본 정보 변환 성공`() {
        // Given: Domain Model
        val model =
            FixtureInfoModel(
                fixtureUid = "testfixture0001",
                referee = "Michael Oliver",
                date = "2025-01-15 20:00",
                league =
                    FixtureInfoModel.LeagueInfo(
                        id = 1L,
                        name = "Premier League",
                        logo = "https://logo.png",
                        leagueUid = "premier-league-2024",
                    ),
                home =
                    FixtureInfoModel.TeamInfo(
                        id = 100L,
                        name = "Manchester City",
                        logo = "https://city-logo.png",
                        teamUid = "team-city-50",
                        playerColor = null,
                    ),
                away =
                    FixtureInfoModel.TeamInfo(
                        id = 200L,
                        name = "Liverpool",
                        logo = "https://liverpool-logo.png",
                        teamUid = "team-liverpool-51",
                        playerColor = null,
                    ),
            )

        // When
        val dto =
            mapper.toFixtureInfoResponse(
                LocalizedFixtureInfoModel(
                    fixtureUid = model.fixtureUid,
                    referee = model.referee,
                    date = model.date,
                    league = LocalizedFixtureInfoModel.League("premier-league-2024", "Premier League", null, "https://logo.png"),
                    home = LocalizedFixtureInfoModel.Team("team-city-50", "Manchester City", null, "https://city-logo.png", null),
                    away = LocalizedFixtureInfoModel.Team("team-liverpool-51", "Liverpool", null, "https://liverpool-logo.png", null),
                ),
            )

        // Then
        assertThat(dto.fixtureUid).isEqualTo("testfixture0001")
        assertThat(dto.referee).isEqualTo("Michael Oliver")
        assertThat(dto.league.name).isEqualTo("Premier League")
        assertThat(dto.league.shortName).isNull()
        assertThat(dto.home?.name).isEqualTo("Manchester City")
        assertThat(dto.home?.shortName).isNull()
        assertThat(dto.away?.name).isEqualTo("Liverpool")
        assertThat(dto.away?.shortName).isNull()
    }

    @Test
    fun `toFixtureInfoResponse - 결정된 localized name을 반영한다`() {
        val model =
            FixtureInfoModel(
                fixtureUid = "fixture-1",
                referee = null,
                date = "2026-08-20 20:00",
                league = FixtureInfoModel.LeagueInfo(name = "Default League", logo = null, leagueUid = "league-1"),
                home = FixtureInfoModel.TeamInfo(name = "Default Home", logo = null, teamUid = "team-home", playerColor = null),
                away = FixtureInfoModel.TeamInfo(name = "Default Away", logo = null, teamUid = "team-away", playerColor = null),
            )

        val dto =
            mapper.toFixtureInfoResponse(
                LocalizedFixtureInfoModel(
                    fixtureUid = model.fixtureUid,
                    referee = model.referee,
                    date = model.date,
                    league = LocalizedFixtureInfoModel.League("league-1", "리그", "리그 약칭", null),
                    home = LocalizedFixtureInfoModel.Team("team-home", "English Home", "EH", null, null),
                    away = LocalizedFixtureInfoModel.Team("team-away", "Default Away", null, null, null),
                ),
            )

        assertThat(dto.league.name).isEqualTo("리그")
        assertThat(dto.league.shortName).isEqualTo("리그 약칭")
        assertThat(dto.home?.name).isEqualTo("English Home")
        assertThat(dto.home?.shortName).isEqualTo("EH")
        assertThat(dto.away?.name).isEqualTo("Default Away")
        assertThat(dto.away?.shortName).isNull()
    }

    @Test
    fun `toFixtureLiveStatusResponse - 라이브 상태 변환 성공`() {
        // Given: Domain Model
        val model =
            FixtureLiveStatusModel(
                fixtureUid = "testfixture0002",
                liveStatus =
                    FixtureLiveStatusModel.LiveStatus(
                        elapsed = 45,
                        shortStatus = "1H",
                        longStatus = "First Half",
                        score =
                            FixtureLiveStatusModel.Score(
                                home = 2,
                                away = 1,
                            ),
                    ),
            )

        // When
        val dto = mapper.toFixtureLiveStatusResponse(model)

        // Then
        assertThat(dto.fixtureUid).isEqualTo("testfixture0002")
        assertThat(dto.liveStatus.elapsed).isEqualTo(45)
        assertThat(dto.liveStatus.shortStatus).isEqualTo("1H")
        assertThat(dto.liveStatus.score.home).isEqualTo(2)
        assertThat(dto.liveStatus.score.away).isEqualTo(1)
    }

    @Test
    fun `toFixtureEventsResponse - 빈 이벤트 목록 변환`() {
        // Given: Empty events
        val model =
            FixtureEventsModel(
                fixtureUid = "testfixture0003",
                events = emptyList(),
            )

        // When
        val dto = mapper.toFixtureEventsResponse(model)

        // Then
        assertThat(dto.fixtureUid).isEqualTo("testfixture0003")
        assertThat(dto.events).isEmpty()
    }

    @Test
    fun `toFixtureLineupResponse - null 라인업 처리`() {
        // Given: Empty lineup
        val model =
            FixtureLineupModel(
                fixtureUid = "testfixture0004",
                lineup =
                    FixtureLineupModel.Lineup(
                        home =
                            FixtureLineupModel.StartLineup(
                                teamId = 0L,
                                teamName = "",
                                formation = null,
                                players = emptyList(),
                                substitutes = emptyList(),
                                teamUid = "",
                                playerColor = null,
                            ),
                        away =
                            FixtureLineupModel.StartLineup(
                                teamId = 0L,
                                teamName = "",
                                formation = null,
                                players = emptyList(),
                                substitutes = emptyList(),
                                teamUid = "",
                                playerColor = null,
                            ),
                    ),
            )

        // When
        val dto = mapper.toFixtureLineupResponse(model)

        // Then
        assertThat(dto.fixtureUid).isEqualTo("testfixture0004")
        assertThat(dto.lineup.home?.teamName).isEmpty()
        assertThat(dto.lineup.home?.teamShortName).isNull()
        assertThat(dto.lineup.away?.teamName).isEmpty()
        assertThat(dto.lineup.away?.teamShortName).isNull()
    }
}
