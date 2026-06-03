# ecommerce

Kotlin/Spring 기반 **완전 MSA** 이커머스 + **Next.js(FSD)** 프론트엔드.
헥사고날 아키텍처 · DDD · 동기(REST)+비동기(Kafka) · 체크아웃 Saga · Kubernetes 배포.

> 설계 근거: [`docs/PLAN.md`](docs/PLAN.md) (아키텍처) · 단계/검증: [`docs/ROADMAP.md`](docs/ROADMAP.md)

---

## 아키텍처 한눈에

```
                    ┌─────────┐   ┌──────────────┐
   Next.js (FSD) ──▶│ Ingress │──▶│ api-gateway  │  JWT 검증 / 라우팅
                    │ TLS/L7  │   └──────┬───────┘
                    └─────────┘          │  k8s Service DNS
   ┌──────────┬──────────┬───────┴───────┬──────────┬──────────┬──────────┐
 auth      member     product          cart       order      payment   ...
(JWT/JWKS) (프로필)   (카탈로그)      (Redis)   (Saga 오케스트레이터)  inventory · shipping
   └──────────────────────────── Kafka (이벤트 / Outbox) ───────────────────┘
        각 서비스는 독립 DB(Postgres) 소유 · 분산추적(OTel→Zipkin)
```

- **백엔드**: Kotlin / Spring Boot, **헥사고날**(`domain → application(port) → adapter`), **DDD**, 서비스별 독립 DB.
- **통신**: 동기 REST(k8s Service DNS) + 비동기 Kafka. 분산 트랜잭션 = **Saga(오케스트레이션) + Transactional Outbox + 멱등**.
- **인증**: 자체 `auth-service`가 JWT 발급(`JwtEncoder`+JWKS), resource server는 `jwk-set-uri`로 검증. `JWT sub = userId(UUID)`.
- **배포**: Kubernetes — 디스커버리=Service DNS, 설정=ConfigMap/Secret, 진입=Ingress (Eureka/config-server 미사용).
- **프론트**: Next.js(App Router) + Feature-Sliced Design. access 토큰=메모리, refresh=httpOnly 쿠키(BFF).

## 서비스 구성

| 서비스 | 포트 | 책임 | 저장소 | 노출 |
|---|---|---|---|---|
| `api-gateway` | 8080 | 라우팅, JWT 검증, 단일 진입점 | — | Ingress |
| `auth-service` | 8081 | 회원가입/로그인, JWT(access/refresh)·JWKS, `UserRegistered` 발행 | PostgreSQL `auth` | gateway |
| `member-service` | 8082 | `UserRegistered` 멱등 소비→프로필, 주소, `/me` | PostgreSQL `member` | gateway |
| `product-service` | 8083 | 상품/카테고리/가격 CRUD·조회 | PostgreSQL `product` | gateway |
| `cart-service` | 8084 | 장바구니(담기/수정/삭제), product 검증(Resilience4j) | **Redis** | gateway |
| `inventory-service` | 8085 | 재고 예약/확정/해제(낙관적 락) | PostgreSQL `inventory` | 내부 |
| `payment-service` | 8086 | 결제 승인/취소(Mock PG), 멱등 키 | PostgreSQL `payment` | 내부 |
| `shipping-service` | 8087 | 배송 생성/출고/추적 | PostgreSQL `shipping` | 내부 |
| `order-service` | 8088 | 주문 + **체크아웃 Saga** + Outbox | PostgreSQL `order` | gateway |

## 핵심 패턴

- **체크아웃 Saga**(오케스트레이션, `order-service` 주도): 재고예약 → 결제 → 재고확정 → 배송 → `CONFIRMED`.
  실패 시 **보상**(예약 해제) 후 `CANCELLED`. → `CheckoutService`.
- **Transactional Outbox + DLQ**: 주문 이벤트를 `outbox_event`에 적재하고 `OutboxRelay`가 Kafka(`order.events`)로 발행. 재시도(`attempts`)·임계치 초과 시 `dead`(DLQ).
- **회복탄력성**: order 아웃바운드 클라이언트에 Resilience4j `@CircuitBreaker`+`@Retry`+RestClient 타임아웃.
- **분산추적**: 전 서비스 Micrometer Tracing + OpenTelemetry → Zipkin.

## 기술 스택

- JDK 21 · Kotlin 2.1.20 · Spring Boot 3.4.4 · Spring Cloud 2024.0.1 · Gradle 8.12(Kotlin DSL + Version Catalog + Convention Plugins)
- PostgreSQL(서비스별) · Redis(cart) · Kafka
- 테스트: Kotest · MockK · Spring REST Docs · ArchUnit · Testcontainers
- 프론트: Next.js 15 · React 19 · TypeScript · steiger(FSD lint)

## 저장소 구조

```
ecommerce/
├── settings.gradle.kts · gradle/libs.versions.toml   # 모노레포 + 버전 카탈로그
├── build-logic/                  # 컨벤션 플러그인(kotlin-common / spring-library / spring-boot-service)
├── services/                     # api-gateway, auth, member, product, cart, inventory, payment, shipping, order
│   └── <svc>/src/main/kotlin/com/padosol/<svc>/{domain, application/{port.in,port.out,service}, adapter/{in,out}, config}
├── libs/                         # common-event(이벤트 계약) · common-web · common-security
├── infra/docker-compose.yml      # postgres · redis · kafka(KRaft) · zipkin (로컬 의존성)
├── deploy/k8s/base/              # Kustomize: namespace · 서비스별 Deployment/Service/ConfigMap · Secret · Ingress
├── frontend/                     # Next.js + FSD (app/views/widgets/features/entities/shared)
└── docs/                         # PLAN.md(아키텍처) · ROADMAP.md(단계/검증)
```

> 헥사고날 의존 방향(`domain → application → adapter`)은 각 서비스의 `HexagonalArchitectureTest`(ArchUnit)로 강제됩니다.

## 빠른 시작

### 1) 로컬 인프라
```bash
docker compose -f infra/docker-compose.yml up -d   # postgres · redis · kafka · zipkin
```

### 2) 빌드 & 테스트
```bash
./gradlew build                                    # 전 서비스 컴파일 + 단위/슬라이스/계약/ArchUnit 테스트
./gradlew :services:<svc>:integrationTest          # Testcontainers 통합(실 Postgres/Kafka, Docker 필요)
```
- 단위/슬라이스 테스트는 `build`에 포함, `@Tag("integration")` 통합 테스트는 `integrationTest` 태스크로 분리되어 항상 green 유지.

### 3) 서비스 실행(개별)
```bash
./gradlew :services:auth-service:bootRun           # :8081 (다른 서비스도 동일)
```

### 4) 프론트엔드
```bash
cd frontend
npm install
npm run dev      # http://localhost:3000  (API: NEXT_PUBLIC_API_BASE_URL, 기본 http://localhost:8080)
npm run build && npm run lint && npm run fsd       # 빌드 / eslint / FSD 경계 검사
```

### 5) Kubernetes 배포
```bash
kubectl apply -k deploy/k8s/base                   # ns + 서비스 + ConfigMap/Secret + Ingress
```
> 상태 저장소(Postgres/Kafka/Redis)는 클러스터 내 Operator 또는 매니지드 서비스로 별도 제공한다(`docs/PLAN.md §11`).

## 테스트 전략 (레이어별)

| 레이어 | 방식 |
|---|---|
| domain | 순수 단위 (Kotest) |
| application·service | 포트 mock (MockK) |
| controller | REST Docs (standalone MockMvc) |
| 서비스 간 | 경량 계약(이벤트 스키마 round-trip + JWT/JWKS) |
| 경계 | ArchUnit |
| 통합 | Testcontainers(Postgres+Kafka) — `integrationTest` |

신규 기능은 **TDD**(실패 테스트 → 최소 구현 → 리팩터)로 개발합니다.

## 문서

- [`docs/RUN.md`](docs/RUN.md) — **실행 가이드**(시나리오별 명령어 순서: 로컬 개발 / 전체 스택 / E2E / 프론트 / k8s)
- [`docs/PLAN.md`](docs/PLAN.md) — 서비스 분해, 헥사고날/DDD 구조, 통신/Saga, 인증, k8s 배포 토폴로지
- [`docs/ROADMAP.md`](docs/ROADMAP.md) — Phase 0~5 단계별 작업과 검증 기준(체크리스트)
