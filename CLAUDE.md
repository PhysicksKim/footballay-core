# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## AI Guidance

-   별도의 지시가 없다면 반드시 한국어를 사용해서 질문 답변 해주세요. 
-   Ignore GEMINI.md and GEMINI-\*.md files
-   To save main context space, for code searches, inspections, troubleshooting or analysis, use code-searcher subagent where appropriate - giving the subagent full context background for the task(s) you assign it.
-   After receiving tool results, carefully reflect on their quality and determine optimal next steps before proceeding. Use your thinking to plan and iterate based on this new information, and then take the best next action.
-   For maximum efficiency, whenever you need to perform multiple independent operations, invoke all relevant tools simultaneously rather than sequentially.
-   Before you finish, please verify your solution
-   Do what has been asked; nothing more, nothing less.
-   NEVER create files unless they're absolutely necessary for achieving your goal.
-   ALWAYS prefer editing an existing file to creating a new one.
-   NEVER proactively create documentation files (\*.md) or README files. Only create documentation files if explicitly requested by the User.
-   When you update or modify core context files, also update markdown documentation and memory bank
-   When asked to commit changes, exclude CLAUDE.md and CLAUDE-\*.md referenced memory bank system files from any commits. Never delete these files.

## Memory Bank System

This project uses a structured memory bank system with specialized context files. Always check these files for relevant information before starting work:

### Core Context Files

-   **CLAUDE-activeContext.md** - Current session state, goals, and progress (if exists)
-   **CLAUDE-patterns.md** - Established code patterns and conventions (if exists)
-   **CLAUDE-decisions.md** - Architecture decisions and rationale (if exists)
-   **CLAUDE-troubleshooting.md** - Common issues and proven solutions (if exists)
-   **CLAUDE-config-variables.md** - Configuration variables reference (if exists)
-   **CLAUDE-temp.md** - Temporary scratch pad (only read when referenced)

**Important:** Always reference the active context file first to understand what's currently being worked on and maintain session continuity.

### Memory Bank System Backups

When asked to backup Memory Bank System files, you will copy the core context files above and @.claude settings directory to directory @/path/to/backup-directory. If files already exist in the backup directory, you will overwrite them.

## Project Overview

## ALWAYS START WITH THESE COMMANDS FOR COMMON TASKS

**Task: "List/summarize all files and directories"**

```bash
fd . -t f           # Lists ALL files recursively (FASTEST)
# OR
rg --files          # Lists files (respects .gitignore)
```

**Task: "Search for content in files"**

```bash
rg "search_term"    # Search everywhere (FASTEST)
```

**Task: "Find files by name"**

```bash
fd "filename"       # Find by name pattern (FASTEST)
```

### Directory/File Exploration

```bash
# FIRST CHOICE - List all files/dirs recursively:
fd . -t f           # All files (fastest)
fd . -t d           # All directories
rg --files          # All files (respects .gitignore)

# For current directory only:
ls -la              # OK for single directory view
```

### BANNED - Never Use These Slow Tools

-   ❌ `tree` - NOT INSTALLED, use `fd` instead
-   ❌ `find` - use `fd` or `rg --files`
-   ❌ `grep` or `grep -r` - use `rg` instead
-   ❌ `ls -R` - use `rg --files` or `fd`
-   ❌ `cat file | grep` - use `rg pattern file`

### Use These Faster Tools Instead

```bash
# ripgrep (rg) - content search
rg "search_term"                # Search in all files
rg -i "case_insensitive"        # Case-insensitive
rg "pattern" -t py              # Only Python files
rg "pattern" -g "*.md"          # Only Markdown
rg -1 "pattern"                 # Filenames with matches
rg -c "pattern"                 # Count matches per file
rg -n "pattern"                 # Show line numbers
rg -A 3 -B 3 "error"            # Context lines
rg " (TODO| FIXME | HACK)"      # Multiple patterns

# ripgrep (rg) - file listing
rg --files                      # List files (respects •gitignore)
rg --files | rg "pattern"       # Find files by name
rg --files -t md                # Only Markdown files

# fd - file finding
fd -e js                        # All •js files (fast find)
fd -x command {}                # Exec per-file
fd -e md -x ls -la {}           # Example with ls

# jq - JSON processing
jq. data.json                   # Pretty-print
jq -r .name file.json           # Extract field
jq '.id = 0' x.json             # Modify field
```

### Search Strategy

1. Start broad, then narrow: `rg "partial" | rg "specific"`
2. Filter by type early: `rg -t python "def function_name"`
3. Batch patterns: `rg "(pattern1|pattern2|pattern3)"`
4. Limit scope: `rg "pattern" src/`

### INSTANT DECISION TREE

```
User asks to "list/show/summarize/explore files"?
  → USE: fd . -t f  (fastest, shows all files)
  → OR: rg --files  (respects .gitignore)

User asks to "search/grep/find text content"?
  → USE: rg "pattern"  (NOT grep!)

User asks to "find file/directory by name"?
  → USE: fd "name"  (NOT find!)

User asks for "directory structure/tree"?
  → USE: fd . -t d  (directories) + fd . -t f  (files)
  → NEVER: tree (not installed!)

Need just current directory?
  → USE: ls -la  (OK for single dir)
```

## Project Overview

**Footballay Backend** - A Spring Boot application providing live football/soccer match data. This is a hybrid Kotlin/Java project transitioning from Java to Kotlin, with the main application entry point in Java and newer modules in Kotlin.

**Main Application Class:** `com.footballay.core.ScoreBoardApplication` (Java)

## Build & Run Commands

### Development

```bash
# Run the application - Local development (HTTP, Mock API)
./gradlew bootRun --args='--spring.profiles.active=local'

# Run with real API - Local
./gradlew bootRun --args='--spring.profiles.active=local,devrealapi'

# Run the application - Dev remote server (HTTPS via Cloudflare Tunnel)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Run with real API - Dev remote
./gradlew bootRun --args='--spring.profiles.active=dev,devrealapi'

# Build the project
./gradlew build

# Build without tests
./gradlew build -x test

# Clean build
./gradlew clean build
```

### Testing

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "com.footballay.core.infra.facade.ApiSportsSyncFacadeIntegrationTest"

# Run a specific test method
./gradlew test --tests "com.footballay.core.infra.facade.ApiSportsSyncFacadeIntegrationTest.완전한 통합 테스트 - 전체 동기화 워크플로우"

# Run tests with specific profile (optional, tests use appropriate profiles by default)
./gradlew test -Dspring.profiles.active=dev
```

### Code Quality

```bash
# Check code style with ktlint
./gradlew ktlintCheck

# Auto-format code with ktlint
./gradlew ktlintFormat
```

### Development Environment Setup

```bash
# 1. Start Docker containers (PostgreSQL, Redis)
cd ./src/main/resources/docker
docker-compose up -d

# 2. Initialize database with Quartz schema
# See: https://github.com/elventear/quartz-scheduler/tree/master/distribution/src/main/assembly/root/docs/dbTables

# 3. Create Spring Security remember-me table
# Run the SQL in README.md

# 4. Add admin user (see README.md for SQL commands)
```

## Architecture Overview

### Layer Structure

The codebase follows a multi-layered architecture with clear separation of concerns:

**1. Domain Layer** (`core.domain`)

-   Core business entities and rules
-   `domain.football`: Football-specific domain models
-   `domain.admin`: Admin functionality models (e.g., ApiSportsRoot)

**2. Application Layer** (`core.app`)

-   Application controllers and service orchestration
-   Thin layer for request handling

**3. Infrastructure Layer** (`core.infra`)

-   External API integration and data persistence
-   **Key subdirectories:**
    -   `infra.apisports`: ApiSports API integration
        -   `backbone`: Static data sync (leagues, teams, players, fixtures)
        -   `match`: Live match data synchronization
        -   `mapper`: Data transformation between ApiSports and core models
        -   `shared`: Common utilities for ApiSports integration
    -   `infra.core`: Core synchronization services
        -   Sync services for leagues, teams, players, fixtures between provider data and core entities
    -   `infra.facade`: Facade pattern implementations
        -   `ApiSportsBackboneSyncFacade`: Admin-triggered synchronization of backbone data
        -   `fetcher`: Provider resolution and match data fetching
    -   `infra.dispatcher`: Match data sync orchestration
        -   `MatchDataSyncDispatcher`: Interface for dispatching match sync jobs
        -   `SimpleMatchDataSyncDispatcher`: Implementation that orchestrates job execution and result processing
    -   `infra.scheduler`: Quartz job implementations
        -   `PreMatchJob`: Executes before match starts
        -   `LiveMatchJob`: Polls during live matches
        -   `PostMatchJob`: Finalizes after match ends
    -   `infra.persistence`: JPA repositories and entities
        -   `persistence.core`: Core entity repositories
        -   `persistence.apisports`: Provider-specific entity repositories
    -   `infra.util`: Infrastructure utilities

**4. Web Layer** (`core.web`)

-   Web controllers and REST endpoints
-   `web.admin`: Admin web interface controllers
-   `web.common`: Common web utilities and DTOs

#### API Version Policy and Request Mapping

The application uses a clear API versioning strategy for subdomain-based routing:

**Legacy API (구버전)** - Direct `/api/*` paths:
-   `/api/admin/football/*` - Admin football data management (Java)
    -   AdminFootballCustomPhotoController - Player photo management
    -   AdminFootballDataRestController - Leagues, teams, fixtures, players CRUD
    -   AdminFootballCacheRestController - Cache management
-   `/api/admin/*` - Admin user role management (Java)
    -   AdminUserRoleController - User info and role management
-   `/api/football/*` - Public football streaming API (Java)
    -   FootballStreamDataController - Live match data, fixtures, leagues, teams
-   `/api/scoreboard/*` - Scoreboard WebSocket API (Java)
    -   ScoreBoardRemoteController - Remote control REST endpoints

**Versioned API (신버전)** - `/api/v1/admin/*` paths:
-   `/api/v1/admin/apisports/*` - ApiSports sync operations (Kotlin)
    -   AdminApiSportsController - Leagues, teams, players, fixtures synchronization
-   `/api/v1/admin/fixtures/*` - Fixture management (Kotlin)
    -   AdminFixtureAvailableController - Fixture availability and job scheduling

**Health Check Endpoints:**
-   `/health` - Root domain health check (footballay.com)
    -   Used by Docker HEALTHCHECK and deployment scripts
    -   Lightweight check without external dependencies
-   `/api/health` - API subdomain health check (api.footballay.com)
    -   Status check for all API endpoints
    -   Same response format as `/health`
-   `/api/v1/admin/apisports/health` - ApiSports admin specific health check
    -   Included in AdminApiSportsController

**Subdomain Routing Summary:**
```
api.footballay.com (API subdomain)
├── /api/football/*          → Public API
├── /api/scoreboard/*        → Scoreboard API
├── /api/admin/*             → Admin API (legacy)
├── /api/v1/admin/*          → Admin API (versioned)
└── /api/health              → API health check

footballay.com (Root domain)
├── /                        → Main page
├── /scoreboard              → Scoreboard page
├── /error                   → Error page
├── /health                  → Root health check (Docker)
├── /gyechune                → Gyechune multi-domain
└── /gyechunhoe              → Gyechunhoe multi-domain

admin.footballay.com (Admin subdomain)
└── (Nginx serves admin SPA static files)
    → API requests go to api.footballay.com/api/admin/*

static.footballay.com (Static subdomain)
└── (CDN serves static assets, not handled by Spring Boot)
```

**5. Configuration** (`core.config`)

-   Spring configuration classes
-   Security, JPA, Redis, WebSocket, monitoring configurations

### Data Synchronization Architecture

The system uses a **two-phase synchronization** approach with distinct responsibilities:

#### Phase 1: Backbone Data Sync (Admin-triggered, Static Data)

Synchronizes foundational static data that doesn't change frequently. This is typically triggered manually by administrators.

**What gets synced:**

-   Leagues, Seasons
-   Teams (by league/season)
-   Players (by team)
-   Fixture Schedules (matches with dates/times, but not live data)

**Flow:**

```
Admin Web → ApiSportsBackboneSyncFacade
         → FixtureApiSportsWithCoreSyncer (for fixtures)
         → Other Backbone Syncers (leagues, teams, players)
         → Core Sync Services (FixtureCoreSyncService, etc.)
         → Persistence (ApiSports + Core entities)
```

**Key Component: `FixtureApiSportsWithCoreSyncer`**

-   Implements 6-phase TDD-driven synchronization for fixture schedules:
    1. **Phase 1**: Input validation (league ID, DTOs, season consistency)
    2. **Phase 2**: Data collection (existing fixtures, teams, leagues from DB)
    3. **Phase 3**: Venue processing (create/update VenueApiSports)
    4. **Phase 4**: Fixture case separation (both exist, api only, both new)
    5. **Phase 5**: FixtureCore creation with UID generation (Identity Pairing Pattern)
    6. **Phase 6**: FixtureApiSports creation/update with FK relationships
-   **Prerequisites:** LeagueApiSports, LeagueApiSportsSeason, TeamCore, TeamApiSports must exist
-   **Creates/Updates:** VenueApiSports → FixtureCore → FixtureApiSports (in order)
-   **Constraint:** All DTOs must belong to the same season

#### Phase 2: Live Match Data Sync (Scheduled/Real-time, Dynamic Data)

Synchronizes real-time match data during live games. This runs on a schedule or triggered by events.

**What gets synced:**

-   Match status (not started, live, finished)
-   Live scores and goals
-   Match events (goals, cards, substitutions)
-   Player statistics
-   Team statistics

**Flow:**

```
Scheduler/Poller → FetcherProviderResolver (SimpleFetcherProviderResolver)
                → MatchDataSyncer (interface)
                → ApiSportsMatchSyncer (implementation)
                → 1. Pre-sync players (FixturePlayerExtractor + PlayerSyncExecutor)
                → 2. MatchApiSportsSyncer (sync match entities)
                → Returns ActionAfterMatchSync (ongoing/finished)
```

**Provider Resolution Pattern:**

-   `FetcherProviderResolver` interface with `SimpleFetcherProviderResolver` implementation
-   Iterates through all registered `MatchDataSyncer` implementations
-   Each syncer's `isSupport(fixtureUid)` determines if it can handle the match
-   Currently only `ApiSportsMatchSyncer` exists, which always returns `true`
-   **Extensible:** Future providers (SportMonks, etc.) can be added by implementing `MatchDataSyncer`

**Match Data Syncer Interface:**

```kotlin
interface MatchDataSyncer {
    fun isSupport(uid: String): Boolean      // Can this provider handle this fixture?
    fun syncMatchData(uid: String): ActionAfterMatchSync  // Sync and return next action
}
```

**ActionAfterMatchSync:**

-   `ongoing(kickoffTime)`: Match is live, continue polling
-   `finished(kickoffTime)`: Match ended, stop polling

**Key Design Patterns:**

-   **Provider Abstraction**: `MatchDataSyncer` interface allows multiple data providers (ApiSports, SportMonks, etc.)
-   **Facade Pattern**: Simplifies complex synchronization workflows
-   **Core Entity Separation**: Provider data (ApiSports tables) kept separate from core entities
-   **UID-based Matching**: Uses provider-prefixed UIDs (e.g., `apisports:12345`) for cross-provider support
-   **Identity Pairing Pattern**: UIDs generated and paired with DTOs before core entity creation
-   **TDD-driven Phases**: Complex sync operations broken into testable phases

### Testing Architecture

**Mock Data Strategy:**

-   Mock JSON files in `src/main/resources/mock/apisports/`
-   Mock fetcher: `ApiSportsV3MockFetcher` provides test data
-   Use `@ActiveProfiles("dev", "mockapi")` for tests with mock data
-   Supported mock scenarios documented in `src/main/resources/mock/apisports/README.md`

**Integration Tests:**

-   Located in `src/test/kotlin/com/footballay/core/infra/`
-   Use Testcontainers for PostgreSQL and Redis
-   Follow the sync workflow: leagues → teams → players/fixtures

## Spring Profiles

The application uses a profile structure based on subdomain architecture with standard `application-XXX.yml` files:

### Profile Files

All configuration files are located in `src/main/resources/`:

-   **application.yml** - Base configuration (common across all environments)
-   **application-local.yml** - Local development (Vite dev server + Spring Boot, HTTP, Mock API)
-   **application-dev.yml** - Dev remote server (Cloudflare Tunnel, HTTPS, Mock API)
-   **application-devrealapi.yml** - Development with real API (overrides Mock API settings)
-   **application-test.yml** - Test environment (H2 in-memory, Mock API)
-   **application-prod.yml** - Production public configuration
-   **application-live.yml** - Production secrets (imports external files)
-   **application-api.yml** - API keys (gitignored)

### Subdomain-based Architecture

The system is designed to separate API and Admin SPA via subdomains:

**Local (localhost):**
- Frontend: Vite dev server at `http://localhost:5173` (proxies `/api` to Spring Boot)
- Backend: Spring Boot at `http://localhost:8083`
- Session cookie: host-only (no domain), `secure: false`

**Dev Remote (*.dev.footballay.com):**
- API: `https://api.dev.footballay.com` → Spring Boot (API only)
- Frontend: **Still using localhost Vite dev server** (`http://localhost:5173`)
  - Each developer runs Vite locally and connects to shared dev API server
  - Enables collaboration without running backend locally
- Admin SPA serving from `admin.dev.footballay.com`: **Not planned yet**
- Session cookie: `domain: .dev.footballay.com`, `secure: true`
- Note: `static.dev.footballay.com` may be used for file upload testing, but not for admin SPA

**Production (*.footballay.com):**
- API: `https://api.footballay.com` → Spring Boot (API only)
- Admin SPA: `https://admin.footballay.com` → Nginx → S3/CloudFront (static files)
- Session cookie: `domain: .footballay.com`, `secure: true`

### Usage

**Local Development (Mock API):**
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

**Local Development with Real API:**
```bash
./gradlew bootRun --args='--spring.profiles.active=local,devrealapi'
```

**Dev Remote Server (Mock API):**
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Dev Remote with Real API:**
```bash
./gradlew bootRun --args='--spring.profiles.active=dev,devrealapi'
```

**Production:**
```bash
--spring.profiles.active=prod,live
```

### Test Profiles

-   `@ActiveProfiles("local")` - Integration tests with PostgreSQL + Mock API (local environment)
-   `@ActiveProfiles("test")` - Unit tests with H2 in-memory + Mock API
-   `@ActiveProfiles("dev", "devrealapi")` - Tests with real API calls

### Important Notes

- **Admin SPA serving:** Spring Boot no longer serves admin static files. All static files are served by Nginx/CDN.
- **CORS:** Each profile has subdomain-specific allowed origins configured.
- **Session cookies:** Domain setting varies by environment to enable subdomain session sharing.
- **Dev Server 환경별 쿠키 정책:**

| 환경 | SameSite | Secure | Domain | 설명 |
|------|----------|--------|--------|------|
| **local** | Lax | false | (host-only) | 로컬 개발, Vite 프록시 사용 |
| **dev** | **None** | true | .dev.footballay.com | **Cross-site 요청 지원** (localhost:5173 → api.dev.footballay.com) |
| **prod** | Lax | true | .footballay.com | 프로덕션 배포 |

- **Dev 환경에서 SameSite=None 사용 이유**: localhost Vite에서 dev server로의 cross-site 요청을 위해 필수. Cloudflare Access로 보호되며, Production DB와 분리된 환경이므로 보안 리스크 제한적.
- **자세한 설정 가이드**: `docs/dev-server-localhost-vite-setup.md` 참조

## Technology Stack

-   **Language**: Kotlin 2.1.21 (migrating from Java), Java 17
-   **Framework**: Spring Boot 3.4.6
-   **Database**: PostgreSQL (prod/dev), H2 (tests)
-   **Cache**: Redis
-   **ORM**: Spring Data JPA with Hibernate
-   **Build Tool**: Gradle with Groovy (build.gradle)
-   **Testing**: JUnit 5, Testcontainers, MockK, Mockito-Kotlin
-   **Monitoring**: Micrometer, Prometheus, Loki
-   **Cloud**: AWS (S3), Cloudflare (DNS, R2)
-   **Scheduling**: Quartz Scheduler
-   **Security**: Spring Security
-   **Code Quality**: ktlint

## Key Implementation Notes

### Kotlin Migration

-   New code should be written in Kotlin
-   The project uses both `src/main/kotlin` and `src/main/java` directories
-   Kotlin annotation processing (KAPT) is configured for JPA entities
-   Use data classes for DTOs and entities where appropriate

### Entity Relationships

-   **Provider Entities** (e.g., `LeagueApiSports`, `TeamApiSports`): Store provider-specific data with `apiId`
-   **Core Entities** (e.g., `LeagueCore`, `TeamCore`): Provider-agnostic entities with cross-references
-   Core entities have relationships to provider entities for data enrichment

### Match Synchronization

-   Each provider implements `MatchDataSyncer` interface
-   `isSupport(uid)` checks if the provider can handle a match
-   `syncMatchData(uid)` performs the actual synchronization
-   Returns `ActionAfterMatchSync` to guide the next polling action

### Testing Best Practices

-   Use `@ActiveProfiles("test")` for unit tests with H2 in-memory database
-   Use `@ActiveProfiles("dev")` for integration tests with PostgreSQL + Mock API
-   Use `@ActiveProfiles("dev", "devrealapi")` for tests requiring real API calls
-   Always sync dependencies first: leagues → teams → players/fixtures
-   Use `em.flush()` and `em.clear()` to ensure database state in tests
-   Check both provider entities and core entities in assertions

## Common Pitfalls

1. **Profile Configuration**: Ensure correct profiles are active.
   - Use `local` for local development (HTTP, localhost)
   - Use `dev` for dev remote server (HTTPS, *.dev.footballay.com)
   - Add `devrealapi` overlay for real API calls in both environments
2. **Admin Static Files**: Spring Boot no longer serves admin static files. All admin pages must be accessed through Nginx/CDN or Vite dev server.
3. **Sync Order**: Always sync in correct order (leagues before teams, teams before fixtures).
4. **UID Generation**: All entity UIDs (fixtures, leagues, teams, players, etc.) are randomly generated 16-character strings containing only lowercase letters and numbers, created by `SimpleUidGenerator`. Example: `"a1b2c3d4e5f6g7h8"`. They are NOT prefixed with provider names.
5. **Mock Data Limitations**: Mock data only supports specific leagues/teams (Premier League ID 39, Manchester City ID 50, etc.). See mock README for details.
6. **Transactional Tests**: Integration tests use `@Transactional` which rolls back changes after each test.
7. **Session Cookies**: Domain settings differ by environment. Dev sessions won't work in prod and vice versa.

## WireMock Mock Server (프론트엔드 개발 지원)

WireMock은 프론트엔드 개발자가 백엔드 서버 없이도 개발할 수 있도록 지원하는 Mock API 서버입니다. **주 용도는 Desktop App에서 live match data polling 시 UI가 제대로 업데이트되는지 테스트하는 것**입니다.

### 사용 목적

-   **Live Data Polling UI 테스트**: `X-Mock-Match-State` 헤더를 통해 경기 상태(pre-match → first-half → half-time → second-half → full-time)를 시뮬레이션
-   **Desktop App 개발**: 프론트엔드에서 polling할 때마다 변하는 응답을 받아 UI 업데이트 동작 확인
-   **독립적인 프론트엔드 개발**: 백엔드 서버 실행 없이 API 응답 테스트 가능

### 사용 제한

-   **Admin Page CRUD 테스트에는 부적합**: Admin 페이지의 실제 CRUD 동작 테스트는 실제 dev 서버를 사용해야 함
-   **부차적 도구**: 실제 개발은 dev 서버를 통해 진행하고, 특정 시나리오 테스트가 필요할 때만 사용

### 빠른 시작

```bash
# WireMock 서버 시작
cd wiremock && docker-compose up -d

# 서버 확인
# - Mock API: http://localhost:8888
# - Swagger UI: http://localhost:8889
```

### 경기 상태 시뮬레이션

```bash
# 전반전 상태로 요청
curl -H "X-Mock-Match-State: first-half" \
  "http://localhost:8888/fixtures?id=1208021"

# 후반전 상태로 요청
curl -H "X-Mock-Match-State: second-half" \
  "http://localhost:8888/fixtures?id=1208021"
```

**사용 가능한 상태**: `pre-match`, `lineup-announced`, `first-half`, `half-time`, `second-half`, `full-time`

자세한 사용법은 `wiremock/README.md`를 참조하세요.
