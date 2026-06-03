# 실행 가이드 (RUN)

실행 시나리오별 **명령어 순서**. 모든 명령은 저장소 루트(`ecommerce/`)에서 실행한다(별도 표기 시 제외).

## 사전 요구사항
- JDK 21, Docker(데몬 실행 중), Docker Compose
- 프론트/E2E: Node 20+
- k8s 배포(선택): kubectl + kustomize, 로컬 클러스터(kind/k3d)

---

## 시나리오 A — 로컬 개발 (인프라=Docker, 서비스=Gradle)

개별 서비스를 IDE/Gradle로 띄우며 개발할 때.

```bash
# 1) 인프라 기동 (postgres·redis·kafka·zipkin)
docker compose -f infra/docker-compose.yml up -d

# 2) 빌드 + 단위/슬라이스/계약/ArchUnit 테스트
./gradlew build

# 3) 서비스 실행 (각각 별도 터미널, 포트 고정)
./gradlew :services:auth-service:bootRun        # 8081
./gradlew :services:member-service:bootRun      # 8082
./gradlew :services:product-service:bootRun     # 8083
./gradlew :services:cart-service:bootRun        # 8084
./gradlew :services:inventory-service:bootRun   # 8085
./gradlew :services:payment-service:bootRun     # 8086
./gradlew :services:shipping-service:bootRun    # 8087
./gradlew :services:order-service:bootRun       # 8088
./gradlew :services:api-gateway:bootRun         # 8080 (진입점)
```
> 기본값(`application.yml`)은 모두 `localhost`를 가리키므로 인프라만 떠 있으면 그대로 동작한다.

---

## 시나리오 B — 전체 스택 한 번에 (docker-compose, 권장)

9개 서비스 + 인프라를 컨테이너로 한 번에. **E2E/데모에 권장.**

```bash
# 1) 부트 jar 빌드 (단일 실행 jar)
./gradlew bootJar

# 2) 서비스 이미지 9개 빌드
for svc in api-gateway auth-service member-service product-service cart-service \
           inventory-service payment-service shipping-service order-service; do
  docker build -t "ecommerce/$svc:latest" "services/$svc"
done

# 3) 인프라 먼저 기동하고 healthy 까지 대기 (kafka 헬스 타이밍 이슈 회피)
docker compose -f infra/docker-compose.full.yml up -d --wait postgres kafka redis

# 4) 나머지 서비스 기동
docker compose -f infra/docker-compose.full.yml up -d

# 5) 게이트웨이 준비 확인 (콜드스타트가 느릴 수 있음)
curl --retry 120 --retry-delay 2 --retry-connrefused --retry-all-errors \
     -s -o /dev/null -w "gateway -> %{http_code}\n" http://localhost:18080/actuator/health
```

> ⚠️ **게이트웨이는 호스트 `:18080`** 으로 노출된다(컨테이너 내부는 8080). 호스트 8080이 점유된 환경 대응. 진입점 = `http://localhost:18080`.

### 종료
```bash
docker compose -f infra/docker-compose.full.yml down        # 볼륨 유지
docker compose -f infra/docker-compose.full.yml down -v     # 데이터까지 삭제
```

---

## 시나리오 C — 백엔드 E2E 플로우 확인 (curl)

시나리오 B로 스택이 떠 있는 상태에서:

```bash
GW=http://localhost:18080; INV=http://localhost:8085

# 1) 회원가입 → 로그인 (토큰)
curl -s -XPOST $GW/api/auth/signup -H 'Content-Type: application/json' \
     -d '{"email":"user@example.com","password":"pw12345!"}'
TOKEN=$(curl -s -XPOST $GW/api/auth/login -H 'Content-Type: application/json' \
     -d '{"email":"user@example.com","password":"pw12345!"}' \
     | python3 -c "import sys,json;print(json.load(sys.stdin)['accessToken'])")

# 2) 프로필(이벤트로 자동 생성) / 무토큰 401
curl -s $GW/api/members/me -H "Authorization: Bearer $TOKEN"      # 200
curl -s -o /dev/null -w "%{http_code}\n" $GW/api/members/me        # 401

# 3) 상품 생성 → 재고 시드(내부 서비스 직접 호출)
PID=$(curl -s -XPOST $GW/api/products -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
     -d '{"name":"상품","description":"d","amount":19900,"currency":"KRW","category":"의류"}' \
     | python3 -c "import sys,json;print(json.load(sys.stdin)['productId'])")
curl -s -XPOST $INV/api/inventory -H 'Content-Type: application/json' \
     -d "{\"productId\":\"$PID\",\"quantity\":100}"

# 4) 장바구니 담기 → 체크아웃(Saga)
curl -s -XPOST $GW/api/carts/items -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
     -d "{\"productId\":\"$PID\",\"quantity\":2}"
curl -s -XPOST $GW/api/orders -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
     -d "{\"lines\":[{\"productId\":\"$PID\",\"quantity\":2,\"unitPrice\":19900}],\"shippingAddress\":{\"zipcode\":\"12345\",\"line1\":\"서울 1로\",\"line2\":null}}"
# → status: CONFIRMED (재고예약→결제→재고확정→배송)
```

---

## 시나리오 D — 프론트엔드 + 브라우저 E2E

시나리오 B로 백엔드 스택이 떠 있어야 한다.

```bash
cd frontend
npm install

# 1) 빌드 — 게이트웨이 주소는 빌드 시점에 주입(NEXT_PUBLIC_*은 빌드 타임에 인라인됨)
NEXT_PUBLIC_API_BASE_URL=http://localhost:18080 npm run build

# 2) 서버 기동 (BFF가 게이트웨이로 프록시)
AUTH_API_BASE_URL=http://localhost:18080 npx next start -p 3000
#   → http://localhost:3000   (dev 서버는 npm run dev)
```

### Playwright E2E
```bash
cd frontend
# 테스트 계정·상품·재고 시드(시나리오 C의 1·3단계) 후:
npx playwright install chromium      # 헤드리스 브라우저 1회 설치
npx playwright test                  # tests/e2e/checkout.spec.ts
```
> 게이트웨이에 CORS가 설정돼 있어 브라우저(:3000)→게이트웨이(:18080) 직접 호출이 허용된다.

---

## 테스트

```bash
# 단위 / 서비스(mock) / REST Docs / ArchUnit / 경량 계약 — 인프라 불필요
./gradlew build
# 또는 특정 서비스만
./gradlew :services:product-service:build

# 통합 테스트(Testcontainers, Docker 필요) — 실 Postgres/Kafka
./gradlew :services:auth-service:integrationTest
./gradlew :services:member-service:integrationTest
./gradlew :services:order-service:integrationTest
```
> 통합 테스트 태스크는 모던 Docker(API ≥ 1.40) 대응으로 `api.version=1.43`을 워커에 주입한다(컨벤션 플러그인). 데몬 소켓(`/var/run/docker.sock`) 접근 권한 필요.

---

## 시나리오 E — Kubernetes 배포

```bash
# (이미지 빌드: 시나리오 B 1~2단계) 로컬 클러스터면 이미지 로드
# kind load docker-image ecommerce/<svc>:latest   (서비스별)

kubectl apply -k deploy/k8s/base       # ns + 서비스 Deployment/Service + ConfigMap/Secret + Ingress
kubectl -n ecommerce get pods
kubectl kustomize deploy/k8s/base      # 적용 전 렌더 확인
```
> 상태 저장소(Postgres/Kafka/Redis)는 클러스터 내 Operator 또는 매니지드 서비스로 별도 제공한다(`docs/PLAN.md §11`). Ingress 진입을 위해 Ingress Controller(nginx 등) 필요.

---

## 포트 요약

| 진입/서비스 | 로컬(bootRun) | docker-compose.full(호스트) |
|---|---|---|
| api-gateway | 8080 | **18080** |
| auth / member / product / cart | 8081 / 8082 / 8083 / 8084 | 동일 |
| inventory / payment / shipping / order | 8085 / 8086 / 8087 / 8088 | 동일 |
| frontend | 3000 | 3000 (호스트 next) |
| postgres / redis / kafka / zipkin | 5432 / 6379 / 9092 / 9411 | 내부 |
