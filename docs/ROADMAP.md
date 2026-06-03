# 실행 로드맵

> 원칙: **완전 MSA가 목표라도 한 번에 하나씩.** 각 Phase는 "검증 기준(Verify)"을 통과해야 다음으로.
> 모든 서비스는 동일한 헥사고날 골격 + ArchUnit 경계 테스트 + Testcontainers 통합 테스트를 기본 포함.

---

## Phase 0 — 기반(Foundation)

목표: 빌드/인프라/플랫폼 골격을 세운다.

- [ ] Gradle 모노레포: `settings.gradle.kts`, `build-logic`(convention plugins), `libs.versions.toml`
- [ ] `infra/docker-compose.yml`(로컬 의존성): PostgreSQL(×N), Kafka, Redis, (옵션) Schema Registry, 관측 스택
- [ ] `api-gateway`(Spring Cloud Gateway) 스켈레톤 — 라우팅은 **k8s Service DNS** 대상 (Eureka/config-server 없음)
- [ ] **k8s 매니페스트** 베이스(Kustomize base/overlay): Deployment+Service, ConfigMap/Secret, Ingress, 로컬은 kind/k3d
- [ ] 공통 라이브러리 `common-web`(에러응답/트레이싱), `common-security`(JWT) 뼈대
- [ ] 관측: OpenTelemetry 트레이싱 전 구간 연결

**Verify**: 로컬 kind/k3d에 배포 → Ingress→gateway→더미 서비스 `/health` 라우팅 성공(Service DNS 경유). 설정은 ConfigMap/Secret에서 주입됨. (별도 디스커버리 등록 단계 없음)

---

## Phase 1 — 회원·인증

- [x] `auth-service`: 회원가입/로그인, JWT(access/refresh) 발급, JWKS 공개, `UserRegistered` 이벤트 발행
- [x] `member-service`: `UserRegistered` 소비(멱등) → 프로필 생성, `/me`·주소 추가, Resource Server JWT 검증
- [x] `api-gateway`: Resource Server로 JWT 검증, `/api/auth/**`·actuator 공개·그 외 인증 요구
- [x] 테스트: 도메인 단위(Kotest) · 서비스 mock(MockK) · 컨트롤러 REST Docs(standalone MockMvc) · ArchUnit 경계 · 경량 계약(이벤트 스키마 round-trip + JWT/JWKS) — auth 14 + member 15 통과
- [~] Testcontainers 통합 테스트 작성 — `@Tag("integration")` 분리 + `integrationTest` 태스크. auth `SignUpIntegrationTest`, member `UserRegisteredConsumerIntegrationTest`. (실행은 Docker 환경 필요; 샌드박스 미실행)
- [ ] (남음) 로컬 런타임 E2E: Docker 환경에서 `./gradlew integrationTest` green + 앱 구동(signup→login→/me 200, 무효 토큰 401)

> 테스트 정책: 신규 기능은 **Phase 2부터 TDD**(실패 테스트 → 구현 → 리팩터). 레이어별 — domain=순수 단위, application/service=포트 mock, controller=REST Docs, 서비스 간=경량 계약, 경계=ArchUnit.

**구현 메모**:
- 인증 토큰은 Spring Security `JwtEncoder`(NimbusJwtEncoder, RS256, 인메모리 RSA 키) + `/oauth2/jwks` 공개 방식으로 발급. OIDC 표준 플로우(authorization_code 등)가 필요해지면 Spring Authorization Server로 승격.
- 검증은 `jwk-set-uri` 기반(게이트웨이·member). JWT `sub`=userId(UUID)로 서비스 간 일관.
- 컨벤션 플러그인에 `kotlin("plugin.jpa")`(no-arg) 추가 — Hibernate 엔티티 인스턴스화용.

**Verify (코드/빌드)**: `./gradlew build` ✅, `kubectl kustomize deploy/k8s/base` ✅(gateway·auth·member 렌더). 
**Verify (런타임, 로컬에서)**: 회원가입 → 로그인 → JWT 발급 → `/api/members/me` 200, 무효 토큰 401, 이벤트로 member 프로필 자동 생성. (이 하니스 샌드박스는 네트워크 바인딩 차단으로 런타임 미검증)

---

## Phase 2 — 상품·장바구니

- [x] `product-service`: 상품 등록/단건·목록 조회 API, Money VO, 카테고리 필드, JPA+Flyway. TDD(red→green), 단위/서비스/REST Docs/ArchUnit 13개 green. (옵션/카테고리 애그리거트는 후속)
- [x] `cart-service`: 사용자별 장바구니(Redis), 담기/수정/삭제/조회, product 유효성 조회(RestClient + Resilience4j 서킷브레이커, 장애 시 낙관적 fallback), RS JWT 검증. TDD 14개 green. (Redis 통합 테스트는 후속)

**Verify**: 상품 등록·조회, 장바구니 담기/조회가 사용자별로 격리. product 장애 시 cart가 graceful 동작(서킷브레이커).

---

## Phase 3 — 주문·결제·재고·배송 (Saga)

- [ ] `order-service`: Order 애그리거트 + 상태머신 + **체크아웃 Saga 오케스트레이터**
- [x] `payment-service`: 결제 승인(멱등 키로 중복 방지)/취소, Mock PG 어댑터(PaymentGateway 포트), 상태머신(PENDING→APPROVED/FAILED→CANCELLED). TDD 13개 green. 내부 서비스.
- [x] `inventory-service`: 재고 예약/확정/해제, 낙관적 락(`@Version`) 동시성 제어, REST(reserve/confirm/release). TDD 13개 green (StockTest, InventoryService mock, InventoryController REST Docs, ArchUnit). 내부 서비스(게이트웨이 비노출).
- [x] `shipping-service`: 배송 생성/출고(운송장)/배송완료/추적, 상태머신(CREATED→IN_TRANSIT→DELIVERED), 주소 스냅샷. TDD 13개 green. 내부 서비스.
- [ ] **Transactional Outbox** + 멱등 consumer 전 서비스 적용

**Verify**:
- 정상: 주문 → 재고예약 → 결제 → 재고확정 → 배송생성 → `CONFIRMED`
- 보상: 결제 실패 → 재고 예약 해제 → 주문 `CANCELLED`
- Testcontainers(Postgres+Kafka)로 happy path + 보상 경로 통합 테스트 통과.

---

## Phase 4 — 프론트엔드 (Next.js FSD)

- [ ] FSD 스캐폴딩: `src/{app,pages,widgets,features,entities,shared}`, steiger/eslint 경계
- [ ] `shared/api`: 백엔드 OpenAPI → 타입/훅 코드 생성, TanStack Query
- [ ] 인증: access(메모리) + refresh(httpOnly 쿠키) + Next Route Handler BFF
- [ ] 화면: 로그인 → 상품목록/상세 → 장바구니 → 체크아웃

**Verify**: Playwright E2E — 로그인 → 상품 탐색 → 담기 → 주문 완료 시나리오 통과.

---

## Phase 5 — 안정화(Hardening)

- [ ] Saga 전 구간 분산 추적(트레이스 한 화면에서 확인)
- [ ] 회복탄력성: timeout/retry/circuit breaker(Resilience4j) 정책 정리
- [ ] 멱등성/Outbox 릴레이/DLQ(Dead Letter) 점검
- [ ] 부하/실패 주입 테스트, 대시보드(메트릭/로그/트레이스)

**Verify**: 의도적 장애 주입 시 보상·재시도·DLQ가 기대대로 동작, 추적으로 원인 1분 내 파악.

---

## 서비스 공통 체크리스트 (서비스 추가 시마다)

- [ ] 헥사고날 패키지(`domain/application/adapter`) + 의존 방향
- [ ] ArchUnit 경계 테스트
- [ ] 자기 DB만 사용(타 서비스 DB 직접 접근 0)
- [ ] 발행/소비 이벤트 문서화 + 멱등 처리
- [ ] OpenAPI 문서 노출(프론트 타입 생성용)
- [ ] Testcontainers 통합 테스트, (서비스 간) 계약 테스트
- [ ] 헬스체크 + 트레이싱 + 메트릭 노출
