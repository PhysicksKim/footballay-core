# WireMock Mock Server - Development Notes

> 이 문서는 AI 및 개발자가 WireMock 설정을 이해하고 향후 수정할 때 참고하기 위한 가이드입니다.

---

## 📌 WireMock의 명확한 목적 (Clear Purpose)

### 🎯 단 하나의 목적: Desktop App 라이브 매치 시뮬레이션 전용

이 WireMock 서버는 **Desktop App Frontend 개발자**가 **라이브 경기 데이터 흐름**을 시뮬레이션하여 UI/UX를 개발할 수 있도록 지원합니다.

**목적:**
- 실시간 경기 상태 전환 (pre-match → first-half → full-time)
- 스코어/이벤트/통계 데이터 변화에 따른 UI 업데이트 테스트
- 실제 경기를 기다리지 않고 모든 경기 단계를 빠르게 테스트

**범위:**
- ✅ Fixture 조회 (경기 정보)
- ✅ 실시간 스코어/이벤트/통계
- ✅ 경기 상태별 데이터 변화
- ❌ Admin 기능 (리그/팀/선수 동기화 등)
- ❌ 데이터 저장/수정
- ❌ 인증/권한

---

## 🚫 WireMock이 다루지 않는 것: Admin Page

### Admin Page는 별도의 접근 방식 사용

**Admin Page 개발 환경:**
```bash
# 로컬 개발 환경 사용
docker-compose up -d         # PostgreSQL + Redis
./gradlew bootRun            # Spring Boot 서버
```

**이유:**
1. **Stateful 동작 필요**: 데이터 동기화 전/후에 GET 응답이 달라져야 함
   - POST `/admin/apisports/leagues/sync` → DB에 데이터 저장
   - GET `/api/v1/leagues` → 방금 저장된 데이터 반환
   - 이런 "실제 영속성"은 WireMock으로 재현하기 복잡함

2. **실제 백엔드 로직 검증 필요**:
   - Quartz 스케줄러
   - Redis 캐시 무효화
   - 트랜잭션 처리
   - 복잡한 비즈니스 로직

3. **1인 개발 프로젝트**:
   - Admin Page는 본인이 직접 개발
   - 프론트엔드 개발자 협업 불필요
   - WireMock 구축 부담 불필요

**Admin Page용 Dev 서버 구성 (향후 필요시):**
- Supabase Free Tier: Postgres 무료 (500MB, 2개 프로젝트까지)
- Render/Railway: Free tier 또는 시간제 과금 (t3/t3a 버스트형 인스턴스)
- Cloudflare Zero Trust: 접근 제어 (최대 50명까지 무료)

---

## 🏗️ 아키텍처 결정 (Architecture Decision)

### 두 개발 영역의 명확한 분리

| 영역 | 도구 | 목적 | 상태 관리 |
|------|------|------|-----------|
| **Admin Page** | 로컬 Dev 서버<br>(Spring Boot + DB) | 데이터 동기화, 관리<br>실제 백엔드 로직 검증 | Stateful<br>(DB 영속성) |
| **Desktop App** | WireMock | 라이브 경기 시뮬레이션<br>UI/UX 개발 | Stateless<br>(헤더 기반 전환) |

**분리의 이점:**
1. **Desktop App 개발자 편의**: `docker-compose up wiremock`만으로 즉시 시작
2. **명확한 책임**: Admin은 실제 서버, Desktop App은 Mock
3. **관리 부담 감소**: WireMock은 경기 시뮬레이션만 집중
4. **비용 절감**: WireMock은 로컬 Docker만 사용 (서버비 0원)

---

## ⚠️ 현재 상태 및 알려진 문제점 (Current Status & Issues)

### 문제점 1: ApiSports 응답 형식 사용 중

**현재:**
- `__files/match-states/*.json` 파일들이 **ApiSports의 응답 형식**을 사용
- 이는 외부 API 제공자의 응답 구조

**문제:**
- footballay-core 서버는 ApiSports 데이터를 받아서 **자체 형식으로 가공하여 응답**
- 현재 JSON은 **실제 프론트엔드가 받을 응답과 다름**

**영향:**
- Desktop App 개발 후 실제 백엔드 연동 시 응답 구조 차이로 수정 작업 발생

---

### 문제점 2: Admin API Stubs 포함됨

**현재:**
- `mappings/admin/fixture-available.json`
- `mappings/admin/apisports-sync.json`

**문제:**
- Admin 기능은 WireMock 범위 밖
- 불필요한 stub이 유지보수 부담 증가

**해결:**
- Admin API stubs 완전 제거 예정

---

## ✅ 재수정 계획 (Refactoring Plan)

### Phase 1: 범위 정리 - Admin API 제거

**작업:**
```bash
# 1. Admin API stubs 제거
rm -rf wiremock/mappings/admin/
rm -rf wiremock/__files/seed/
```

**이유:**
- Admin 기능은 로컬 dev 서버에서 처리
- WireMock은 Desktop App 라이브 매치 시뮬레이션만 담당

**결과:**
```
wiremock/
├── mappings/
│   └── match-states/               # Desktop App 전용
│       └── fixture-match-state-router.json
└── __files/
    └── match-states/               # 경기 상태별 응답만
        ├── pre-match.json
        ├── lineup-announced.json
        ├── first-half.json
        ├── half-time.json
        ├── second-half.json
        └── full-time.json
```

---

### Phase 2: footballay-core 실제 응답 형식 파악

**방법:**
```bash
# 1. footballay-core 서버 실행
./gradlew bootRun --args='--spring.profiles.active=dev'

# 2. 실제 fixture API 엔드포인트 확인
curl http://localhost:8083/v3/api-docs | jq '.paths' | grep fixture

# 3. 실제 응답 확인
curl http://localhost:8083/api/v1/fixtures/{fixtureId} | jq > actual-fixture-response.json

# 4. OpenAPI 스펙에서 응답 스키마 확인
curl http://localhost:8083/v3/api-docs | jq '.components.schemas' > schemas.json
```

**확인 사항:**
- [ ] Fixture 조회 API 경로 (예: `/api/v1/fixtures/{id}`)
- [ ] 응답 JSON 최상위 구조
- [ ] 경기 상태(status), 스코어(goals), 라인업(lineups), 이벤트(events) 필드명
- [ ] 날짜/시간 형식
- [ ] 팀, 선수 정보 포함 여부

---

### Phase 3: JSON 응답 파일 교체

**교체 대상:**
```
wiremock/__files/match-states/
├── pre-match.json          ❌ ApiSports 형식 → ✅ footballay-core 형식
├── lineup-announced.json   ❌ ApiSports 형식 → ✅ footballay-core 형식
├── first-half.json         ❌ ApiSports 형식 → ✅ footballay-core 형식
├── half-time.json          ❌ ApiSports 형식 → ✅ footballay-core 형식
├── second-half.json        ❌ ApiSports 형식 → ✅ footballay-core 형식
└── full-time.json          ❌ ApiSports 형식 → ✅ footballay-core 형식
```

**작업 절차:**
1. Phase 2에서 확인한 실제 응답 구조 사용
2. 각 경기 상태별로 적절한 데이터 구성:
   - **pre-match**: 라인업 없음, 스코어 없음, status=NS
   - **lineup-announced**: 라인업 있음, 스코어 없음, status=NS
   - **first-half**: 라인업 있음, 1골, elapsed=23, status=1H, events=1
   - **half-time**: 라인업 있음, 2골, elapsed=45, status=HT, events=2
   - **second-half**: 라인업 있음, 3골, elapsed=67, status=2H, events=3
   - **full-time**: 라인업 있음, 5골, elapsed=90, status=FT, events=5
3. 새로운 JSON으로 파일 교체
4. WireMock 재시작 및 테스트

---

### Phase 4: WireMock 매핑 파일 수정

**확인 및 수정 대상:**
```
wiremock/mappings/match-states/fixture-match-state-router.json
```

**수정 사항:**
1. **URL 패턴 확인**:
   ```json
   // 현재
   "urlPathPattern": "/fixtures"
   "queryParameters": {"id": {"equalTo": "1208021"}}

   // 실제 (확인 필요)
   "urlPathPattern": "/api/v1/fixtures/([0-9]+)"
   ```

2. **Path Parameter vs Query Parameter**:
   - footballay-core 서버의 실제 API 스타일에 맞춰 수정

3. **응답 헤더 추가** (필요 시):
   ```json
   "headers": {
     "Content-Type": "application/json",
     "X-Custom-Header": "value"
   }
   ```

---

### Phase 5: Swagger UI 제거 또는 유지 검토

**현재:**
- Swagger UI 컨테이너가 `openapi.json` 참조
- `openapi.json`은 없거나 오래된 상태

**옵션 1: Swagger UI 제거** (추천)
- WireMock은 Desktop App용 간단한 시뮬레이터
- Swagger UI 불필요
- `docker-compose.yml`에서 `swagger-ui` 서비스 제거

**옵션 2: Swagger UI 유지**
- footballay-core에서 OpenAPI 스펙 export
- `openapi.json`을 `__files/`에 배치
- Desktop App 개발자가 API 문서 참고 가능

**결정 보류**: Desktop App 개발자 피드백 후 결정

---

## 🔧 핵심 동작 요구사항 (Critical Requirements)

### 1. Header-based Routing 유지

**핵심 동작:**
```
X-Mock-Match-State: pre-match      → pre-match.json 반환
X-Mock-Match-State: first-half     → first-half.json 반환
X-Mock-Match-State: full-time      → full-time.json 반환
(헤더 없음)                         → pre-match.json 반환 (기본값)
```

**WireMock Priority 기반 매칭:**
- 구체적인 헤더 매칭: `priority: 1` (낮을수록 우선)
- 기본 fallback: `priority: 10` (높은 값)

---

### 2. 6가지 경기 상태 시뮬레이션

| 상태 | 헤더 값 | 필수 요소 |
|------|---------|-----------|
| 경기 전 (라인업 미공개) | `pre-match` | status=NS, lineups=[], goals=null |
| 경기 전 (라인업 발표) | `lineup-announced` | status=NS, lineups=✓, goals=null |
| 전반전 진행 중 | `first-half` | status=1H, elapsed=23, goals=1-0, events=1 |
| 하프타임 | `half-time` | status=HT, elapsed=45, goals=1-1, events=2 |
| 후반전 진행 중 | `second-half` | status=2H, elapsed=67, goals=2-1, events=3 |
| 경기 종료 | `full-time` | status=FT, elapsed=90, goals=3-2, events=5 |

---

### 3. Volume Mount 구조 유지

```yaml
services:
  wiremock:
    volumes:
      - ./mappings:/home/wiremock/mappings   # 매핑 파일
      - ./__files:/home/wiremock/__files     # 응답 데이터
```

**이유:**
- 파일 수정 시 컨테이너 재빌드 불필요
- Desktop App 개발자가 JSON 파일만 수정하면 즉시 반영

---

## 📁 최종 디렉토리 구조 (Final Structure)

```
wiremock/
├── docker-compose.yml              # Docker Compose 설정
├── WIREMOCK_DEV_NOTES.md          # 이 파일 (AI/개발자용)
├── README.md                       # Desktop App 개발자용 가이드
├── scripts/
│   └── auto-simulate-match.sh     # 자동 상태 순환 데모 스크립트
├── mappings/
│   └── match-states/
│       └── fixture-match-state-router.json  # Header-based routing
└── __files/
    └── match-states/               # 경기 상태별 응답
        ├── pre-match.json
        ├── lineup-announced.json
        ├── first-half.json
        ├── half-time.json
        ├── second-half.json
        └── full-time.json
```

**제거 예정:**
- ❌ `mappings/admin/` (Admin API stubs)
- ❌ `__files/seed/` (ApiSports mock 데이터)
- ❌ `swagger-ui` 서비스 (검토 후 결정)

---

## 🧪 테스트 계획 (Testing Plan)

### Phase 3 완료 후 필수 테스트

```bash
# 1. WireMock 재시작
cd wiremock
docker-compose restart wiremock

# 2. 헤더 없이 기본 응답 확인 (pre-match)
curl http://localhost:8888/api/v1/fixtures/1208021 | jq

# 3. 각 상태별 테스트
curl -H "X-Mock-Match-State: lineup-announced" \
  "http://localhost:8888/api/v1/fixtures/1208021" | jq

curl -H "X-Mock-Match-State: first-half" \
  "http://localhost:8888/api/v1/fixtures/1208021" | jq

curl -H "X-Mock-Match-State: full-time" \
  "http://localhost:8888/api/v1/fixtures/1208021" | jq

# 4. Auto-simulate 스크립트 실행
cd ..
./scripts/auto-simulate-match.sh 1208021 2
```

### 검증 체크리스트

- [ ] 응답 JSON 구조가 footballay-core 서버와 동일
- [ ] 각 경기 상태별로 적절한 데이터 포함/제외
- [ ] 라인업이 있어야 하는 상태에서만 라인업 존재
- [ ] 스코어가 경기 진행에 따라 변화
- [ ] 이벤트 개수가 경기 진행에 따라 증가
- [ ] URL 패턴이 실제 API와 일치

---

## 💡 향후 개선 아이디어 (Future Enhancements)

### 1. 더 많은 Fixture 추가
- 현재: fixture ID 1208021만 지원
- 향후: 여러 경기 데이터 추가로 다양한 시나리오 테스트

### 2. 추가 경기 상태
- 연장전 (Extra Time)
- 승부차기 (Penalty)
- 중단/연기 (Suspended/Postponed)

### 3. Error Scenarios
- 404: 경기 없음
- 500: 서버 에러
- Timeout: 네트워크 지연

### 4. Available Flag 시뮬레이션
- 현재: 모든 경기가 available=true
- 향후: 헤더로 available=false 상태 시뮬레이션

### 5. Polling Scenario (고급)
- 시간 경과에 따라 자동으로 상태 전환
- WireMock Scenarios 활용
- 폴링 기반 UI 검증

---

## 📚 참고 자료 (References)

### WireMock 공식 문서
- **Main**: https://wiremock.org/docs/
- **Response Templating**: https://wiremock.org/docs/response-templating/
- **Request Matching**: https://wiremock.org/docs/request-matching/
- **Stubbing**: https://wiremock.org/docs/stubbing/
- **Scenarios (Stateful)**: https://wiremock.org/docs/stateful-behaviour/

### Admin Page 대안 (Dev 서버 구축 시 참고)
- **Supabase Free Tier**: https://supabase.com/pricing
- **Render Free Tier**: https://render.com/docs/free
- **Cloudflare Zero Trust**: https://developers.cloudflare.com/cloudflare-one/

---

## 📝 변경 이력 (Changelog)

### 2025-01-15 (v2) - 아키텍처 결정 및 재수정 계획 수립
- WireMock 목적을 Desktop App 라이브 매치 시뮬레이션 전용으로 명확화
- Admin Page는 별도의 로컬 dev 서버 사용 결정
- Admin API stubs 제거 계획 수립
- footballay-core 실제 응답 형식으로 JSON 교체 계획 수립
- 5단계 재수정 계획 작성

### 2025-01-15 (v1) - 초기 작성
- WireMock 기본 구조 생성
- Header-based match state simulation 구현
- ApiSports 응답 형식 사용 (문제점 파악)

---

**마지막 수정일**: 2025-01-15
**작성자**: AI (Claude Code)
**상태**: 🚧 재수정 대기 중 - Phase 1부터 순차 진행 필요
