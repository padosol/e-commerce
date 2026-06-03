# ecommerce-frontend

Next.js (App Router) + Feature-Sliced Design.

## 레이어 (하향 의존만)
`app → pages → widgets → features → entities → shared`

- **app**: Next 라우팅(얇게) + 인증 BFF 라우트 핸들러
- **pages**: 화면 조립 (FSD pages)
- **widgets**: 독립 UI 블록 (header)
- **features**: 사용자 행위 (로그인, 담기, 주문)
- **entities**: 비즈니스 엔티티 (product, cart, order) — model/api/ui
- **shared**: api 클라이언트, config, 토큰 스토어, ui

## 인증
- access 토큰은 메모리(`shared/auth`), refresh 토큰은 httpOnly 쿠키(`app/api/auth/*` BFF).

## 명령
```bash
npm install
npm run build   # 타입체크 + 컴파일
npm run lint    # eslint (next/core-web-vitals)
npm run fsd     # steiger FSD 경계 검사
npx playwright test   # E2E (백엔드 구동 + 브라우저 필요)
```

API 게이트웨이: `NEXT_PUBLIC_API_BASE_URL` (기본 `http://localhost:8080`).
