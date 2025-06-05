# Footballay API 리팩토링 아키텍처

## 전체 아키텍처 다이어그램

```
+------------------+     +------------------+     +------------------+
|   Application    |     |     Domain      |     | Infrastructure  |
|      Layer       |     |      Layer      |     |      Layer      |
+------------------+     +------------------+     +------------------+
|                  |     |                  |     |                  |
|  DataProvider    |     |  Entity Models   |     |  API Providers   |
|  Manager         |     |  - League        |     |  - ApiSports     |
|  DataFusion      |     |  - Team          |     |  - Sportmonks    |
|  Service         |     |  - Player        |     |                  |
|                  |     |  - Fixture       |     |                  |
|                  |     |                  |     |                  |
+------------------+     +------------------+     +------------------+
         |                       |                        |
         |                       |                        |
         v                       v                        v
+------------------+     +------------------+     +------------------+
|    Database      |     |    External     |     |    External     |
|      Layer       |     |     APIs        |     |     APIs        |
+------------------+     +------------------+     +------------------+
|                  |     |                  |     |                  |
|  Common Tables   |     |  API-Sports      |     |  Sportmonks      |
|  - leagues       |     |  API             |     |  API             |
|  - teams         |     |                  |     |                  |
|  - players       |     |                  |     |                  |
|  - fixtures      |     |                  |     |                  |
|                  |     |                  |     |                  |
|  API Tables      |     |                  |     |                  |
|  - league_*      |     |                  |     |                  |
|  - team_*        |     |                  |     |                  |
|  - player_*      |     |                  |     |                  |
|  - fixture_*     |     |                  |     |                  |
+------------------+     +------------------+     +------------------+

## 데이터 흐름

1. 데이터 수집 (Polling)
   ┌─────────────┐
   │  Scheduler  │
   └──────┬──────┘
          │
          ▼
2. API Provider
   ┌─────────────────────┐
   │  Data Fetching      │
   │  - Primary          │
   │  - Fallback         │
   └──────────┬──────────┘
              │
              ▼
3. 데이터 저장
   ┌─────────────────────┐
   │  Data Persistence   │
   │  - Common Table     │
   │  - API Table        │
   └──────────┬──────────┘
              │
              ▼
4. 클라이언트 요청
   ┌─────────────┐
   │  Request    │
   └──────┬──────┘
          │
          ▼
5. 데이터 조회
   ┌─────────────────────┐
   │  Data Retrieval     │
   │  - From DB          │
   └──────────┬──────────┘
              │
              ▼
6. Response
   ┌─────────────┐
   │  Response   │
   └─────────────┘

## 엔티티 관계

League (1) ────────┐
                  │
                  ▼
Team (N) ◀─────── Fixture (1) ────────▶ Team (N)
                  │
                  ▼
Player (N) ◀──── PlayerStatistics (1) ─────▶ Fixture (1)

## API Provider 구조

DataProvider (Interface)
    ├── ApiSportsProvider
    │   ├── League API
    │   ├── Team API
    │   ├── Player API
    │   └── Fixture API
    │
    └── SportmonksProvider
        ├── League API
        ├── Team API
        ├── Player API
        └── Fixture API

## 데이터 동기화 흐름

1. 스케줄러 작업
   ┌─────────────┐
   │  Scheduler  │
   └──────┬──────┘
          │
          ▼
2. API 호출
   ┌─────────────────────┐
   │  API Call           │
   │  - Polling          │
   └──────────┬──────────┘
              │
              ▼
3. 데이터 변환
   ┌─────────────────────┐
   │  Data Conversion    │
   │  - API → Domain     │
   └──────────┬──────────┘
              │
              ▼
4. 엔티티 매핑
   ┌─────────────────────┐
   │  Entity Mapping     │
   │  - Common Entity    │
   │  - API Entity       │
   └──────────┬──────────┘
              │
              ▼
5. 데이터 저장
   ┌─────────────────────┐
   │  Data Persistence   │
   │  - Common Table     │
   │  - API Table        │
   └─────────────────────┘
```
