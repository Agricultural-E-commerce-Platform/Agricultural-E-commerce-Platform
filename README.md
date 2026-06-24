# 🌾 Agricultural E-commerce Platform

## 🚜 프로젝트 소개

신선한 농산물을 온라인으로 구매할 수 있는 이커머스 플랫폼입니다.  
특가 타임세일 상품과 선착순 쿠폰 발급 기능을 통해  
**동시성 제어와 성능 최적화 문제를 해결하는 데 초점을 맞춘 프로젝트**입니다.

## 🚀 핵심 기능

- 선착순 쿠폰 발급 (Redis 분산 락)
- 주문 재고 차감 동시성 제어 (Redis + DB 이중 방어)
- 검색 성능 개선 (Caffeine Cache 적용)
- 타임세일 상품 (Quartz Scheduler 기반 자동 처리)

---

## ⚡ 핵심 설계

### 1. 선착순 쿠폰 발급

- Redis 분산 락으로 동일 쿠폰 발급 요청 제어
- 사용자별 동일 쿠폰 중복 발급 방지

### 2. 주문 재고 차감

- 상품 ID 기준 Redis 분산 락 적용
- DB 조건 검증으로 재고 음수 방지
- 장바구니는 재고 예약이 아닌 임시 저장소로 설계

### 3. 검색 성능 개선

- v1: 캐시 미적용
- v2: Caffeine Cache 적용
- TTL 5분, 최대 캐시 수 100개

### 4. 인기 검색어

- Redis Sorted Set 기반 실시간 집계
- 검색 발생 시점에 점수 증가
- 사용자별 동일 키워드 1회만 카운팅
- 매일 자정 초기화

---

## 🧩 아키텍처

<img width="3324" height="2684" alt="서버 아키텍쳐 구조도" src="https://github.com/user-attachments/assets/0a43cfb3-aea4-4b95-b061-9c5b6206f5cf" />


- AWS EC2 기반 배포
- Docker 컨테이너 환경 구성
- GitHub Actions 기반 CI/CD
- Redis / MySQL 분리 구조

---

## ⚡ 동시성 제어 구조

<img width="2894" height="2024" alt="동시성 락 동작 흐름" src="https://github.com/user-attachments/assets/67b7bebb-ce45-487f-90f5-5b18685d5e8a" />


### 주문 재고 차감

- Redis 분산 락 → 동시 요청 제어 (1차 방어)
- DB 조건 검증 → 재고 음수 방지 (2차 방어)

### 쿠폰 발급

- Redis 락 기반 선착순 처리
- 사용자 중복 발급 방지
- 수량 초과 발급 방지

👉 [상세 설계 보기](https://github.com/Agricultural-E-commerce-Platform/Agricultural-E-commerce-Platform-Docs.git)

---

## 🛠 기술 스택

### Backend
- Java 17
- Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA
- QueryDSL
- Quartz Scheduler

### Database / Cache

- MySQL
- Redis
- Caffeine Cache

### Infra / DevOps

- Docker
- Docker Compose
- GitHub Actions

### Test

- JUnit5
- Mockito
- SpringBootTest
- Postman
- ExecutorService
- CyclicBarrier

---

## 🧪 테스트

총 **110개 테스트 케이스**를 기반으로 정상 / 예외 / 인증 / 권한 / 동시성 시나리오를 검증했습니다.

| 도메인 | 테스트 수 |
| --- | ---: |
| Auth | 22 |
| User | 7 |
| Product | 15 |
| Cart | 22 |
| Coupon | 21 |
| Order | 20 |
| Global | 3 |
| **Total** | **110** |

### 테스트 전략

- Postman 기반 API 테스트
- JUnit5 + Mockito 기반 단위 테스트
- SpringBootTest 기반 통합 테스트
- ExecutorService + CyclicBarrier 기반 동시성 테스트
- 본인이 작성하지 않은 도메인을 테스트하는 교차 테스트 방식 적용

👉 [테스트 케이스 상세 보기](https://github.com/Agricultural-E-commerce-Platform/Agricultural-E-commerce-Platform-Docs/blob/50c7d199a2d3525c6ee79ca0020525c3b0881502/documents/test/TestCases.md)

---

## 📁 프로젝트 구조

```
Agricultural_Ecommerce
├── Dockerfile                         # 애플리케이션 Docker 이미지 빌드 설정
├── docker-compose.yml                 # 로컬 실행용 Docker Compose 설정
├── docker-compose.prod.yml            # 운영/배포 환경 Docker Compose 설정
├── build.gradle                       # Gradle 빌드 및 의존성 설정
├── settings.gradle
├── gradlew
├── gradlew.bat
└── src
    ├── main
    │   ├── java/com/spartafarmer/agri_commerce
    │   │   ├── AgriCommerceApplication.java
    │   │   ├── common
    │   │   │   ├── config              # Security, Cache, QueryDSL, Quartz 등 공통 설정
    │   │   │   ├── entity              # BaseEntity
    │   │   │   ├── enums               # 공통 Enum
    │   │   │   ├── exception           # CustomException, ErrorCode, GlobalExceptionHandler
    │   │   │   ├── lock                # RedisLockRepository, LockService
    │   │   │   ├── response            # 공통 응답 ApiResponse
    │   │   │   └── security            # JWT 인증/인가
    │   │   └── domain
    │   │       ├── auth                # 회원가입 / 로그인
    │   │       ├── user                # 회원 정보 수정
    │   │       ├── product             # 상품, 검색, 인기 검색어, 타임세일
    │   │       ├── cart                # 장바구니
    │   │       ├── order               # 주문, 재고 차감
    │   │       └── coupon              # 쿠폰 생성, 발급, 만료
    │   └── resources
    │       ├── application.yml
    │       ├── application-local-example.yml
    │       └── application-test-example.yml
    └── test
        └── java/com/spartafarmer/agri_commerce
            ├── auth                    # Auth 단위 / 통합 테스트
            ├── user                    # User 단위 / 통합 테스트
            ├── product                 # Product, Search, TimeSale 테스트
            ├── cart                    # Cart 단위 / 통합 테스트
            ├── order                   # Order 단위 / 통합 테스트
            ├── coupon                  # Coupon 단위 / 통합 / 동시성 테스트
            └── common                  # 공통 응답, 예외, JWT, Lock 테스트
```

---

## ⚙️ 실행 방법

### 필수 요구사항

- Java 17
- Docker
- MySQL
- Redis

---

### 1. 프로젝트 클론

```bash
git clone https://github.com/Agricultural-E-commerce-Platform/코드레포명.git
cd 코드레포명
```

---

### 2. 환경 설정

`application-local-example.yml` 또는 `application-test-example.yml`을 참고하여 로컬 환경 설정 파일을 구성합니다.

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/agri_commerce
    username: your-username
    password: your-password

  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret:
    key: your-jwt-secret-key
```

---

### 3. Docker 실행

```bash
docker-compose up -d
```

---

### 4. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는

```bash
./gradlew build
java -jar build/libs/*.jar
```

---

### 5. 서버 상태 확인

- 로컬
```http
GET http://localhost:8080/actuator/health
```

- AWS 배포 서버
http://13.209.12.239:8080/actuator/health


---

## 📄 프로젝트 문서

자세한 정책, 설계, API 명세, 테스트 케이스, 회의록은 Docs 레포지토리에서 관리합니다.

| 문서 | 링크 |
| --- | --- |
| 서비스 정책 | [Service Policy](https://github.com/Agricultural-E-commerce-Platform/Agricultural-E-commerce-Platform-Docs/blob/8d79269c75335dbac254ce7cc7987cd4b2f5aedd/documents/policy/service-policy/ServicePolicy.md) |
| 팀 협업 정책 | [Team Policy](https://github.com/Agricultural-E-commerce-Platform/Agricultural-E-commerce-Platform-Docs/blob/8d79269c75335dbac254ce7cc7987cd4b2f5aedd/documents/policy/team-policy/TeamPolicy.md) |
| ERD 설계 | [ERD](https://github.com/Agricultural-E-commerce-Platform/Agricultural-E-commerce-Platform-Docs/blob/8d79269c75335dbac254ce7cc7987cd4b2f5aedd/documents/erd/ERD.md) |
| API 명세 | [API Specification](https://github.com/Agricultural-E-commerce-Platform/Agricultural-E-commerce-Platform-Docs/blob/8d79269c75335dbac254ce7cc7987cd4b2f5aedd/documents/api/API.md) |
| 마일스톤 | [Milestone](https://github.com/Agricultural-E-commerce-Platform/Agricultural-E-commerce-Platform-Docs/blob/8d79269c75335dbac254ce7cc7987cd4b2f5aedd/documents/milestone/Milestone.md) |
| 테스트 케이스 | [Test Cases](https://github.com/Agricultural-E-commerce-Platform/Agricultural-E-commerce-Platform-Docs/blob/50c7d199a2d3525c6ee79ca0020525c3b0881502/documents/test/TestCases.md) |
| 트러블슈팅 | [Troubleshooting](https://github.com/Agricultural-E-commerce-Platform/Agricultural-E-commerce-Platform-Docs/tree/50c7d199a2d3525c6ee79ca0020525c3b0881502/documents/troubleshooting) |
| 회의록 | [Meetings](https://github.com/Agricultural-E-commerce-Platform/Agricultural-E-commerce-Platform-Docs/tree/8d79269c75335dbac254ce7cc7987cd4b2f5aedd/documents/meetings) |


---

## 👥 팀원

| 이름 | 역할 | 담당 도메인 |
| --- | --- | --- |
| 정지훈 | 팀장 / DevOps | Auth, User, Common, Infra |
| 정은지 | 부팀장 | Product, Search |
| 김예은 | QA / 서기 | Order, Cart |
| 이중현 | QA 총괄 / DB | Coupon, Admin |


---

# ⚖️ License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
