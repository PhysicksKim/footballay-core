# Footballay API 리팩토링

이 브랜치는 Footballay 서비스의 API 의존성을 줄이기 위한 리팩토링 작업을 포함하고 있습니다.

## 주요 목표

-   API 제공자(API-Sports, Sportmonks 등)에 대한 의존성 제거
-   API ID 의존성 문제 해결 (API별로 다른 ID 체계, ID가 없는 경우의 대응)
-   API 장애 발생 시 Fallback 기능 구현
-   다중 API 데이터 융합 기능 구현
-   DDD(Domain Driven Design) 원칙 적용

## 주요 컴포넌트

### 도메인 레이어 (Domain Layer)

-   `domain/shared`: 공통 도메인 모델 및 유틸리티
-   `domain/fixture`: 경기 관련 도메인 모델 및 서비스
-   `domain/player`: 선수 관련 도메인 모델 및 서비스
-   `domain/team`: 팀 관련 도메인 모델 및 서비스
-   `domain/league`: 리그 관련 도메인 모델 및 서비스

### 인프라스트럭처 레이어 (Infrastructure Layer)

-   `infrastructure/apisports`: API-Sports 관련 구현체
-   `infrastructure/sportmonks`: Sportmonks 관련 구현체

### 어플리케이션 레이어 (Application Layer)

-   `application/service/provider`: API 제공자 관리 서비스
-   `application/service`: 데이터 융합 서비스 등
-   `application/config`: API 설정 클래스
-   `application/dto`: 데이터 전송 객체

## 주요 기능

### 1. API 추상화

`DataProvider` 인터페이스를 통해 여러 API 제공자(API-Sports, Sportmonks 등)에 대한 추상화 계층을 제공합니다.

```kotlin
interface DataProvider {
    val name: String
    val priority: Int

    fun fetchLeague(leagueId: EntityId): League?
    fun fetchTeam(teamId: EntityId): Team?
    // ...
}
```

### 2. API ID 의존성 제거

API 제공자별로 다른 ID 체계를 사용하거나, ID가 없는 경우를 대응하기 위해 `EntityId` 추상화를 통한 ID 독립성을 제공합니다.

```kotlin
interface EntityId {
    fun getValue(): String
    fun isEmpty(): Boolean
}

data class CompositeEntityId(private val values: Map<String, String>) : EntityId {
    // 이름, 포지션 등 복합키를 통한 식별
}
```

### 3. API Fallback

Primary API에 문제가 발생하면 Secondary API로 자동 전환하는 기능을 제공합니다.

```kotlin
fun fetchFixture(fixtureId: EntityId): Fixture? {
    val sortedProviders = getProviders(TargetType.FIXTURE, fixtureId.getValue())

    for (provider in sortedProviders) {
        try {
            val fixture = provider.fetchFixture(fixtureId)
            if (fixture != null && fixtureIntegrityChecker.checkFixtureIntegrity(fixture).isValid) {
                return fixture
            }
        } catch (e: Exception) {
            logger.warn("Provider ${provider.name} failed: ${e.message}")
        }
    }

    return null
}
```

### 4. 수동 API 오버라이드

관리자가 특정 데이터에 대해 사용할 API 제공자를 수동으로 지정할 수 있는 기능을 제공합니다.

```kotlin
@Entity
@Table(name = "provider_override", schema = "footballay_core")
data class ProviderOverride(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "target_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val targetType: TargetType,

    @Column(name = "target_id", nullable = false)
    val targetId: String,

    @Column(name = "provider_name", nullable = false)
    val providerName: String,

    // ...
)
```

### 5. 데이터 융합

여러 API 제공자에서 가져온 데이터를 통합하는 기능을 제공합니다.

```kotlin
fun combineFixtures(primary: Fixture, secondary: Fixture): Fixture {
    // 두 경기 데이터를 병합하는 로직
}
```

## 설정 방법

1. `application-apirefac.yml` 파일에 API 키 등 필요한 설정 추가
2. 스프링 프로파일을 `apirefac`으로 설정하여 실행

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=apirefac
```

## 테스트

-   단위 테스트: `./mvnw test`
-   통합 테스트: `./mvnw verify`

## 주의사항

-   기존 Java 코드는 변경하지 않고, Kotlin 코드만 추가
-   프로덕션 환경에서는 API 키를 환경 변수로 설정 권장
-   API 제공자 추가 시 `DataProvider` 인터페이스 구현체만 추가하면 됨
