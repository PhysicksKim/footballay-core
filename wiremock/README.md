# WireMock Mock Server for Frontend Development

이 디렉토리는 프론트엔드 개발자를 위한 WireMock 기반 Mock API 서버를 제공합니다.

## 🚀 빠른 시작

### 1. Mock 서버 시작

```bash
cd wiremock
docker-compose up -d
```

### 2. 서버 확인

- **Mock API 서버**: http://localhost:8888
- **Swagger UI**: http://localhost:8889
- **WireMock Admin**: http://localhost:8888/__admin

### 3. 서버 중지

```bash
cd wiremock
docker-compose down
```

---

## ⚽ 경기 상태 시뮬레이션 (Match State Simulation)

**핵심 기능**: `X-Mock-Match-State` 헤더를 사용하여 경기의 다양한 상태를 시뮬레이션할 수 있습니다.

### 사용 가능한 경기 상태

| 헤더 값 | 설명 | 주요 특징 |
|--------|------|----------|
| `pre-match` | 경기 전 (라인업 미공개) | status: NS, lineups: [], goals: null |
| `lineup-announced` | 경기 전 (라인업 발표됨) | status: NS, lineups: ✓, goals: null |
| `first-half` | 전반전 진행 중 | status: 1H, elapsed: 23, goals: 1-0 |
| `half-time` | 하프타임 | status: HT, elapsed: 45, goals: 1-1 |
| `second-half` | 후반전 진행 중 | status: 2H, elapsed: 67, goals: 2-1 |
| `full-time` | 경기 종료 | status: FT, elapsed: 90, goals: 3-2 |

### 헤더 없이 호출하면?

기본값으로 `pre-match` 상태가 반환됩니다.

---

## 📡 API 사용 예제

### cURL 예제

```bash
# 전반전 데이터 조회
curl -H "X-Mock-Match-State: first-half" \
  "http://localhost:8888/fixtures?id=1208021"

# 하프타임 데이터 조회
curl -H "X-Mock-Match-State: half-time" \
  "http://localhost:8888/fixtures?id=1208021"

# 경기 종료 데이터 조회
curl -H "X-Mock-Match-State: full-time" \
  "http://localhost:8888/fixtures?id=1208021"
```

### JavaScript/TypeScript (fetch)

```typescript
// 전반전 데이터 가져오기
const response = await fetch('http://localhost:8888/fixtures?id=1208021', {
  headers: {
    'X-Mock-Match-State': 'first-half'
  }
});
const data = await response.json();

console.log(data.response[0].fixture.status.short); // "1H"
console.log(data.response[0].goals); // { home: 1, away: 0 }
```

### JavaScript/TypeScript (axios)

```typescript
import axios from 'axios';

const response = await axios.get('http://localhost:8888/fixtures', {
  params: { id: '1208021' },
  headers: {
    'X-Mock-Match-State': 'second-half'
  }
});

console.log(response.data.response[0].fixture.status.short); // "2H"
console.log(response.data.response[0].goals); // { home: 2, away: 1 }
```

### React 예제

```tsx
import { useState, useEffect } from 'react';

function MatchSimulator() {
  const [matchState, setMatchState] = useState<'pre-match' | 'first-half' | 'half-time' | 'second-half' | 'full-time'>('pre-match');
  const [matchData, setMatchData] = useState(null);

  useEffect(() => {
    const fetchMatchData = async () => {
      const response = await fetch('http://localhost:8888/fixtures?id=1208021', {
        headers: {
          'X-Mock-Match-State': matchState
        }
      });
      const data = await response.json();
      setMatchData(data.response[0]);
    };

    fetchMatchData();
  }, [matchState]);

  return (
    <div>
      <h1>Match State Simulator</h1>

      {/* 상태 선택 버튼 */}
      <div>
        <button onClick={() => setMatchState('pre-match')}>Pre-Match</button>
        <button onClick={() => setMatchState('lineup-announced')}>Lineup Announced</button>
        <button onClick={() => setMatchState('first-half')}>First Half</button>
        <button onClick={() => setMatchState('half-time')}>Half Time</button>
        <button onClick={() => setMatchState('second-half')}>Second Half</button>
        <button onClick={() => setMatchState('full-time')}>Full Time</button>
      </div>

      {/* 경기 데이터 표시 */}
      {matchData && (
        <div>
          <h2>{matchData.teams.home.name} vs {matchData.teams.away.name}</h2>
          <p>Status: {matchData.fixture.status.long}</p>
          <p>Score: {matchData.goals.home ?? '-'} - {matchData.goals.away ?? '-'}</p>
          {matchData.fixture.status.elapsed && (
            <p>Elapsed: {matchData.fixture.status.elapsed}'</p>
          )}
        </div>
      )}
    </div>
  );
}
```

---

## 🧪 자동 시뮬레이션 스크립트

모든 경기 상태를 빠르게 순회하며 테스트하려면:

```bash
./scripts/auto-simulate-match.sh
```

옵션:
```bash
# 특정 fixture ID로 테스트
./scripts/auto-simulate-match.sh 1208021

# 상태 전환 간격 조정 (기본 3초)
./scripts/auto-simulate-match.sh 1208021 5
```

---

## 📋 사용 가능한 Mock 데이터

### Fixtures (경기 일정)

- **Fixture ID**: `1208021`
- **경기**: Manchester City vs Liverpool
- **리그**: Premier League (ID: 39)
- **시즌**: 2024
- **날짜**: 2025-01-15T19:30:00+00:00

### 경기 상태별 데이터 포함 사항

| 데이터 | pre-match | lineup-announced | first-half | half-time | second-half | full-time |
|--------|-----------|------------------|------------|-----------|-------------|-----------|
| 기본 정보 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| 라인업 | ✗ | ✓ | ✓ | ✓ | ✓ | ✓ |
| 골/스코어 | ✗ | ✗ | ✓ | ✓ | ✓ | ✓ |
| 이벤트 | ✗ | ✗ | ✓ (1개) | ✓ (2개) | ✓ (3개) | ✓ (5개) |
| 통계 | ✗ | ✗ | ✓ | ✓ | ✓ | ✓ |

---

## 🎯 Admin API 엔드포인트

WireMock 서버는 다음 Admin API를 모킹합니다:

### Fixture Management

```bash
# Fixture를 Available로 설정 (Match Job 등록)
POST http://localhost:8888/api/v1/admin/fixtures/{fixtureId}/available

# Fixture를 Unavailable로 설정 (Match Job 삭제)
DELETE http://localhost:8888/api/v1/admin/fixtures/{fixtureId}/available
```

### ApiSports Sync

```bash
# 리그 동기화
POST http://localhost:8888/api/v1/admin/apisports/leagues/sync

# 팀 동기화
POST http://localhost:8888/api/v1/admin/apisports/leagues/{leagueId}/teams/sync

# 선수 동기화
POST http://localhost:8888/api/v1/admin/apisports/teams/{teamId}/players/sync

# 경기 일정 동기화
POST http://localhost:8888/api/v1/admin/apisports/leagues/{leagueId}/fixtures/sync

# 리그 available 설정
POST http://localhost:8888/api/v1/admin/apisports/leagues/{leagueId}/available?available=true
```

**참고**: 전체 API 문서는 Swagger UI (http://localhost:8889)에서 확인하세요.

---

## 🔧 개발 팁

### 1. CORS 이슈 해결

로컬 개발 시 CORS 이슈가 발생하면, 프록시 설정을 추가하세요:

**Vite (vite.config.ts)**
```typescript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8888',
        changeOrigin: true,
      }
    }
  }
})
```

**Next.js (next.config.js)**
```javascript
module.exports = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8888/api/:path*',
      },
    ]
  },
}
```

### 2. 환경 변수 설정

```.env
VITE_API_BASE_URL=http://localhost:8888
VITE_MOCK_ENABLED=true
```

### 3. 경기 상태 타입 정의

```typescript
export type MatchState =
  | 'pre-match'
  | 'lineup-announced'
  | 'first-half'
  | 'half-time'
  | 'second-half'
  | 'full-time';

export interface MatchStateConfig {
  state: MatchState;
  description: string;
  hasLineup: boolean;
  hasScore: boolean;
  hasStatistics: boolean;
}

export const MATCH_STATES: Record<MatchState, MatchStateConfig> = {
  'pre-match': {
    state: 'pre-match',
    description: '경기 전 (라인업 미공개)',
    hasLineup: false,
    hasScore: false,
    hasStatistics: false,
  },
  'lineup-announced': {
    state: 'lineup-announced',
    description: '경기 전 (라인업 발표)',
    hasLineup: true,
    hasScore: false,
    hasStatistics: false,
  },
  'first-half': {
    state: 'first-half',
    description: '전반전 진행 중',
    hasLineup: true,
    hasScore: true,
    hasStatistics: true,
  },
  'half-time': {
    state: 'half-time',
    description: '하프타임',
    hasLineup: true,
    hasScore: true,
    hasStatistics: true,
  },
  'second-half': {
    state: 'second-half',
    description: '후반전 진행 중',
    hasLineup: true,
    hasScore: true,
    hasStatistics: true,
  },
  'full-time': {
    state: 'full-time',
    description: '경기 종료',
    hasLineup: true,
    hasScore: true,
    hasStatistics: true,
  },
};
```

---

## 📂 디렉토리 구조

```
wiremock/
├── docker-compose.yml           # Docker Compose 설정
├── README.md                    # 이 파일
├── mappings/                    # WireMock stub 정의
│   └── match-states/
│       └── fixture-match-state-router.json  # 경기 상태 라우팅
└── __files/                     # Mock 응답 데이터
    ├── match-states/            # 경기 상태별 응답
    │   ├── pre-match.json
    │   ├── lineup-announced.json
    │   ├── first-half.json
    │   ├── half-time.json
    │   ├── second-half.json
    │   └── full-time.json
    └── openapi.json             # OpenAPI 스펙 (Swagger UI용)
```

---

## 🐛 트러블슈팅

### WireMock이 시작되지 않을 때

```bash
# Docker 로그 확인
docker logs footballay-wiremock

# 컨테이너 재시작
cd wiremock
docker-compose restart
```

### 헤더가 작동하지 않을 때

1. 헤더 이름이 정확한지 확인: `X-Mock-Match-State`
2. 헤더 값이 유효한지 확인: `pre-match`, `first-half`, 등
3. WireMock 요청 로그 확인:
   ```bash
   curl http://localhost:8888/__admin/requests | jq
   ```

### Swagger UI에서 API를 볼 수 없을 때

1. OpenAPI JSON 파일이 있는지 확인:
   ```bash
   curl http://localhost:8888/__files/openapi.json
   ```

2. OpenAPI JSON을 생성하려면:
   ```bash
   # 먼저 실제 백엔드 서버를 시작 (포트 8083)
   ./gradlew bootRun --args='--spring.profiles.active=dev'

   # 그 다음 OpenAPI 추출
   ./gradlew exportOpenApi
   ```

---

## 📞 문의

질문이나 문제가 있으면 백엔드 팀에 문의하세요.

**Happy Frontend Development! 🎉**
