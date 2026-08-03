# Footballay 개발용 Docker 구성

## 용도

로컬 개발에서 필요한 Docker Image들을 실행합니다.
운영 환경 docker는 prod 경로의 docker-compose.yml을 사용합니다.

---

## 사용 방법

```bash
cd docker/dev
docker compose up -d
```

컨테이너 상태를 확인

```bash
docker compose ps
```

Kafka topic을 확인

```bash
docker compose exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9093 --list
```

## 구성

```text
postgres:
  footballay-core 로컬 RDB.

redis:
  로컬 캐시와 Data Quality duplicate gate.

kafka:
  로컬 Apache Kafka 4.2.1 KRaft 단일 브로커.
  호스트에서 실행하는 앱은 localhost:9092로 접근한다.
  같은 compose network 안의 컨테이너는 kafka:9093으로 접근한다.

kafka-init:
  Kafka topic을 생성하는 1회성 컨테이너.
  football-data-raw-collected와 football-data-quality-result를 만든다.

mongodb:
  Data Quality quality_results와 issue_types smoke test용 로컬 MongoDB.
  호스트에서 실행하는 앱은 mongodb://localhost:27017로 접근한다.

seaweedfs:
  Core와 Data Quality Service가 같은 S3-compatible raw response 저장소로 사용하는 로컬 SeaweedFS weed mini.
  API는 http://localhost:8333로 접근하며 footballay-data-quality-raw-local bucket을 자동 생성한다.
```

## Volume 영속성.

named volume을 사용하여 컨테이너를 종료해도 데이터가 유지되도록 합니다.
`docker compose down`으로 종료해도 데이터가 유지됩니다.

```text
postgres_data:
  PostgreSQL 데이터 디렉터리.

kafka_data:
  Kafka log, metadata, topic data, offset 저장소.

mongodb_data:
  MongoDB 데이터 디렉터리.
  기존 volume에 footballay_data_quality database가 있으면 자동으로 footballay로 이전되거나 삭제되지 않는다.
  개발 데이터가 필요하면 명시적으로 정리하거나 새 footballay database를 생성한다.

seaweedfs_data:
  SeaweedFS S3 데이터 디렉터리.
```

---

### 데이터 초기화

로컬 데이터를 완전히 지우고 처음부터 다시 만들고 싶으면 `docker/dev`에서 아래 명령을 실행

```bash
docker compose down -v
```

### 종료

컨테이너를 종료하되 volume은 유지

```bash
docker compose down
```

기존 MinIO volume의 데이터는 자동으로 이전하거나 삭제하지 않는다. 필요한 fixture만 S3 API로 명시적으로 export/import한다.
