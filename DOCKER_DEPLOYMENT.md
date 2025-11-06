# Docker 기반 Blue-Green 배포 가이드

이 문서는 Footballay Core 애플리케이션의 Docker 기반 Blue-Green 배포 구조에 대한 설명입니다.

## 📋 목차

1. [개요](#개요)
2. [로컬 테스트](#로컬-테스트)
3. [EC2 배포 준비](#ec2-배포-준비)
4. [배포 프로세스](#배포-프로세스)
5. [롤백](#롤백)
6. [트러블슈팅](#트러블슈팅)

## 개요

### 배포 아키텍처

```
Cloudflare DNS → EC2 Instance
                    │
                    ├─ Nginx (443/80)
                    │   └─ Reverse Proxy → active_slot.conf
                    │
                    ├─ Blue Slot (8081)
                    │   └─ Docker Container
                    │
                    └─ Green Slot (8082)
                        └─ Docker Container
```

### 주요 특징

- **무중단 배포**: 한 슬롯에 새 버전 배포 후 트래픽 전환
- **즉시 롤백**: 문제 발생 시 이전 슬롯으로 즉시 복귀
- **외부 설정 주입**: 민감 정보를 이미지에 포함하지 않음
- **버전 관리**: 타임스탬프 + Git SHA로 이미지 태깅
- **자동 Health Check**: 배포 후 자동으로 애플리케이션 상태 확인

### ⚠️ 중요 사항

**운영 방식의 변화:**

1. **EC2에서 직접 코드 수정 금지**
   - `deploy.sh`는 `git reset --hard origin/main`을 실행합니다
   - EC2에서 vi로 소스 코드를 직접 수정하면 다음 배포 시 **모두 삭제**됩니다
   - 긴급 수정(hotfix)은 반드시: 로컬 수정 → git push → `deploy.sh` 실행

2. **테스트 실행 정책**
   - Docker 이미지 빌드 시 테스트는 **스킵**됩니다 (`-x test`)
   - 이유: Testcontainers가 Docker-in-Docker 환경에서 작동하지 않음
   - 테스트는 CI/CD 파이프라인 또는 로컬에서 실행해야 합니다
   - 컴파일은 검증됩니다 (compileKotlin, compileTestKotlin)

3. **로컬 Docker 네트워킹**
   - `local-config/` 디렉토리의 설정 파일들은 `host.docker.internal`을 사용합니다
   - 이미 PostgreSQL/Redis 연결을 위해 올바르게 구성되어 있습니다
   - Mac/Windows Docker Desktop에서 자동으로 작동합니다

## 로컬 테스트

### 1. 사전 요구사항

- Docker Desktop 설치
- PostgreSQL (localhost:5432) 실행 중
- Redis (localhost:6379) 실행 중

현재 실행 중인 DB 컨테이너:
```bash
docker ps --filter "name=footballay-dev"
```

### 2. 빌드 및 실행

```bash
# docker-compose로 빌드 및 시작
docker compose up --build

# 백그라운드 실행
docker compose up -d --build

# 로그 확인
docker compose logs -f
```

### 3. Health Check

```bash
# Health 상태 확인 (/ 엔드포인트가 200 OK 반환)
curl http://localhost:8080/

# 또는 actuator 사용 (별도 포트 9001)
# 주의: 로컬에서는 actuator가 127.0.0.1:9001로만 바인딩됨
```

### 4. 종료

```bash
docker compose down
```

### 5. Blue/Green 슬롯 테스트

```bash
# Blue 슬롯 (8081)
docker run -d \
  --name footballay-blue \
  -p 8081:8081 \
  -e SERVER_PORT=8081 \
  -e SPRING_PROFILE="base,dev,mockapi" \
  -e JAVA_TOOL_OPTIONS="-Xmx512m" \
  -v $(pwd)/local-config:/config-external:ro \
  --add-host=host.docker.internal:host-gateway \
  footballay-core-footballay-core:latest

# Green 슬롯 (8082)
docker run -d \
  --name footballay-green \
  -p 8082:8082 \
  -e SERVER_PORT=8082 \
  -e SPRING_PROFILE="base,dev,mockapi" \
  -e JAVA_TOOL_OPTIONS="-Xmx512m" \
  -v $(pwd)/local-config:/config-external:ro \
  --add-host=host.docker.internal:host-gateway \
  footballay-core-footballay-core:latest

# Health Check (/ 엔드포인트 사용)
curl http://localhost:8081/
curl http://localhost:8082/

# 정리
docker stop footballay-blue footballay-green
docker rm footballay-blue footballay-green
```

## EC2 배포 준비

### 1. EC2 인스턴스 설정

- **타입**: t3.small
- **OS**: Amazon Linux 2023
- **RAM**: 2GB (Swap 추가 권장)

```bash
# Swap 파일 생성 (2GB)
sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# 확인
free -h
```

### 2. 디렉토리 구조 생성

```bash
sudo mkdir -p /srv/footballay/{workdir,config-live,runtime/{blue,green}}
```

디렉토리 구조:
```
/srv/footballay/
├── workdir/              # Git 리포지토리
├── config-live/          # 운영 환경 설정 파일 (secrets)
│   ├── application-secret.yml  # DB, JWT, Loki 비밀번호
│   ├── application-aws.yml     # AWS S3, Cloudflare R2 자격증명
│   ├── application-api.yml     # ApiSports, RapidAPI 키
│   └── application-path.yml    # 정적 파일 경로 설정
├── runtime/
│   ├── blue/
│   │   └── image_tag.txt      # Blue 슬롯 이미지 태그 (롤백용)
│   └── green/
│       └── image_tag.txt      # Green 슬롯 이미지 태그 (롤백용)
├── ACTIVE_SLOT           # 현재 활성 슬롯 ("blue" 또는 "green")
├── deploy.sh             # 배포 스크립트
└── switch-slot.sh        # Nginx + 모니터링 포트 전환 스크립트
```

**중요**:
- `config-live/` 디렉토리의 YAML 파일들은 Docker 컨테이너에 `-v` 마운트되어 `/config-external/`로 접근됩니다.
- 이 파일들은 **Docker 이미지에 포함되지 않으므로** 반드시 EC2에 직접 생성해야 합니다.
- `.dockerignore`에 의해 이미지 빌드 시 제외됩니다.

### 3. Git 리포지토리 클론

```bash
cd /srv/footballay
sudo git clone https://github.com/your-username/footballay-core.git workdir
```

### 4. 운영 설정 파일 준비

**중요**: `local-config/`의 더미 파일들을 참고하여 실제 운영 값으로 수정하세요!

```bash
# 설정 파일 복사 (템플릿으로 사용)
sudo cp /srv/footballay/workdir/local-config/* /srv/footballay/config-live/

# 실제 운영 값으로 수정
sudo vim /srv/footballay/config-live/application-secret.yml
sudo vim /srv/footballay/config-live/application-aws.yml
sudo vim /srv/footballay/config-live/application-api.yml
sudo vim /srv/footballay/config-live/application-path.yml

# 권한 설정 (보안)
sudo chmod 600 /srv/footballay/config-live/*.yml
sudo chown root:root /srv/footballay/config-live/*.yml
```

### 5. 배포 스크립트 설치

```bash
sudo cp /srv/footballay/workdir/docs/archi/script/deploy.sh /srv/footballay/
sudo cp /srv/footballay/workdir/docs/archi/script/switch-slot.sh /srv/footballay/
sudo chmod +x /srv/footballay/*.sh
```

### 6. Nginx 설정

```bash
# Upstream 설정
sudo tee /etc/nginx/conf.d/footballay_upstream.conf > /dev/null <<'EOF'
upstream footballay_backend {
    include /etc/nginx/conf.d/active_slot.conf;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com;

    # SSL 인증서 설정
    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    # Proxy 설정
    location / {
        proxy_pass http://footballay_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket 지원
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # Actuator 엔드포인트 (health check)
    location /actuator {
        proxy_pass http://footballay_backend;
        proxy_set_header Host $host;
    }
}

# HTTP → HTTPS 리다이렉트
server {
    listen 80;
    server_name yourdomain.com;
    return 301 https://$server_name$request_uri;
}
EOF

# 초기 active slot 설정 (Blue)
echo "server 127.0.0.1:8081;" | sudo tee /etc/nginx/conf.d/active_slot.conf

# Nginx 설정 테스트
sudo nginx -t

# Nginx 재시작
sudo systemctl reload nginx
```

### 7. Docker 설치

```bash
# Amazon Linux 2023
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# 재로그인 필요
```

## 배포 프로세스

### 자동 배포 (권장)

```bash
sudo /srv/footballay/deploy.sh
```

#### 배포 스크립트 동작:

1. ✅ main 브랜치 최신 코드 pull
2. ✅ Docker 이미지 빌드 (`YYYYMMDD-HHMMSS-{git-sha}` 태그)
3. ✅ 현재 활성 슬롯 확인
4. ✅ 비활성 슬롯에 새 컨테이너 배포
5. ✅ Health Check (최대 30회, 2초 간격)
6. ✅ 성공 시 이미지 태그 기록
7. ⏸️ 수동 승인 대기 (트래픽 전환은 수동)

### 트래픽 전환

배포 성공 후, 수동으로 트래픽을 전환합니다:

```bash
# Green 슬롯으로 전환 (Blue가 활성 상태일 때)
sudo /srv/footballay/switch-slot.sh green

# 또는 Blue 슬롯으로 전환 (Green이 활성 상태일 때)
sudo /srv/footballay/switch-slot.sh blue
```

**⚠️ 최초 배포 시 주의사항**:
- `deploy.sh`는 standby 슬롯에만 배포하고 **9001 포트를 publish하지 않습니다**
- 따라서 **최초 배포 후 반드시 `switch-slot.sh`를 실행**해야 모니터링이 활성화됩니다
- `switch-slot.sh` 실행 시 해당 슬롯이 active가 되며 9001 포트가 자동으로 publish됩니다

예시:
```bash
# 1. 최초 배포 (Blue 슬롯에 배포, 9001 포트 없음)
sudo /srv/footballay/deploy.sh

# 2. 트래픽 전환 + 모니터링 활성화 (Blue가 active가 되며 9001 포트 publish)
sudo /srv/footballay/switch-slot.sh blue
```

#### switch-slot.sh 동작:

1. ✅ 대상 슬롯 검증
2. ✅ **Docker 컨테이너 재시작 (9001 포트 관리)** ⭐ 중요
   - 새 active 슬롯: `-p 9001:9001` 추가하여 재시작 (Prometheus 메트릭 노출)
   - 이전 active 슬롯: 9001 포트 제거하고 재시작 (standby 모드)
   - 결과: 항상 active 슬롯만 `127.0.0.1:9001`에서 메트릭 제공
3. ✅ Nginx 설정 백업
4. ✅ `/etc/nginx/conf.d/active_slot.conf` 업데이트
5. ✅ Nginx 설정 테스트
6. ✅ Nginx 리로드
7. ✅ `/srv/footballay/ACTIVE_SLOT` 파일 업데이트

**모니터링 아키텍처**:
- Prometheus는 `127.0.0.1:9001/actuator/prometheus`를 scrape
- Active 슬롯만 9001 포트를 호스트에 publish
- 슬롯 전환 시 자동으로 9001 포트도 이동
- Grafana Cloud로 메트릭이 끊김 없이 전송됨

## 롤백

문제 발생 시 즉시 이전 슬롯으로 롤백:

```bash
# 현재 활성 슬롯 확인
cat /srv/footballay/ACTIVE_SLOT

# 이전 슬롯으로 전환
sudo /srv/footballay/switch-slot.sh <previous-slot>
```

예시:
```bash
# Green에서 문제 발생 → Blue로 롤백
sudo /srv/footballay/switch-slot.sh blue
```

## 트러블슈팅

### 1. Health Check 실패

```bash
# 컨테이너 로그 확인
docker logs footballay-blue
docker logs footballay-green

# 컨테이너 상태 확인
docker ps -a --filter "name=footballay-"

# Health endpoint 직접 확인 (/ 엔드포인트 사용)
curl http://localhost:8081/
curl http://localhost:8082/

# Actuator는 9001 포트에서만 접근 가능 (active 슬롯만)
curl http://localhost:9001/actuator/health
```

### 2. 데이터베이스 연결 실패

```bash
# 설정 파일 확인
cat /srv/footballay/config-live/application-secret.yml

# DB 연결 테스트 (컨테이너 내부에서)
docker exec -it footballay-blue bash
curl http://localhost:8081/actuator/health
```

### 3. 메모리 부족 (OOM)

```bash
# Swap 확인
free -h

# 컨테이너 메모리 사용량 확인
docker stats footballay-blue footballay-green

# 필요시 heap 크기 조정 (deploy.sh 수정)
JAVA_MEMORY="-Xmx512m"  # 기본값
```

### 4. Nginx 설정 오류

```bash
# Nginx 설정 테스트
sudo nginx -t

# Nginx 에러 로그 확인
sudo tail -f /var/log/nginx/error.log

# active_slot.conf 확인
cat /etc/nginx/conf.d/active_slot.conf
```

### 5. 이미지 빌드 실패

```bash
# 디스크 공간 확인
df -h

# Docker 캐시 정리
docker system prune -a

# 수동 빌드 테스트
cd /srv/footballay/workdir
docker build -t test .
```

## CI/CD 연동

GitHub Actions를 통해 자동 배포를 설정할 수 있습니다:

```yaml
# .github/workflows/deploy.yml
name: Deploy to EC2

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to EC2
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ec2-user
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            sudo /srv/footballay/deploy.sh
```

**참고**: 트래픽 전환은 여전히 수동으로 수행하여 안전성을 보장합니다.

## 참고 문서

- [Dockerfile](./Dockerfile) - Docker 이미지 빌드 설정
- [docker-compose.yml](./docker-compose.yml) - 로컬 테스트 환경
- [local-config/README.md](./local-config/README.md) - 설정 파일 가이드
- [docs/archi/script/deploy.sh](./docs/archi/script/deploy.sh) - 배포 스크립트
- [docs/archi/script/switch-slot.sh](./docs/archi/script/switch-slot.sh) - Nginx 전환 스크립트
- [docs/archi/server_architecture_refactoring_plan.md](./docs/archi/server_architecture_refactoring_plan.md) - 원본 계획서

## 문의

문제가 발생하거나 개선 사항이 있으면 이슈를 생성해주세요.
