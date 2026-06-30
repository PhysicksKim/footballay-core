---
apply: always
---

# 백엔드 프로젝트 아키텍처

본 백엔드 프로젝트 footballay-core 는 아래의 구조를 따라 작성하여야 합니다.  

## 1. Football Entity는 Data Provider 의존성 제거를 위해 Core - Backbone 구조를 사용합니다  

축구 정보는 Relation 구조가 Data Provider 마다 다를 수 있습니다. 따라서 Data Provider 의존성을 완충시키기 위해 Core - Backbone 구조를 사용합니다.

- Core : 현실의 대상에 대응됨. public 접근 경로와 맞 닿아서 uid가 노출되는 지점 
- Backbone : Data Provider의 구조에 대응됨. public 접근 이후 요청에 맞게 Core에 대응되는 Backbone을 조회하여 Data Provider Specific 데이터를 얻을 수 있음.

관련 규약은 아래와 같습니다.  

- ADMIN 요청은 Backbone Specific 하여도 됩니다. 근본적으로 Data Provider로 부터 축구 리그 및 일정 데이터를 가져오는 것이 시작점이며 이는 Admin에 의해서 요청이 이뤄집니다.
- ADMIN Endpoint는 가능하다면 Core UID를 통한 요청이 적절하며, 필요 또는 편의에 따라 API Specific id를 사용하여도 됩니다.
- User의 요청은 Core UID로만 이뤄져야 합니다. User가 Data Provider에 의존성을 두지 않아야 합니다. 
- UID를 통한 요청 위주로 구성하는 중요한 이유 중 하나는 향후에 다른 Data Provider로 전환 하거나 또는 융합하여 데이터를 제공하는데에 문제 없도록 하기 위함입니다. 


## 2. 다음과 같은 레이어 구조를 따릅니다 ; Controller – Web Service - Domain Facade - Domain Service - Repository

각 계층의 역할은 아래와 같습니다.  

- controller : 통념상 여겨지는 컨트롤러의 역할과 같습니다. 웹 및 API 스펙에 밀접하게 연관되어 Validation 및   
- Web Service : Domain Facade에 요청을 보내서 적절한 응답을 수집합니다. 캐싱 같은 웹에 밀접한 응용 기능을 수행합니다. 웹과 중심 핵심 로직인 도메인 코드 사이의 경계에 해당합니다. 
- Domain Facade : 웹 뿐만 아니라 Scheduler 또는 Batch 처리 등 여러 요청을 받는 핵심 진입점입니다. 외부 구조에 의존성을 지니지 않아야 합니다. 필요한 Domain Service 들을 호출하며, 작은 작업들은 직접 수행합니다.
- Domain Service : 핵심 로직들을 단위로 구현합니다. 성능 최적화 또는 특수한 경우 제외하곤 외부 요청 사례에 맞추기 보다 범용적이며 재사용성 있도록 해야 합니다.    
- Repository : DB 조회를 제공합니다.  

규약은 다음과 같습니다.  

- JPA Entity는 현재 구조에서는 Domain에 넓게 퍼져있으나, 로직이 복잡해지고 순수성이 요구되는 경우에는 Domain에서도 JPA 대신 모델을 사용하도록 개선할 수 있습니다. 
- Domain Service는 필요에 따라서 Domain Service가 또 다른 Service를 호출하거나 또는 다른 Facade를 호출하여도 됩니다. 
- 중복이 발생하더라도 분리가 적절하다면 다른 Facade 호출 대신 직접 유사한 로직을 작성하여도 됩니다. 예를 들어 Fixtures Of League는 매우 빈번히 사용되는 조회이지만 Admin, Scheduler, Batch, 통계 산출 등 요구 사항에 따라서 분리 또는 세부 구현 사항이 달라질 수 있습니다. 이러한 경우는 중복인 게 아니라 개념적 또는 성능 측면에서 분리되는게 바람직합니다.
- 아주 단순한 조회더라도 최소한 Domain Facade를 거치는 것이 권장됩니다. 예외적으로 개발 과정 또는 admin에서 필요에 따라 Web Service에서 곧장 Repository를 호출할 수 있으나 자주 사용되어선 안되며 로직이 추가된다면 반드시 Domain Facade를 두고 따로 분리해야 합니다.
- 절대로 JPA Entity는 최대 Domain Facade를 넘어서 외부로 퍼지면 안됩니다. Domain Facade는 Domain Model 또는 use case에 맞는 DTO를 반환해야 합니다. DTO 내부에도 JPA Entity를 담아선 안되며, 이는 DB 구조 노출을 막고 사이드 이펙트를 줄이기 위함입니다. 


