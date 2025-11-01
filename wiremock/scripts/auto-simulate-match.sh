#!/bin/bash

###############################################################################
# Auto Match State Simulation Script
#
# 이 스크립트는 WireMock 서버를 이용하여 경기 상태를 자동으로 순환하며 테스트합니다.
#
# 사용법:
#   ./scripts/auto-simulate-match.sh [fixture_id] [delay_seconds]
#
# 예시:
#   ./scripts/auto-simulate-match.sh 1208021 2
#
# 매개변수:
#   fixture_id     - 테스트할 경기 ID (기본값: 1208021)
#   delay_seconds  - 각 상태 전환 간 대기 시간(초) (기본값: 3)
###############################################################################

# 기본값 설정
FIXTURE_ID="${1:-1208021}"
DELAY="${2:-3}"
WIREMOCK_URL="${WIREMOCK_URL:-http://localhost:8888}"

# 색상 코드
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 경기 상태 배열
STATES=(
  "pre-match"
  "lineup-announced"
  "first-half"
  "half-time"
  "second-half"
  "full-time"
)

# 상태별 설명
declare -A STATE_DESCRIPTIONS=(
  ["pre-match"]="경기 전 (라인업 미공개)"
  ["lineup-announced"]="경기 전 (라인업 발표됨)"
  ["first-half"]="전반전 진행 중 (23분)"
  ["half-time"]="하프타임"
  ["second-half"]="후반전 진행 중 (67분)"
  ["full-time"]="경기 종료"
)

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║       Football Match State Auto-Simulation Script             ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""
echo "Fixture ID: ${BLUE}${FIXTURE_ID}${NC}"
echo "WireMock URL: ${BLUE}${WIREMOCK_URL}${NC}"
echo "Delay between states: ${BLUE}${DELAY}s${NC}"
echo ""
echo "────────────────────────────────────────────────────────────────"
echo ""

# WireMock 서버 연결 확인
echo -n "Checking WireMock server connection... "
if curl -sf "${WIREMOCK_URL}/__admin/health" > /dev/null; then
  echo -e "${GREEN}✓ OK${NC}"
else
  echo -e "${RED}✗ FAILED${NC}"
  echo ""
  echo "WireMock 서버에 연결할 수 없습니다."
  echo "다음 명령으로 WireMock을 시작하세요:"
  echo "  cd wiremock && docker-compose up -d"
  exit 1
fi

echo ""
echo "────────────────────────────────────────────────────────────────"
echo ""
echo "Starting match state simulation..."
echo ""

# 각 상태를 순회하며 테스트
for i in "${!STATES[@]}"; do
  STATE="${STATES[$i]}"
  DESCRIPTION="${STATE_DESCRIPTIONS[$STATE]}"

  echo -e "${YELLOW}[State $((i+1))/${#STATES[@]}]${NC} ${BLUE}${STATE}${NC} - ${DESCRIPTION}"
  echo ""

  # API 호출
  RESPONSE=$(curl -s -w "\n%{http_code}" \
    -H "X-Mock-Match-State: ${STATE}" \
    "${WIREMOCK_URL}/fixtures?id=${FIXTURE_ID}")

  # HTTP 상태 코드 추출
  HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
  BODY=$(echo "$RESPONSE" | sed '$d')

  # 상태 코드 확인
  if [ "$HTTP_CODE" = "200" ]; then
    echo -e "  ${GREEN}✓${NC} HTTP Status: ${GREEN}${HTTP_CODE}${NC}"

    # 주요 정보 추출 및 출력
    STATUS=$(echo "$BODY" | jq -r '.response[0].fixture.status.short')
    ELAPSED=$(echo "$BODY" | jq -r '.response[0].fixture.status.elapsed')
    HOME_GOALS=$(echo "$BODY" | jq -r '.response[0].goals.home')
    AWAY_GOALS=$(echo "$BODY" | jq -r '.response[0].goals.away')
    HOME_TEAM=$(echo "$BODY" | jq -r '.response[0].teams.home.name')
    AWAY_TEAM=$(echo "$BODY" | jq -r '.response[0].teams.away.name')

    echo "  Match Status: ${STATUS}"
    if [ "$ELAPSED" != "null" ]; then
      echo "  Elapsed: ${ELAPSED}'"
    fi
    echo "  Score: ${HOME_TEAM} ${HOME_GOALS:-0} - ${AWAY_GOALS:-0} ${AWAY_TEAM}"

    # 라인업 확인
    LINEUPS_COUNT=$(echo "$BODY" | jq -r '.response[0].lineups | length')
    if [ "$LINEUPS_COUNT" != "0" ]; then
      echo "  Lineups: ✓ Available"
    fi

    # 이벤트 수 확인
    EVENTS_COUNT=$(echo "$BODY" | jq -r '.response[0].events | length')
    if [ "$EVENTS_COUNT" != "0" ]; then
      echo "  Events: ${EVENTS_COUNT} events recorded"
    fi

    # 통계 확인
    STATS_COUNT=$(echo "$BODY" | jq -r '.response[0].statistics | length')
    if [ "$STATS_COUNT" != "0" ]; then
      echo "  Statistics: ✓ Available"
    fi
  else
    echo -e "  ${RED}✗${NC} HTTP Status: ${RED}${HTTP_CODE}${NC}"
    echo "  Response: ${BODY}"
  fi

  echo ""

  # 마지막 상태가 아니면 대기
  if [ $i -lt $((${#STATES[@]} - 1)) ]; then
    echo -e "${BLUE}⏳${NC} Waiting ${DELAY}s before next state..."
    echo ""
    sleep "$DELAY"
  fi
done

echo "────────────────────────────────────────────────────────────────"
echo ""
echo -e "${GREEN}✓${NC} Match state simulation completed!"
echo ""
echo "전체 상태 순환:"
for STATE in "${STATES[@]}"; do
  echo "  → ${STATE}"
done
echo ""
echo "수동으로 특정 상태를 테스트하려면:"
echo "  curl -H \"X-Mock-Match-State: first-half\" \\"
echo "    \"${WIREMOCK_URL}/fixtures?id=${FIXTURE_ID}\" | jq"
echo ""
