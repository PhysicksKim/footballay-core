# Fixture ETag Experiment App Implementation Plan

## 문서 목적

이 문서는 `fixtureUid` 단위로 실시간 매치 polling endpoint의 `ETag`, `304 Not Modified`, cached `200`, cache bypass baseline 성능을 비교 실험하기 위한 **별도 로컬 앱**의 구현 계획을 정리한다.

이 앱은 백엔드 서버 내부 구현을 직접 읽거나 의존하지 않는다.
필요한 것은 아래 5개 endpoint와 몇 가지 HTTP 동작 규칙뿐이다.

- fixture 기본 정보 조회용 `1개`
- polling 실험 대상 `4개`

즉 이 문서는 백엔드 코드 설계 문서가 아니라,
**React UI + Node 실험 엔진**으로 구성된 독립 실험 도구 설계 문서다.

## 왜 이 앱이 필요한가

이번 실험의 목적은 단순히 "`ETag`가 붙는다" 또는 "`304`가 나온다" 수준의 기능 확인이 아니다.
실제 경기 상황에서 다음 질문에 수치로 답하기 위해서다.

- `ETag` 기반 요청이 실제로 얼마나 많은 body 전송량을 줄여주는가
- `If-None-Match`를 보내지 않아도 cached snapshot `200`이 baseline 대비 얼마나 빠른가
- cache bypass 기준선과 비교했을 때 latency 차이가 어느 정도인가
- 각 endpoint별로 절감 효과가 같은가, 아니면 `status`, `events`, `statistics`마다 다른가
- polling 주기(`5s`, `10s`, `15s`, `30s`)에 따라 절감 효과가 어떻게 달라지는가
- 경기 전, 경기 중, 경기 종료 후 구간에서 행동이 어떻게 달라지는가

CLI 스크립트 하나로도 측정은 가능하다.
하지만 이번 작업은 단발성 실행이 아니라 아래 기능이 필요하다.

- fixture 등록과 관리
- kickoff 기준 실험 시작 시점 판단
- 반복 실험 실행/정지/재시작
- raw log 저장
- summary 계산
- endpoint/시나리오별 비교 시각화
- 이전 실험 결과 재조회

이 정도 요구사항이면 단순 스크립트보다 **로컬 실험 앱**으로 분리하는 편이 훨씬 적절하다.

## 앱의 목표

이 앱은 다음을 제공해야 한다.

1. 사용자가 `fixtureUid`를 입력해서 실험 대상을 등록할 수 있다.
2. 앱이 `info` endpoint를 호출해 kickoff 시간과 기본 메타데이터를 가져온다.
3. 사용자가 interval, duration, scenario 조합을 선택해 실험을 시작할 수 있다.
4. 앱이 동일 fixture에 대해 A/B/C 실험을 병렬 수행한다.
5. 앱이 raw log와 summary를 로컬 파일로 저장한다.
6. 앱이 결과를 표와 그래프로 즉시 확인할 수 있게 한다.
7. 여러 fixture 실험 결과를 누적 관리할 수 있다.

## 앱이 해결하려는 실험 문제

### 문제 1. 실제 경기 기준 검증 필요

mock 데이터만으로는 `304` 비율과 네트워크 절감률을 평가하기 어렵다.
절감 효과는 백엔드 코드만으로 결정되지 않고 다음 두 요소의 상대 관계에 좌우된다.

- 클라이언트 polling 주기
- 실제 provider 데이터 변화 주기

예를 들어 polling이 `15초`인데 provider 데이터가 `1분` 동안 거의 안 바뀌면,
많은 요청이 `304`로 떨어질 수 있다.
반대로 provider 값이 자주 바뀌면 `200` 비율이 높아질 수 있다.

즉 이번 검증은 반드시 **실제 경기 중 데이터를 기준으로 장시간 관찰**해야 의미가 있다.

### 문제 2. endpoint마다 특성이 다름

`status`, `events`, `lineup`, `statistics`는 payload 크기와 변화 빈도가 다를 가능성이 높다.

- `status`: 변화가 가장 자주 보일 가능성
- `events`: 특정 시점에만 크게 변할 가능성
- `lineup`: 경기 전 공개 후 거의 안 바뀔 가능성
- `statistics`: payload가 상대적으로 크고 계산 비용도 클 가능성

그래서 전체 평균만 보면 안 되고, endpoint별로 분리해서 봐야 한다.

### 문제 3. baseline 없이 절감 효과를 말할 수 없음

`304`가 많이 나와도 baseline과 비교하지 않으면 의미가 제한적이다.
반드시 cache bypass 기준선이 필요하다.

## 왜 별도 React 앱 + Node 구조가 적절한가

## 핵심 판단

브라우저 UI만으로 끝내는 구조는 권장하지 않는다.
실험 실행 엔진은 Node가 맡아야 한다.

이유:

- 장시간 polling 제어가 필요하다
- raw log를 로컬 파일로 안정적으로 써야 한다
- summary 계산과 재실행 관리가 필요하다
- 브라우저 탭 상태에 따라 측정이 흔들리면 안 된다
- 향후 CSV/JSON/Markdown export가 필요하다

권장 구조:

- React: 실험 설정, 상태 표시, 그래프, 결과 조회
- Node: 실험 실행, endpoint 호출, 파일 저장, 집계 계산

## 상위 아키텍처

### 구성 요소

1. `React UI`
2. `Node experiment engine`
3. `Local file storage`
4. `Backend target server`

### 데이터 흐름

1. 사용자가 UI에서 `fixtureUid`를 등록한다.
2. Node가 백엔드 `info` endpoint를 호출한다.
3. Node가 fixture 메타데이터를 저장한다.
4. 사용자가 실험 파라미터를 입력하고 실행한다.
5. Node가 4개 polling endpoint에 대해 A/B/C 시나리오 요청을 병렬 수행한다.
6. Node가 raw log를 파일에 append한다.
7. Node가 summary를 계산한다.
8. React가 Node API를 통해 상태와 결과를 표시한다.

## 백엔드와의 계약 범위

이 앱은 백엔드 내부 구조를 알 필요가 없다.
다만 다음 HTTP 계약은 문서로 고정해야 한다.

## 대상 endpoint

Base URL:

`http://localhost:8083/api/v1/football/fixtures`

### 1. fixture 기본 정보 조회

`GET /{fixtureUid}/info`

용도:

- fixture 존재 여부 확인
- kickoff 시간 확보
- 실험 메타데이터 저장

최소 기대 정보:

- `fixtureUid`
- `date`
- 리그/팀 식별에 도움이 되는 표시용 정보

중요:

- 이 앱은 `date`를 기준으로 kickoff 시각만 알면 된다
- 나머지 body는 화면 표시용 보조 정보 수준으로만 사용한다

### 2. polling 대상 1: status

`GET /{fixtureUid}/status`

### 3. polling 대상 2: lineup

`GET /{fixtureUid}/lineup`

### 4. polling 대상 3: events

`GET /{fixtureUid}/events`

### 5. polling 대상 4: statistics

`GET /{fixtureUid}/statistics`

## HTTP 동작 규칙

### ETag 응답

polling endpoint의 정상 `200` 응답은 `ETag` 헤더를 가질 수 있다.

실험 앱은 각 endpoint별로 마지막 `ETag`를 저장해야 한다.

### If-None-Match 요청

클라이언트가 이전 `ETag`를 `If-None-Match` 헤더로 보내면,
서버는 데이터가 동일할 경우 `304 Not Modified`를 반환할 수 있다.

### 304 Not Modified

`304` 응답은 body가 없거나 매우 작아야 하며,
실험상 중요한 의미는 "네트워크 body 절감"이다.

### cache bypass baseline

실험용 baseline 시나리오에서는 아래 헤더를 사용한다.

Header:

`X-Fixture-Cache-Control: bypass`

의미:

- 캐시 read를 우회하는 실험용 호출
- baseline 용도
- 운영 기본 흐름이 아니라 실험 비교 기준선

### body 스펙 비의존 원칙

이 앱은 polling response body 구조를 상세히 이해할 필요가 없다.
실험 목적상 필요한 것은 아래뿐이다.

- HTTP status
- response headers
- body byte length
- latency
- `ETag`

즉 이 앱은 body를 "표시 가능한 원문" 정도로만 다루고,
비즈니스 필드 파싱에는 최소한으로 의존해야 한다.

## 실험 시나리오 정의

이 앱은 동일 fixture와 동일 endpoint에 대해 아래 3개 시나리오를 병렬 측정한다.

### Scenario A: etag-hit

동작:

- 첫 요청은 일반 GET
- `200` 응답에서 `ETag`를 저장
- 다음 요청부터 `If-None-Match`로 직전 `ETag` 전송

기대 효과:

- 변경이 없으면 `304`
- body 전송량 최소화

측정 의의:

- 실제 `304` 비율
- baseline 대비 총 body 절감량

### Scenario B: snapshot-cache

동작:

- `If-None-Match` 없이 일반 GET
- cache bypass 헤더도 보내지 않음

기대 효과:

- body는 계속 `200`으로 받음
- cached snapshot 사용 시 baseline보다 빠를 수 있음

측정 의의:

- 서버 cache hit의 latency 이점 측정

### Scenario C: db-direct-bypass

동작:

- `X-Fixture-Cache-Control: bypass`
- `If-None-Match`는 보내지 않음

기대 효과:

- baseline 역할
- body 전송량은 크고 latency도 상대적으로 높을 가능성

측정 의의:

- A/B 비교 기준선

## 실험 시간축 관점

실험은 kickoff 기준 상대 시간으로 해석해야 한다.

예시:

- kickoff `-30분`
- kickoff `-10분`
- kickoff `+5분`
- kickoff `+45분`
- kickoff `+90분`

이 값을 저장해야 하는 이유:

- 같은 endpoint라도 경기 전/중/후에 변화 빈도가 달라질 수 있다
- `lineup`은 경기 직전 변할 가능성이 높고 경기 중에는 거의 안 바뀔 수 있다
- `events`, `status`는 경기 중 변화가 집중될 수 있다

따라서 각 요청 log에는 반드시 **kickoff 대비 offset**을 넣어야 한다.

## 수집해야 할 정보

이 문서는 수집 항목을 `raw log`, `run summary`, `fixture metadata`로 나눈다.

## 1. fixture metadata

fixture 등록 시 저장한다.

- `fixtureUid`
- `infoUrl`
- `kickoffRaw`
- `kickoffIso`
- `leagueName` 또는 표시용 league label
- `homeTeamName`
- `awayTeamName`
- 최초 등록 시각
- 마지막 확인 시각

이 데이터는 실험 대상 리스트와 결과 화면 모두에 사용된다.

## 2. raw request log

요청 1회당 1레코드로 저장한다.

필수 항목:

- `runId`
- `fixtureUid`
- `endpoint`
- `scenario`
- `requestStartedAt`
- `requestEndedAt`
- `tickStartedAt`
- `latencyMs`
- `statusCode`
- `etagReceived`
- `ifNoneMatchSent`
- `usedBypassHeader`
- `bodyBytes`
- `contentLengthHeader`
- `notModified`
- `ok200`
- `error`
- `kickoffIso`
- `kickoffOffsetMs`

권장 추가 항목:

- `sequence`
- `attempt`
- `pollIntervalMs`
- `baseUrl`
- `hostTag`

## 3. run summary

실험 1회 종료 후 계산한다.

필수 항목:

- `runId`
- `fixtureUid`
- `startedAt`
- `endedAt`
- `durationMs`
- `intervalMs`
- `selectedEndpoints`
- `selectedScenarios`

### endpoint x scenario 집계 항목

- 총 요청 수
- `200` 수
- `304` 수
- 기타 실패 수
- `304` 비율
- 총 body bytes
- 평균 body bytes
- 평균 latency
- p50 latency
- p95 latency
- max latency

### baseline 비교 항목

기준선은 같은 endpoint의 Scenario C.

- baseline 대비 절감된 총 body bytes
- baseline 대비 body 절감률
- baseline 대비 평균 latency 차이
- baseline 대비 평균 latency 절감률

## 앱이 제공해야 할 핵심 화면

## 1. Fixture Registry 화면

목적:

- 실험 대상 fixture를 등록/조회

필수 요소:

- `fixtureUid` 입력
- `info` 조회 버튼
- 조회 성공 시 fixture 카드 생성
- kickoff 시각, 팀 이름, 리그 이름 표시
- fixture 저장 버튼

보조 요소:

- 최근 등록 fixture 목록
- 마지막 실험 실행 시각

## 2. Experiment Setup 화면

목적:

- 실험 파라미터 선택

필수 입력:

- fixture 선택
- polling interval
- duration
- endpoint 선택
- scenario 선택

권장 옵션:

- 즉시 시작 / 예약 시작
- kickoff 기준 상대 시작 시점
- 결과 저장 폴더명 메모

## 3. Live Run 화면

목적:

- 현재 실행 중인 실험 상태 모니터링

필수 표시:

- run 시작 시각
- 경과 시간
- 다음 tick 예정 시각
- 현재까지 요청 수
- endpoint x scenario별 최근 status
- 최근 `304`/`200` 비율

권장 표시:

- 최근 1분 평균 latency
- 최근 1분 body bytes
- 오류 요청 수

## 4. Result Detail 화면

목적:

- 실험 종료 후 분석

필수 표시:

- fixture 정보
- run 설정
- scenario 총괄 표
- endpoint별 상세 표
- raw log 다운로드 버튼

권장 차트:

- latency time series
- body bytes time series
- endpoint별 `304` 비율 bar chart
- scenario별 total bytes 비교

## 5. Run History 화면

목적:

- 이전 실험 재조회

필수 기능:

- run 목록 조회
- fixtureUid별 필터
- 기간 필터
- 결과 파일 열기

## 차트 및 시각화 제안

시각화는 과하게 복잡할 필요 없다.
다만 아래 차트는 실험 가치가 높다.

### 우선순위 1

- scenario별 total body bytes bar chart
- scenario별 average / p95 latency bar chart
- endpoint별 `304` ratio bar chart

### 우선순위 2

- 시간축 latency line chart
- 시간축 body bytes line chart
- kickoff 기준 offset scatter or line chart

### 우선순위 3

- multiple run 비교 chart
- polling interval별 절감률 비교 chart

## 저장 포맷 전략

`txt` 단일 파일보다 구조화된 포맷이 낫다.

권장 저장 구조:

- raw log: `jsonl`
- summary: `json`
- 사람 읽기용 리포트: `md`
- 필요 시 spreadsheet 연동용: `csv`

### 이유

`jsonl`의 장점:

- append 쓰기 쉬움
- 요청 단위 레코드 저장에 적합
- 부분 손상 시 복구가 쉬움

`summary.json`의 장점:

- UI 재로드 시 바로 표시 가능
- 차트 데이터 가공이 쉬움

`summary.md`의 장점:

- 사람에게 공유하기 좋음
- 실험 회고 문서에 붙이기 쉬움

## 권장 로컬 폴더 구조

```text
fixture-etag-experiment-app/
  app/
    ui/
    server/
    shared/
  data/
    fixtures/
    runs/
  docs/
    IMPLEMENTATION_PLAN.md
  reports/
```

### 파일 예시

```text
data/
  fixtures/
    fixtures.json
  runs/
    2026-04-24T22-10-00_s1o3dr62vwcbufn4/
      run.json
      raw-log.jsonl
      summary.json
      summary.md
```

## 데이터 모델 제안

## Fixture 등록 모델

```json
{
  "fixtureUid": "s1o3dr62vwcbufn4",
  "kickoffIso": "2026-04-23T04:00:00+09:00",
  "kickoffRaw": "2026-04-23 04:00",
  "leagueLabel": "Premier League",
  "homeLabel": "Manchester City",
  "awayLabel": "Arsenal",
  "createdAt": "2026-04-24T21:00:00+09:00",
  "updatedAt": "2026-04-24T21:00:00+09:00"
}
```

## Run 설정 모델

```json
{
  "runId": "2026-04-24T22-10-00_s1o3dr62vwcbufn4",
  "fixtureUid": "s1o3dr62vwcbufn4",
  "intervalMs": 15000,
  "durationMs": 600000,
  "endpoints": ["status", "lineup", "events", "statistics"],
  "scenarios": ["A", "B", "C"],
  "startedAt": "2026-04-24T22:10:00+09:00"
}
```

## Node 서버 책임 범위

Node는 다음 책임을 가진다.

### 1. Fixture 조회/저장

- fixtureUid 검증
- `info` endpoint 조회
- fixture 메타데이터 저장

### 2. 실험 실행

- run 생성
- interval마다 endpoint 요청 수행
- scenario별 header 적용
- ETag 상태 관리

### 3. 파일 저장

- raw log append
- summary 계산
- run 결과 파일 작성

### 4. React에 상태 제공

- fixture 목록 조회
- run 목록 조회
- run 상세 조회
- 현재 실행 상태 조회

## React UI 책임 범위

React는 다음 책임을 가진다.

### 1. 조작 인터페이스

- fixture 추가
- 실험 시작/중지
- 설정값 입력

### 2. 모니터링

- 진행률 표시
- 최근 요청 결과 표시
- endpoint/시나리오별 상태 표시

### 3. 분석 화면

- summary table 렌더링
- 차트 렌더링
- raw log 다운로드 링크 제공

## 실험 실행 정책

## 기본 정책

- 기본 interval: `15초`
- 기본 duration: `10분`
- 기본 endpoints: 4개 모두
- 기본 scenarios: A/B/C 모두

## 초기 우선순위

1. `status`
2. `events`
3. `lineup`
4. `statistics`

초기에는 전체를 다 지원하되,
UI 기본 선택은 `status + events`부터 시작하는 것도 괜찮다.

## 예약 실행 정책

향후 아래 모드를 고려할 수 있다.

- kickoff 기준 `-10분` 자동 시작
- kickoff 기준 `+5분` 자동 시작
- 수동 즉시 시작

초기 버전에서는 **수동 즉시 시작**만 먼저 지원해도 충분하다.

## 오류 처리 정책

실험 도구는 오류를 조용히 삼키면 안 된다.
오류 자체도 관측 데이터다.

반드시 기록해야 할 오류:

- connection refused
- timeout
- 4xx
- 5xx
- JSON parse 실패
- 파일 쓰기 실패

오류 처리 원칙:

- 요청 실패 1회로 run 전체를 중단하지 않는다
- raw log에 실패 레코드를 남긴다
- UI에 현재 오류 수를 표시한다

## 성능 측정 원칙

latency는 최소한 **end-to-end wall clock** 기준으로 측정한다.

포함 범위:

- 요청 시작
- 서버 응답 수신
- body text 읽기 완료

중요:

- body가 없는 `304`와 body가 있는 `200`은 latency 해석이 다를 수 있다
- 따라서 body bytes와 latency를 함께 봐야 한다

## 실험 결과 해석 가이드

### A vs C

의미:

- ETag 기반 `304`가 baseline 대비 얼마나 절감되는지

중심 지표:

- total body bytes
- average body bytes
- `304` ratio
- average / p95 latency

### B vs C

의미:

- cached `200` 응답이 baseline 대비 얼마나 빠른지

중심 지표:

- average latency
- p95 latency

### A vs B

의미:

- `304`가 cached `200`보다 얼마나 더 네트워크를 아끼는지

중심 지표:

- average body bytes
- total body bytes
- latency

## 구현 범위 제안

## Phase 1. 최소 실행 가능 버전

목표:

- fixture 등록
- 실험 실행
- raw log 저장
- summary 계산
- 간단한 표 표시

포함:

- React 기본 UI
- Node 실험 엔진
- `jsonl` 저장
- summary JSON 생성

제외:

- 고급 차트
- 예약 실행
- 다중 run 비교

## Phase 2. 분석 UI 강화

목표:

- 그래프 추가
- run history 조회
- endpoint별 세부 비교 강화

포함:

- bar chart
- line chart
- 결과 필터링

## Phase 3. 운영성 강화

목표:

- 예약 실행
- 다중 fixture 관리
- 결과 export 개선

포함:

- kickoff 기준 예약
- csv export
- 비교 리포트 생성

## 기술 선택 가이드

권장:

- Frontend: React
- Backend local runner: Node.js
- 차트: 가벼운 React chart library 1개
- 저장: filesystem 기반 JSONL/JSON/MD

중요:

- 초기에는 DB를 붙이지 않는다
- 과한 상태관리 라이브러리 도입을 피한다
- 측정 도구가 복잡해져 본래 실험 목적을 흐리면 안 된다

## 비기능 요구사항

### 재현성

- 동일 run 설정이 파일에 남아야 한다
- 나중에 같은 조건으로 재실행 가능해야 한다

### 관찰 가능성

- 현재 실행 상태가 UI에서 보여야 한다
- 요청 실패도 통계에 포함되어야 한다

### 로컬 우선

- 이 앱은 로컬 전용 도구다
- 계정 시스템, 배포 인증, 멀티유저 기능은 고려하지 않는다

### 단순성

- 백엔드 응답 body 구조에 깊게 결합하지 않는다
- 실험 엔진은 HTTP 레벨 계측에 집중한다

## 완료 기준

이 계획의 완료 기준은 아래다.

1. 사용자가 `fixtureUid`를 입력해 fixture를 등록할 수 있다.
2. kickoff 시간과 표시용 기본 정보를 로컬에 저장할 수 있다.
3. A/B/C 3개 시나리오를 4개 polling endpoint에 대해 실행할 수 있다.
4. raw log를 `jsonl`로 저장할 수 있다.
5. summary를 `json`과 `md`로 생성할 수 있다.
6. UI에서 endpoint별, scenario별 latency/body/304 비율을 볼 수 있다.
7. 실험 결과를 재조회할 수 있다.

## 구현 전 확인사항

- backend 대상 서버가 로컬에서 실행 중이어야 한다
- 대상 fixtureUid가 유효해야 한다
- `info` endpoint의 `date` 값이 kickoff 시간으로 사용 가능해야 한다
- polling 4개 endpoint가 `ETag`를 반환해야 한다
- baseline 실험용 `X-Fixture-Cache-Control: bypass`가 동작해야 한다

## 최종 요약

이 앱은 "ETag 실험용 보조 UI"가 아니다.
실제로는 아래를 수행하는 **로컬 성능 검증 도구**다.

- fixture 등록
- kickoff 기반 실험 관리
- ETag/304/cache snapshot/bypass 비교
- raw log 수집
- summary 계산
- 시각화와 결과 재조회

따라서 별도 React 앱으로 분리하는 접근은 충분히 타당하고,
구조적으로도 `React + Node + local file storage`가 가장 실용적이다.
