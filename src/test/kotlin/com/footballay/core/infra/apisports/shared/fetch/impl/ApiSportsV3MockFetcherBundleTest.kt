package com.footballay.core.infra.apisports.shared.fetch.impl

import com.footballay.core.infra.apisports.shared.fetch.impl.ApiSportsV3MockFetcher.TestHelpers
import com.footballay.core.logger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test", "mockapi")
@DisplayName("ApiSportsV3MockFetcher Bundle 구조 테스트")
class ApiSportsV3MockFetcherBundleTest {
    private val log = logger()

    @Autowired
    private lateinit var mockFetcher: ApiSportsV3MockFetcher

    @Test
    @DisplayName("지원하는 모든 번들을 조회할 수 있습니다")
    fun `지원하는 모든 번들을 조회할 수 있다`() {
        // when
        val bundles = mockFetcher.getSupportedBundles()

        // then
        assertThat(bundles).isNotEmpty()
        assertThat(bundles).hasSize(2)

        val premierLeagueBundle = bundles.find { it.name == "Premier League 2024" }
        val manchesterCityBundle = bundles.find { it.name == "Manchester City Squad" }

        assertThat(premierLeagueBundle).isNotNull()
        assertThat(manchesterCityBundle).isNotNull()
    }

    @Test
    @DisplayName("Premier League 2024 번들의 정보를 확인할 수 있습니다")
    fun `Premier League 2024 번들의 정보를 확인할 수 있다`() {
        // given
        val bundle = TestHelpers.getPremierLeague2024Bundle()

        // when & then
        assertThat(bundle.name).isEqualTo("Premier League 2024")
        assertThat(bundle.description).contains("Premier League 2024 시즌")
        assertThat(bundle.leagueId).isEqualTo(39L)
        assertThat(bundle.season).isEqualTo(2024)
        assertThat(bundle.hasJsonSupport()).isTrue()
        assertThat(bundle.supportedFixtureIds).contains(1208021L, 1208022L)
        assertThat(bundle.jsonFilePaths).containsKey("teams")
        assertThat(bundle.jsonFilePaths).containsKey("fixture_1208021")
        assertThat(bundle.jsonFilePaths).containsKey("fixture_1208022")
    }

    @Test
    @DisplayName("Manchester City Squad 번들의 정보를 확인할 수 있습니다")
    fun `Manchester City Squad 번들의 정보를 확인할 수 있다`() {
        // given
        val bundle = TestHelpers.getManchesterCitySquadBundle()

        // when & then
        assertThat(bundle.name).isEqualTo("Manchester City Squad")
        assertThat(bundle.description).contains("Manchester City 팀 선수 명단")
        assertThat(bundle.teamId).isEqualTo(50L)
        assertThat(bundle.hasJsonSupport()).isFalse()
        assertThat(bundle.supportedPlayerIds).contains(1L, 2L, 3L, 4L, 5L)
    }

    @Test
    @DisplayName("번들 기반 리그 지원 확인이 동작합니다")
    fun `번들 기반 리그 지원 확인이 동작한다`() {
        // when
        val premierLeagueBundle = mockFetcher.findBundleForLeague(39L, 2024)
        val unsupportedBundle = mockFetcher.findBundleForLeague(999L, 2024)

        // then
        assertThat(premierLeagueBundle).isNotNull()
        assertThat(premierLeagueBundle!!.name).isEqualTo("Premier League 2024")
        assertThat(unsupportedBundle).isNull()
    }

    @Test
    @DisplayName("번들 기반 팀 지원 확인이 동작합니다")
    fun `번들 기반 팀 지원 확인이 동작한다`() {
        // when
        val manchesterCityBundle = mockFetcher.findBundleForTeam(50L)
        val unsupportedBundle = mockFetcher.findBundleForTeam(999L)

        // then
        assertThat(manchesterCityBundle).isNotNull()
        assertThat(manchesterCityBundle!!.name).isEqualTo("Manchester City Squad")
        assertThat(unsupportedBundle).isNull()
    }

    @Test
    @DisplayName("번들 기반 경기 지원 확인이 동작합니다")
    fun `번들 기반 경기 지원 확인이 동작한다`() {
        // when
        val supportedBundle = mockFetcher.findBundleForFixture(1208021L)
        val unsupportedBundle = mockFetcher.findBundleForFixture(999999L)

        // then
        assertThat(supportedBundle).isNotNull()
        assertThat(supportedBundle!!.name).isEqualTo("Premier League 2024")
        assertThat(unsupportedBundle).isNull()
    }

    @Test
    @DisplayName("JSON 파일 지원 번들들을 조회할 수 있습니다")
    fun `JSON 파일 지원 번들들을 조회할 수 있다`() {
        // when
        val jsonBundles = mockFetcher.getJsonSupportedBundles()

        // then
        assertThat(jsonBundles).isNotEmpty()
        assertThat(jsonBundles).hasSize(1)
        assertThat(jsonBundles.first().name).isEqualTo("Premier League 2024")
    }

    @Test
    @DisplayName("번들 정보를 로그로 출력할 수 있습니다")
    fun `번들 정보를 로그로 출력할 수 있다`() {
        // given
        val bundle = TestHelpers.getPremierLeague2024Bundle()

        // when & then (로그 출력 확인)
        assertDoesNotThrow {
            TestHelpers.logBundleInfo(bundle)
        }
    }

    @Disabled
    @Test
    @DisplayName("번들에서 지원하는 fixture ID들을 조회할 수 있습니다")
    fun `번들에서 지원하는 fixture ID들을 조회할 수 있다`() {
        // given
        val bundle = TestHelpers.getPremierLeague2024Bundle()

        // when
        val fixtureIds = TestHelpers.getSupportedFixtureIds(bundle)

        // then
        assertThat(fixtureIds).contains(1208021L, 1208022L, 1208397L)
        assertThat(fixtureIds).hasSize(3)
    }

    @Test
    @DisplayName("번들에서 지원하는 player ID들을 조회할 수 있습니다")
    fun `번들에서 지원하는 player ID들을 조회할 수 있다`() {
        // given
        val bundle = TestHelpers.getManchesterCitySquadBundle()

        // when
        val playerIds = TestHelpers.getSupportedPlayerIds(bundle)

        // then
        assertThat(playerIds).contains(1L, 2L, 3L, 4L, 5L)
        assertThat(playerIds).hasSize(5)
    }

    @Test
    @DisplayName("번들에서 JSON 파일 경로들을 조회할 수 있습니다")
    fun `번들에서 JSON 파일 경로들을 조회할 수 있다`() {
        // given
        val bundle = TestHelpers.getPremierLeague2024Bundle()

        // when
        val jsonPaths = TestHelpers.getJsonFilePaths(bundle)

        // then
        assertThat(jsonPaths).containsKey("teams")
        assertThat(jsonPaths).containsKey("fixture_1208021")
        assertThat(jsonPaths).containsKey("fixture_1208022")
        assertThat(jsonPaths["teams"]).contains("teamsOfLeague_leagueId39_season2024.json")
    }
}
