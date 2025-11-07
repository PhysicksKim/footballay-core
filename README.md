# ❗ 꼭 읽어주세요

<img width="574" height="255" alt="footballay-core-kotlin-refac-resize" src="https://github.com/user-attachments/assets/85a439f7-c6e3-4116-9161-cc9be6e1cbb5" />

## 인터페이스 없이 개발했다고?

현재 라이브 서버에서 돌아가는 `main` 코드에는 거의 인터페이스 경계가 없습니다.
초기에는 객체지향과 SOLID 원칙을 알고는 있었지만 실제로 어디에, 어떻게 적용해야 할지 기준이 없었고, 그래서

> **"어차피 개인 프로젝트니까 인터페이스 없이 만들어서 문제를 체감해보자"**

라는 방식으로 대부분을 구현체 중심, 추상화 없이 작성했습니다.

<br>

## 재설계 브랜치: `kotlin-refac`
진행 중인 재설계 작업은 `kotlin-refac` 브랜치에서 확인하실 수 있습니다.
운영하면서 드러난 여러 문제를 바탕으로 구조를 다음과 같이 재정의했습니다.

- **Core / API (Primary → Secondary) 분리**
  사용자 요청과 흐름은 Core가 담당하고, 실제 데이터 취득과 소스별 책임은 API가 맡도록 역할을 분리했습니다.

- **API 계층을 Backbone / Match 구조로 분리**
  API 쪽은 `{리그, 팀, 선수, 경기일정}` 같은 기반 정보(Backbone)와, 경기별 `{라인업, 이벤트, 통계}`를 담는 Match로 나뉩니다.

- **FullDto → 세분화된 DTO → Entity 저장 파이프라인**
  기존에는 전체 경기 응답을 통째로 쓰는 구조 때문에 통합 테스트에만 의존하게 되었지만, 현재는 FullDto를 `{base, lineup, events, playerStats, teamStats}`로 분해해 책임 단위로 독립적인 단위 테스트가 가능하도록 했습니다.

- **인터페이스 기반 책임 분리 및 단위 테스트**
  기존에는 테스트를 위해 매번 방대한 연관 엔티티를 셋업해야 했습니다. 재설계 후에는 책임 경계를 인터페이스로 나누고, JPA 엔티티가 반드시 필요하지 않은 흐름에서는 DTO를 활용해 테스트 셋업 부담을 줄였습니다.


<br><br><br>

---

<br><br><br>

# Footballay Backend

축구 라이브 데이터 제공 앱 Footballay 의 백엔드 입니다.

<br>

# Architecture

<img width="3992" height="2351" alt="footballay-architecture" src="https://github.com/user-attachments/assets/97d4b4ff-e9bb-4adb-82e8-ded79d24c543" />

---

# Dev Env Init

### 1. Docker-Compose up
```bash
cd ./src/main/resources/docker
docker-compose up -d
```

### 2. RDB : Quartz Scheme
[🔗 Quartz SQL schemes](https://github.com/elventear/quartz-scheduler/tree/master/distribution/src/main/assembly/root/docs/dbTables)

### 3. RDB : Spring Security Remember-Me Scheme
```sql
create table persistent_logins (
	username varchar(64) not null,
	series varchar(64) primary key,
	token varchar(64) not null,
	last_used timestamp not null
);
```
[🔗 Spring Security docs - Security Database Schema](https://docs.spring.io/spring-security/reference/servlet/appendix/database-schema.html#_persistent_login_remember_me_schema)

### 4. Add admin user and authority

```sql
INSERT INTO footballay_core.users(username,password,nickname,enabled,created_date, modified_date)
    VALUES ('qwer','{noop}qwer','qwer',true,NOW(),NOW());
```

```sql
INSERT INTO footballay_core.authorities (user_id,authority,created_date,modified_date)
    VALUES (1,'ROLE_ADMIN',NOW(),NOW());
```
