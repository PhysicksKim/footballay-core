# ApiSports Mock Data Guide

이 디렉토리는 실제 ApiSports API 호출을 대체하는 Mock 데이터를 포함합니다.

## 🎯 목적

-   테스트 시 실제 API 호출 제한 방지
-   일관된 테스트 데이터로 안정적인 테스트 환경 제공
-   개발 환경에서 빠른 피드백 제공

## 📋 지원되는 Mock 데이터

### 1. Current Leagues (`currentLeagues.json`)

-   **총 1,186개** 리그 데이터
-   다양한 시즌과 국가의 리그 정보
-   **주요 리그**: Premier League (ID: 39), La Liga, Serie A 등

### 2. Teams of League (`teamsOfLeagueBySeason.json`)

-   **지원 리그**: Premier League (ID: 39)
-   **지원 시즌**: 2024
-   **팀 수**: 20개 팀
-   **포함 데이터**: 팀 정보, 경기장 정보

### 3. Squad of Team (`squadOfTeam.json`)

-   **지원 팀**: Manchester City (ID: 50)
-   **선수 수**: 약 25-30명
-   **포함 데이터**: 선수 기본 정보, 포지션, 등번호

### 4. Fixtures (`fixturesOfLeagueBySeason.json`, `fixtureByFixtureid.json`)

-   Premier League 2024 시즌 경기 데이터
-   개별 경기 상세 정보

## 🔧 사용 방법

### 프로필 활성화

테스트에서 Mock API를 사용하려면 `mocks` 프로필을 활성화하세요:

```kotlin
@ActiveProfiles("dev", "mocks")
```

### 지원되는 테스트 시나리오

```kotlin
// ✅ 지원되는 호출
apiSportsSyncFacade.syncCurrentLeagues()                    // 모든 리그
apiSportsSyncFacade.syncTeamsOfLeague(39L, 2024)           // 프리미어 리그
apiSportsSyncFacade.syncPlayersOfTeam(50L)                 // 맨체스터 시티
apiSportsSyncFacade.syncTeamsOfLeagueWithCurrentSeason(39L) // 프리미어 리그 현재 시즌

// ❌ 지원되지 않는 호출 (빈 응답 반환)
apiSportsSyncFacade.syncTeamsOfLeague(1L, 2024)            // 다른 리그
apiSportsSyncFacade.syncPlayersOfTeam(33L)                 // 다른 팀
```

## ⚠️ 제한사항

1. **제한된 데이터**: 위에 명시된 ID와 시즌만 지원
2. **정적 데이터**: 실제 API와 달리 시간에 따른 변화 없음
3. **부분 지원**: 모든 API 엔드포인트가 완전히 구현되지 않음

## 🔄 데이터 업데이트

Mock 데이터를 업데이트하려면:

1. `.ai/data/apisports/` 디렉토리의 JSON 파일 수정
2. 이 디렉토리로 파일 복사: `cp .ai/data/apisports/* src/main/resources/mock/apisports/`
3. `ApiSportsV3MockFetcher.kt`의 상수 값 필요시 업데이트

## 🧪 테스트 예시

```kotlin
@Test
fun `mock API를 사용한 통합 테스트`() {
    // Given - Mock API는 자동으로 프리미어 리그 데이터 반환
    val syncedCount = apiSportsSyncFacade.syncTeamsOfLeague(39L, 2024)

    // Then - 20개 팀이 동기화됨
    assertEquals(20, syncedCount)
}
```
