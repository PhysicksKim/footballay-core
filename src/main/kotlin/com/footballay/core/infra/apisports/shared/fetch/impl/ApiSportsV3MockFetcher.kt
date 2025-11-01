package com.footballay.core.infra.apisports.shared.fetch.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.footballay.core.infra.apisports.shared.fetch.ApiSportsV3Fetcher
import com.footballay.core.infra.apisports.shared.fetch.response.*
import com.footballay.core.infra.apisports.shared.fetch.response.ApiSportsFixture.Single
import com.footballay.core.infra.apisports.shared.fetch.response.ApiSportsFixture.Single.LineupTeam
import com.footballay.core.infra.apisports.shared.fetch.response.ApiSportsFixture.Single.PlayerStatistics
import com.footballay.core.logger
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.file.Files
import java.time.OffsetDateTime

/**
 * Mock implementation of [ApiSportsV3Fetcher] that provides test data.
 *
 * This implementation is used for testing to avoid real API calls.
 * Activate this by using the 'mockapi' profile.
 *
 * Supported test data:
 * - League 39 (Premier League), Season 2024: 20 teams
 * - Team 50 (Manchester City): Squad with players
 * - Fixtures: 5 Premier League fixtures for 2024 season
 * - Current leagues: Premier League and La Liga
 * - JSON file based fixtures: fixture_1208021.json, fixture_1208022.json
 */
@Profile("mockapi")
@Component
class ApiSportsV3MockFetcher(
    private val objectMapper: ObjectMapper,
) : ApiSportsV3Fetcher {
    private val log = logger()

    companion object {
        const val SUPPORTED_LEAGUE_ID = 39L // Premier League
        const val SUPPORTED_SEASON = 2024
        const val SUPPORTED_TEAM_ID = 50L // Manchester City

        private val SUPPORTED_FIXTURE_IDS =
            listOf(
                1208021L, // Manchester United vs Fulham
                1208022L, // Ipswich vs Liverpool
                1208025L, // Newcastle vs Southampton
                1208028L, // Arsenal vs Wolves
                1208397L, // Manchester United vs Aston Villa (detailed fixture)
            )

        // JSON 파일 기반 지원 fixture IDs
        private val JSON_FILE_SUPPORTED_FIXTURE_IDS =
            listOf(
                1208021L, // fixture_1208021.json
                1208022L, // fixture_1208022.json
            )
    }

    /**
     * Mock Fetcher에서 지원하는 데이터 번들들을 정의합니다.
     *
     * 각 번들은 논리적으로 연결된 데이터들을 포함하며,
     * 테스트에서 특정 시나리오를 위한 완전한 데이터 세트를 제공합니다.
     */
    object MockDataBundles {
        /**
         * Premier League 2024 시즌 데이터 번들
         *
         * **포함 데이터:**
         * - League: Premier League (ID: 39)
         * - Season: 2024
         * - Teams: 20개 팀 (JSON 파일 기반)
         * - Fixtures: 5개 경기 (JSON 파일 기반)
         *
         * **사용 시나리오:**
         * - Backbone 데이터 완전 구성 테스트
         * - Match 데이터 동기화 테스트
         * - 전체 워크플로우 통합 테스트
         */
        val PREMIER_LEAGUE_2024 =
            MockDataBundle(
                name = "Premier League 2024",
                description = "Premier League 2024 시즌 완전 데이터 세트",
                leagueId = 39L,
                season = 2024,
                supportedFixtureIds = listOf(1208021L, 1208022L, 1208397L),
                hasJsonFileSupport = true,
                jsonFilePaths =
                    mapOf(
                        "teams" to "/devdata/mockapiv2/teamsOfLeague_leagueId39_season2024.json",
                        "fixture_1208021" to "/devdata/mockapiv2/fixture_1208021.json",
                        "fixture_1208022" to "/devdata/mockapiv2/fixture_1208022.json",
                        "fixture_1208397" to "/devdata/mockapiv2/fixture_1208397.json",
                    ),
            )

        /**
         * Manchester City 팀 데이터 번들
         *
         * **포함 데이터:**
         * - Team: Manchester City (ID: 50)
         * - Players: 5명 선수 (하드코딩)
         *
         * **사용 시나리오:**
         * - 팀별 선수 데이터 테스트
         * - 선수 정보 동기화 테스트
         */
        val MANCHESTER_CITY_SQUAD =
            MockDataBundle(
                name = "Manchester City Squad",
                description = "Manchester City 팀 선수 명단",
                teamId = 50L,
                supportedPlayerIds = listOf(1L, 2L, 3L, 4L, 5L),
                hasJsonFileSupport = false,
            )

        /**
         * 모든 지원 데이터 번들 목록
         */
        val ALL_BUNDLES = listOf(PREMIER_LEAGUE_2024, MANCHESTER_CITY_SQUAD)
    }

    /**
     * Mock Fetcher에서 지원하는 데이터 번들을 정의하는 데이터 클래스
     *
     * @param name 번들 이름
     * @param description 번들 설명
     * @param leagueId 지원하는 리그 ID (선택적)
     * @param season 지원하는 시즌 (선택적)
     * @param teamId 지원하는 팀 ID (선택적)
     * @param supportedFixtureIds 지원하는 경기 ID 목록
     * @param supportedPlayerIds 지원하는 선수 ID 목록
     * @param hasJsonFileSupport JSON 파일 지원 여부
     * @param jsonFilePaths JSON 파일 경로 매핑
     */
    data class MockDataBundle(
        val name: String,
        val description: String,
        val leagueId: Long? = null,
        val season: Int? = null,
        val teamId: Long? = null,
        val supportedFixtureIds: List<Long> = emptyList(),
        val supportedPlayerIds: List<Long> = emptyList(),
        val hasJsonFileSupport: Boolean = false,
        val jsonFilePaths: Map<String, String> = emptyMap(),
    ) {
        /**
         * 이 번들이 특정 리그/시즌을 지원하는지 확인
         */
        fun supportsLeague(
            leagueId: Long,
            season: Int,
        ): Boolean = this.leagueId == leagueId && this.season == season

        /**
         * 이 번들이 특정 팀을 지원하는지 확인
         */
        fun supportsTeam(teamId: Long): Boolean = this.teamId == teamId

        /**
         * 이 번들이 특정 경기를 지원하는지 확인
         */
        fun supportsFixture(fixtureId: Long): Boolean = supportedFixtureIds.contains(fixtureId)

        /**
         * 이 번들이 특정 선수를 지원하는지 확인
         */
        fun supportsPlayer(playerId: Long): Boolean = supportedPlayerIds.contains(playerId)

        /**
         * 이 번들이 JSON 파일 기반 데이터를 지원하는지 확인
         */
        fun hasJsonSupport(): Boolean = hasJsonFileSupport && jsonFilePaths.isNotEmpty()

        /**
         * 특정 키에 대한 JSON 파일 경로를 반환
         */
        fun getJsonFilePath(key: String): String? = jsonFilePaths[key]
    }

    /**
     * 지원하는 모든 데이터 번들을 반환합니다.
     */
    fun getSupportedBundles(): List<MockDataBundle> = MockDataBundles.ALL_BUNDLES

    /**
     * 특정 리그/시즌을 지원하는 번들을 찾습니다.
     */
    fun findBundleForLeague(
        leagueId: Long,
        season: Int,
    ): MockDataBundle? = MockDataBundles.ALL_BUNDLES.find { it.supportsLeague(leagueId, season) }

    /**
     * 특정 팀을 지원하는 번들을 찾습니다.
     */
    fun findBundleForTeam(teamId: Long): MockDataBundle? = MockDataBundles.ALL_BUNDLES.find { it.supportsTeam(teamId) }

    /**
     * 특정 경기를 지원하는 번들을 찾습니다.
     */
    fun findBundleForFixture(fixtureId: Long): MockDataBundle? = MockDataBundles.ALL_BUNDLES.find { it.supportsFixture(fixtureId) }

    /**
     * JSON 파일 기반 데이터를 지원하는 번들들을 반환합니다.
     */
    fun getJsonSupportedBundles(): List<MockDataBundle> = MockDataBundles.ALL_BUNDLES.filter { it.hasJsonSupport() }

    /**
     * 테스트에서 사용할 수 있는 헬퍼 메서드들
     */
    object TestHelpers {
        /**
         * Premier League 2024 번들의 모든 지원 데이터를 반환합니다.
         */
        fun getPremierLeague2024Bundle(): MockDataBundle = MockDataBundles.PREMIER_LEAGUE_2024

        /**
         * Manchester City Squad 번들의 모든 지원 데이터를 반환합니다.
         */
        fun getManchesterCitySquadBundle(): MockDataBundle = MockDataBundles.MANCHESTER_CITY_SQUAD

        /**
         * 특정 번들의 모든 지원 fixture ID들을 반환합니다.
         */
        fun getSupportedFixtureIds(bundle: MockDataBundle): List<Long> = bundle.supportedFixtureIds

        /**
         * 특정 번들의 모든 지원 player ID들을 반환합니다.
         */
        fun getSupportedPlayerIds(bundle: MockDataBundle): List<Long> = bundle.supportedPlayerIds

        /**
         * 특정 번들이 지원하는 JSON 파일 경로들을 반환합니다.
         */
        fun getJsonFilePaths(bundle: MockDataBundle): Map<String, String> = bundle.jsonFilePaths

        /**
         * 번들 정보를 로그로 출력합니다.
         */
        fun logBundleInfo(bundle: MockDataBundle) {
            val log = logger()
            log.info("=== Mock Data Bundle Info ===")
            log.info("Name: ${bundle.name}")
            log.info("Description: ${bundle.description}")
            log.info("League ID: ${bundle.leagueId}")
            log.info("Season: ${bundle.season}")
            log.info("Team ID: ${bundle.teamId}")
            log.info("Supported Fixture IDs: ${bundle.supportedFixtureIds}")
            log.info("Supported Player IDs: ${bundle.supportedPlayerIds}")
            log.info("Has JSON Support: ${bundle.hasJsonSupport()}")
            log.info("JSON File Paths: ${bundle.jsonFilePaths}")
            log.info("===============================")
        }
    }

    override fun fetchStatus(): ApiSportsV3LiveStatusEnvelope<ApiSportsAccountStatus> {
        log.info("Mock fetching API status")

        val statusResponse =
            ApiSportsAccountStatus(
                account =
                    ApiSportsAccountStatus.Account(
                        firstname = "Mock",
                        lastname = "User",
                        email = "mock@test.com",
                    ),
                subscription =
                    ApiSportsAccountStatus.Subscription(
                        plan = "Mock Plan",
                        end = "2025-12-31",
                        active = true,
                    ),
                requests =
                    ApiSportsAccountStatus.Requests(
                        current = 0,
                        limit_day = 1000,
                    ),
            )

        return ApiSportsV3LiveStatusEnvelope(
            get = "status",
            parameters = emptyList(),
            errors = emptyList(),
            results = 1,
            paging = Paging(current = 1, total = 1),
            response = statusResponse,
        )
    }

    override fun fetchLeaguesCurrent(): ApiSportsV3Envelope<ApiSportsLeague.Current> {
        log.info("Mock fetching current leagues")

        val premierLeague =
            ApiSportsLeague.Current(
                league =
                    ApiSportsLeague.Current.LeagueInfo(
                        id = 39,
                        name = "Premier League",
                        type = "League",
                        logo = "https://media.api-sports.io/football/leagues/39.png",
                    ),
                country =
                    ApiSportsLeague.Current.CountryInfo(
                        name = "England",
                        code = "GB",
                        flag = "https://media.api-sports.io/flags/gb.svg",
                    ),
                seasons =
                    listOf(
                        ApiSportsLeague.Current.SeasonInfo(
                            year = 2024,
                            start = "2024-08-16",
                            end = "2025-05-25",
                            current = true,
                            coverage = null,
                        ),
                    ),
            )

        val laLiga =
            ApiSportsLeague.Current(
                league =
                    ApiSportsLeague.Current.LeagueInfo(
                        id = 140,
                        name = "La Liga",
                        type = "League",
                        logo = "https://media.api-sports.io/football/leagues/140.png",
                    ),
                country =
                    ApiSportsLeague.Current.CountryInfo(
                        name = "Spain",
                        code = "ES",
                        flag = "https://media.api-sports.io/flags/es.svg",
                    ),
                seasons =
                    listOf(
                        ApiSportsLeague.Current.SeasonInfo(
                            year = 2024,
                            start = "2024-08-18",
                            end = "2025-05-25",
                            current = true,
                            coverage = null,
                        ),
                    ),
            )

        return ApiSportsV3Envelope(
            get = "leagues",
            parameters = mapOf("current" to "true"),
            errors = emptyList(),
            results = 2,
            paging = Paging(current = 1, total = 1),
            response = listOf(premierLeague, laLiga),
        )
    }

    override fun fetchTeamsOfLeague(
        leagueApiId: Long,
        season: Int,
    ): ApiSportsV3Envelope<ApiSportsTeam.OfLeague> {
        log.info("Mock fetching teams for league: $leagueApiId, season: $season")

        // 번들 기반 지원 확인
        val bundle = findBundleForLeague(leagueApiId, season)
        if (bundle == null) {
            log.warn(
                "No mock data bundle found for league $leagueApiId season $season. " +
                    "Available bundles: ${getSupportedBundles().map { "${it.name} (${it.leagueId}/${it.season})" }}",
            )
            return ApiSportsV3Envelope(
                get = "teams",
                parameters = mapOf("league" to leagueApiId.toString(), "season" to season.toString()),
                errors = emptyList(),
                results = 0,
                paging = Paging(current = 1, total = 1),
                response = emptyList(),
            )
        }

        log.info("Using mock data bundle: ${bundle.name}")

        // JSON 파일 지원 확인
        if (bundle.hasJsonSupport()) {
            return fetchTeamsOfLeagueFromJsonFile(leagueApiId, season)
        }

        // 하드코딩된 데이터 반환 (기존 로직)
        if (leagueApiId == SUPPORTED_LEAGUE_ID && season == SUPPORTED_SEASON) {
            return ApiSportsV3Envelope(
                get = "teams",
                parameters = mapOf("league" to leagueApiId.toString(), "season" to season.toString()),
                errors = emptyList(),
                results = 11,
                paging = Paging(current = 1, total = 1),
                response = createMockPremierLeagueTeams(),
            )
        }

        return ApiSportsV3Envelope(
            get = "teams",
            parameters = mapOf("league" to leagueApiId.toString(), "season" to season.toString()),
            errors = emptyList(),
            results = 0,
            paging = Paging(current = 1, total = 1),
            response = emptyList(),
        )
    }

    override fun fetchSquadOfTeam(teamApiId: Long): ApiSportsV3Envelope<ApiSportsPlayer.OfTeam> {
        log.info("Mock fetching squad for team: $teamApiId")

        // 번들 기반 지원 확인
        val bundle = findBundleForTeam(teamApiId)
        if (bundle == null) {
            log.warn(
                "No mock data bundle found for team $teamApiId. " +
                    "Available bundles: ${getSupportedBundles().map { "${it.name} (team: ${it.teamId})" }}",
            )
            return ApiSportsV3Envelope(
                get = "players/squads",
                parameters = mapOf("team" to teamApiId.toString()),
                errors = emptyList(),
                results = 0,
                paging = Paging(current = 1, total = 1),
                response = emptyList(),
            )
        }

        log.info("Using mock data bundle: ${bundle.name}")

        val players = createMockManchesterCityPlayers()

        return ApiSportsV3Envelope(
            get = "players/squads",
            parameters = mapOf("team" to teamApiId.toString()),
            errors = emptyList(),
            results = players.size,
            paging = Paging(current = 1, total = 1),
            response = players,
        )
    }

    override fun fetchFixturesOfLeague(
        leagueApiId: Long,
        season: Int,
    ): ApiSportsV3Envelope<ApiSportsFixture.OfLeague> {
        log.info("Mock fetching fixtures for league: $leagueApiId, season: $season")

        if (leagueApiId != SUPPORTED_LEAGUE_ID || season != SUPPORTED_SEASON) {
            log.warn(
                "Mock data only available for league $SUPPORTED_LEAGUE_ID (Premier League) season $SUPPORTED_SEASON. " +
                    "Requested: league $leagueApiId, season $season",
            )
            return ApiSportsV3Envelope(
                get = "fixtures",
                parameters = mapOf("league" to leagueApiId.toString(), "season" to season.toString()),
                errors = emptyList(),
                results = 0,
                paging = Paging(current = 1, total = 1),
                response = emptyList(),
            )
        }

        val fixtures = createMockPremierLeagueFixtures()

        return ApiSportsV3Envelope(
            get = "fixtures",
            parameters = mapOf("league" to leagueApiId.toString(), "season" to season.toString()),
            errors = emptyList(),
            results = fixtures.size,
            paging = Paging(current = 1, total = 1),
            response = fixtures,
        )
    }

    override fun fetchFixtureSingle(fixtureApiId: Long): ApiSportsV3Envelope<Single> {
        log.info("Mock fetching single fixture: $fixtureApiId")

        // 번들 기반 지원 확인
        val bundle = findBundleForFixture(fixtureApiId)
        if (bundle == null) {
            log.warn(
                "No mock data bundle found for fixture $fixtureApiId. " +
                    "Available bundles: ${getSupportedBundles().map {
                        "${it.name} (fixtures: ${it.supportedFixtureIds})"
                    }}",
            )
            return ApiSportsV3Envelope(
                get = "fixtures",
                parameters = mapOf("id" to fixtureApiId.toString()),
                errors = emptyList(),
                results = 0,
                paging = Paging(current = 1, total = 1),
                response = emptyList(),
            )
        }

        log.info("Using mock data bundle: ${bundle.name}")

        // JSON 파일 지원 확인
        if (bundle.hasJsonSupport() && bundle.supportsFixture(fixtureApiId)) {
            return fetchFixtureSingleFromJsonFile(fixtureApiId)
        }

        // 하드코딩된 데이터 반환 (기존 로직)
        if (fixtureApiId in SUPPORTED_FIXTURE_IDS) {
            val fixture = createMockSingleFixture(fixtureApiId)

            return ApiSportsV3Envelope(
                get = "fixtures",
                parameters = mapOf("id" to fixtureApiId.toString()),
                errors = emptyList(),
                results = 1,
                paging = Paging(current = 1, total = 1),
                response = listOf(fixture),
            )
        }

        return ApiSportsV3Envelope(
            get = "fixtures",
            parameters = mapOf("id" to fixtureApiId.toString()),
            errors = emptyList(),
            results = 0,
            paging = Paging(current = 1, total = 1),
            response = emptyList(),
        )
    }

    private fun createMockPremierLeagueTeams(): List<ApiSportsTeam.OfLeague> =
        listOf(
            // Manchester United (ID: 33)
            createMockTeam(33, "Manchester United", "MUN", "England", 1878, false),
            // Newcastle (ID: 34)
            createMockTeam(34, "Newcastle", "NEW", "England", 1892, false),
            // Bournemouth (ID: 35)
            createMockTeam(35, "Bournemouth", "BOU", "England", 1899, false),
            // Fulham (ID: 36)
            createMockTeam(36, "Fulham", "FUL", "England", 1879, false),
            // Wolves (ID: 39)
            createMockTeam(39, "Wolves", "WOL", "England", 1877, false),
            // Liverpool (ID: 40)
            createMockTeam(40, "Liverpool", "LIV", "England", 1892, false),
            // Southampton (ID: 41)
            createMockTeam(41, "Southampton", "SOU", "England", 1885, false),
            // Arsenal (ID: 42)
            createMockTeam(42, "Arsenal", "ARS", "England", 1886, false),
            // Manchester City (ID: 50)
            createMockTeam(50, "Manchester City", "MCI", "England", 1880, false),
            // Aston Villa (ID: 66)
            createMockTeam(66, "Aston Villa", "AVL", "England", 1874, false),
            // Ipswich (ID: 57)
            createMockTeam(57, "Ipswich", "IPS", "England", 1878, false),
        )

    private fun createMockTeam(
        id: Int,
        name: String,
        code: String,
        country: String,
        founded: Int,
        national: Boolean,
    ): ApiSportsTeam.OfLeague =
        ApiSportsTeam.OfLeague(
            team =
                ApiSportsTeam.OfLeague.TeamDetail(
                    id = id,
                    name = name,
                    code = code,
                    country = country,
                    founded = founded,
                    national = national,
                    logo = "https://media.api-sports.io/football/teams/$id.png",
                ),
            venue =
                ApiSportsTeam.OfLeague.VenueDetail(
                    id = id * 100,
                    name = "$name Stadium",
                    address = "$name Address",
                    city = country,
                    capacity = 50000,
                    surface = "grass",
                    image = "https://media.api-sports.io/football/venues/${id * 100}.png",
                ),
        )

    private fun createMockManchesterCityPlayers(): List<ApiSportsPlayer.OfTeam> {
        val teamInfo =
            ApiSportsPlayer.OfTeam.TeamInfo(
                id = SUPPORTED_TEAM_ID,
                name = "Manchester City",
                logo = "https://media.api-sports.io/football/teams/$SUPPORTED_TEAM_ID.png",
            )

        val players =
            listOf(
                createMockPlayerInfo(1, "Ederson", "Goalkeeper", 30),
                createMockPlayerInfo(2, "Kyle Walker", "Defender", 33),
                createMockPlayerInfo(3, "Erling Haaland", "Attacker", 23),
                createMockPlayerInfo(4, "Kevin De Bruyne", "Midfielder", 32),
                createMockPlayerInfo(5, "Phil Foden", "Midfielder", 23),
            )

        return listOf(
            ApiSportsPlayer.OfTeam(
                team = teamInfo,
                players = players,
            ),
        )
    }

    private fun createMockPlayerInfo(
        id: Long,
        name: String,
        position: String,
        age: Int,
    ): ApiSportsPlayer.OfTeam.PlayerInfo =
        ApiSportsPlayer.OfTeam.PlayerInfo(
            id = id,
            name = name,
            age = age,
            number = id.toInt(),
            position = position,
            photo = "https://media.api-sports.io/football/players/$id.png",
        )

    private fun createMockPremierLeagueFixtures(): List<ApiSportsFixture.OfLeague> =
        listOf(
            // Manchester United vs Fulham
            createMockFixtureOfLeague(
                fixtureId = 1208021L,
                homeTeamId = 33,
                homeTeamName = "Manchester United",
                awayTeamId = 36,
                awayTeamName = "Fulham",
                homeGoals = 1,
                awayGoals = 0,
                round = "Regular Season - 1",
                date = OffsetDateTime.parse("2024-08-16T19:00:00+00:00"),
                timestamp = 1723834800L,
            ),
            // Ipswich vs Liverpool
            createMockFixtureOfLeague(
                fixtureId = 1208022L,
                homeTeamId = 57,
                homeTeamName = "Ipswich",
                awayTeamId = 40,
                awayTeamName = "Liverpool",
                homeGoals = 0,
                awayGoals = 2,
                round = "Regular Season - 1",
                date = OffsetDateTime.parse("2024-08-17T11:30:00+00:00"),
                timestamp = 1723894200L,
            ),
            // Newcastle vs Southampton
            createMockFixtureOfLeague(
                fixtureId = 1208025L,
                homeTeamId = 34,
                homeTeamName = "Newcastle",
                awayTeamId = 41,
                awayTeamName = "Southampton",
                homeGoals = 2,
                awayGoals = 1,
                round = "Regular Season - 1",
                date = OffsetDateTime.parse("2024-08-17T14:00:00+00:00"),
                timestamp = 1723903200L,
            ),
            // Arsenal vs Wolves
            createMockFixtureOfLeague(
                fixtureId = 1208028L,
                homeTeamId = 42,
                homeTeamName = "Arsenal",
                awayTeamId = 39,
                awayTeamName = "Wolves",
                homeGoals = 3,
                awayGoals = 0,
                round = "Regular Season - 1",
                date = OffsetDateTime.parse("2024-08-17T16:30:00+00:00"),
                timestamp = 1723912200L,
            ),
            // Manchester United vs Aston Villa
            createMockFixtureOfLeague(
                fixtureId = 1208397L,
                homeTeamId = 33,
                homeTeamName = "Manchester United",
                awayTeamId = 66,
                awayTeamName = "Aston Villa",
                homeGoals = 2,
                awayGoals = 0,
                round = "Regular Season - 38",
                date = OffsetDateTime.parse("2025-05-26T00:00:00+09:00"),
                timestamp = 1748185200L,
            ),
        )

    private fun createMockFixtureOfLeague(
        fixtureId: Long,
        homeTeamId: Int,
        homeTeamName: String,
        awayTeamId: Int,
        awayTeamName: String,
        homeGoals: Int,
        awayGoals: Int,
        round: String,
        date: OffsetDateTime, // <- OffsetDateTime format
        timestamp: Long,
    ): ApiSportsFixture.OfLeague =
        ApiSportsFixture.OfLeague(
            fixture =
                ApiSportsFixture.OfLeague.Fixture(
                    id = fixtureId,
                    referee = "Mock Referee",
                    timezone = "UTC",
                    date = date,
                    timestamp = timestamp,
                    periods =
                        ApiSportsFixture.OfLeague.Fixture.Periods(
                            first = timestamp,
                            second = timestamp + 3600,
                        ),
                    venue =
                        ApiSportsFixture.OfLeague.Fixture.Venue(
                            id = 556,
                            name = "Mock Stadium",
                            city = "Mock City",
                        ),
                    status =
                        ApiSportsFixture.OfLeague.Fixture.Status(
                            long = "Match Finished",
                            short = "FT",
                            elapsed = 90,
                            extra = null,
                        ),
                ),
            league =
                ApiSportsFixture.OfLeague.League(
                    id = SUPPORTED_LEAGUE_ID,
                    name = "Premier League",
                    country = "England",
                    logo = "https://media.api-sports.io/football/leagues/39.png",
                    flag = "https://media.api-sports.io/flags/gb-eng.svg",
                    season = SUPPORTED_SEASON,
                    round = round,
                    standings = true,
                ),
            teams =
                ApiSportsFixture.OfLeague.Teams(
                    home =
                        ApiSportsFixture.OfLeague.Teams.Team(
                            id = homeTeamId,
                            name = homeTeamName,
                            logo = "https://media.api-sports.io/football/teams/$homeTeamId.png",
                            winner = homeGoals > awayGoals,
                        ),
                    away =
                        ApiSportsFixture.OfLeague.Teams.Team(
                            id = awayTeamId,
                            name = awayTeamName,
                            logo = "https://media.api-sports.io/football/teams/$awayTeamId.png",
                            winner = awayGoals > homeGoals,
                        ),
                ),
            goals =
                ApiSportsFixture.OfLeague.Goals(
                    home = homeGoals,
                    away = awayGoals,
                ),
            score =
                ApiSportsFixture.OfLeague.Score(
                    halftime =
                        ApiSportsFixture.OfLeague.Score.Pair(
                            home = homeGoals,
                            away = awayGoals,
                        ),
                    fulltime =
                        ApiSportsFixture.OfLeague.Score.Pair(
                            home = homeGoals,
                            away = awayGoals,
                        ),
                    extratime = null,
                    penalty = null,
                ),
        )

    private fun createMockSingleFixture(fixtureApiId: Long): Single {
        // For now, return a detailed fixture for the last fixture ID (1208397)
        // This matches the detailed fixture data from apisports_fixture.json
        return Single(
            fixture =
                Single.Fixture(
                    id = fixtureApiId,
                    referee = "T. Bramall",
                    timezone = "Asia/Seoul",
                    date = OffsetDateTime.parse("2025-05-26T00:00:00+09:00"),
                    timestamp = 1748185200L,
                    periods =
                        Single.Fixture.Periods(
                            first = 1748185200L,
                            second = 1748188800L,
                        ),
                    venue =
                        Single.Fixture.Venue(
                            id = 556,
                            name = "Old Trafford",
                            city = "Manchester",
                        ),
                    status =
                        Single.Fixture.Status(
                            long = "Match Finished",
                            short = "FT",
                            elapsed = 90,
                            extra = 8,
                        ),
                ),
            league =
                Single.League(
                    id = SUPPORTED_LEAGUE_ID,
                    name = "Premier League",
                    country = "England",
                    logo = "https://media.api-sports.io/football/leagues/39.png",
                    flag = "https://media.api-sports.io/flags/gb-eng.svg",
                    season = SUPPORTED_SEASON,
                    round = "Regular Season - 38",
                    standings = true,
                ),
            teams =
                Single.Teams(
                    home =
                        Single.Teams.Team(
                            id = 33,
                            name = "Manchester United",
                            logo = "https://media.api-sports.io/football/teams/33.png",
                            winner = true,
                        ),
                    away =
                        Single.Teams.Team(
                            id = 66,
                            name = "Aston Villa",
                            logo = "https://media.api-sports.io/football/teams/66.png",
                            winner = false,
                        ),
                ),
            goals =
                Single.Goals(
                    home = 2,
                    away = 0,
                ),
            score =
                Single.Score(
                    halftime =
                        Single.Score.Pair(
                            home = 0,
                            away = 0,
                        ),
                    fulltime =
                        Single.Score.Pair(
                            home = 2,
                            away = 0,
                        ),
                    extratime = null,
                    penalty = null,
                ),
            events = createMockEvents(),
            lineups = createMockLineups(),
            statistics = createMockStatistics(),
            players = createMockPlayers(),
        )
    }

    private fun createMockEvents(): List<Single.Event> =
        listOf(
            Single.Event(
                time =
                    Single.Event.Time(
                        elapsed = 20,
                        extra = null,
                    ),
                team =
                    Single.Event.Team(
                        id = 33,
                        name = "Manchester United",
                        logo = "https://media.api-sports.io/football/teams/33.png",
                    ),
                player =
                    Single.Event.Player(
                        id = 545,
                        name = "N. Mazraoui",
                    ),
                assist =
                    Single.Event.Player(
                        id = 886,
                        name = "Diogo Dalot",
                    ),
                type = "subst",
                detail = "Substitution 1",
                comments = null,
            ),
            Single.Event(
                time =
                    Single.Event.Time(
                        elapsed = 76,
                        extra = null,
                    ),
                team =
                    Single.Event.Team(
                        id = 33,
                        name = "Manchester United",
                        logo = "https://media.api-sports.io/football/teams/33.png",
                    ),
                player =
                    Single.Event.Player(
                        id = 157997,
                        name = "A. Diallo",
                    ),
                assist =
                    Single.Event.Player(
                        id = 1485,
                        name = "Bruno Fernandes",
                    ),
                type = "Goal",
                detail = "Normal Goal",
                comments = null,
            ),
        )

    private fun createMockLineups(): List<Single.Lineup> =
        listOf(
            Single.Lineup(
                team =
                    LineupTeam(
                        id = 33,
                        name = "Manchester United",
                        logo = "https://media.api-sports.io/football/teams/33.png",
                        colors =
                            LineupTeam.Colors(
                                player =
                                    LineupTeam.Colors.ColorDetail(
                                        primary = "ea0000",
                                        number = "ffffff",
                                        border = "ea0000",
                                    ),
                                goalkeeper =
                                    LineupTeam.Colors.ColorDetail(
                                        primary = "b356f6",
                                        number = "ffffff",
                                        border = "b356f6",
                                    ),
                            ),
                    ),
                coach =
                    Single.Lineup.Coach(
                        id = 4720,
                        name = "Ruben Amorim",
                        photo = "https://media.api-sports.io/football/coachs/4720.png",
                    ),
                formation = "3-4-2-1",
                startXI =
                    listOf(
                        Single.Lineup.LineupPlayer(
                            player =
                                Single.Lineup.LineupPlayer.Player(
                                    id = 50132,
                                    name = "A. Bayındır",
                                    number = 1,
                                    pos = "G",
                                    grid = "1:1",
                                ),
                        ),
                    ),
                substitutes =
                    listOf(
                        Single.Lineup.LineupPlayer(
                            player =
                                Single.Lineup.LineupPlayer.Player(
                                    id = 886,
                                    name = "Diogo Dalot",
                                    number = 20,
                                    pos = "D",
                                    grid = null,
                                ),
                        ),
                    ),
            ),
        )

    private fun createMockStatistics(): List<Single.TeamStatistics> =
        listOf(
            Single.TeamStatistics(
                team =
                    Single.Event.Team(
                        id = 33,
                        name = "Manchester United",
                        logo = "https://media.api-sports.io/football/teams/33.png",
                    ),
                statistics =
                    listOf(
                        Single.TeamStatistics.StatItem("Shots on Goal", "2"),
                        Single.TeamStatistics.StatItem("Shots off Goal", "3"),
                        Single.TeamStatistics.StatItem("Total Shots", "5"),
                        Single.TeamStatistics.StatItem("Blocked Shots", "1"),
                        Single.TeamStatistics.StatItem("Shots insidebox", "3"),
                        Single.TeamStatistics.StatItem("Shots outsidebox", "2"),
                        Single.TeamStatistics.StatItem("Fouls", "10"),
                        Single.TeamStatistics.StatItem("Corner Kicks", "4"),
                        Single.TeamStatistics.StatItem("Offsides", "2"),
                        Single.TeamStatistics.StatItem("Ball Possession", "67%"),
                        Single.TeamStatistics.StatItem("Yellow Cards", "1"),
                        Single.TeamStatistics.StatItem("Red Cards", "0"),
                        Single.TeamStatistics.StatItem("Goalkeeper Saves", "1"),
                        Single.TeamStatistics.StatItem("Total passes", "300"),
                        Single.TeamStatistics.StatItem("Passes accurate", "250"),
                        Single.TeamStatistics.StatItem("Passes %", "83%"),
                        Single.TeamStatistics.StatItem("expected_goals", "1.95"),
                        Single.TeamStatistics.StatItem("goals_prevented", "0"),
                    ),
            ),
        )

    private fun createMockPlayers(): List<PlayerStatistics> =
        listOf(
            PlayerStatistics(
                team =
                    Single.PlayerStatistics.TeamOfPlayerStat(
                        id = 33,
                        name = "Manchester United",
                        logo = "https://media.api-sports.io/football/teams/33.png",
                    ),
                players =
                    listOf(
                        PlayerStatistics.Player(
                            player =
                                PlayerStatistics.Player.Detail(
                                    id = 50132,
                                    name = "Altay Bayındır",
                                    photo = "https://media.api-sports.io/football/players/50132.png",
                                ),
                            statistics =
                                listOf(
                                    PlayerStatistics.Player.StatDetail(
                                        games =
                                            PlayerStatistics.Player.StatDetail.GameStats(
                                                minutes = 90,
                                                number = 1,
                                                position = "G",
                                                rating = "7",
                                                captain = false,
                                                substitute = false,
                                            ),
                                        offsides = null,
                                        shots =
                                            PlayerStatistics.Player.StatDetail.ShotStats(
                                                total = null,
                                                on = null,
                                            ),
                                        goals =
                                            PlayerStatistics.Player.StatDetail.GoalStats(
                                                total = null,
                                                conceded = 0,
                                                assists = 0,
                                                saves = 1,
                                            ),
                                        passes =
                                            PlayerStatistics.Player.StatDetail.PassStats(
                                                total = 21,
                                                key = null,
                                                accuracy = "13",
                                            ),
                                        tackles =
                                            PlayerStatistics.Player.StatDetail.TackleStats(
                                                total = null,
                                                blocks = null,
                                                interceptions = null,
                                            ),
                                        duels =
                                            PlayerStatistics.Player.StatDetail.DuelStats(
                                                total = 1,
                                                won = 1,
                                            ),
                                        dribbles =
                                            PlayerStatistics.Player.StatDetail.DribbleStats(
                                                attempts = null,
                                                success = null,
                                                past = null,
                                            ),
                                        fouls =
                                            PlayerStatistics.Player.StatDetail.FoulStats(
                                                drawn = 1,
                                                committed = null,
                                            ),
                                        cards =
                                            PlayerStatistics.Player.StatDetail.CardStats(
                                                yellow = 0,
                                                red = 0,
                                            ),
                                        penalty =
                                            PlayerStatistics.Player.StatDetail.PenaltyStats(
                                                won = null,
                                                commited = null,
                                                scored = 0,
                                                missed = 0,
                                                saved = 0,
                                            ),
                                    ),
                                ),
                        ),
                    ),
            ),
        )

    /**
     * JSON 파일에서 fixture 데이터를 읽어서 반환합니다.
     */
    private fun fetchFixtureSingleFromJsonFile(fixtureApiId: Long): ApiSportsV3Envelope<Single> {
        val resourcePath = resolvePathOfFixtureSingle(fixtureApiId)
        val rawString = readFile(resourcePath)

        try {
            val jsonNode = objectMapper.readTree(rawString)
            val responseArray = jsonNode.get("response")

            if (responseArray.isArray && responseArray.size() > 0) {
                val firstResponse = responseArray.get(0)
                val fixture = objectMapper.treeToValue(firstResponse, ApiSportsFixture.Single::class.java)

                return ApiSportsV3Envelope(
                    get = jsonNode.get("get").asText(),
                    parameters =
                        objectMapper.convertValue(
                            jsonNode.get("parameters"),
                            object : TypeReference<Map<String, String>>() {},
                        ),
                    errors =
                        objectMapper.convertValue(
                            jsonNode.get("errors"),
                            object : TypeReference<List<String>>() {},
                        ),
                    results = jsonNode.get("results").asInt(),
                    paging = objectMapper.treeToValue(jsonNode.get("paging"), Paging::class.java),
                    response = listOf(fixture),
                )
            } else {
                return ApiSportsV3Envelope(
                    get = jsonNode.get("get").asText(),
                    parameters =
                        objectMapper.convertValue(
                            jsonNode.get("parameters"),
                            object : TypeReference<Map<String, String>>() {},
                        ),
                    errors =
                        objectMapper.convertValue(
                            jsonNode.get("errors"),
                            object : TypeReference<List<String>>() {},
                        ),
                    results = jsonNode.get("results").asInt(),
                    paging = objectMapper.treeToValue(jsonNode.get("paging"), Paging::class.java),
                    response = emptyList(),
                )
            }
        } catch (e: IOException) {
            log.error("Mock data parsing error for fixture $fixtureApiId: $resourcePath", e)
            throw RuntimeException("Mock data parsing error: $resourcePath", e)
        }
    }

    /**
     * JSON 파일을 읽어서 문자열로 반환합니다.
     */
    private fun readFile(path: String): String =
        try {
            val file = ClassPathResource(path).file
            Files.readString(file.toPath())
        } catch (e: IOException) {
            throw RuntimeException("Mock data file reading error: $path", e)
        }

    /**
     * fixture ID에 해당하는 JSON 파일 경로를 반환합니다.
     */
    private fun resolvePathOfFixtureSingle(fixtureId: Long): String = "/devdata/mockapiv2/fixture_$fixtureId.json"

    /**
     * JSON 파일에서 팀 데이터를 읽어서 반환합니다.
     */
    private fun fetchTeamsOfLeagueFromJsonFile(
        leagueApiId: Long,
        season: Int,
    ): ApiSportsV3Envelope<ApiSportsTeam.OfLeague> {
        val resourcePath = resolvePathOfTeamsOfLeague(leagueApiId, season)
        val rawString = readFile(resourcePath)

        try {
            val jsonNode = objectMapper.readTree(rawString)
            val responseArray = jsonNode.get("response")

            if (responseArray.isArray && responseArray.size() > 0) {
                val teams =
                    objectMapper.convertValue(
                        responseArray,
                        object : TypeReference<List<ApiSportsTeam.OfLeague>>() {},
                    )

                return ApiSportsV3Envelope(
                    get = jsonNode.get("get").asText(),
                    parameters =
                        objectMapper.convertValue(
                            jsonNode.get("parameters"),
                            object : TypeReference<Map<String, String>>() {},
                        ),
                    errors =
                        objectMapper.convertValue(
                            jsonNode.get("errors"),
                            object : TypeReference<List<String>>() {},
                        ),
                    results = jsonNode.get("results").asInt(),
                    paging = objectMapper.treeToValue(jsonNode.get("paging"), Paging::class.java),
                    response = teams,
                )
            } else {
                return ApiSportsV3Envelope(
                    get = jsonNode.get("get").asText(),
                    parameters =
                        objectMapper.convertValue(
                            jsonNode.get("parameters"),
                            object : TypeReference<Map<String, String>>() {},
                        ),
                    errors =
                        objectMapper.convertValue(
                            jsonNode.get("errors"),
                            object : TypeReference<List<String>>() {},
                        ),
                    results = jsonNode.get("results").asInt(),
                    paging = objectMapper.treeToValue(jsonNode.get("paging"), Paging::class.java),
                    response = emptyList(),
                )
            }
        } catch (e: IOException) {
            log.error("Mock data parsing error for teams of league $leagueApiId season $season: $resourcePath", e)
            throw RuntimeException("Mock data parsing error: $resourcePath", e)
        }
    }

    /**
     * 팀 데이터 JSON 파일 경로를 반환합니다.
     */
    private fun resolvePathOfTeamsOfLeague(
        leagueId: Long,
        season: Int,
    ): String = "/devdata/mockapiv2/teamsOfLeague_leagueId${leagueId}_season$season.json"
}
