# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Footballay Backend** - A Spring Boot application providing live football/soccer match data. This is a hybrid Kotlin/Java project transitioning from Java to Kotlin, with the main application entry point in Java and newer modules in Kotlin.

**Main Application Class:** `com.footballay.core.ScoreBoardApplication` (Java)

## Build & Run Commands

### Development
```bash
# Run the application
./gradlew bootRun

# Run with specific profile (dev, mockapi, live, prod)
./gradlew bootRun --args='--spring.profiles.active=dev,mockapi'

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

# Run tests with profile
./gradlew test -Dspring.profiles.active=dev,mockapi
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
- Core business entities and rules
- `domain.football`: Football-specific domain models
- `domain.admin`: Admin functionality models (e.g., ApiSportsRoot)

**2. Application Layer** (`core.app`)
- Application controllers and service orchestration
- Thin layer for request handling

**3. Infrastructure Layer** (`core.infra`)
- External API integration and data persistence
- **Key subdirectories:**
  - `infra.apisports`: ApiSports API integration
    - `backbone`: Static data sync (leagues, teams, players, fixtures)
    - `match`: Live match data synchronization
    - `mapper`: Data transformation between ApiSports and core models
    - `shared`: Common utilities for ApiSports integration
  - `infra.core`: Core synchronization services
    - Sync services for leagues, teams, players, fixtures between provider data and core entities
  - `infra.facade`: Facade pattern implementations
    - `ApiSportsBackboneSyncFacade`: Admin-triggered synchronization of backbone data
    - `fetcher`: Provider resolution and match data fetching
  - `infra.persistence`: JPA repositories and entities
    - `persistence.core`: Core entity repositories
    - `persistence.apisports`: Provider-specific entity repositories
  - `infra.util`: Infrastructure utilities

**4. Web Layer** (`core.web`)
- Web controllers and REST endpoints
- `web.admin`: Admin web interface controllers
- `web.common`: Common web utilities and DTOs

**5. Configuration** (`core.config`)
- Spring configuration classes
- Security, JPA, Redis, WebSocket, monitoring configurations

### Data Synchronization Architecture

The system uses a **two-phase synchronization** approach with distinct responsibilities:

#### Phase 1: Backbone Data Sync (Admin-triggered, Static Data)

Synchronizes foundational static data that doesn't change frequently. This is typically triggered manually by administrators.

**What gets synced:**
- Leagues, Seasons
- Teams (by league/season)
- Players (by team)
- Fixture Schedules (matches with dates/times, but not live data)

**Flow:**
```
Admin Web → ApiSportsBackboneSyncFacade
         → FixtureApiSportsWithCoreSyncer (for fixtures)
         → Other Backbone Syncers (leagues, teams, players)
         → Core Sync Services (FixtureCoreSyncService, etc.)
         → Persistence (ApiSports + Core entities)
```

**Key Component: `FixtureApiSportsWithCoreSyncer`**
- Implements 6-phase TDD-driven synchronization for fixture schedules:
  1. **Phase 1**: Input validation (league ID, DTOs, season consistency)
  2. **Phase 2**: Data collection (existing fixtures, teams, leagues from DB)
  3. **Phase 3**: Venue processing (create/update VenueApiSports)
  4. **Phase 4**: Fixture case separation (both exist, api only, both new)
  5. **Phase 5**: FixtureCore creation with UID generation (Identity Pairing Pattern)
  6. **Phase 6**: FixtureApiSports creation/update with FK relationships
- **Prerequisites:** LeagueApiSports, LeagueApiSportsSeason, TeamCore, TeamApiSports must exist
- **Creates/Updates:** VenueApiSports → FixtureCore → FixtureApiSports (in order)
- **Constraint:** All DTOs must belong to the same season

#### Phase 2: Live Match Data Sync (Scheduled/Real-time, Dynamic Data)

Synchronizes real-time match data during live games. This runs on a schedule or triggered by events.

**What gets synced:**
- Match status (not started, live, finished)
- Live scores and goals
- Match events (goals, cards, substitutions)
- Player statistics
- Team statistics

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
- `FetcherProviderResolver` interface with `SimpleFetcherProviderResolver` implementation
- Iterates through all registered `MatchDataSyncer` implementations
- Each syncer's `isSupport(fixtureUid)` determines if it can handle the match
- Currently only `ApiSportsMatchSyncer` exists, which always returns `true`
- **Extensible:** Future providers (SportMonks, etc.) can be added by implementing `MatchDataSyncer`

**Match Data Syncer Interface:**
```kotlin
interface MatchDataSyncer {
    fun isSupport(uid: String): Boolean      // Can this provider handle this fixture?
    fun syncMatchData(uid: String): ActionAfterMatchSync  // Sync and return next action
}
```

**ActionAfterMatchSync:**
- `ongoing(kickoffTime)`: Match is live, continue polling
- `finished(kickoffTime)`: Match ended, stop polling

**Key Design Patterns:**
- **Provider Abstraction**: `MatchDataSyncer` interface allows multiple data providers (ApiSports, SportMonks, etc.)
- **Facade Pattern**: Simplifies complex synchronization workflows
- **Core Entity Separation**: Provider data (ApiSports tables) kept separate from core entities
- **UID-based Matching**: Uses provider-prefixed UIDs (e.g., `apisports:12345`) for cross-provider support
- **Identity Pairing Pattern**: UIDs generated and paired with DTOs before core entity creation
- **TDD-driven Phases**: Complex sync operations broken into testable phases

### Testing Architecture

**Mock Data Strategy:**
- Mock JSON files in `src/main/resources/mock/apisports/`
- Mock fetcher: `ApiSportsV3MockFetcher` provides test data
- Use `@ActiveProfiles("dev", "mockapi")` for tests with mock data
- Supported mock scenarios documented in `src/main/resources/mock/apisports/README.md`

**Integration Tests:**
- Located in `src/test/kotlin/com/footballay/core/infra/`
- Use Testcontainers for PostgreSQL and Redis
- Follow the sync workflow: leagues → teams → players/fixtures

## Spring Profiles

The application uses profile groups for environment configuration:

- **dev**: Development environment (includes `devbase`, `devpostgre`, `devaws`)
- **mockapi**: Uses mock JSON data instead of real API calls (includes `mockpath`, `mockapi`)
- **live**: Production with live API (includes `api`, `aws`, `secret`)
- **prod**: Production environment (includes `prodbase`)

Configuration files are organized in `src/main/resources/config/{profile}/`

## Technology Stack

- **Language**: Kotlin 2.1.21 (migrating from Java), Java 17
- **Framework**: Spring Boot 3.4.6
- **Database**: PostgreSQL (prod/dev), H2 (tests)
- **Cache**: Redis
- **ORM**: Spring Data JPA with Hibernate
- **Build Tool**: Gradle with Kotlin DSL
- **Testing**: JUnit 5, Testcontainers, MockK, Mockito-Kotlin
- **Monitoring**: Micrometer, Prometheus, Loki
- **Cloud**: AWS (S3, CloudFront)
- **Scheduling**: Quartz Scheduler
- **Security**: Spring Security

## Key Implementation Notes

### Kotlin Migration
- New code should be written in Kotlin
- The project uses both `src/main/kotlin` and `src/main/java` directories
- Kotlin annotation processing (KAPT) is configured for JPA entities
- Use data classes for DTOs and entities where appropriate

### Entity Relationships
- **Provider Entities** (e.g., `LeagueApiSports`, `TeamApiSports`): Store provider-specific data with `apiId`
- **Core Entities** (e.g., `LeagueCore`, `TeamCore`): Provider-agnostic entities with cross-references
- Core entities have relationships to provider entities for data enrichment

### Match Synchronization
- Each provider implements `MatchDataSyncer` interface
- `isSupport(uid)` checks if the provider can handle a match
- `syncMatchData(uid)` performs the actual synchronization
- Returns `ActionAfterMatchSync` to guide the next polling action

### Testing Best Practices
- Use mock profile for integration tests to avoid API rate limits
- Always sync dependencies first: leagues → teams → players/fixtures
- Use `em.flush()` and `em.clear()` to ensure database state in tests
- Check both provider entities and core entities in assertions

## Common Pitfalls

1. **Profile Configuration**: Ensure correct profiles are active. Dev requires database setup; mockapi bypasses real APIs.
2. **Sync Order**: Always sync in correct order (leagues before teams, teams before fixtures).
3. **Provider UIDs**: Match UIDs are prefixed with provider name (e.g., `apisports:1208021`).
4. **Mock Data Limitations**: Mock data only supports specific leagues/teams (Premier League ID 39, Manchester City ID 50, etc.). See mock README for details.
5. **Transactional Tests**: Integration tests use `@Transactional` which rolls back changes after each test.
