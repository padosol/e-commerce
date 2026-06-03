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

- [ ] `auth-service`: Spring Authorization Server, 회원가입/로그인, JWT(access/refresh) 발급, `UserRegistered` 이벤트 발행
- [ ] `member-service`: `UserRegistered` 소비 → 프로필 생성, 주소 관리
- [ ] `api-gateway`: Resource Server로 JWT 검증, 보호 라우트 구성

**Verify**: 회원가입 → 로그인 → JWT 발급 → 보호 API 호출 성공/실패(토큰 무효 시 401). member 프로필이 이벤트로 자동 생성. auth↔member 계약 테스트 통과.

---

## Phase 2 — 상품·장바구니

- [ ] `product-service`: 상품/카테고리 CRUD, 가격/옵션, 조회 API
- [ ] `cart-service`: 사용자별 장바구니(Redis), 담기/수정/삭제, 상품 유효성은 product 조회(Resilience4j)

**Verify**: 상품 등록·조회, 장바구니 담기/조회가 사용자별로 격리. product 장애 시 cart가 graceful 동작(서킷브레이커).

---

## Phase 3 — 주문·결제·재고·배송 (Saga)

- [ ] `order-service`: Order 애그리거트 + 상태머신 + **체크아웃 Saga 오케스트레이터**
- [ ] `payment-service`: 결제 승인/취소(초기 Mock PG 어댑터), 멱등성 키
- [ ] `inventory-service`: 재고 예약/확정/해제(동시성 제어)
- [ ] `shipping-service`: 배송 생성/추적
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
