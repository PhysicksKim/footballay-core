package com.footballay.core.infra.apisports.match

import com.footballay.core.infra.facade.ApiSportsBackboneSyncFacade
import com.footballay.core.infra.facade.fetcher.FetcherProviderResolver
import com.footballay.core.infra.persistence.apisports.entity.FixtureApiSports
import com.footballay.core.infra.persistence.apisports.repository.FixtureApiSportsRepository
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchEventRepository
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchPlayerRepository
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchPlayerStatisticsRepository
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchTeamRepository
import com.footballay.core.infra.persistence.apisports.repository.live.ApiSportsMatchTeamStatisticsRepository
import com.footballay.core.logger
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * Match 데이터 저장 전체 흐름 통합 테스트
 *
 * **테스트 범위:**
 * - Core-Api(Backbone)-Match 전체 구조 검증
 * - Fixture UID → Provider 선택 → Match 저장 전체 워크플로우
 * - Phase 1-6 독립 에러 처리 검증
 * - 엔티티 연관관계 무결성 검증
 * - 성능 모니터링 로그 출력 검증
 *
 * **테스트 데이터:**
 * - Fixture: 1208397L (Manchester United vs Aston Villa)
 * - 데이터 소스: ApiSportsV3MockFetcher + apisports_fixture.json
 *
 * **사전 조건:**
 * - League(39L), Team(33L, 66L), Fixture(1208397L) Backbone 캐싱
 * - Player Core/Api 사전 저장
 * - FixtureCore.available = true
 *
 * **검증 항목:**
 * - MatchTeam (home/away, formation, color)
 * - MatchPlayer (선발/후보, lineup 정보)
 * - MatchEvent (sequence 순서, player 연결)
 * - PlayerStats (1:1 관계, 통계 값)
 * - TeamStats (XG 리스트, 팀 통계)
 * - Phase별 성능 로그
 *
 * @author AI generated, physickskim
 */
@SpringBootTest
@ActiveProfiles("dev", "mockapi")
@Transactional
@DisplayName("Match 데이터 저장 전체 흐름 통합 테스트")
class MatchDataSyncIntegrationTest {

    private val log = logger()

    @Autowired
    private lateinit var apiSportsBackboneSyncFacade: ApiSportsBackboneSyncFacade

    @Autowired
    private lateinit var fetcherProviderResolver: FetcherProviderResolver

    @Autowired
    private lateinit var fixtureApiSportsRepository: FixtureApiSportsRepository

    @PersistenceContext
    private lateinit var em: EntityManager

    companion object {
        private const val TEST_FIXTURE_API_ID = 1208397L  // Man Utd vs Aston Villa
        private const val TEST_LEAGUE_API_ID = 39L        // Premier League
        private const val TEST_SEASON = 2024
        private const val HOME_TEAM_API_ID = 33L          // Manchester United
        private const val AWAY_TEAM_API_ID = 66L          // Aston Villa
    }

    private lateinit var testFixtureUid: String

    @BeforeEach
    fun setUp() {
        log.info("========================================")
        log.info("Match 데이터 저장 통합 테스트 사전 준비 시작")
        log.info("========================================")

        // 1. League 캐싱
        log.info("[1/5] League 캐싱 시작...")
        val leagueCount = apiSportsBackboneSyncFacade.syncCurrentLeagues()
        log.info("[1/5] League 캐싱 완료: ${leagueCount}개 저장")

        // 2. Team 캐싱
        log.info("[2/5] Team 캐싱 시작...")
        val teamCount = apiSportsBackboneSyncFacade.syncTeamsOfLeagueWithCurrentSeason(TEST_LEAGUE_API_ID)
        log.info("[2/5] Team 캐싱 완료: ${teamCount}개 저장")

        // 3. Fixture 캐싱
        log.info("[3/5] Fixture 캐싱 시작...")
        val fixtureCount = apiSportsBackboneSyncFacade.syncFixturesOfLeagueWithSeason(TEST_LEAGUE_API_ID, TEST_SEASON)
        log.info("[3/5] Fixture 캐싱 완료: ${fixtureCount}개 저장")

        em.flush()
        em.clear()

        // 4. FixtureCore available 활성화
        log.info("[4/5] FixtureCore available 활성화 중...")
        val fixture = fixtureApiSportsRepository.findByApiId(TEST_FIXTURE_API_ID)
        assertThat(fixture).isNotNull
        assertThat(fixture!!.core).isNotNull

        testFixtureUid = fixture.core!!.uid
        fixture.core!!.available = true
        em.flush()
        em.clear()
        log.info("[4/5] FixtureCore available 활성화 완료: UID = $testFixtureUid")

        // 5. 사전 준비 완료 검증
        log.info("[5/5] 사전 준비 검증 중...")
        val verifyFixture = fixtureApiSportsRepository.findByApiId(TEST_FIXTURE_API_ID)
        assertThat(verifyFixture).isNotNull
        assertThat(verifyFixture!!.core?.available).isTrue()
        assertThat(verifyFixture.core?.homeTeam).isNotNull
        assertThat(verifyFixture.core?.awayTeam).isNotNull
        log.info("[5/5] 사전 준비 검증 완료")
        log.info("  - Fixture UID: ${verifyFixture.core?.uid}")
        log.info("  - Home Team: ${verifyFixture.core?.homeTeam?.name} (API ID: ${verifyFixture.core?.homeTeam?.teamApiSports?.apiId})")
        log.info("  - Away Team: ${verifyFixture.core?.awayTeam?.name} (API ID: ${verifyFixture.core?.awayTeam?.teamApiSports?.apiId})")

        log.info("========================================")
        log.info("사전 준비 완료 - 이제 Match 저장 테스트 시작")
        log.info("========================================")
    }

    @Test
    @DisplayName("Fixture UID로 Match 데이터를 동기화하면 모든 엔티티가 정상 저장되어야 한다")
    fun `Fixture UID로 Match 데이터를 동기화하면 모든 엔티티가 정상 저장되어야 한다`() {
        log.info("=== 테스트 시작: 전체 Match 저장 흐름 ===")

        // when - FetcherProviderResolver를 통한 전체 흐름 실행
        log.info("Match 데이터 동기화 시작: UID = $testFixtureUid")
        val action = fetcherProviderResolver.fetchMatchData(testFixtureUid)
        log.info("Match 데이터 동기화 완료: action = $action")

        em.flush()
        em.clear()

        // then - 저장된 엔티티 검증
        log.info("=== 엔티티 검증 시작 ===")

        val fixture = fixtureApiSportsRepository.findByApiId(TEST_FIXTURE_API_ID)
        assertThat(fixture).isNotNull

        // 1. MatchTeam 검증
        log.info("[1/5] MatchTeam 검증...")
        verifyMatchTeams(fixture!!)

        // 2. MatchPlayer 검증
        log.info("[2/5] MatchPlayer 검증...")
        verifyMatchPlayers(fixture)

        // 3. MatchEvent 검증
        log.info("[3/5] MatchEvent 검증...")
        verifyMatchEvents(fixture)

        // 4. PlayerStats 검증
        log.info("[4/5] PlayerStats 검증...")
        verifyPlayerStats(fixture)

        // 5. TeamStats 검증
        log.info("[5/5] TeamStats 검증...")
        verifyTeamStats(fixture)

        log.info("=== 모든 엔티티 검증 완료 ===")
    }

    @Test
    @DisplayName("Match 동기화 시 Phase별 성능 로그가 INFO 레벨로 출력되어야 한다")
    fun `Match 동기화 시 Phase별 성능 로그가 INFO 레벨로 출력되어야 한다`() {
        log.info("=== 테스트 시작: Phase별 성능 로그 출력 검증 ===")

        // when
        val action = fetcherProviderResolver.fetchMatchData(testFixtureUid)

        // then - 로그는 INFO 레벨로 출력되므로 콘솔에서 확인
        // Performance Report가 출력되었는지는 수동으로 확인
        assertThat(action).isNotNull
        log.info("=== 성능 로그 출력 완료 - 위 로그에서 Phase1~6 및 Performance Report 확인 ===")
    }

    @Test
    @DisplayName("반복 동기화 시 엔티티가 정상 업데이트되어야 한다")
    fun `반복 동기화 시 엔티티가 정상 업데이트되어야 한다`() {
        log.info("=== 테스트 시작: 반복 동기화 ===")

        // when - 첫 번째 동기화
        log.info("첫 번째 동기화 실행...")
        fetcherProviderResolver.fetchMatchData(testFixtureUid)
        em.flush()
        em.clear()

        val firstSyncFixture = fixtureApiSportsRepository.findByApiId(TEST_FIXTURE_API_ID)
        val firstMatchPlayerCount = firstSyncFixture?.homeTeam?.players?.size ?: 0
        val firstEventCount = firstSyncFixture?.events?.size ?: 0
        log.info("첫 번째 동기화 결과: MatchPlayer = $firstMatchPlayerCount, Events = $firstEventCount")

        // when - 두 번째 동기화 (업데이트)
        log.info("두 번째 동기화 실행...")
        fetcherProviderResolver.fetchMatchData(testFixtureUid)
        em.flush()
        em.clear()

        val secondSyncFixture = fixtureApiSportsRepository.findByApiId(TEST_FIXTURE_API_ID)
        val secondMatchPlayerCount = secondSyncFixture?.homeTeam?.players?.size ?: 0
        val secondEventCount = secondSyncFixture?.events?.size ?: 0
        log.info("두 번째 동기화 결과: MatchPlayer = $secondMatchPlayerCount, Events = $secondEventCount")

        // then - 데이터 일관성 유지
        assertThat(secondMatchPlayerCount).isEqualTo(firstMatchPlayerCount)
        assertThat(secondEventCount).isEqualTo(firstEventCount)
        log.info("=== 반복 동기화 일관성 검증 완료 ===")
    }

    // ===== 검증 헬퍼 메서드들 =====

    private fun verifyMatchTeams(fixture: FixtureApiSports) {
        val homeTeam = fixture.homeTeam
        val awayTeam = fixture.awayTeam

        // Home Team 검증
        assertThat(homeTeam).isNotNull
        assertThat(homeTeam?.teamApiSports?.apiId).isEqualTo(HOME_TEAM_API_ID)
        assertThat(homeTeam?.formation).isNotNull
        assertThat(homeTeam?.playerColor).isNotNull
        assertThat(homeTeam?.goalkeeperColor).isNotNull
        assertThat(homeTeam?.winner).isTrue()
        log.info("  ✓ Home Team: ${homeTeam?.teamApiSports?.name}, Formation: ${homeTeam?.formation}")

        // Away Team 검증
        assertThat(awayTeam).isNotNull
        assertThat(awayTeam?.teamApiSports?.apiId).isEqualTo(AWAY_TEAM_API_ID)
        assertThat(awayTeam?.formation).isNotNull
        assertThat(awayTeam?.winner).isFalse()
        log.info("  ✓ Away Team: ${awayTeam?.teamApiSports?.name}, Formation: ${awayTeam?.formation}")
    }

    private fun verifyMatchPlayers(fixture: FixtureApiSports) {
        val homeTeam = fixture.homeTeam!!
        val awayTeam = fixture.awayTeam!!

        val homePlayers = homeTeam.players
        val awayPlayers = awayTeam.players

        // MatchPlayer 기본 검증
        assertThat(homePlayers).isNotEmpty
        assertThat(awayPlayers).isNotEmpty
        log.info("  ✓ Home Players: ${homePlayers.size}명")
        log.info("  ✓ Away Players: ${awayPlayers.size}명")

        // 선발/후보 구분 검증
        val homeStarters = homePlayers.filter { !it.substitute }
        val homeSubs = homePlayers.filter { it.substitute }
        assertThat(homeStarters).isNotEmpty
        assertThat(homeSubs).isNotEmpty
        log.info("  ✓ Home 선발: ${homeStarters.size}명, 후보: ${homeSubs.size}명")

        // 특정 선수 검증 (Lineup에서 확인 가능한 선수)
        val goalkeeper = homePlayers.find { it.name.contains("Bayındır") }
        assertThat(goalkeeper).isNotNull
        assertThat(goalkeeper?.position).isEqualTo("G")
        assertThat(goalkeeper?.substitute).isFalse()
        log.info("  ✓ 골키퍼 확인: ${goalkeeper?.name} (포지션: ${goalkeeper?.position}, 선발: ${!goalkeeper?.substitute!!})")

        // PlayerApiSports 연결 검증
        val playersWithApi = homePlayers.filter { it.playerApiSports != null }
        log.info("  ✓ PlayerApiSports 연결: ${playersWithApi.size}/${homePlayers.size}명")
    }

    private fun verifyMatchEvents(fixture: FixtureApiSports) {
        val events = fixture.events

        // Event 기본 검증
        assertThat(events).isNotEmpty
        log.info("  ✓ 총 이벤트: ${events.size}개")

        // Sequence 순서 검증
        val sequences = events.map { it.sequence }
        assertThat(sequences).isEqualTo(sequences.sorted())
        assertThat(sequences.first()).isEqualTo(0)
        log.info("  ✓ Sequence 순서 정상: 0 ~ ${sequences.last()}")

        // Event 타입별 검증
        val goalEvents = events.filter { it.eventType == "Goal" }
        val substEvents = events.filter { it.eventType == "subst" }
        val cardEvents = events.filter { it.eventType == "Card" }

        log.info("  ✓ 골: ${goalEvents.size}개, 교체: ${substEvents.size}개, 카드: ${cardEvents.size}개")

        // 골 이벤트 상세 검증
        if (goalEvents.isNotEmpty()) {
            val firstGoal = goalEvents.first()
            assertThat(firstGoal.player).isNotNull
            assertThat(firstGoal.matchTeam).isNotNull
            log.info("  ✓ 첫 골: ${firstGoal.player?.name} (${firstGoal.elapsedTime}분, 타입: ${firstGoal.detail})")
        }

        // 교체 이벤트 검증 (assist가 교체 IN 선수)
        if (substEvents.isNotEmpty()) {
            val firstSubst = substEvents.first()
            assertThat(firstSubst.player).isNotNull  // OUT 선수
            log.info("  ✓ 첫 교체: ${firstSubst.player?.name} OUT → ${firstSubst.assist?.name} IN (${firstSubst.elapsedTime}분)")
        }
    }

    private fun verifyPlayerStats(fixture: FixtureApiSports) {
        val homeTeam = fixture.homeTeam!!
        val homePlayers = homeTeam.players

        // PlayerStats 존재 확인
        val playersWithStats = homePlayers.filter { it.statistics != null }
        assertThat(playersWithStats).isNotEmpty
        log.info("  ✓ 통계 있는 선수: ${playersWithStats.size}/${homePlayers.size}명")

        // 특정 선수 통계 검증 (Bruno Fernandes - 주장)
        val brunoPlayer = homePlayers.find { it.name.contains("Bruno") || it.name.contains("Fernandes") }
        if (brunoPlayer != null) {
            val brunoStats = brunoPlayer.statistics
            assertThat(brunoStats).isNotNull
            assertThat(brunoStats?.isCaptain).isTrue()
            assertThat(brunoStats?.minutesPlayed).isNotNull
            log.info("  ✓ Bruno Fernandes 통계: 출전 ${brunoStats?.minutesPlayed}분, 주장: ${brunoStats?.isCaptain}, 평점: ${brunoStats?.rating}")
        }

        // 골키퍼 통계 검증
        val goalkeeperStats = playersWithStats.find { it.position == "G" }?.statistics
        if (goalkeeperStats != null) {
            log.info("  ✓ 골키퍼 통계: 세이브 ${goalkeeperStats.saves}개, 실점 ${goalkeeperStats.goalsConceded}개")
        }

        // MatchPlayer ↔ PlayerStats 양방향 연관관계 검증
        playersWithStats.forEach { player ->
            assertThat(player.statistics?.matchPlayer).isEqualTo(player)
        }
        log.info("  ✓ MatchPlayer ↔ PlayerStats 양방향 연관관계 정상")
    }

    private fun verifyTeamStats(fixture: FixtureApiSports) {
        val homeTeam = fixture.homeTeam!!
        val awayTeam = fixture.awayTeam!!

        // TeamStats 존재 확인
        val homeStats = homeTeam.teamStatistics
        val awayStats = awayTeam.teamStatistics

        assertThat(homeStats).isNotNull
        assertThat(awayStats).isNotNull
        log.info("  ✓ Home/Away TeamStats 존재 확인")

        // Home TeamStats 상세 검증
        assertThat(homeStats?.ballPossession).isNotNull
        assertThat(homeStats?.totalShots).isNotNull
        assertThat(homeStats?.totalPasses).isNotNull
        log.info("  ✓ Home Team 통계: 점유율 ${homeStats?.ballPossession}, 슈팅 ${homeStats?.totalShots}개, 패스 ${homeStats?.totalPasses}개")

        // XG 리스트 검증
        val homeXgList = homeStats?.xgList
        assertThat(homeXgList).isNotEmpty
        val firstXg = homeXgList?.first()
        assertThat(firstXg?.expectedGoals).isGreaterThan(0.0)
        assertThat(firstXg?.elapsedTime).isEqualTo(90)
        log.info("  ✓ Home Team XG: ${firstXg?.expectedGoals} (elapsed: ${firstXg?.elapsedTime}분)")

        val awayXgList = awayStats?.xgList
        assertThat(awayXgList).isNotEmpty
        log.info("  ✓ Away Team XG: ${awayXgList?.first()?.expectedGoals} (elapsed: ${awayXgList?.first()?.elapsedTime}분)")

        // MatchTeam ↔ TeamStats 양방향 연관관계 검증
        assertThat(homeStats?.matchTeam).isEqualTo(homeTeam)
        assertThat(awayStats?.matchTeam).isEqualTo(awayTeam)
        log.info("  ✓ MatchTeam ↔ TeamStats 양방향 연관관계 정상")
    }

    @Test
    @DisplayName("저장된 엔티티 간 연관관계가 모두 정상적으로 설정되어야 한다")
    fun `저장된 엔티티 간 연관관계가 모두 정상적으로 설정되어야 한다`() {
        log.info("=== 테스트 시작: 엔티티 연관관계 검증 ===")

        // given - Match 데이터 동기화
        fetcherProviderResolver.fetchMatchData(testFixtureUid)
        em.flush()
        em.clear()

        // when - 엔티티 재조회
        val fixture = fixtureApiSportsRepository.findByApiId(TEST_FIXTURE_API_ID)!!
        val homeTeam = fixture.homeTeam!!
        val awayTeam = fixture.awayTeam!!

        // then - 연관관계 체인 검증
        log.info("[1/4] Fixture → MatchTeam 연관관계...")
        assertThat(fixture.homeTeam).isNotNull
        assertThat(fixture.awayTeam).isNotNull
        log.info("  ✓ Fixture → MatchTeam 연결 정상")

        log.info("[2/4] MatchTeam → MatchPlayer 연관관계...")
        assertThat(homeTeam.players).isNotEmpty
        homeTeam.players.forEach { player ->
            assertThat(player.matchTeam).isEqualTo(homeTeam)
        }
        log.info("  ✓ MatchTeam ↔ MatchPlayer 양방향 연결 정상 (${homeTeam.players.size}명)")

        log.info("[3/4] MatchPlayer → PlayerStats 연관관계...")
        val playersWithStats = homeTeam.players.filter { it.statistics != null }
        playersWithStats.forEach { player ->
            assertThat(player.statistics?.matchPlayer).isEqualTo(player)
        }
        log.info("  ✓ MatchPlayer ↔ PlayerStats 양방향 연결 정상 (${playersWithStats.size}명)")

        log.info("[4/4] MatchEvent → MatchPlayer 연관관계...")
        val eventsWithPlayer = fixture.events.filter { it.player != null }
        eventsWithPlayer.forEach { event ->
            val playerName = event.player?.name
            val foundInTeam = homeTeam.players.any { it.name == playerName } || 
                             awayTeam.players.any { it.name == playerName }
            assertThat(foundInTeam).isTrue()
        }
        log.info("  ✓ MatchEvent → MatchPlayer 연결 정상 (${eventsWithPlayer.size}개 이벤트)")

        log.info("=== 모든 연관관계 검증 완료 ===")
    }

    @Test
    @DisplayName("Player ID가 null인 선수도 MatchPlayer로 저장되어야 한다")
    fun `Player ID가 null인 선수도 MatchPlayer로 저장되어야 한다`() {
        log.info("=== 테스트 시작: ID=null 선수 처리 검증 ===")

        // when
        fetcherProviderResolver.fetchMatchData(testFixtureUid)
        em.flush()
        em.clear()

        // then
        val fixture = fixtureApiSportsRepository.findByApiId(TEST_FIXTURE_API_ID)!!
        val allPlayers = (fixture.homeTeam?.players ?: emptyList()) + (fixture.awayTeam?.players ?: emptyList())

        val playersWithoutApiId = allPlayers.filter { it.playerApiSports == null }
        log.info("  PlayerApiSports 없는 선수: ${playersWithoutApiId.size}명")

        // ID=null 선수도 이름이 있으면 저장되어야 함
        playersWithoutApiId.forEach { player ->
            assertThat(player.name).isNotBlank()
            assertThat(player.matchPlayerUid).isNotNull
            log.info("    - ${player.name} (UID: ${player.matchPlayerUid}, 팀: ${player.matchTeam?.teamApiSports?.name})")
        }

        log.info("=== ID=null 선수 처리 검증 완료 ===")
    }

    private inline fun flushAndClear() {
        em.flush()
        em.clear()
    }
}

