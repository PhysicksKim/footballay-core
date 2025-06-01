# Footballay Backend  
  
축구 라이브 데이터 제공 앱 Footballay 의 백엔드 입니다.   

# Architecture 

![footballay-architecture](https://github.com/user-attachments/assets/609d6dca-7d79-41f0-b434-70e376d69e23)


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
