# Footballay Mock Server for Frontend Development

프론트엔드 개발을 위한 Mock Server 가이드입니다.

## 📋 목차

- [개요](#개요)
- [아키텍처](#아키텍처)
- [빠른 시작](#빠른-시작)
- [사용 방법](#사용-방법)
- [시나리오 관리](#시나리오-관리)
- [API 엔드포인트](#api-엔드포인트)
- [트러블슈팅](#트러블슈팅)

---

## 개요

Footballay 프론트엔드 개발을 위한 두 가지 서버를 제공합니다:

| 서버 | 용도 | 포트 | 특징 |
|------|------|------|------|
| **Mock Server** | Desktop App 개발 | 8080 | Read-only, 시간 흐름 시뮬레이션 |
| **Dev Server** | Admin Page 개발 | 8081 | Full CRUD, 실제 비즈니스 로직 |

### Mock Server vs Dev Server

#### Mock Server (Desktop App용)
- ✅ **Read-only API**: 리그 목록, 경기 목록, 경기 상세 조회
- ✅ **시간 흐름 시뮬레이션**: 경기 진행 상황 자동 변화
  - 0분: 경기 시작
  - 21분: 첫 골
  - 45분: 하프타임
  - 90분: 경기 종료
- ✅ **Polling 테스트**: 17초마다 polling 시 변화하는 데이터 제공
- ❌ **CRUD 없음**: 데이터 수정/삭제 불가

#### Dev Server (Admin Page용)
- ✅ **Full CRUD**: Available League/Fixture 추가/삭제
- ✅ **실제 DB**: PostgreSQL + Redis
- ✅ **비즈니스 로직**: Quartz Job, 실제 저장 로직
- ✅ **Admin API**: 모든 관리 기능

---

## 아키텍처

```
┌─────────────────────────────────────────────────────┐
│ Frontend Applications                               │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Desktop App (Electron)                            │
│    └─> Mock Server (localhost:8080)                │
│        - 시간 흐름 시뮬레이션                        │
│        - Read-only                                  │
│                                                     │
│  Admin Page (Web)                                  │
│    └─> Dev Server (localhost:8081)                 │
│        - Full CRUD                                  │
│        - PostgreSQL + Redis                         │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## 빠른 시작

### 방법 1: Docker Compose (권장)

#### 1-1. Mock Server만 실행 (Desktop App 개발)

```bash
# Mock Server만 실행
docker-compose -f docker-compose.mock.yml up -d

# 로그 확인
docker-compose -f docker-compose.mock.yml logs -f

# 종료
docker-compose -f docker-compose.mock.yml down
```

**API 접근:**
- Mock Server: `http://localhost:8080`

#### 1-2. 전체 환경 실행 (Desktop App + Admin Page)

```bash
# Mock Server + Dev Server + PostgreSQL + Redis
docker-compose -f docker-compose.frontend-dev.yml up -d

# 로그 확인
docker-compose -f docker-compose.frontend-dev.yml logs -f mock-server
docker-compose -f docker-compose.frontend-dev.yml logs -f dev-server

# 종료
docker-compose -f docker-compose.frontend-dev.yml down
```

**API 접근:**
- Mock Server: `http://localhost:8080`
- Dev Server: `http://localhost:8081`
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`

### 방법 2: 로컬 실행 (개발자용)

#### Mock Server 실행

```bash
# Gradle로 실행
./gradlew bootRun --args='--spring.profiles.active=mockserver'

# 또는 JAR 빌드 후 실행
./gradlew bootJar
java -jar build/libs/footballay-core-*.jar --spring.profiles.active=mockserver
```

#### Dev Server 실행

```bash
# PostgreSQL, Redis 먼저 실행 필요
docker-compose -f docker-compose.frontend-dev.yml up -d postgres redis

# Gradle로 실행
./gradlew bootRun --args='--spring.profiles.active=dev'
```

---

## 사용 방법

### Desktop App 개발 흐름

```typescript
// 1. 리그 목록 조회
const leagues = await fetch('http://localhost:8080/api/football/leagues/available')
  .then(res => res.json());

// 2. 경기 목록 조회
const fixtures = await fetch('http://localhost:8080/api/football/fixtures?leagueId=4')
  .then(res => res.json());

// 3. 경기 상세 조회 (시간 흐름 시뮬레이션)
const fixtureId = 1145526;

// Polling 시작 (17초마다)
setInterval(async () => {
  const [info, events, lineup, stats] = await Promise.all([
    fetch(`http://localhost:8080/api/football/fixtures/info?fixtureId=${fixtureId}`).then(r => r.json()),
    fetch(`http://localhost:8080/api/football/fixtures/events?fixtureId=${fixtureId}`).then(r => r.json()),
    fetch(`http://localhost:8080/api/football/fixtures/lineup?fixtureId=${fixtureId}`).then(r => r.json()),
    fetch(`http://localhost:8080/api/football/fixtures/statistics?fixtureId=${fixtureId}`).then(r => r.json())
  ]);

  // UI 업데이트
  updateMatchUI(info, events, lineup, stats);

  // 경기 종료 시 polling 중지
  if (info.response[0].status === 'FT') {
    clearInterval(pollingInterval);
  }
}, 17000);
```

### Admin Page 개발 흐름

```typescript
// Dev Server 사용 (localhost:8081)

// 1. Available League 추가
await fetch('http://localhost:8081/api/admin/football/leagues/39/available', {
  method: 'POST'
});

// 2. Available League 목록 조회
const availableLeagues = await fetch('http://localhost:8081/api/admin/football/leagues/available')
  .then(res => res.json());

// 3. Fixtures 조회
const fixtures = await fetch('http://localhost:8081/api/admin/football/leagues/39/fixtures')
  .then(res => res.json());

// 4. Available Fixture 추가 (Quartz Job 생성)
await fetch('http://localhost:8081/api/admin/football/fixtures/1145526/available', {
  method: 'POST'
});

// 5. Available Fixture 삭제
await fetch('http://localhost:8081/api/admin/football/fixtures/1145526/available', {
  method: 'DELETE'
});
```

---

## 시나리오 관리

### 시나리오 파일 위치

```
src/main/resources/mockserver/scenarios/
├── leagues.json          # 리그 목록
├── fixtures.json         # 경기 목록
└── match-1145526.json    # 경기 시나리오 (Turkey vs Portugal)
```

### 시나리오 파일 구조

```json
{
  "fixtureId": 1145526,
  "name": "Turkey vs Portugal - Euro 2024",
  "description": "Exciting match with 3 goals from Portugal",
  "mode": "accelerated",
  "speedMultiplier": 1,  // 1초 = 1분 (가속 모드)
  "snapshots": [
    {
      "minute": 0,
      "status": "1H",
      "elapsed": 0,
      "info": { /* FixtureInfoResponse */ },
      "events": { /* FixtureEventsResponse */ },
      "lineup": { /* FixtureLineupResponse */ },
      "statistics": { /* MatchStatisticsResponse */ }
    },
    {
      "minute": 21,
      "status": "1H",
      "elapsed": 21,
      // ... 첫 골 발생
    },
    {
      "minute": 45,
      "status": "HT",
      "elapsed": 45,
      // ... 하프타임
    },
    {
      "minute": 90,
      "status": "FT",
      "elapsed": 90,
      // ... 경기 종료
    }
  ]
}
```

### 새 시나리오 추가

1. `src/main/resources/mockserver/scenarios/match-{fixtureId}.json` 파일 생성
2. 위 구조에 맞춰 스냅샷 작성
3. `fixtures.json`에 경기 추가
4. Mock Server 재시작

### 시간 흐름 조정

```json
{
  "speedMultiplier": 1  // 1초 = 1분 (90초면 경기 종료)
  "speedMultiplier": 5  // 1초 = 5분 (18초면 경기 종료)
  "speedMultiplier": 90 // 1초 = 90분 (1초면 경기 종료, 빠른 테스트용)
}
```

### 경기 시작 시간 리셋

```bash
# Mock Admin API로 경기 시작 시간 리셋
curl -X POST http://localhost:8080/api/football/mock/admin/fixtures/1145526/reset

# 응답
{
  "status": "success",
  "message": "Match start time reset for fixtureId=1145526"
}
```

---

## API 엔드포인트

### Mock Server (localhost:8080)

#### 리그 관련
```
GET /api/football/leagues/available
→ 이용 가능한 리그 목록 조회
```

#### 경기 목록
```
GET /api/football/fixtures?leagueId={leagueId}&date={date}
→ 가장 가까운 날짜의 경기 목록

GET /api/football/fixtures/date?leagueId={leagueId}&date={date}
→ 특정 날짜의 경기 목록

GET /api/football/fixtures/available?leagueId={leagueId}
→ Available 경기 목록
```

#### 경기 상세 (시간 흐름 시뮬레이션)
```
GET /api/football/fixtures/info?fixtureId={fixtureId}
→ 경기 기본 정보

GET /api/football/fixtures/events?fixtureId={fixtureId}
→ 경기 이벤트 (골, 카드, 교체 등)

GET /api/football/fixtures/lineup?fixtureId={fixtureId}
→ 경기 라인업

GET /api/football/fixtures/statistics?fixtureId={fixtureId}
→ 경기 통계
```

#### Mock Admin API
```
POST /api/football/mock/admin/fixtures/{fixtureId}/reset
→ 경기 시작 시간 리셋 (처음부터 다시 시뮬레이션)
```

### Dev Server (localhost:8081)

#### Admin API
```
GET    /api/admin/football/leagues/available
POST   /api/admin/football/leagues/{leagueId}/available
DELETE /api/admin/football/leagues/{leagueId}/available

GET    /api/admin/football/leagues/{leagueId}/fixtures
GET    /api/admin/football/leagues/{leagueId}/fixtures/available
POST   /api/admin/football/fixtures/{fixtureId}/available
DELETE /api/admin/football/fixtures/{fixtureId}/available
```

---

## 트러블슈팅

### 1. Mock Server 시작 실패

#### 문제: 포트 충돌
```bash
# 에러: Port 8080 already in use
```

**해결:**
```bash
# 포트 사용 중인 프로세스 확인
lsof -i :8080

# 프로세스 종료 또는 다른 포트 사용
docker-compose -f docker-compose.mock.yml down
```

#### 문제: 시나리오 파일 로드 실패
```bash
# 에러: Failed to load scenarios
```

**해결:**
```bash
# 시나리오 파일 위치 확인
ls -la src/main/resources/mockserver/scenarios/

# JSON 형식 검증
cat src/main/resources/mockserver/scenarios/match-1145526.json | jq .
```

### 2. Dev Server 시작 실패

#### 문제: PostgreSQL 연결 실패
```bash
# 에러: Connection refused
```

**해결:**
```bash
# PostgreSQL 상태 확인
docker-compose -f docker-compose.frontend-dev.yml ps postgres

# PostgreSQL 로그 확인
docker-compose -f docker-compose.frontend-dev.yml logs postgres

# PostgreSQL 재시작
docker-compose -f docker-compose.frontend-dev.yml restart postgres
```

### 3. 시간 흐름이 너무 빠름/느림

**해결:**
시나리오 파일의 `speedMultiplier` 조정 후 재시작

```json
{
  "speedMultiplier": 1  // 1초 = 1분 (권장)
}
```

### 4. Polling 데이터가 변하지 않음

**원인:** 경기가 이미 종료됨 (90분 경과)

**해결:**
```bash
# 경기 시작 시간 리셋
curl -X POST http://localhost:8080/api/football/mock/admin/fixtures/1145526/reset
```

---

## 개발 팁

### 1. Frontend 환경 변수 설정

```typescript
// Desktop App
const API_BASE_URL = process.env.NODE_ENV === 'development'
  ? 'http://localhost:8080'  // Mock Server
  : 'https://api.footballay.com';

// Admin Page
const API_BASE_URL = process.env.NODE_ENV === 'development'
  ? 'http://localhost:8081'  // Dev Server
  : 'https://api.footballay.com';
```

### 2. Polling 주기

실제 프로덕션과 동일하게 **17초** 주기 사용 권장:

```typescript
const POLLING_INTERVAL = 17000; // 17초
```

### 3. Error Handling

```typescript
try {
  const response = await fetch('http://localhost:8080/api/football/fixtures/info?fixtureId=1145526');

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }

  const data = await response.json();

  if (data.metaData.status !== 'SUCCESS') {
    console.error('API Error:', data.metaData.message);
  }

} catch (error) {
  console.error('Network Error:', error);
}
```

### 4. Response 구조

모든 API 응답은 `ApiResponse<T>` 형식:

```typescript
interface ApiResponse<T> {
  metaData: {
    requestId: string;
    timestamp: string;
    status: 'SUCCESS' | 'FAILURE';
    responseCode: number;
    message: string;
    requestUrl: string;
    params: Record<string, string>;
    version: string;
  };
  response: T[];  // 항상 배열
}
```

---

## 문의

문제가 발생하면 다음을 확인해주세요:

1. Docker 로그: `docker-compose logs -f`
2. API 응답: 브라우저 개발자 도구 Network 탭
3. Mock Server 로그: `LOGGING_LEVEL_COM_FOOTBALLAY_CORE_MOCKSERVER=DEBUG`

---

## 라이선스

Footballay Core © 2024
