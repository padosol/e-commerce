# E-Commerce 아키텍처 계획서

> Backend: Kotlin / Spring Boot · Hexagonal · DDD · 완전 MSA
> Frontend: Next.js · FSD (Feature-Sliced Design)

## 0. 핵심 결정 요약

| 항목 | 결정 |
|---|---|
| MSA 전략 | **처음부터 완전 MSA** (서비스별 독립 배포 · 독립 DB) |
| MVP 도메인 | 회원·인증 / 상품·장바구니·주문·결제 / 재고·배송 |
| 동기 통신 | REST (Gateway 경유, 서비스 간 WebClient/OpenFeign) |
| 비동기 통신 | Kafka (도메인 이벤트 + Saga) |
| 인증/인가 | 자체 Auth 서비스(Spring Authorization Server) + JWT |
| 리포지토리 | **모노레포**(Gradle 멀티모듈) — 서비스는 독립 배포 가능 |
| 분산 트랜잭션 | **Saga(오케스트레이션, order-service 주도)** + Transactional Outbox |
| 배포 | **Kubernetes** — 디스커버리=Service DNS, 설정=ConfigMap/Secret, 진입/TLS=Ingress |

> **k8s 대체로 제거한 플랫폼 구성요소**: `discovery-server`(Eureka) → k8s Service/DNS, `config-server`(Spring Cloud Config) → ConfigMap/Secret. `api-gateway`(Spring Cloud Gateway)는 JWT 검증·rate limit 등 앱 레벨 관심사 때문에 **유지**.

> ⚠️ **솔직한 권고**: "처음부터 완전 MSA"는 학습/포트폴리오 가치가 가장 크지만 초기 운영 부담(게이트웨이·디스커버리·분산추적·11개 서비스의 CI/CD)이 가장 큰 경로입니다. 목표는 그대로 두되, **서비스 내부는 헥사고날로 깔끔히 분리**해서 필요 시 서비스를 합치거나 쪼개기 쉽게 만들고, **로드맵은 한 번에 하나씩** 세우는 것을 권장합니다. (→ `ROADMAP.md`)

---

## 1. 서비스 분해 (Bounded Context → Service)

### 1.1 플랫폼 — 게이트웨이 + k8s 프리미티브

| 구성요소 | 책임 | 형태 |
|---|---|---|
| `api-gateway` | 라우팅, JWT 검증, CORS, Rate Limit, 단일 앱 진입점 | Spring Cloud Gateway (서비스) |
| Ingress | 외부 진입, TLS 종료, L7 라우팅 → gateway | k8s Ingress (nginx/Traefik) |
| 서비스 디스커버리 | 서비스 위치 해석 | **k8s Service + DNS** (Eureka 제거) |
| 설정 | 환경별 구성·시크릿 | **ConfigMap / Secret** (config-server 제거) |

> 게이트웨이는 Discovery 클라이언트 없이 **안정적인 k8s Service DNS 이름**(`http://order-service.<ns>.svc.cluster.local`)으로 라우팅. 동적 디스커버리가 필요하면 `spring-cloud-kubernetes` 옵션.

### 1.2 도메인 서비스 (각자 자기 DB만 소유)

| 서비스 | 애그리거트 핵심 | 저장소 |
|---|---|---|
| `auth-service` | Credential, Client, Token | PostgreSQL `auth` |
| `member-service` | Member, Address, Grade | PostgreSQL `member` |
| `product-service` | Product, Category, Option, Price | PostgreSQL `product` |
| `cart-service` | Cart, CartItem | **Redis** (+ 영속 백업 선택) |
| `order-service` | Order, OrderLine (Saga 오케스트레이터) | PostgreSQL `order` |
| `payment-service` | Payment, PaymentAttempt | PostgreSQL `payment` |
| `inventory-service` | Stock, Reservation | PostgreSQL `inventory` |
| `shipping-service` | Shipment, Tracking | PostgreSQL `shipping` |

> **DB per service 원칙**: 서비스 간 DB 직접 조회 금지. 다른 컨텍스트의 데이터가 필요하면 REST 조회 또는 이벤트로 복제(read model).

### 1.3 시스템 다이어그램

```
              ┌─────────┐   ┌──────────────┐
 Next.js ───▶ │ Ingress │──▶│ api-gateway  │  JWT 검증 / 라우팅
  (FSD)       │ TLS/L7  │   └──────┬───────┘
              └─────────┘          │  (k8s Service DNS로 호출)
            ┌─────────────────┼───────────────────────────┐
            ▼                 ▼                            ▼
     ┌────────────┐   ┌──────────────┐            ┌──────────────┐
     │auth-service│   │member-service│   ...       │order-service │◀─ Saga 오케스트레이터
     └─────┬──────┘   └──────┬───────┘            └──────┬───────┘
           │                 │                            │ (commands/events)
           └──────────────── Kafka ─────────────────────┘
        product · cart · payment · inventory · shipping  (각 서비스 + 각 DB)

   관측: Gateway~서비스~Kafka 전 구간 OpenTelemetry 트레이싱
```

---

## 2. 서비스 간 통신

### 2.1 동기 (REST)
- 외부 → 시스템: **Gateway** 단일 진입점.
- 서비스 → 서비스(조회성): `WebClient`(또는 OpenFeign) + **k8s Service DNS** + **Resilience4j**(timeout/retry/circuit breaker).
- 예: order 생성 시 product 가격/유효성 조회.

### 2.2 비동기 (Kafka)
- **도메인 이벤트** 발행으로 컨텍스트 간 결합도 최소화.
- **Transactional Outbox 패턴**: 비즈니스 트랜잭션과 이벤트 발행을 원자적으로(아웃박스 테이블 → 릴레이/CDC가 Kafka로 발송). "DB는 커밋됐는데 이벤트 누락" 방지.
- **멱등성**: consumer는 eventId 기반 중복 처리 방어. payment 등 외부효과 명령은 idempotency-key.

### 2.3 체크아웃 Saga (오케스트레이션, order-service 주도)
```
1. Order 생성 (PENDING)
2. inventory 예약        ── 실패 ▶ Order CANCELLED
3. payment 승인          ── 실패 ▶ inventory 예약 해제 → Order CANCELLED
4. inventory 확정 차감
5. shipping 생성
6. Order CONFIRMED
```
- 각 단계는 보상 트랜잭션(compensation)을 가짐.
- **오케스트레이션 선택 이유**: 주문 플로우는 단계·실패처리가 명확해 중앙 통제가 디버깅/관측에 유리. (코레오그래피는 추후 단순 이벤트 전파에 부분 적용 가능)

---

## 3. 인증 / 인가

```
[회원가입] Client → auth-service: 자격증명 생성 → UserRegistered 이벤트
                                          └▶ member-service: 프로필 생성
[로그인]   Client → auth-service: 검증 → Access JWT(단명) + Refresh(장명)
[요청]     Client → api-gateway: JWT 검증(서명/만료/스코프) → 라우팅
[하위서비스] gateway가 검증한 사용자 컨텍스트(헤더/전파) 신뢰 또는 자체 재검증
```
- `auth-service` = **Spring Authorization Server** (OAuth2/OIDC, JWT 발급).
- Gateway는 **Resource Server**로 토큰 검증(JWK).
- Refresh 토큰은 프론트에서 **httpOnly 쿠키 + Next Route Handler(BFF)** 로 다루는 것을 권장(아래 8.4).

---

## 4. 서비스 내부 구조 (Hexagonal / Ports & Adapters)

### 4.1 패키지 레이아웃 (서비스 1개 기준, 예: order)
```
com.padosol.order
├── domain                  # 순수 도메인. 프레임워크 의존성 0
│   ├── model               #   Aggregate, Entity, Value Object
│   ├── event               #   도메인 이벤트
│   └── service             #   도메인 서비스(여러 애그리거트 규칙)
├── application             # 유스케이스 + 포트(인터페이스) 정의
│   ├── port
│   │   ├── in              #   inbound port = UseCase 인터페이스
│   │   └── out             #   outbound port = Repository / EventPublisher / 외부서비스 Client
│   └── service             #   UseCase 구현 (포트 오케스트레이션, @Transactional)
└── adapter                 # 어댑터 = IO/프레임워크
    ├── in
    │   ├── web             #   REST Controller (inbound port 호출)
    │   └── messaging       #   Kafka Consumer (inbound)
    └── out
        ├── persistence     #   JPA Repository 구현 (outbound port 구현)
        ├── messaging       #   Kafka Producer (outbound port 구현)
        └── client          #   타 서비스 REST Client (outbound port 구현)
```

### 4.2 의존성 규칙
```
adapter ──▶ application ──▶ domain
              (port 정의)     (의존 없음)
```
- domain은 어떤 것에도 의존하지 않음(JPA/Spring 어노테이션 금지 → 영속 모델은 adapter.persistence에 별도 엔티티로 매핑).
- application은 domain만 의존하고 **포트 인터페이스**를 정의. adapter가 그 포트를 구현/호출.

### 4.3 경계 강제 (선택지)
- **패키지 레벨** (간단): 위 구조 + `ArchUnit` 테스트로 의존 방향 위반 검출.
- **모듈 레벨** (강함): `domain` / `application` / `adapter`를 Gradle 서브모듈로 분리해 컴파일 타임에 강제. 학습 효과 큼, 보일러플레이트 증가.
- 권장: **MVP는 패키지 레벨 + ArchUnit**, 안정화 후 핵심 서비스만 모듈 분리.

---

## 5. 모노레포 구조 (Gradle, Kotlin DSL)

```
e-commerce/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml        # Version Catalog (의존성 버전 단일 관리)
├── build-logic/                     # Convention Plugins (kotlin/spring/test 공통 설정)
├── services/
│   ├── api-gateway/                 # (Eureka/config-server 제거 — k8s가 대체)
│   ├── auth-service/  member-service/
│   ├── product-service/  cart-service/
│   ├── order-service/  payment-service/
│   └── inventory-service/  shipping-service/
├── libs/                            # 공유 라이브러리 (⚠️ 기술 횡단 관심사만!)
│   ├── common-web/                  #   공통 에러응답, 트레이싱 필터
│   ├── common-security/             #   JWT 검증 공통
│   └── common-event/                #   이벤트 envelope (또는 Schema Registry 사용)
├── infra/
│   └── docker-compose.yml           # postgres×N, kafka, redis, auth, observability
└── frontend/                        # Next.js (FSD)
```

> ⚠️ **공유 라이브러리 주의**: `libs/`에 **도메인 코드**를 넣지 말 것(서비스 간 숨은 결합 발생). 기술 횡단 관심사만. 이벤트 계약은 가능하면 **Schema Registry(Avro/Protobuf)** 로 관리하면 jar 공유 결합을 피할 수 있음.

> **모노레포 선택 이유**: 초기 1인/소규모에선 원자적 변경·일괄 리팩터·단일 CI 설정이 폴리레포보다 압도적으로 편함. 서비스 자체는 독립 배포 가능하므로 MSA 원칙과 충돌하지 않음. 조직이 커지면 서비스별 폴리레포로 분리.

---

## 6. DDD 도메인 모델 (컨텍스트별 애그리거트 초안)

- **auth**: `Credential`(이메일/해시/상태), `OAuthClient`. → 이벤트 `UserRegistered`, `PasswordChanged`
- **member**: `Member`(프로필, 등급), `Address`(VO 리스트). ← `UserRegistered` 소비
- **product**: `Product`(이름/설명/상태), `Sku`/`Option`(VO), `Money`(VO), `Category`.
- **cart**: `Cart`(memberId), `CartItem`(skuId, qty). Redis 해시 구조.
- **order**: `Order`(상태머신 PENDING→CONFIRMED/CANCELLED), `OrderLine`(VO), `Money`. Saga 상태 보유.
- **payment**: `Payment`(orderId, 금액, 상태), `PaymentAttempt`. PG 연동은 포트로 추상화(초기 Mock 어댑터).
- **inventory**: `Stock`(skuId, available/reserved), `Reservation`(orderId 기준). 동시성: 낙관적 락 또는 원자적 차감.
- **shipping**: `Shipment`(orderId, 주소 스냅샷, 상태), `Tracking`.

> **유비쿼터스 언어**를 컨텍스트별로 분리: 같은 "상품"이라도 catalog의 `Product`와 order의 `OrderLine`은 다른 모델(스냅샷). 컨텍스트 간 ID + 필요한 데이터만 복제.

---

## 7. 데이터 & 일관성

- DB per service. 서비스 간 조인 금지 → API 조회 or 이벤트 복제(read model).
- 강한 일관성이 필요한 단일 애그리거트 내부만 ACID 트랜잭션. 서비스 경계를 넘으면 **최종 일관성(eventual consistency)** + Saga.
- 재고 차감 동시성: 낙관적 락(@Version) + 재시도, 또는 `UPDATE ... WHERE available >= qty` 원자적 차감.

---

## 8. 프론트엔드 — Next.js + FSD

### 8.1 FSD 레이어 (위→아래로만 import 가능)
```
app       → Next.js App Router 진입, providers, 전역 레이아웃/스타일
pages      → 라우트별 "화면" 조립 (FSD pages ≠ Next의 pages dir)
widgets    → 독립 UI 블록 (헤더, 상품목록, 장바구니요약)
features   → 사용자 행위 (담기, 로그인, 주문하기)
entities   → 비즈니스 엔티티 (product, cart, order, user) = model/api/ui
shared     → ui kit, lib, api client, config (도메인 무관)
```

### 8.2 슬라이스/세그먼트
- 레이어 안에 **슬라이스**(도메인 단위, 예 `entities/product`), 슬라이스 안에 **세그먼트**(`ui` / `model` / `api` / `lib`).
- 슬라이스마다 `index.ts`로 **public API**만 노출. 내부 직접 import 금지.

### 8.3 Next.js App Router와의 정합성 (중요 마찰점)
- Next는 라우팅을 `app/`에서 하고, FSD에도 `app`·`pages` 레이어가 있어 충돌처럼 보임.
- **표준 해법**: 소스를 `src/`에 FSD로 두고, Next의 `src/app/**/page.tsx`는 **얇게** 유지하며 내부에서 FSD `pages` 레이어 슬라이스를 import해 렌더만. 라우팅(Next) ↔ 화면조립(FSD pages)을 분리.
```
frontend/src/
├── app/                 # Next App Router (라우팅 전용, 얇게)
│   └── (shop)/products/[id]/page.tsx  → import { ProductDetailPage } from '@/pages/product-detail'
├── pages/               # FSD pages (화면 조립)
├── widgets/  features/  entities/  shared/
```
- import 경계는 **steiger** 또는 `eslint-plugin-boundaries`로 강제.

### 8.4 API / 인증 연동
- `shared/api`: 타입드 클라이언트. 백엔드 OpenAPI에서 **openapi-typescript / orval**로 타입·훅 코드 생성 권장.
- 상태: 서버상태는 **TanStack Query**, 클라이언트 전역상태는 **Zustand**(가벼움).
- 인증: **Access 토큰은 메모리**, **Refresh는 httpOnly 쿠키** + Next **Route Handler를 BFF**처럼 사용해 토큰 갱신/주입. (XSS로부터 refresh 보호)

---

## 9. 기술 스택 & 버전 (제안)

**Backend**
- JDK 21 (LTS), Kotlin 2.x, Gradle 8.x (Kotlin DSL + Version Catalog + Convention Plugins)
- Spring Boot 3.4+, Spring Cloud 2024.x (**Gateway**, OpenFeign) — ~~Config, Eureka~~는 k8s로 대체
- Spring Authorization Server (auth), Spring Security (resource server)
- PostgreSQL(서비스별), Redis(cart), Kafka(+ 선택 Schema Registry)
- **배포: Kubernetes** — 매니페스트는 Kustomize 또는 Helm, 설정=ConfigMap/Secret, 진입=Ingress. (로컬 k8s: kind/k3d, 옵션 `spring-cloud-kubernetes`)
- 관측: Micrometer + OpenTelemetry → Tempo/Zipkin(트레이스), Prometheus/Grafana(메트릭), Loki(로그)
- 테스트: JUnit5 + Kotest, MockK, **Testcontainers**(통합), **ArchUnit**(아키텍처), **Spring Cloud Contract**(소비자 주도 계약)

**Frontend**
- Next.js 15 (App Router, React 19), TypeScript
- TanStack Query, Zustand, Tailwind CSS, shadcn/ui
- FSD + steiger / eslint-plugin-boundaries, openapi-typescript(or orval)

---

## 10. 리스크 & 권고 (요약)

1. **완전 MSA 운영 부담** — 11개 서비스 + 인프라. → 로드맵을 한 번에 하나씩(아래), 서비스 내부는 헥사고날로 합/분리 용이하게.
2. **분산 트랜잭션** — Saga + 최종 일관성은 본질적으로 복잡. Outbox/멱등성/보상 트랜잭션을 처음부터 패턴화.
3. **공유 라이브러리 결합** — 도메인 공유 금지, 기술 횡단만. 이벤트 계약은 Schema Registry 고려.
4. **FSD ↔ Next App Router 마찰** — 8.3의 `src/` 분리 패턴을 초기에 확정.
5. **인증 토큰 보안** — refresh는 httpOnly 쿠키 + BFF.

> 다음 단계와 단계별 검증 기준은 `docs/ROADMAP.md` 참고.

---

## 11. 배포 (Kubernetes)

```
                 Ingress (TLS 종료, L7)
                        │
                 api-gateway (Deployment + Service)
                        │  Service DNS
   ┌────────────┬───────┴────────┬───────────────┐
 Deployment   Deployment       ...            Deployment
 (각 서비스 = Deployment + Service + HPA)
   │
   ├─ 설정: ConfigMap (비밀: Secret / 외부 시크릿 매니저)
   ├─ 디스커버리: k8s Service DNS (별도 레지스트리 없음)
   └─ 상태저장: PostgreSQL/Redis/Kafka는 Operator 또는 매니지드 서비스 권장
```

- **서비스 = Deployment + Service(ClusterIP)**, 게이트웨이만 Ingress로 외부 노출.
- **설정/시크릿**: ConfigMap + Secret. 시크릿은 SealedSecrets/External Secrets로 git 관리 분리 권장.
- **디스커버리**: 추가 컴포넌트 없이 Service DNS. (Eureka/Config 서버 불필요)
- **스테이트풀(Postgres/Kafka/Redis)**: 클러스터 내부에 둔다면 Operator(예: CloudNativePG, Strimzi) 사용, 또는 매니지드 서비스. 학습 환경은 단순 StatefulSet도 가능.
- **매니페스트**: Kustomize(base/overlay로 env 분리) 또는 Helm 차트. 로컬은 kind/k3d.
- **헬스/오토스케일**: liveness/readiness probe + HPA(메트릭 기반).
