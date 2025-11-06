# 열어야 하는 도커

- Redis : 서버가 Redis DB 이용을 위해 EC2 내부 Docker Redis 를 사용
- Prometheus : Grafana Cloud 에게 metric 전송 위해 EC2 내부 Docker Prometheus 를 사용

# 도커 인식을 위한 동일 네트워크 만들어 두기

Docker 컨테이너 안에서 실행되는 Spring 이 다른 Docker Container 내부에 있는 Redis 에 접속하려면,
동일한 Docker 네트워크 안에 있도록 구성해야 합니다.

```
sudo docker network inspect footballay-net >/dev/null 2>&1 || \
  sudo docker network create --driver bridge footballay-net
```

# 서버 세팅 전 후 디렉토리 구조 비교

## 세팅 전 디렉토리 구조
git 에서 clone 직후에는 secret 파일들이 없으므로, 아래와 같은 상태입니다.

```
docker/prod
    ├── README.md
    ├── log
    │   ├── docker-compose.yml
    │   └── prometheus
    │       ├── prometheus.yml
    │       └── secrets
    └── redis
        └── docker-compose.yml
```

## 세팅 후 디렉토리 구조

```
docker/prod
    ├── README.md
    ├── log
    │   ├── docker-compose.yml
    │   └── prometheus
    │       ├── credentials.yml  <-- 생성 필요
    │       ├── prometheus.yml
    │       └── secrets
    │           ├── prom_password <-- 생성 필요
    │           └── prom_user     <-- 생성 필요
    └── redis
        ├── .env   <-- 생성 필요
        └── docker-compose.yml
```

위와 같은 구조로 세팅되어야 합니다.


# 생성 해야 하는 secret 파일들

## 1. Redis 비밀번호 설정

- Redis 비밀번호 : Redis docker-compose 경로에 .env 환경변수로 `REDIS_PASSWORD` 값 설정

**중요** : .env 파일은 redis `docker-compose.yml` 파일과 동일한 경로에 생성해야 합니다.

```
vim .env
```

```
REDIS_PASSWORD=<YOUR_REDIS_PASSWORD>
```

### ex

```
REDIS_PASSWORD=12345password
```

## 2. Prometheus user/password 설정

- Prometheus Grafana Cloud 연동용 user/password

단순 파일 로 생성 후 value 만 입력

### prom_user

`prom_user` 파일 생성 (파일 경로 : prometheus/secrets/prom_user)

```
vim prom_user
```

`prom_user` 파일 안에 grafana cloud 에서 제공하는 user ID 입력

### prom_password

`prom_password` 파일 생성

grafana cloud 에서 제공하는 user password 입력 (파일 경로 : prometheus/secrets/prom_password)

```
vim prom_password
```

`prom_password` 파일 안에 grafana cloud 에서 제공하는 password 입력

### 예시 값

user ID 예시

```
1234567
```

password 예시

```
glc_JvITay0xMjYwLWhZvb3RiW
```
